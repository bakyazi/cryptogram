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

    public static final boolean ENABLED = false;

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

        int completed = 0;
        for (Cryptogram cryptogram : CryptogramProvider.getInstance(context).getAll()) {
            if (cryptogram.isCompleted()) {
                completed++;
            }
        }
        for (int achievementResId : ACHIEVEMENTS) {
            switch (achievementResId) {
                case R.string.achievement_boned_up: {
                    if (completed >= 1) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                case R.string.achievement_bookworm: {
                    if (completed >= 10) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                case R.string.achievement_whizkid: {
                    if (completed >= 20) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                case R.string.achievement_nobrainer: {
                }
                break;
                case R.string.achievement_flight_mode: {
                }
                break;
                case R.string.achievement_its_the_bees_knees: {
                }
                break;
                case R.string.achievement_cream_of_the_crop: {
                }
                break;
                case R.string.achievement_man_of_all_trades: {
                }
                break;
                case R.string.achievement_hope_youre_comfortable: {
                }
                break;
                case R.string.achievement_hope_youre_really_comfortable: {
                }
                break;
                case R.string.achievement_zen_master: {
                }
                break;
                case R.string.achievement_twoday_streak: {
                }
                break;
                case R.string.achievement_threeday_streak: {
                }
                break;
                case R.string.achievement_fiveday_streak: {
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
