package com.pixplicity.cryptogram.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.providers.PuzzleProvider;

public class StatisticsUtils {

    public static void showDialog(Context context) {
        {
            // Analytics
            CryptogramApp.getInstance().getFirebaseAnalytics().logEvent(CryptogramApp.CONTENT_STATISTICS, null);
            Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_STATISTICS));
        }
        // Compose the dialog
        TableLayout dialogView = (TableLayout) LayoutInflater.from(context).inflate(R.layout.dialog_statistics, null);
        PuzzleProvider provider = PuzzleProvider.getInstance(context);
        int count = 0, scoreCount = 0;
        float score = 0f;
        long shortestDurationMs = 0, totalDurationMs = 0;
        for (Puzzle c : provider.getAll()) {
            long duration = c.getProgress().getDurationMs();
            if (!c.isInstruction() && c.isCompleted()) {
                count++;
                Float puzzleScore = c.getScore();
                if (puzzleScore == null) {
                    continue;
                }
                score += puzzleScore;
                scoreCount++;
                if (shortestDurationMs == 0 || shortestDurationMs > duration) {
                    shortestDurationMs = duration;
                }
            }
            totalDurationMs += duration;
        }
        String scoreAverageText;
        if (scoreCount > 0) {
            scoreAverageText = context.getString(R.string.stats_average_score_format, score / (float) scoreCount * 100f);
        } else {
            scoreAverageText = context.getString(R.string.not_applicable);
        }
        String scoreCumulativeText = context.getString(R.string.stats_cumulative_score_format, score * 100f);
        String fastestCompletion;
        if (shortestDurationMs == 0) {
            fastestCompletion = context.getString(R.string.not_applicable);
        } else {
            fastestCompletion = StringUtils.getDurationString(shortestDurationMs);
        }
        AchievementProvider.AchievementStats achievementStats = AchievementProvider.getInstance().getAchievementStats();
        achievementStats.calculate(context);
        int longestStreak = achievementStats.getLongestStreak();
        {
            View view = LayoutInflater.from(context).inflate(R.layout.in_statistics_row, null);
            ((TextView) view.findViewById(R.id.tv_label)).setText(R.string.stats_total_completed_label);
            final int lastNumber = provider.getLastNumber();
            ((TextView) view.findViewById(R.id.tv_value)).setText(
                    context.getString(R.string.stats_total_completed_value,
                            count,
                            lastNumber,
                            lastNumber == 0 ? 0 : count / (float) lastNumber * 100f));
            dialogView.addView(view);
        }
        {
            View view = LayoutInflater.from(context).inflate(R.layout.in_statistics_row, null);
            ((TextView) view.findViewById(R.id.tv_label)).setText(R.string.stats_average_score_label);
            ((TextView) view.findViewById(R.id.tv_value)).setText(
                    context.getString(R.string.stats_average_score_value,
                            scoreAverageText));
            dialogView.addView(view);
        }
        {
            View view = LayoutInflater.from(context).inflate(R.layout.in_statistics_row, null);
            ((TextView) view.findViewById(R.id.tv_label)).setText(R.string.stats_cumulative_score_label);
            ((TextView) view.findViewById(R.id.tv_value)).setText(
                    context.getString(R.string.stats_cumulative_score_value,
                            scoreCumulativeText));
            dialogView.addView(view);
        }
        {
            View view = LayoutInflater.from(context).inflate(R.layout.in_statistics_row, null);
            ((TextView) view.findViewById(R.id.tv_label)).setText(R.string.stats_fastest_completion_label);
            ((TextView) view.findViewById(R.id.tv_value)).setText(
                    context.getString(R.string.stats_fastest_completion_value,
                            fastestCompletion));
            dialogView.addView(view);
        }
        {
            View view = LayoutInflater.from(context).inflate(R.layout.in_statistics_row, null);
            ((TextView) view.findViewById(R.id.tv_label)).setText(R.string.stats_total_time_spent_label);
            ((TextView) view.findViewById(R.id.tv_value)).setText(
                    context.getString(R.string.stats_total_time_spent_value,
                            StringUtils.getDurationString(totalDurationMs)));
            dialogView.addView(view);
        }
        {
            View view = LayoutInflater.from(context).inflate(R.layout.in_statistics_row, null);
            ((TextView) view.findViewById(R.id.tv_label)).setText(R.string.stats_longest_streak_label);
            ((TextView) view.findViewById(R.id.tv_value)).setText(
                    context.getString(R.string.stats_longest_streak_value,
                            longestStreak,
                            context.getResources().getQuantityString(R.plurals.days, longestStreak)));
            dialogView.addView(view);
        }
        new AlertDialog.Builder(context)
                .setTitle(R.string.statistics)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }

}
