package com.pixplicity.cryptogram.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ShareEvent;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.pixplicity.cryptogram.BuildConfig;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.events.PuzzleEvent;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.providers.PuzzleProvider;
import com.pixplicity.cryptogram.utils.AchievementProvider;
import com.pixplicity.cryptogram.utils.EventProvider;
import com.pixplicity.cryptogram.utils.LeaderboardProvider;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.pixplicity.cryptogram.utils.StatisticsUtils;
import com.pixplicity.cryptogram.utils.StringUtils;
import com.pixplicity.cryptogram.utils.VideoUtils;
import com.pixplicity.cryptogram.views.CryptogramLayout;
import com.pixplicity.cryptogram.views.CryptogramView;
import com.pixplicity.cryptogram.views.HintView;
import com.pixplicity.generate.Rate;
import com.squareup.otto.Subscribe;

import net.soulwolf.widget.ratiolayout.widget.RatioFrameLayout;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;

public class PuzzleActivity extends BaseActivity {

    private static final int ONBOARDING_PAGES = 2;

    public static final int HIGHLIGHT_DELAY = 1200;

    @BindView(R.id.vg_cryptogram)
    protected CryptogramLayout mVgCryptogram;

    @BindView(R.id.tv_author)
    protected TextView mTvAuthor;

    @BindView(R.id.tv_topic)
    protected TextView mTvTopic;

    @BindView(R.id.cryptogram)
    protected CryptogramView mCryptogramView;

    @BindView(R.id.hint)
    protected HintView mHintView;

    @BindView(R.id.tv_error)
    protected TextView mTvError;

    @BindView(R.id.vg_stats)
    protected ViewGroup mVgStats;

    @BindView(R.id.vg_stats_excess)
    protected ViewGroup mVgStatsExcess;

    @BindView(R.id.tv_stats_excess)
    protected TextView mTvStatsExcess;

    @BindView(R.id.vg_stats_time)
    protected ViewGroup mVgStatsTime;

    @BindView(R.id.tv_stats_time)
    protected TextView mTvStatsTime;

    @BindView(R.id.tv_stats_reveals)
    protected TextView mTvStatsReveals;

    @BindView(R.id.vg_stats_score)
    protected ViewGroup mVgStatsScore;

    @BindView(R.id.tv_stats_score)
    protected TextView mTvStatsScore;

    @BindView(R.id.vg_stats_practice)
    protected ViewGroup mVgStatsPractice;

    @BindView(R.id.vs_keyboard)
    protected ViewStub mVsKeyboard;

    @Nullable
    private View mVwKeyboard;

    private Rate mRate;

    private boolean mFreshInstall;

    // FIXME enforce that puzzle UUID and topic ID are provided
    public static Intent create(Context context) {
        return new Intent(context, PuzzleActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
            getWindow().setBackgroundDrawableResource(R.drawable.bg_activity_dark);
        }
        setContentView(R.layout.activity_puzzle);
        if (isDarkTheme()) {
            mVgStats.setBackgroundResource(R.drawable.bg_statistics_dark);
        }

        final PuzzleProvider puzzleProvider = PuzzleProvider.getInstance(this);

        mRate = new Rate.Builder(this)
                .setTriggerCount(10)
                .setMinimumInstallTime((int) TimeUnit.DAYS.toMillis(2))
                .setMessage(getString(R.string.rating, getString(R.string.app_name)))
                .setFeedbackAction(Uri.parse("mailto:paul+cryptogram@pixplicity.com"))
                .build();

        mVgCryptogram.setCrytogramView(mCryptogramView);
        mCryptogramView.setOnPuzzleProgressListener(this::onCryptogramUpdated);
        mCryptogramView.setOnHighlightListener(new CryptogramView.OnHighlightListener() {
            private SparseBooleanArray mHighlightShown = new SparseBooleanArray();

            @Override
            public void onHighlight(int type, PointF point) {
                if (mHighlightShown.get(type, false)) {
                    return;
                }
                if (PrefsUtils.getHighlighted(type)) {
                    return;
                }
                mHighlightShown.put(type, true);
                switch (type) {
                    case PrefsUtils.TYPE_HIGHLIGHT_HYPHENATION:
                        showHighlight(type, point,
                                getString(R.string.highlight_hyphenation_title),
                                getString(R.string.highlight_hyphenation_description)
                        );
                        break;
                    case PrefsUtils.TYPE_HIGHLIGHT_TOUCH_INPUT:
                        if (mFreshInstall) {
                            PrefsUtils.setHighlighted(type, true);
                        } else {
                            showHighlight(PrefsUtils.TYPE_HIGHLIGHT_TOUCH_INPUT, point,
                                    getString(R.string.highlight_touch_input_title),
                                    getString(R.string.highlight_touch_input_description)
                            );
                        }
                        break;
                }
            }
        });

        if (PrefsUtils.getUseSystemKeyboard()) {
            mVsKeyboard.setVisibility(View.GONE);
        } else {
            mVwKeyboard = mVsKeyboard.inflate();
            mVsKeyboard.setVisibility(View.VISIBLE);
            mCryptogramView.setKeyboardView(mVwKeyboard);
        }
    }

    @Nullable
    @Override
    protected Class<? extends Activity> getHierarchicalParent() {
        return BaseActivity.class;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final PuzzleProvider puzzleProvider = PuzzleProvider.getInstance(this);
        Puzzle puzzle = puzzleProvider.getCurrent();
        if (puzzle != null) {
            puzzle.onResume();
        }
        updateCryptogram(puzzle);

        EventProvider.getBus().register(this);

        if (hasOnBoardingPages()) {
            showOnboarding(0);
        } else {
            onGameplayReady();
        }

        mHintView.setVisibility(PrefsUtils.getShowHints() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        final PuzzleProvider puzzleProvider = PuzzleProvider.getInstance(this);
        Puzzle puzzle = puzzleProvider.getCurrent();
        if (puzzle != null) {
            puzzle.onPause();
        }

        EventProvider.getBus().unregister(this);
    }

    @Override
    public void onBackPressed() {
        if (mVwKeyboard != null && mVwKeyboard.isShown()) {
            mCryptogramView.hideSoftInput();
            return;
        }
        super.onBackPressed();
    }

    private boolean hasOnBoardingPages() {
        return PrefsUtils.getOnboarding() < ONBOARDING_PAGES - 1;
    }

    private void showHighlight(final int type, PointF point, final String title,
                               final String description) {
        Rect viewRect = new Rect();
        mCryptogramView.getGlobalVisibleRect(viewRect);
        final int targetX = (int) (point.x + viewRect.left);
        final int targetY = (int) (point.y + viewRect.top);
        final int targetRadius = 48;
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            final long showTime = System.currentTimeMillis();
            TapTargetView.showFor(
                    PuzzleActivity.this,
                    TapTarget.forBounds(new Rect(targetX - targetRadius, targetY - targetRadius, targetX + targetRadius, targetY + targetRadius),
                            title, description)
                             .titleTextColor(R.color.white)
                             .descriptionTextColor(R.color.white)
                             .outerCircleColor(R.color.highlight_color)
                             .cancelable(true)
                             .tintTarget(false)
                             .transparentTarget(true),
                    new TapTargetView.Listener() {
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            dismiss(view);
                        }

                        @Override
                        public void onOuterCircleClick(TapTargetView view) {
                            dismiss(view);
                        }

                        @Override
                        public void onTargetCancel(TapTargetView view) {
                            dismiss(view);
                        }

                        private void dismiss(TapTargetView view) {
                            if (System.currentTimeMillis() - showTime >= 1500) {
                                // Ensure that the user saw the message
                                PrefsUtils.setHighlighted(type, true);
                            }
                            view.dismiss(false);
                        }
                    });
        }, HIGHLIGHT_DELAY);
    }

    private void showOnboarding(final int page) {
        int titleStringResId;
        int textStringResId;
        int actionStringResId = R.string.intro_next;
        VideoUtils.Video video;
        switch (page) {
            case 0:
                titleStringResId = R.string.intro1_title;
                textStringResId = R.string.intro1_text;
                video = VideoUtils.VIDEO_INSTRUCTION;
                break;
            case 1:
                titleStringResId = R.string.intro2_title;
                textStringResId = R.string.intro2_text;
                actionStringResId = R.string.intro_done;
                video = VideoUtils.VIDEO_HELP;
                break;
            case ONBOARDING_PAGES:
            default:
                onGameplayReady();
                return;
        }

        int onboarding = PrefsUtils.getOnboarding();
        if (onboarding == -1) {
            mFreshInstall = true;
        }
        if (onboarding >= page) {
            showOnboarding(page + 1);
            return;
        }

        View customView = LayoutInflater.from(this).inflate(R.layout.dialog_intro, null);

        TextView tvIntro = customView.findViewById(R.id.tv_intro);
        tvIntro.setText(textStringResId);

        final RatioFrameLayout vgRatio = customView.findViewById(R.id.vg_ratio);
        EasyVideoPlayer player = VideoUtils.setup(this, vgRatio, video);

        new MaterialDialog.Builder(this)
                .title(titleStringResId)
                .customView(customView, false)
                .cancelable(false)
                .positiveText(actionStringResId)
                .showListener(dialogInterface -> {
                    if (player != null) {
                        player.start();
                    }
                })
                .onAny((dialog, which) -> {
                    PrefsUtils.setOnboarding(page);
                    showOnboarding(page + 1);
                })
                .show();
    }

    private void updateCryptogram(Puzzle puzzle) {
        if (puzzle != null) {
            PuzzleProvider provider = PuzzleProvider.getInstance(this);
            provider.setCurrentId(puzzle.getId());
            mTvError.setVisibility(View.GONE);
            mVgCryptogram.setVisibility(View.VISIBLE);
            // Apply the puzzle to the CryptogramView
            mCryptogramView.setPuzzle(puzzle);
            // Show other puzzle details
            String author = puzzle.getAuthor();
            if (author == null) {
                mTvAuthor.setVisibility(View.GONE);
            } else {
                mTvAuthor.setVisibility(View.VISIBLE);
                mTvAuthor.setText(getString(R.string.quote_by, author));
            }
            String topic = puzzle.getTopic();
            if ((!PrefsUtils.getShowTopic() && !puzzle.isCompleted()) || topic == null) {
                mTvTopic.setVisibility(View.GONE);
            } else {
                mTvTopic.setVisibility(View.VISIBLE);
                mTvTopic.setText(getString(R.string.topic, topic));
            }
            if (puzzle.isInstruction() || puzzle.isNoScore()) {
                setToolbarSubtitle(puzzle.getTitle(this));
            } else {
                setToolbarSubtitle(getString(
                        R.string.puzzle_number,
                        puzzle.getNumber()));
            }
            // Invoke various events
            onCryptogramUpdated(puzzle);
            puzzle.onResume();
        } else {
            mTvError.setVisibility(View.VISIBLE);
            mVgCryptogram.setVisibility(View.GONE);
            setToolbarSubtitle(null);
        }
    }

    private void onGameplayReady() {
        mCryptogramView.requestFocus();
    }

    public void onCryptogramUpdated(Puzzle puzzle) {
        // Update the HintView as the puzzle updates
        mHintView.setPuzzle(puzzle);
        if (puzzle.isCompleted()) {
            mHintView.setVisibility(View.GONE);
            mVgStats.setVisibility(View.VISIBLE);
            long durationMs = puzzle.getDurationMs();
            if (durationMs <= 0) {
                mVgStatsTime.setVisibility(View.GONE);
            } else {
                mVgStatsTime.setVisibility(View.VISIBLE);
                mTvStatsTime.setText(StringUtils.getDurationString(durationMs));
            }
            int excessCount = puzzle.getExcessCount();
            if (excessCount < 0) {
                mVgStatsExcess.setVisibility(View.GONE);
            } else {
                mVgStatsExcess.setVisibility(View.VISIBLE);
                mTvStatsExcess.setText(String.valueOf(excessCount));
            }
            mTvStatsReveals.setText(String.valueOf(puzzle.getReveals()));
            Float score = puzzle.getScore();
            if (score != null) {
                mVgStatsPractice.setVisibility(View.GONE);
                mVgStatsScore.setVisibility(View.VISIBLE);
                mTvStatsScore.setText(String.format(
                        Locale.ENGLISH,
                        "%.1f%%",
                        score * 100));
            } else {
                mVgStatsScore.setVisibility(View.GONE);
                mVgStatsPractice.setVisibility(puzzle.isNoScore() ? View.VISIBLE : View.GONE);
            }
        } else {
            if (PrefsUtils.getShowHints() && puzzle.hasUserChars()) {
                puzzle.setHadHints(true);
            }
            mHintView.setVisibility(PrefsUtils.getShowHints() ? View.VISIBLE : View.GONE);
            mVgStats.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void onPuzzleLoaded(PuzzleEvent.PuzzlesLoaded event) {
        // Reload the current puzzle we're working on
        updateCryptogram(PuzzleProvider.getInstance(PuzzleActivity.this)
                                       .getCurrent());
    }

    @Subscribe
    public void onPuzzleStyleChanged(PuzzleEvent.PuzzleStyleChanged event) {
        // Just recreate the activity
        recreate();
    }

    @Subscribe
    public void onPuzzleStarted(PuzzleEvent.PuzzleStartedEvent event) {
        if (getGoogleApiClient().isConnected()) {
            // Submit any achievements
            AchievementProvider.getInstance().onCryptogramStart(getGoogleApiClient());
        }
    }

    @Subscribe
    public void onPuzzleReset(PuzzleEvent.PuzzleResetEvent event) {
        updateCryptogram(event.getPuzzle());
    }

    @Subscribe
    public void onPuzzleCompleted(PuzzleEvent.PuzzleCompletedEvent event) {
        updateCryptogram(event.getPuzzle());

        // Increment the trigger for displaying the rating dialog
        mRate.count();

        // Allow the rating dialog to appear if needed
        mRate.showRequest();

        if (getGoogleApiClient().isConnected()) {
            // Submit score
            LeaderboardProvider.getInstance().submit(getGoogleApiClient());

            // Submit any achievements
            AchievementProvider.getInstance().onCryptogramCompleted(getGoogleApiClient());
        }

        // Attempt to save the game to Google Play Saved Games
        PuzzleProvider.getInstance(this).save(getGoogleApiClient(), null);
    }

    @Subscribe
    public void onPuzzleKeyboardInput(PuzzleEvent.KeyboardInputEvent event) {
        final int keyCode = event.getKeyCode();
        mCryptogramView.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
                keyCode, 0));
        mCryptogramView.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_UP,
                keyCode, 0));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_puzzle, menu);
        {
            MenuItem item = menu.findItem(R.id.action_reveal_puzzle);
            item.setVisible(BuildConfig.DEBUG);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }

        final Puzzle puzzle = mCryptogramView.getPuzzle();
        switch (item.getItemId()) {
            case R.id.action_next: {
                nextPuzzle();
            }
            return true;
            case R.id.action_reveal_letter: {
                if (puzzle == null || !mCryptogramView.hasSelectedCharacter()) {
                    showSnackbar(getString(R.string.reveal_letter_instruction));
                } else {
                    if (PrefsUtils.getNeverAskRevealLetter()) {
                        mCryptogramView.revealCharacterMapping(
                                mCryptogramView.getSelectedCharacter());
                    } else {
                        new MaterialDialog.Builder(this)
                                .content(R.string.reveal_letter_confirmation)
                                .checkBoxPromptRes(R.string.never_ask_again, false, null)
                                .positiveText(R.string.reveal)
                                .onPositive((dialog, which) -> {
                                    PrefsUtils.setNeverAskRevealLetter(dialog.isPromptCheckBoxChecked());
                                    mCryptogramView.revealCharacterMapping(
                                            mCryptogramView.getSelectedCharacter());
                                })
                                .negativeText(R.string.cancel)
                                .show();
                    }
                }
            }
            return true;
            case R.id.action_reveal_mistakes: {
                if (PrefsUtils.getNeverAskRevealMistakes()) {
                    mCryptogramView.revealMistakes();
                } else {
                    new MaterialDialog.Builder(this)
                            .content(R.string.reveal_mistakes_confirmation)
                            .checkBoxPromptRes(R.string.never_ask_again, false, null)
                            .positiveText(R.string.reveal)
                            .onPositive((dialog, which) -> {
                                PrefsUtils.setNeverAskRevealMistakes(dialog.isPromptCheckBoxChecked());
                                mCryptogramView.revealMistakes();
                            })
                            .negativeText(R.string.cancel)
                            .show();
                }
            }
            return true;
            case R.id.action_reveal_puzzle: {
                if (BuildConfig.DEBUG) {
                    if (puzzle != null) {
                        puzzle.revealPuzzle();
                    }
                    mCryptogramView.redraw();
                } else {
                    throw new IllegalStateException("Only applicable to debug builds");
                }
            }
            return true;
            case R.id.action_reset: {
                if (puzzle != null) {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.reset_puzzle)
                            .setPositiveButton(R.string.reset, (dialogInterface, i) -> {
                                puzzle.reset(true);
                                mCryptogramView.reset();
                                onCryptogramUpdated(puzzle);
                            })
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                            })
                            .show();
                }
            }
            return true;
            case R.id.action_share: {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String text;
                if (puzzle != null && puzzle.isCompleted()) {
                    text = getString(
                            R.string.share_full,
                            puzzle.getText(),
                            puzzle.getAuthor(),
                            getString(R.string.share_url));
                } else {
                    text = getString(
                            R.string.share_partial,
                            puzzle == null
                                    ? getString(R.string.author_unknown)
                                    : puzzle.getAuthor(),
                            getString(R.string.share_url));
                }
                intent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(intent, getString(R.string.share)));
                // Log the event
                Answers.getInstance().logShare(
                        new ShareEvent()
                                .putContentId(puzzle == null
                                        ? null
                                        : String.valueOf(puzzle.getId()))
                                .putContentType("puzzle")
                                .putContentName(text)
                );
            }
            return true;
            case R.id.action_stats: {
                if (puzzle != null) {
                    // Make sure to save the puzzle first
                    puzzle.save();
                }
                // Now show the stats
                StatisticsUtils.showDialog(this, isDarkTheme());
            }
            return true;
            case R.id.action_how_to_play: {
                startActivity(HowToPlayActivity.create(this));
            }
            return true;
            case R.id.action_about: {
                startActivity(AboutActivity.create(this));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void nextPuzzle() {
        Puzzle puzzle = PuzzleProvider.getInstance(this).getNext();
        updateCryptogram(puzzle);
    }

}
