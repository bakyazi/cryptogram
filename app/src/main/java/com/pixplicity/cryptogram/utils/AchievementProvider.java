package com.pixplicity.cryptogram.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.SortedMap;
import java.util.TreeMap;

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
            R.string.achievement_jack_of_all_trades,
            R.string.achievement_hope_youre_comfortable,
            R.string.achievement_hope_youre_really_comfortable,
            R.string.achievement_zen_master,
            R.string.achievement_twoday_streak,
            R.string.achievement_threeday_streak,
            R.string.achievement_fiveday_streak,
            };

    private static final String KEY_STARTED_IN_AIRPLANE_MODE = "started_in_airplane_mode";
    private static final String KEY_UNLOCKED_FLIGHT_MODE = "unlocked_flight_mode";

    private static AchievementProvider sInstance;

    private boolean mStartedInAirplaneMode;
    private boolean mUnlockedFlightMode;

    @NonNull
    public static AchievementProvider getInstance() {
        if (sInstance == null) {
            sInstance = new AchievementProvider();
        }
        return sInstance;
    }

    private AchievementProvider() {
        // TODO read prefs
        mStartedInAirplaneMode = Prefs.getBoolean(KEY_STARTED_IN_AIRPLANE_MODE, false);
        mUnlockedFlightMode = Prefs.getBoolean(KEY_UNLOCKED_FLIGHT_MODE, false);
    }

    private void save() {
        Prefs.putBoolean(KEY_STARTED_IN_AIRPLANE_MODE, mStartedInAirplaneMode);
        Prefs.putBoolean(KEY_UNLOCKED_FLIGHT_MODE, mUnlockedFlightMode);
    }

    /**
     * Register start events
     *
     * @param googleApiClient
     */
    public void onCryptogramStart(GoogleApiClient googleApiClient) {
        CryptogramApp context = CryptogramApp.getInstance();
        Toast.makeText(context,
                "start at " + System.currentTimeMillis(),
                Toast.LENGTH_SHORT).show();

        mStartedInAirplaneMode = SystemUtils.isAirplaneModeOn(context);

        save();
    }

    /**
     * Register completion events and perform any achievement unlocks as necessary
     *
     * @param googleApiClient
     */
    public void onCryptogramCompleted(GoogleApiClient googleApiClient) {
        CryptogramApp context = CryptogramApp.getInstance();
        Toast.makeText(context,
                "complete at " + System.currentTimeMillis(),
                Toast.LENGTH_SHORT).show();

        if (mStartedInAirplaneMode && SystemUtils.isAirplaneModeOn(context)) {
            mUnlockedFlightMode = true;
        }

        save();

        check(googleApiClient);
    }

    public void check(GoogleApiClient googleApiClient) {
        CryptogramApp context = CryptogramApp.getInstance();

        SortedMap<Long, Long> times = new TreeMap<>();

        int completed = 0, perfectScore = 0;
        boolean unlockedJackOfAllTrades = false;
        boolean unlockedNoBrainer = false;
        for (Cryptogram cryptogram : CryptogramProvider.getInstance(context).getAll()) {
            if (cryptogram.isCompleted()) {
                completed++;
            }
            if (cryptogram.getScore() >= 1f) {
                perfectScore++;
            }
            if (cryptogram.getExcessCount() == 0 && cryptogram.getReveals() == 0 && !cryptogram.hadHints()) {
                unlockedJackOfAllTrades = true;
            }
            long duration = cryptogram.getProgress().getDuration();
            if (duration <= 45 * 1000) {
                unlockedNoBrainer = true;
            }
            times.put(cryptogram.getProgress().getStartTime(), duration);
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
                    if (mUnlockedFlightMode) {
                        unlock(context, googleApiClient, achievementResId);
                    }
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
                case R.string.achievement_jack_of_all_trades: {
                    // Solve a puzzle with no reveals or excess inputs—and without using the hint bar.
                    if (unlockedJackOfAllTrades) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                case R.string.achievement_nobrainer: {
                    // Breeze through a puzzle in 45 seconds or less.
                    if (unlockedNoBrainer) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                case R.string.achievement_hope_youre_comfortable: {
                    // Complete five puzzles in ten minutes.
                    if (hasSeries(times, 5, 10 * 60 * 1000)) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                case R.string.achievement_hope_youre_really_comfortable: {
                    // Complete ten puzzles in thirty minutes.
                    if (hasSeries(times, 10, 30 * 60 * 1000)) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                case R.string.achievement_zen_master: {
                    // Complete twenty puzzles in an hour.
                    if (hasSeries(times, 20, 60 * 60 * 1000)) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                case R.string.achievement_twoday_streak: {
                    // Play for two consecutive days.
                    if (longestStreak(times) >= 2) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                case R.string.achievement_threeday_streak: {
                    // Play for three consecutive days.
                    if (longestStreak(times) >= 3) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                case R.string.achievement_fiveday_streak: {
                    // Play for five consecutive days.
                    if (longestStreak(times) >= 5) {
                        unlock(context, googleApiClient, achievementResId);
                    }
                }
                break;
                default:
                    String achievementId = CryptogramApp.getInstance().getString(achievementResId);
                    throw new IllegalStateException("unknown achievement " + achievementId);
            }
        }
    }

    private boolean hasSeries(SortedMap<Long, Long> times, int length, int duration) {
        Long[] keys = new Long[times.size()];
        times.keySet().toArray(keys);
        for (int i = length; i < times.size(); i++) {
            long start = keys[i - length];
            long finish = keys[i] + times.get(keys[i]);
            long curDuration = finish - start;
            if (curDuration <= duration) {
                return true;
            }
        }
        return false;
    }

    private int longestStreak(SortedMap<Long, Long> times) {
        // TODO
        return 0;
    }

    private void unlock(Context context, GoogleApiClient googleApiClient, int achievementResId) {
        String achievementId = context.getString(achievementResId);
        Games.Achievements.unlock(googleApiClient, achievementId);
    }

}
