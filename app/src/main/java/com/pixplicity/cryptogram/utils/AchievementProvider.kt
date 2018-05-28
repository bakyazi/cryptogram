package com.pixplicity.cryptogram.utils

import android.content.Context
import android.os.AsyncTask
import android.util.Log

import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.games.Games
import com.pixplicity.cryptogram.BuildConfig
import com.pixplicity.cryptogram.CryptogramApp
import com.pixplicity.cryptogram.R
import com.pixplicity.easyprefs.library.Prefs

import java.util.Calendar
import java.util.TreeMap

object AchievementProvider {

    private val TAG = AchievementProvider::class.java.simpleName
    private val DEBUG = BuildConfig.DEBUG && false

    private val ACHIEVEMENTS = intArrayOf(R.string.achievement_wet_feet, R.string.achievement_boned_up, R.string.achievement_bookworm, R.string.achievement_whizkid, R.string.achievement_nobrainer, R.string.achievement_flight_mode, R.string.achievement_its_the_bees_knees, R.string.achievement_cream_of_the_crop, R.string.achievement_jack_of_all_trades, R.string.achievement_hope_youre_comfortable, R.string.achievement_hope_youre_really_comfortable, R.string.achievement_zen_master, R.string.achievement_twoday_streak, R.string.achievement_threeday_streak, R.string.achievement_fiveday_streak)

    private val KEY_STARTED_IN_AIRPLANE_MODE = "started_in_airplane_mode"
    private val KEY_UNLOCKED_FLIGHT_MODE = "unlocked_flight_mode"

    private var mStartedInAirplaneMode: Boolean = false
    private var mUnlockedFlightMode: Boolean = false

    private class AchievementTask internal constructor(private val mGoogleApiClient: GoogleApiClient?) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg voids: Void): Void? {
            if (mGoogleApiClient != null) {
                val context = mGoogleApiClient.context
                AchievementStats.calculate(context)
            }
            return null
        }

        override fun onPostExecute(aVoid: Void) {
            if (mGoogleApiClient == null || !mGoogleApiClient.isConnected) {
                return
            }
            val context = mGoogleApiClient.context
            for (achievementResId in ACHIEVEMENTS) {
                when (achievementResId) {
                    R.string.achievement_wet_feet -> {
                        // Finish the instructional puzzles.
                        if (AchievementStats.isUnlockedWetFeet) {
                            AchievementProvider.unlock(context, mGoogleApiClient, achievementResId)
                        }
                    }
                    R.string.achievement_boned_up -> {
                        // Complete your first puzzle to figure out how cryptograms work by trial and error.
                        if (AchievementStats.completed >= 1) {
                            AchievementProvider.unlock(context, mGoogleApiClient, achievementResId)
                        }
                    }
                    R.string.achievement_bookworm -> {
                        // Complete ten puzzles.
                        if (AchievementStats.completed >= 10) {
                            AchievementProvider.unlock(context, mGoogleApiClient, achievementResId)
                        }
                    }
                    R.string.achievement_whizkid -> {
                        // Complete twenty puzzles.
                        if (AchievementStats.completed >= 20) {
                            AchievementProvider.unlock(context, mGoogleApiClient, achievementResId)
                        }
                    }
                    R.string.achievement_flight_mode -> {
                        // Solve a puzzle in airplane mode. Isn't this the perfect game for a long flight?
                        if (AchievementProvider.mUnlockedFlightMode) {
                            AchievementProvider.unlock(context, mGoogleApiClient, achievementResId)
                        }
                    }
                    R.string.achievement_its_the_bees_knees -> {
                        // Score a perfect 100%.
                        if (AchievementStats.perfectScore >= 1) {
                            AchievementProvider.unlock(context, mGoogleApiClient, achievementResId)
                        }
                    }
                    R.string.achievement_cream_of_the_crop -> {
                        // Secure ten perfect scores.
                        if (AchievementStats.perfectScore >= 10) {
                            AchievementProvider.unlock(context, mGoogleApiClient, achievementResId)
                        }
                    }
                    R.string.achievement_jack_of_all_trades -> {
                        // Solve a puzzle with no reveals or excess inputs—and without using the hint bar.
                        if (AchievementStats.isUnlockedJackOfAllTrades) {
                            AchievementProvider.unlock(context, mGoogleApiClient, achievementResId)
                        }
                    }
                    R.string.achievement_nobrainer -> {
                        // Breeze through a puzzle in 45 seconds or less.
                        if (AchievementStats.isUnlockedNoBrainer) {
                            AchievementProvider.unlock(context, mGoogleApiClient, achievementResId)
                        }
                    }
                    R.string.achievement_hope_youre_comfortable -> {
                        // Complete five puzzles in ten minutes.
                        if (AchievementStats.hasSeries(5, 10 * 60 * 1000)) {
                            AchievementProvider.unlock(context, mGoogleApiClient, achievementResId)
                        }
                    }
                    R.string.achievement_hope_youre_really_comfortable -> {
                        // Complete ten puzzles in thirty minutes.
                        if (AchievementStats.hasSeries(10, 30 * 60 * 1000)) {
                            AchievementProvider.unlock(context, mGoogleApiClient, achievementResId)
                        }
                    }
                    R.string.achievement_zen_master -> {
                        // Complete twenty puzzles in an hour.
                        if (AchievementStats.hasSeries(20, 60 * 60 * 1000)) {
                            AchievementProvider.unlock(context, mGoogleApiClient, achievementResId)
                        }
                    }
                    R.string.achievement_twoday_streak -> {
                        // Play for two consecutive days.
                        if (AchievementStats.longestStreak >= 2) {
                            AchievementProvider.unlock(context, mGoogleApiClient, achievementResId)
                        }
                    }
                    R.string.achievement_threeday_streak -> {
                        // Play for three consecutive days.
                        if (AchievementStats.longestStreak >= 3) {
                            AchievementProvider.unlock(context, mGoogleApiClient, achievementResId)
                        }
                    }
                    R.string.achievement_fiveday_streak -> {
                        // Play for five consecutive days.
                        if (AchievementStats.longestStreak >= 5) {
                            AchievementProvider.unlock(context, mGoogleApiClient, achievementResId)
                        }
                    }
                    else -> {
                        val achievementId = CryptogramApp.getInstance().getString(achievementResId)
                        throw IllegalStateException("unknown achievement $achievementId")
                    }
                }
            }
        }
    }

    init {
        mStartedInAirplaneMode = Prefs.getBoolean(KEY_STARTED_IN_AIRPLANE_MODE, false)
        mUnlockedFlightMode = Prefs.getBoolean(KEY_UNLOCKED_FLIGHT_MODE, false)
    }

    private fun save() {
        Prefs.putBoolean(KEY_STARTED_IN_AIRPLANE_MODE, mStartedInAirplaneMode)
        Prefs.putBoolean(KEY_UNLOCKED_FLIGHT_MODE, mUnlockedFlightMode)
    }

    /**
     * Register start events
     *
     * @param googleApiClient
     */
    fun onCryptogramStart(googleApiClient: GoogleApiClient) {
        val context = CryptogramApp.getInstance()

        mStartedInAirplaneMode = SystemUtils.isAirplaneModeOn(context)

        save()
    }

    /**
     * Register completion events and perform any achievement unlocks as necessary
     *
     * @param googleApiClient
     */
    fun onCryptogramCompleted(googleApiClient: GoogleApiClient) {
        val context = CryptogramApp.getInstance()

        if (mStartedInAirplaneMode && SystemUtils.isAirplaneModeOn(context)) {
            mUnlockedFlightMode = true
        }

        save()

        check(googleApiClient)
    }

    fun check(googleApiClient: GoogleApiClient) {
        AchievementTask(googleApiClient).execute()
    }

    fun unlock(context: Context, googleApiClient: GoogleApiClient, achievementResId: Int) {
        try {
            val achievementId = context.getString(achievementResId)
            Games.Achievements.unlock(googleApiClient, achievementId)
            if (DEBUG) {
                Log.d(TAG, "unlocked: $achievementId")
            }
        } catch (e: SecurityException) {
            // Not sure why we're still seeing errors about the connection state, but here we are
            Crashlytics.logException(e)
        } catch (e: IllegalStateException) {
            Crashlytics.logException(e)
        } catch (e: NullPointerException) {
            Crashlytics.logException(e)
        }

    }

    object AchievementStats {

        private val mTimes = TreeMap<Long, Long>()

        var completed: Int = 0
            private set
        var perfectScore: Int = 0
            private set
        var isUnlockedWetFeet: Boolean = false
            private set
        var isUnlockedJackOfAllTrades: Boolean = false
            private set
        var isUnlockedNoBrainer: Boolean = false
            private set
        var longestStreak: Int = 0
            private set

        @Synchronized
        fun calculate(context: Context) {
            mTimes.clear()
            completed = 0
            perfectScore = 0
            isUnlockedWetFeet = true
            isUnlockedJackOfAllTrades = false
            isUnlockedNoBrainer = false
            for (puzzle in PuzzleProvider.getInstance(context).all) {
                if (!puzzle.isCompleted) {
                    // Puzzle was not completed
                    if (puzzle.isInstruction) {
                        isUnlockedWetFeet = false
                    }
                    continue
                }
                if (puzzle.isNoScore) {
                    // Puzzle does not qualify for achievements
                    continue
                }
                completed++
                val score = puzzle.score
                if (score != null && score >= 1f) {
                    perfectScore++
                }
                if (puzzle.excessCount == 0 && puzzle.reveals == 0) {
                    isUnlockedJackOfAllTrades = true
                }
                val startTime = puzzle.progress.startTime
                if (startTime > 0) {
                    var duration = puzzle.progress.durationMs
                    if (!puzzle.isCompleted) {
                        duration = 0
                    }
                    if (duration > 0 && duration <= 45 * 1000) {
                        isUnlockedNoBrainer = true
                    }
                    mTimes[startTime] = duration
                }
            }
            var streak = 0
            var bestStreak = 0
            var lastCalendar: Calendar? = null
            for (start in mTimes.keys) {
                val calendar = toCalendar(start)
                if (lastCalendar == null) {
                    // First puzzle
                    streak = 1
                } else if (calendar == lastCalendar) {
                    // Puzzle made on the same day
                } else {
                    val tempCalendar = calendar.clone() as Calendar
                    tempCalendar.add(Calendar.DAY_OF_MONTH, -1)
                    if (tempCalendar == lastCalendar) {
                        // Puzzle made on subsequent day
                        streak++
                    } else {
                        // Puzzle not on a subsequent day; reset the streak
                        bestStreak = Math.max(bestStreak, streak)
                        streak = 1
                    }
                }
                lastCalendar = calendar
            }
            longestStreak = Math.max(bestStreak, streak)
        }

        private fun toCalendar(timestamp: Long): Calendar {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timestamp
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar
        }

        @Synchronized
        fun hasSeries(seriesLength: Int, seriesDuration: Int): Boolean {
            val keys = arrayOfNulls<Long>(mTimes.size)
            mTimes.keys.toTypedArray()
            for (i in seriesLength until mTimes.size) {
                val firstStartTime = keys[i - seriesLength]
                val lastStartTime = keys[i]
                if (firstStartTime == null || lastStartTime == null) {
                    continue
                }
                val duration = mTimes[lastStartTime]
                if (duration == null || duration == 0L) {
                    continue
                }
                val finish = lastStartTime + duration
                val curDuration = finish - firstStartTime
                if (curDuration <= seriesDuration) {
                    return true
                }
            }
            return false
        }

    }

}
