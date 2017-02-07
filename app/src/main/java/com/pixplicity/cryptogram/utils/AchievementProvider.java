package com.pixplicity.cryptogram.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.pixplicity.cryptogram.BuildConfig;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Calendar;
import java.util.SortedMap;
import java.util.TreeMap;

public class AchievementProvider {

    private static final String TAG = AchievementProvider.class.getSimpleName();

    private static final int[] ACHIEVEMENTS = new int[]{
            R.string.achievement_wet_feet,
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

    private final AchievementStats mAchievementStats = new AchievementStats();

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
        mStartedInAirplaneMode = Prefs.getBoolean(KEY_STARTED_IN_AIRPLANE_MODE, false);
        mUnlockedFlightMode = Prefs.getBoolean(KEY_UNLOCKED_FLIGHT_MODE, false);
    }

    public AchievementStats getAchievementStats() {
        return mAchievementStats;
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

        if (mStartedInAirplaneMode && SystemUtils.isAirplaneModeOn(context)) {
            mUnlockedFlightMode = true;
        }

        save();

        check(googleApiClient);
    }

    public void check(final GoogleApiClient googleApiClient) {
        final CryptogramApp context = CryptogramApp.getInstance();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                synchronized (AchievementProvider.this) {
                    mAchievementStats.calculate(context);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (!googleApiClient.isConnected()) {
                    return;
                }
                for (int achievementResId : ACHIEVEMENTS) {
                    switch (achievementResId) {
                        case R.string.achievement_wet_feet: {
                            // Finish the instructional puzzles.
                            if (mAchievementStats.isUnlockedWetFeet()) {
                                unlock(context, googleApiClient, achievementResId);
                            }
                        }
                        break;
                        case R.string.achievement_boned_up: {
                            // Complete your first puzzle to figure out how cryptograms work by trial and error.
                            if (mAchievementStats.getCompleted() >= 1) {
                                unlock(context, googleApiClient, achievementResId);
                            }
                        }
                        break;
                        case R.string.achievement_bookworm: {
                            // Complete ten puzzles.
                            if (mAchievementStats.getCompleted() >= 10) {
                                unlock(context, googleApiClient, achievementResId);
                            }
                        }
                        break;
                        case R.string.achievement_whizkid: {
                            // Complete twenty puzzles.
                            if (mAchievementStats.getCompleted() >= 20) {
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
                            if (mAchievementStats.getPerfectScore() >= 1) {
                                unlock(context, googleApiClient, achievementResId);
                            }
                        }
                        break;
                        case R.string.achievement_cream_of_the_crop: {
                            // Secure ten perfect scores.
                            if (mAchievementStats.getPerfectScore() >= 10) {
                                unlock(context, googleApiClient, achievementResId);
                            }
                        }
                        break;
                        case R.string.achievement_jack_of_all_trades: {
                            // Solve a puzzle with no reveals or excess inputs—and without using the hint bar.
                            if (mAchievementStats.isUnlockedJackOfAllTrades()) {
                                unlock(context, googleApiClient, achievementResId);
                            }
                        }
                        break;
                        case R.string.achievement_nobrainer: {
                            // Breeze through a puzzle in 45 seconds or less.
                            if (mAchievementStats.isUnlockedNoBrainer()) {
                                unlock(context, googleApiClient, achievementResId);
                            }
                        }
                        break;
                        case R.string.achievement_hope_youre_comfortable: {
                            // Complete five puzzles in ten minutes.
                            if (mAchievementStats.hasSeries(5, 10 * 60 * 1000)) {
                                unlock(context, googleApiClient, achievementResId);
                            }
                        }
                        break;
                        case R.string.achievement_hope_youre_really_comfortable: {
                            // Complete ten puzzles in thirty minutes.
                            if (mAchievementStats.hasSeries(10, 30 * 60 * 1000)) {
                                unlock(context, googleApiClient, achievementResId);
                            }
                        }
                        break;
                        case R.string.achievement_zen_master: {
                            // Complete twenty puzzles in an hour.
                            if (mAchievementStats.hasSeries(20, 60 * 60 * 1000)) {
                                unlock(context, googleApiClient, achievementResId);
                            }
                        }
                        break;
                        case R.string.achievement_twoday_streak: {
                            // Play for two consecutive days.
                            if (mAchievementStats.getLongestStreak() >= 2) {
                                unlock(context, googleApiClient, achievementResId);
                            }
                        }
                        break;
                        case R.string.achievement_threeday_streak: {
                            // Play for three consecutive days.
                            if (mAchievementStats.getLongestStreak() >= 3) {
                                unlock(context, googleApiClient, achievementResId);
                            }
                        }
                        break;
                        case R.string.achievement_fiveday_streak: {
                            // Play for five consecutive days.
                            if (mAchievementStats.getLongestStreak() >= 5) {
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
        }.execute();
    }

    private void unlock(Context context, GoogleApiClient googleApiClient, int achievementResId) {
        String achievementId = context.getString(achievementResId);
        Games.Achievements.unlock(googleApiClient, achievementId);
        if (BuildConfig.DEBUG) {
            String message = "unlocked " + context.getResources().getResourceEntryName(achievementResId);
            Log.i(TAG, message);
        }
    }

    public static class AchievementStats {

        private final SortedMap<Long, Long> mTimes = new TreeMap<>();

        private int mCompleted;
        private int mPerfectScore;
        private boolean mUnlockedWetFeet;
        private boolean mUnlockedJackOfAllTrades;
        private boolean mUnlockedNoBrainer;
        private int mLongestStreak;

        public AchievementStats() {
        }

        public void calculate(Context context) {
            mTimes.clear();
            mCompleted = 0;
            mPerfectScore = 0;
            mUnlockedWetFeet = true;
            mUnlockedJackOfAllTrades = false;
            mUnlockedNoBrainer = false;
            for (Cryptogram cryptogram : CryptogramProvider.getInstance(context).getAll()) {
                if (!cryptogram.isCompleted()) {
                    // Puzzle was not completed
                    if (cryptogram.isInstruction()) {
                        mUnlockedWetFeet = false;
                    }
                    continue;
                }
                if (cryptogram.isNoScore()) {
                    // Puzzle does not qualify for achievements
                    continue;
                }
                mCompleted++;
                Float score = cryptogram.getScore();
                if (score != null && score >= 1f) {
                    mPerfectScore++;
                }
                if (cryptogram.getExcessCount() == 0 && cryptogram.getReveals() == 0 && !cryptogram.hadHints()) {
                    mUnlockedJackOfAllTrades = true;
                }
                long startTime = cryptogram.getProgress().getStartTime();
                if (startTime > 0) {
                    long duration = cryptogram.getProgress().getDuration();
                    if (!cryptogram.isCompleted()) {
                        duration = 0;
                    }
                    if (duration > 0 && duration <= 45 * 1000) {
                        mUnlockedNoBrainer = true;
                    }
                    mTimes.put(startTime, duration);
                }
            }
            int streak = 0, bestStreak = 0;
            Calendar lastCalendar = null;
            for (long start : mTimes.keySet()) {
                Calendar calendar = toCalendar(start);
                if (lastCalendar == null) {
                    // First puzzle
                    streak = 1;
                } else if (calendar.equals(lastCalendar)) {
                    // Puzzle made on the same day
                } else {
                    Calendar tempCalendar = (Calendar) calendar.clone();
                    tempCalendar.add(Calendar.DAY_OF_MONTH, -1);
                    if (tempCalendar.equals(lastCalendar)) {
                        // Puzzle made on subsequent day
                        streak++;
                    } else {
                        // Puzzle not on a subsequent day; reset the streak
                        bestStreak = Math.max(bestStreak, streak);
                        streak = 1;
                    }
                }
                lastCalendar = calendar;
            }
            mLongestStreak = Math.max(bestStreak, streak);
        }

        public int getCompleted() {
            return mCompleted;
        }

        public int getPerfectScore() {
            return mPerfectScore;
        }

        public boolean isUnlockedWetFeet() {
            return mUnlockedWetFeet;
        }

        public boolean isUnlockedJackOfAllTrades() {
            return mUnlockedJackOfAllTrades;
        }

        public boolean isUnlockedNoBrainer() {
            return mUnlockedNoBrainer;
        }

        public int getLongestStreak() {
            return mLongestStreak;
        }

        private Calendar toCalendar(long timestamp) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar;
        }

        public boolean hasSeries(int length, int seriesDuration) {
            Long[] keys = new Long[mTimes.size()];
            mTimes.keySet().toArray(keys);
            for (int i = length; i < mTimes.size(); i++) {
                long start = keys[i - length];
                long duration = mTimes.get(keys[i]);
                if (duration == 0) {
                    continue;
                }
                long finish = keys[i] + duration;
                long curDuration = finish - start;
                if (curDuration <= seriesDuration) {
                    return true;
                }
            }
            return false;
        }

    }

}
