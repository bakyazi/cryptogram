package com.pixplicity.cryptogram.utils

import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.games.Games
import com.google.android.gms.games.snapshot.Snapshot
import com.google.android.gms.games.snapshot.SnapshotMetadata
import com.google.android.gms.games.snapshot.SnapshotMetadataChange
import com.pixplicity.cryptogram.R
import java.io.IOException
import java.math.BigInteger
import java.util.*
import java.util.concurrent.TimeUnit

object SavegameManager {

    private val lastSavegameName: String
        get() {
            var savegameName: String? = PrefsUtils.lastSavegameName
            if (savegameName == null) {
                val unique = BigInteger(281, Random()).toString(13)
                savegameName = "snapshot-$unique"
            }
            return savegameName
        }

    interface OnSaveResult {
        fun onSaveSuccess()

        fun onSaveFailure()
    }

    interface OnLoadResult {
        fun onLoadSuccess()

        fun onLoadFailure()
    }

    private fun getSnapshot(googleApiClient: GoogleApiClient?,
                            snapshotName: String,
                            createIfNotFound: Boolean): Snapshot? {
        if (googleApiClient == null || !googleApiClient.isConnected) {
            return null
        }
        try {
            val result = Games.Snapshots.open(googleApiClient, snapshotName, createIfNotFound).await(30, TimeUnit.SECONDS)
            return if (result.status.isSuccess) {
                result.snapshot
            } else {
                null
            }
        } catch (e: SecurityException) {
            // Not sure why we're still seeing errors about the connection state, but here we are
            Crashlytics.logException(e)
            return null
        } catch (e: IllegalStateException) {
            Crashlytics.logException(e)
            return null
        }

    }

    fun load(googleApiClient: GoogleApiClient,
             snapshotName: String): Snapshot? {
        val snapshot = getSnapshot(googleApiClient, snapshotName, false)
        if (snapshot != null) {
            val context = googleApiClient.context
            try {
                val progressJson = String(snapshot.snapshotContents.readFully())
                PuzzleProvider.getInstance(context).setProgressFromJson(progressJson)
                PrefsUtils.lastSavegameName = snapshotName
            } catch (e: IOException) {
                Crashlytics.logException(e)
                return null
            }

        }
        return snapshot
    }

    fun save(googleApiClient: GoogleApiClient?): SnapshotMetadata? {
        return save(googleApiClient, lastSavegameName)
    }

    private fun save(googleApiClient: GoogleApiClient?,
                     snapshotName: String): SnapshotMetadata? {
        val snapshot = getSnapshot(googleApiClient, snapshotName, true) ?: return null

        val context = googleApiClient!!.context

        // Set the data payload for the snapshot
        val progressJson = PuzzleProvider.getInstance(context).progressJson
        val data = progressJson.toByteArray()
        snapshot.snapshotContents.writeBytes(data)

        // Create the change operation
        val metadataChange = SnapshotMetadataChange.Builder()
                .setDescription(context.getString(R.string.saved_game_name, SystemUtils.deviceName))
                .build()

        try {
            // Commit the operation
            val saveResult = Games.Snapshots.commitAndClose(googleApiClient, snapshot, metadataChange).await()
            if (saveResult.status.isSuccess) {
                PrefsUtils.lastSavegameName = snapshotName
                return saveResult.snapshotMetadata
            }
        } catch (e: IllegalStateException) {
            // Not sure why we're still seeing errors about the connection state, but here we are
            Crashlytics.logException(e)
            return null
        }

        return null
    }

}
