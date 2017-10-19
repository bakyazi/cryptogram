package com.pixplicity.cryptogram.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.games.snapshot.Snapshots;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.providers.PuzzleProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SavegameManager {

    public interface OnSaveResult {
        void onSaveSuccess();

        void onSaveFailure();
    }

    public interface OnLoadResult {
        void onLoadSuccess();

        void onLoadFailure();
    }

    @Nullable
    private static Snapshot getSnapshot(@Nullable GoogleApiClient googleApiClient,
                                        String snapshotName,
                                        boolean createIfNotFound) {
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            return null;
        }
        try {
            Snapshots.OpenSnapshotResult result = Games.Snapshots.open(googleApiClient, snapshotName, createIfNotFound).await(30, TimeUnit.SECONDS);
            if (result.getStatus().isSuccess()) {
                return result.getSnapshot();
            } else {
                return null;
            }
        } catch (IllegalStateException e) {
            // Not sure why we're still seeing errors about the connection state, but here we are
            Crashlytics.logException(e);
            return null;
        }
    }

    @NonNull
    private static String getLastSavegameName() {
        String savegameName = PrefsUtils.getLastSavegameName();
        if (savegameName == null) {
            String unique = new BigInteger(281, new Random()).toString(13);
            savegameName = "snapshot-" + unique;
        }
        return savegameName;
    }

    public static Snapshot load(GoogleApiClient googleApiClient,
                                String snapshotName) {
        final Snapshot snapshot = getSnapshot(googleApiClient, snapshotName, false);
        if (snapshot != null) {
            final Context context = googleApiClient.getContext();
            try {
                final String progressJson = new String(snapshot.getSnapshotContents().readFully());
                PuzzleProvider.getInstance(context).setProgressFromJson(progressJson);
                PrefsUtils.setLastSavegameName(snapshotName);
            } catch (IOException e) {
                Crashlytics.logException(e);
                return null;
            }
        }
        return snapshot;
    }

    public static SnapshotMetadata save(@Nullable GoogleApiClient googleApiClient) {
        return save(googleApiClient, getLastSavegameName());
    }

    private static SnapshotMetadata save(@Nullable GoogleApiClient googleApiClient,
                                         String snapshotName) {
        final Snapshot snapshot = getSnapshot(googleApiClient, snapshotName, true);
        if (snapshot == null) {
            return null;
        }

        final Context context = googleApiClient.getContext();

        // Set the data payload for the snapshot
        String progressJson = PuzzleProvider.getInstance(context).getProgressJson();
        final byte[] data = progressJson.getBytes();
        snapshot.getSnapshotContents().writeBytes(data);

        // Create the change operation
        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                .setDescription(context.getString(R.string.saved_game_name, SystemUtils.getDeviceName()))
                .build();

        try {
            // Commit the operation
            Snapshots.CommitSnapshotResult saveResult = Games.Snapshots.commitAndClose(googleApiClient, snapshot, metadataChange).await();
            if (saveResult.getStatus().isSuccess()) {
                PrefsUtils.setLastSavegameName(snapshotName);
                return saveResult.getSnapshotMetadata();
            }
        } catch (IllegalStateException e) {
            // Not sure why we're still seeing errors about the connection state, but here we are
            Crashlytics.logException(e);
            return null;
        }
        return null;
    }

}
