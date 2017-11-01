package com.pixplicity.cryptogram.utils;

import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;

public class LeaderboardProvider {

    private static LeaderboardProvider sInstance;

    public static LeaderboardProvider getInstance() {
        if (sInstance == null) {
            sInstance = new LeaderboardProvider();
        }
        return sInstance;
    }

    public void submit(final GoogleApiClient googleApiClient) {
        final CryptogramApp context = CryptogramApp.getInstance();
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                synchronized (LeaderboardProvider.this) {
                    return PuzzleProvider.getInstance(context).getTotalScore();
                }
            }

            @Override
            protected void onPostExecute(Long score) {
                if (!googleApiClient.isConnected()) {
                    return;
                }
                try {
                    Games.Leaderboards.submitScore(googleApiClient,
                            context.getString(R.string.leaderboard_scoreboard),
                            score);
                } catch (SecurityException e) {
                    // Not sure why we're still seeing errors about the connection state, but here we are
                    Crashlytics.logException(e);
                }
            }
        }.execute();
    }

}
