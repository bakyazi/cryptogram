package com.pixplicity.cryptogram.utils;

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

    public void submit(GoogleApiClient googleApiClient) {
        CryptogramApp context = CryptogramApp.getInstance();

        long score = CryptogramProvider.getInstance(context).getTotalScore();
        Games.Leaderboards.submitScore(googleApiClient,
                context.getString(R.string.leaderboard_scoreboard), score);
    }

}
