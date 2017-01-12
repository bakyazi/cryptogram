package com.pixplicity.cryptogram.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Cryptogram;

public class AchievementProvider {

    private static final String TAG = AchievementProvider.class.getSimpleName();

    private static final int[] ACHIEVEMENTS = new int[]{
            R.string.achievement_boned_up,
            R.string.achievement_bookworm,
            R.string.achievement_whizkid,
            R.string.achievement_nobrainer,
            R.string.achievement_flight_mode,
            R.string.achievement_its_the_bees_knees,
            R.string.achievement_cream_of_the_crop,
            R.string.achievement_man_of_all_trades,
            R.string.achievement_hope_youre_comfortable,
            R.string.achievement_hope_youre_really_comfortable,
            R.string.achievement_zen_master,
            R.string.achievement_twoday_streak,
            R.string.achievement_threeday_streak,
            R.string.achievement_fiveday_streak,
            };

    private static AchievementProvider sInstance;

    @NonNull
    public static AchievementProvider getInstance() {
        if (sInstance == null) {
            sInstance = new AchievementProvider();
        }
        return sInstance;
    }

    private AchievementProvider() {
        // TODO read prefs
        //Prefs.getBoolean(...);
    }

    /**
     * Update the state of any ongoing achievement checks.
     */
    public void ping() {
        // TODO
    }

    public void check(GoogleApiClient googleApiClient) {
        CryptogramApp context = CryptogramApp.getInstance();

        int completed = 0, perfectScore = 0;
        for (Cryptogram cryptogram : CryptogramProvider.getInstance(context).getAll()) {
            if (cryptogram.isCompleted()) {
                completed++;
            }
            if (cryptogram.getScore() >= 1f) {
                perfectScore++;
            }
        }
        for (int achievementResId : ACHIEVEMENTS) {
            switch (achievementResId) {
                case R.string.achievement_boned_up: {
                    // Complete your first puzzle to figure out how cryptograms work by trial and error.
                    if (completed >= 1) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                case R.string.achievement_bookworm: {
                    // Complete ten puzzles.
                    if (completed >= 10) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                case R.string.achievement_whizkid: {
                    // Complete twenty puzzles.
                    if (completed >= 20) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                case R.string.achievement_flight_mode: {
                    // Solve a puzzle in airplane mode. Isn't this the perfect game for a long flight?
                }
                break;
                case R.string.achievement_its_the_bees_knees: {
                    // Score a perfect 100%.
                    if (perfectScore >= 1) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                case R.string.achievement_cream_of_the_crop: {
                    // Secure ten perfect scores.
                    if (perfectScore >= 10) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                case R.string.achievement_man_of_all_trades: {
                    // Solve a puzzle with no reveals or excess inputs—and without using the hint bar.
                }
                break;
                case R.string.achievement_nobrainer: {
                    // Breeze through a puzzle in 45 seconds or less.
                }
                break;
                case R.string.achievement_hope_youre_comfortable: {
                    // Complete five puzzles in ten minutes.
                }
                break;
                case R.string.achievement_hope_youre_really_comfortable: {
                    // Complete ten puzzles in thirty minutes.
                }
                break;
                case R.string.achievement_zen_master: {
                    // Complete twenty puzzles in an hour.
                }
                break;
                case R.string.achievement_twoday_streak: {
                    // Play for two consecutive days.
                }
                break;
                case R.string.achievement_threeday_streak: {
                    // Play for three consecutive days.
                }
                break;
                case R.string.achievement_fiveday_streak: {
                    // Play for five consecutive days.
                }
                break;
                default:
                    String achievementId = CryptogramApp.getInstance().getString(achievementResId);
                    throw new IllegalStateException("unknown achievement " + achievementId);
            }
        }
    }

    public void unlock(Context context, GoogleApiClient googleApiClient, int achievementResId) {
        String achievementId = context.getString(achievementResId);
        Games.Achievements.unlock(googleApiClient, achievementId);
    }

}
