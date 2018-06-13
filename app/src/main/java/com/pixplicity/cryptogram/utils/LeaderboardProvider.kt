package com.pixplicity.cryptogram.utils

import android.os.AsyncTask

import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.games.Games
import com.pixplicity.cryptogram.CryptogramApp
import com.pixplicity.cryptogram.R

object LeaderboardProvider {

    fun submit(googleApiClient: GoogleApiClient) {
        val context = CryptogramApp.instance!!
        object : AsyncTask<Void?, Void?, Long>() {
            override fun doInBackground(vararg voids: Void?): Long {
                synchronized(this@LeaderboardProvider) {
                    return PuzzleProvider.getInstance(context).totalScore
                }
            }

            override fun onPostExecute(score: Long) {
                if (!googleApiClient.isConnected) {
                    return
                }
                try {
                    Games.Leaderboards.submitScore(googleApiClient,
                            context.getString(R.string.leaderboard_scoreboard),
                            score!!)
                } catch (e: NullPointerException) {
                    // Not sure why we're still seeing errors about the connection state, but here we are
                    Crashlytics.logException(e)
                } catch (e: SecurityException) {
                    Crashlytics.logException(e)
                } catch (e: IllegalStateException) {
                    Crashlytics.logException(e)
                }

            }
        }.execute()
    }

}
