package com.pixplicity.cryptogram.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.providers.PuzzleProvider;

public class StatisticsUtils {

    public static class StatsTask extends AsyncTask<Void, Void, Boolean> {

        private final ViewGroup mStatsView;
        private int count;
        private int scoreCount;
        private float score;
        private long shortestDurationMs;
        private long totalDurationMs;
        private int longestStreak;
        private int lastNumber;

        public StatsTask(ViewGroup statsView) {
            mStatsView = statsView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mStatsView.findViewById(R.id.pb_stats).setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Context context = CryptogramApp.getInstance();
            PuzzleProvider provider = PuzzleProvider.getInstance(context);
            count = 0;
            scoreCount = 0;
            score = 0f;
            shortestDurationMs = 0;
            totalDurationMs = 0;
            for (Puzzle c : provider.getAll()) {
                long durationMs = c.getProgress().getDurationMs();
                if (!c.isInstruction() && c.isCompleted()) {
                    count++;
                    Float puzzleScore = c.getScore();
                    if (puzzleScore == null) {
                        continue;
                    }
                    score += puzzleScore;
                    scoreCount++;
                    if (shortestDurationMs == 0 || shortestDurationMs > durationMs) {
                        shortestDurationMs = durationMs;
                    }
                }
                totalDurationMs += durationMs;
            }
            AchievementProvider.AchievementStats achievementStats = AchievementProvider.getInstance().getAchievementStats();
            achievementStats.calculate(context);
            longestStreak = achievementStats.getLongestStreak();
            lastNumber = provider.getLastNumber();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mStatsView.findViewById(R.id.pb_stats).setVisibility(View.GONE);
            Context context = CryptogramApp.getInstance();
            String scoreAverageText;
            if (scoreCount > 0) {
                scoreAverageText = context.getString(R.string.stats_average_score_format, score / (float) scoreCount * 100f);
            } else {
                scoreAverageText = context.getString(R.string.not_applicable);
            }
            String scoreCumulativeText = context.getString(R.string.stats_cumulative_score_format, score * 100f);
            String fastestCompletionText;
            if (shortestDurationMs == 0) {
                fastestCompletionText = context.getString(R.string.not_applicable);
            } else {
                fastestCompletionText = StringUtils.getDurationString(shortestDurationMs);
            }
            ((TextView) mStatsView.findViewById(R.id.tv_stats_completed)).setText(
                    context.getString(R.string.stats_total_completed_value,
                            count,
                            lastNumber,
                            lastNumber == 0 ? 0 : count / (float) lastNumber * 100f));
            ((TextView) mStatsView.findViewById(R.id.tv_stats_average_score)).setText(
                    context.getString(R.string.stats_average_score_value,
                            scoreAverageText));
            ((TextView) mStatsView.findViewById(R.id.tv_stats_total_score)).setText(
                    context.getString(R.string.stats_cumulative_score_value,
                            scoreCumulativeText));
            ((TextView) mStatsView.findViewById(R.id.tv_stats_fastest)).setText(
                    context.getString(R.string.stats_fastest_completion_value,
                            fastestCompletionText));
            ((TextView) mStatsView.findViewById(R.id.tv_stats_total_time)).setText(
                    context.getString(R.string.stats_total_time_spent_value,
                            StringUtils.getDurationString(totalDurationMs)));
            ((TextView) mStatsView.findViewById(R.id.tv_stats_streak)).setText(
                    context.getString(R.string.stats_longest_streak_value,
                            longestStreak,
                            context.getResources().getQuantityString(R.plurals.days, longestStreak)));
        }

    }

    public static void showDialog(Context context, boolean darkTheme) {
        // Log the event
        Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_STATISTICS));
        // Compose the dialog
        ViewGroup dialogView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.dialog_statistics, null);
        populateTable(context, dialogView);
        new AlertDialog.Builder(context, darkTheme ? R.style.Dialog_Dark : R.style.Dialog_Light)
                .setTitle(R.string.statistics)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                })
                .show();
    }

    public static void populateTable(Context context, ViewGroup statsView) {
        new StatsTask(statsView).execute();
    }

}
