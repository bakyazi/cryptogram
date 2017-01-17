package com.pixplicity.cryptogram.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.pixplicity.cryptogram.BuildConfig;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.adapters.CryptogramAdapter;
import com.pixplicity.cryptogram.events.CryptogramEvent;
import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.cryptogram.utils.AchievementProvider;
import com.pixplicity.cryptogram.utils.CryptogramProvider;
import com.pixplicity.cryptogram.utils.LeaderboardProvider;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.pixplicity.cryptogram.utils.StringUtils;
import com.pixplicity.cryptogram.views.CryptogramView;
import com.pixplicity.cryptogram.views.HintView;
import com.pixplicity.generate.Rate;
import com.squareup.otto.Subscribe;

import net.soulwolf.widget.ratiolayout.RatioDatumMode;
import net.soulwolf.widget.ratiolayout.widget.RatioFrameLayout;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;

public class CryptogramActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = CryptogramActivity.class.getSimpleName();

    private static final int RC_UNUSED = 1000;
    private static final int RC_SIGN_IN = 1001;

    @BindView(R.id.iv_google_play_games_banner)
    protected ImageView mIvGooglePlayGamesBanner;

    @BindView(R.id.iv_google_play_games_icon)
    protected ImageView mIvGooglePlayGamesIcon;

    @BindView(R.id.tv_google_play_games)
    protected TextView mTvGooglePlayGames;

    @BindView(R.id.iv_google_play_games_avatar)
    protected ImageView mIvGooglePlayGamesAvatar;

    @BindView(R.id.tv_google_play_games_name)
    protected TextView mTvGooglePlayGamesName;

    @BindView(R.id.rv_drawer)
    protected RecyclerView mRvDrawer;

    @BindView(R.id.vg_cryptogram)
    protected ViewGroup mVgCryptogram;

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

    private CryptogramAdapter mAdapter;

    private Rate mRate;

    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = false;

    private int mLastConnectionError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cryptogram);

        final CryptogramProvider cryptogramProvider = CryptogramProvider.getInstance(this);

        // Create the Google Api Client with access to Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mRate = new Rate.Builder(this)
                .setTriggerCount(10)
                .setMinimumInstallTime((int) TimeUnit.DAYS.toMillis(2))
                .setMessage(getString(R.string.rating, getString(R.string.app_name)))
                .setFeedbackAction(Uri.parse("mailto:paul@pixplicity.com"))
                .build();

        mRvDrawer.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CryptogramAdapter(this, new CryptogramAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (mDrawerLayout != null) {
                    mDrawerLayout.closeDrawers();
                }
                updateCryptogram(cryptogramProvider.get(position));
            }
        });
        mRvDrawer.setAdapter(mAdapter);

        mCryptogramView.setOnCryptogramProgressListener(new CryptogramView.OnCryptogramProgressListener() {
            @Override
            public void onCryptogramProgress(Cryptogram cryptogram) {
                onCryptogramUpdated(cryptogram);
            }
        });

        showOnboarding(0);

        updateCryptogram(cryptogramProvider.getCurrent());
    }

    private void showOnboarding(final int page) {
        int titleStringResId;
        int textStringResId;
        int actionStringResId = R.string.intro_next;
        int stillFrameResId;
        String videoResName;
        int videoW, videoH;
        switch (page) {
            case 0:
                titleStringResId = R.string.intro1_title;
                textStringResId = R.string.intro1_text;
                videoResName = "vid_intro1";
                stillFrameResId = R.drawable.im_intro1;
                videoW = 1088;
                videoH = 386;
                break;
            case 1:
                titleStringResId = R.string.intro2_title;
                textStringResId = R.string.intro2_text;
                actionStringResId = R.string.intro_done;
                videoResName = "vid_intro2";
                stillFrameResId = R.drawable.im_intro2;
                videoW = 1088;
                videoH = 962;
                break;
            default:
                onGameplayReady();
                return;
        }

        if (PrefsUtils.getOnboarding() >= page) {
            showOnboarding(page + 1);
            return;
        }

        View customView = LayoutInflater.from(this).inflate(R.layout.dialog_intro, null);

        TextView tvIntro = (TextView) customView.findViewById(R.id.tv_intro);
        tvIntro.setText(textStringResId);

        final RatioFrameLayout vgRatio = (RatioFrameLayout) customView.findViewById(R.id.vg_ratio);
        vgRatio.setRatio(RatioDatumMode.DATUM_WIDTH, videoW, videoH);

        final EasyVideoPlayer player = (EasyVideoPlayer) customView.findViewById(R.id.player);
        if (player != null) {
            player.disableControls();
            player.setBackgroundColor(Color.WHITE);
            player.setCallback(new EasyVideoCallback() {
                @Override
                public void onStarted(EasyVideoPlayer player) {
                }

                @Override
                public void onPaused(EasyVideoPlayer player) {
                }

                @Override
                public void onPreparing(EasyVideoPlayer player) {
                }

                @Override
                public void onPrepared(EasyVideoPlayer player) {
                }

                @Override
                public void onBuffering(int percent) {
                }

                @Override
                public void onError(EasyVideoPlayer player, Exception e) {
                }

                @Override
                public void onCompletion(EasyVideoPlayer player) {
                    player.seekTo(0);
                    player.start();
                }

                @Override
                public void onRetry(EasyVideoPlayer player, Uri source) {
                }

                @Override
                public void onSubmit(EasyVideoPlayer player, Uri source) {
                }
            });
            player.setAutoPlay(true);

            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/raw/" + videoResName);
            player.setSource(uri);
        } else {
            ImageView ivVideo = (ImageView) customView.findViewById(R.id.iv_still_frame);
            ivVideo.setImageResource(stillFrameResId);
        }

        new MaterialDialog.Builder(this)
                .title(titleStringResId)
                .customView(customView, false)
                .cancelable(false)
                .positiveText(actionStringResId)
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        if (player != null) {
                            player.start();
                        }
                    }
                })
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PrefsUtils.setOnboarding(page);
                        showOnboarding(page + 1);
                    }
                })
                .show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();

        final CryptogramProvider cryptogramProvider = CryptogramProvider.getInstance(this);
        Cryptogram cryptogram = cryptogramProvider.getCurrent();
        if (cryptogram != null) {
            cryptogram.onResume();
        }

        CryptogramApp.getInstance().getBus().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        final CryptogramProvider cryptogramProvider = CryptogramProvider.getInstance(this);
        Cryptogram cryptogram = cryptogramProvider.getCurrent();
        if (cryptogram != null) {
            cryptogram.onPause();
        }

        CryptogramApp.getInstance().getBus().unregister(this);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                showGmsError(resultCode);
            }
        }
    }

    @OnClick(R.id.vg_google_play_games)
    protected void onClickGooglePlayGames() {
        if (mGoogleApiClient.isConnected()) {
            // Connected; show gameplay options
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_google_play_games, null);
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .show();

            Button btLeaderboards = (Button) dialogView.findViewById(R.id.bt_leaderboards);
            btLeaderboards.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    startActivityForResult(
                            Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, getString(R.string.leaderboard_scoreboard)),
                            RC_UNUSED);
                }
            });

            Button btAchievements = (Button) dialogView.findViewById(R.id.bt_achievements);
            btAchievements.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    startActivityForResult(
                            Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                            RC_UNUSED);
                }
            });

            Button btSignOut = (Button) dialogView.findViewById(R.id.bt_sign_out);
            btSignOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    mSignInClicked = false;
                    Games.signOut(mGoogleApiClient);
                    if (mGoogleApiClient.isConnected()) {
                        mGoogleApiClient.disconnect();
                    }
                    updateGooglePlayGames();
                }
            });
        } else {
            // start the sign-in flow
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
    }

    private void updateCryptogram(Cryptogram cryptogram) {
        if (cryptogram != null) {
            CryptogramProvider provider = CryptogramProvider.getInstance(this);
            provider.setCurrentId(cryptogram.getId());
            mRvDrawer.smoothScrollToPosition(
                    provider.getCurrentIndex());
            mTvError.setVisibility(View.GONE);
            mVgCryptogram.setVisibility(View.VISIBLE);
            // Apply the puzzle to the CryptogramView
            mCryptogramView.setCryptogram(cryptogram);
            // Show other puzzle details
            String author = cryptogram.getAuthor();
            if (author == null) {
                mTvAuthor.setVisibility(View.GONE);
            } else {
                mTvAuthor.setVisibility(View.VISIBLE);
                mTvAuthor.setText(getString(R.string.quote, author));
            }
            String topic = cryptogram.getTopic();
            if (topic == null) {
                mTvTopic.setVisibility(View.GONE);
            } else {
                mTvTopic.setVisibility(View.VISIBLE);
                mTvTopic.setText(getString(R.string.topic, topic));
            }
            if (cryptogram.isInstruction() || cryptogram.isNoScore()) {
                mToolbar.setSubtitle(cryptogram.getTitle(this));
            } else {
                mToolbar.setSubtitle(getString(
                        R.string.puzzle_number_of_total,
                        cryptogram.getNumber(),
                        provider.getCount()));
            }
            // Invoke various events
            onCryptogramUpdated(cryptogram);
            cryptogram.onResume();
        } else {
            mTvError.setVisibility(View.VISIBLE);
            mVgCryptogram.setVisibility(View.GONE);
            mToolbar.setSubtitle(null);
        }
    }

    private void onGameplayReady() {
        mCryptogramView.requestFocus();
    }

    public void onCryptogramUpdated(Cryptogram cryptogram) {
        // Update the HintView as the puzzle updates
        mHintView.setCryptogram(cryptogram);
        mAdapter.notifyDataSetChanged();
        if (cryptogram.isCompleted()) {
            mHintView.setVisibility(View.GONE);
            mVgStats.setVisibility(View.VISIBLE);
            long durationMs = cryptogram.getDuration();
            if (durationMs <= 0) {
                mVgStatsTime.setVisibility(View.GONE);
            } else {
                mVgStatsTime.setVisibility(View.VISIBLE);
                mTvStatsTime.setText(StringUtils.getDurationString(durationMs));
            }
            int excessCount = cryptogram.getExcessCount();
            if (excessCount < 0) {
                mVgStatsExcess.setVisibility(View.GONE);
            } else {
                mVgStatsExcess.setVisibility(View.VISIBLE);
                mTvStatsExcess.setText(String.valueOf(excessCount));
            }
            mTvStatsReveals.setText(String.valueOf(cryptogram.getReveals()));
            mVgStatsScore.setVisibility(View.GONE);
            Float score = cryptogram.getScore();
            if (score != null) {
                mVgStatsScore.setVisibility(View.VISIBLE);
                mTvStatsScore.setText(String.format(
                        Locale.ENGLISH,
                        "%.1f%%",
                        score * 100));
            }
        } else {
            if (PrefsUtils.getShowHints() && cryptogram.hasUserChars()) {
                cryptogram.setHadHints(true);
            }
            mHintView.setVisibility(PrefsUtils.getShowHints() ? View.VISIBLE : View.GONE);
            mVgStats.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void onCryptogramStarted(CryptogramEvent.CryptogramStartedEvent event) {
        if (mGoogleApiClient.isConnected()) {
            // Submit any achievements
            AchievementProvider.getInstance().onCryptogramStart(mGoogleApiClient);
        }
    }

    @Subscribe
    public void onCryptogramCompleted(CryptogramEvent.CryptogramCompletedEvent event) {
        // Increment the trigger for displaying the rating dialog
        mRate.launched();

        // Allow the rating dialog to appear if needed
        mRate.check();

        if (mGoogleApiClient.isConnected()) {
            // Submit score
            LeaderboardProvider.getInstance().submit(mGoogleApiClient);

            // Submit any achievements
            AchievementProvider.getInstance().onCryptogramCompleted(mGoogleApiClient);
        }
    }

    @Override
    protected void onDrawerOpened(View drawerView) {
    }

    @Override
    protected void onDrawerClosed(View drawerView) {
    }

    @Override
    protected void onDrawerMoving() {
        mCryptogramView.hideSoftInput();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cryptogram, menu);
        {
            MenuItem item = menu.findItem(R.id.action_randomize);
            item.setChecked(PrefsUtils.getRandomize());
        }
        {
            MenuItem item = menu.findItem(R.id.action_show_hints);
            item.setChecked(PrefsUtils.getShowHints());
        }
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

        final Cryptogram cryptogram = mCryptogramView.getCryptogram();
        switch (item.getItemId()) {
            case R.id.action_google_play_games: {
                onClickGooglePlayGames();
            }
            return true;
            case R.id.action_next: {
                if (cryptogram == null || cryptogram.isCompleted()) {
                    nextPuzzle();
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.skip_puzzle)
                            .setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    nextPuzzle();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .show();
                }
            }
            return true;
            case R.id.action_reveal: {
                if (cryptogram == null || !mCryptogramView.hasSelectedCharacter()) {
                    Snackbar.make(mVgContent, "Please select a letter first.", Snackbar.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.reveal_confirmation)
                            .setPositiveButton(R.string.reveal, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mCryptogramView.revealCharacterMapping(
                                            mCryptogramView.getSelectedCharacter())) {
                                        // Answer revealed; clear the selection
                                        mCryptogramView.setSelectedCharacter((char) 0);
                                    }
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .show();
                }
            }
            return true;
            case R.id.action_reveal_puzzle: {
                if (BuildConfig.DEBUG) {
                    if (cryptogram != null) {
                        cryptogram.revealPuzzle();
                    }
                    mCryptogramView.invalidate();
                } else {
                    throw new IllegalStateException("Only applicable to debug builds");
                }
            }
            return true;
            case R.id.action_reset: {
                if (cryptogram != null) {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.reset_puzzle)
                            .setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    cryptogram.reset();
                                    mCryptogramView.reset();
                                    onCryptogramUpdated(cryptogram);
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .show();
                }
            }
            return true;
            case R.id.action_go_to: {
                String currentId = String.valueOf(cryptogram.getNumber());
                new MaterialDialog.Builder(this)
                        .content(R.string.go_to_puzzle_content)
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input(null, currentId, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                MDButton button = dialog.getActionButton(DialogAction.POSITIVE);
                                try {
                                    //noinspection ResultOfMethodCallIgnored
                                    Integer.parseInt(input.toString());
                                    button.setEnabled(true);
                                } catch (NumberFormatException ignored) {
                                    button.setEnabled(false);
                                }
                            }
                        })
                        .alwaysCallInputCallback()
                        .showListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialogInterface) {
                                MaterialDialog dialog = (MaterialDialog) dialogInterface;
                                //noinspection ConstantConditions
                                dialog.getInputEditText().selectAll();
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                //noinspection ConstantConditions
                                Editable input = dialog.getInputEditText().getText();
                                try {
                                    int puzzleNumber = Integer.parseInt(input.toString());
                                    CryptogramProvider provider = CryptogramProvider
                                            .getInstance(CryptogramActivity.this);
                                    Cryptogram cryptogram = provider.getByNumber(puzzleNumber);
                                    if (cryptogram == null) {
                                        Snackbar.make(mVgContent, getString(R.string.puzzle_nonexistant, puzzleNumber),
                                                Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        updateCryptogram(cryptogram);
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }).show();
            }
            return true;
            case R.id.action_randomize: {
                boolean randomize = !item.isChecked();
                PrefsUtils.setRandomize(randomize);
                item.setChecked(randomize);
            }
            return true;
            case R.id.action_show_hints: {
                boolean showHints = !item.isChecked();
                PrefsUtils.setShowHints(showHints);
                item.setChecked(showHints);
                onCryptogramUpdated(cryptogram);
            }
            return true;
            case R.id.action_share: {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                if (cryptogram != null && cryptogram.isCompleted()) {
                    intent.putExtra(Intent.EXTRA_TEXT, getString(
                            R.string.share_full,
                            cryptogram.getText(),
                            cryptogram.getAuthor(),
                            getString(R.string.share_url)));
                } else {
                    intent.putExtra(Intent.EXTRA_TEXT, getString(
                            R.string.share_partial,
                            cryptogram.getAuthor(),
                            getString(R.string.share_url)));
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share)));
            }
            return true;
            case R.id.action_stats: {
                TableLayout dialogView = (TableLayout) LayoutInflater.from(this).inflate(R.layout.dialog_statistics, null);
                if (cryptogram != null) {
                    cryptogram.save();
                }
                CryptogramProvider provider = CryptogramProvider.getInstance(this);
                int count = 0, scoreCount = 0;
                float score = 0f;
                long shortestDurationMs = 0, totalDurationMs = 0;
                for (Cryptogram c : provider.getAll()) {
                    long duration = c.getProgress().getDuration();
                    if (c.isCompleted()) {
                        count++;
                        Float puzzleScore = c.getScore();
                        if (puzzleScore != null) {
                            score += puzzleScore;
                            scoreCount++;
                        }
                        if (shortestDurationMs == 0 || shortestDurationMs > duration) {
                            shortestDurationMs = duration;
                        }
                    }
                    totalDurationMs += duration;
                }
                String scoreAverageText;
                if (scoreCount > 0) {
                    scoreAverageText = getString(R.string.stats_average_score_format, score / (float) scoreCount * 100f);
                } else {
                    scoreAverageText = getString(R.string.not_applicable);
                }
                String scoreCumulativeText = getString(R.string.stats_cumulative_score_format, score * 100f);
                String fastestCompletion;
                if (shortestDurationMs == 0) {
                    fastestCompletion = getString(R.string.not_applicable);
                } else {
                    fastestCompletion = StringUtils.getDurationString(shortestDurationMs);
                }
                AchievementProvider.AchievementStats achievementStats = AchievementProvider.getInstance().getAchievementStats();
                achievementStats.calculate(this);
                int longestStreak = achievementStats.getLongestStreak();
                {
                    View view = LayoutInflater.from(this).inflate(R.layout.in_statistics_row, null);
                    ((TextView) view.findViewById(R.id.tv_label)).setText(R.string.stats_total_completed_label);
                    ((TextView) view.findViewById(R.id.tv_value)).setText(
                            getString(R.string.stats_total_completed_value,
                                    count,
                                    provider.getCount()));
                    dialogView.addView(view);
                }
                {
                    View view = LayoutInflater.from(this).inflate(R.layout.in_statistics_row, null);
                    ((TextView) view.findViewById(R.id.tv_label)).setText(R.string.stats_average_score_label);
                    ((TextView) view.findViewById(R.id.tv_value)).setText(
                            getString(R.string.stats_average_score_value,
                                    scoreAverageText));
                    dialogView.addView(view);
                }
                {
                    View view = LayoutInflater.from(this).inflate(R.layout.in_statistics_row, null);
                    ((TextView) view.findViewById(R.id.tv_label)).setText(R.string.stats_cumulative_score_label);
                    ((TextView) view.findViewById(R.id.tv_value)).setText(
                            getString(R.string.stats_cumulative_score_value,
                                    scoreCumulativeText));
                    dialogView.addView(view);
                }
                {
                    View view = LayoutInflater.from(this).inflate(R.layout.in_statistics_row, null);
                    ((TextView) view.findViewById(R.id.tv_label)).setText(R.string.stats_fastest_completion_label);
                    ((TextView) view.findViewById(R.id.tv_value)).setText(
                            getString(R.string.stats_fastest_completion_value,
                                    fastestCompletion));
                    dialogView.addView(view);
                }
                {
                    View view = LayoutInflater.from(this).inflate(R.layout.in_statistics_row, null);
                    ((TextView) view.findViewById(R.id.tv_label)).setText(R.string.stats_total_time_spent_label);
                    ((TextView) view.findViewById(R.id.tv_value)).setText(
                            getString(R.string.stats_total_time_spent_value,
                                    StringUtils.getDurationString(totalDurationMs)));
                    dialogView.addView(view);
                }
                {
                    View view = LayoutInflater.from(this).inflate(R.layout.in_statistics_row, null);
                    ((TextView) view.findViewById(R.id.tv_label)).setText(R.string.stats_longest_streak_label);
                    ((TextView) view.findViewById(R.id.tv_value)).setText(
                            getString(R.string.stats_longest_streak_value,
                                    longestStreak,
                                    getResources().getQuantityString(R.plurals.days, longestStreak)));
                    dialogView.addView(view);
                }
                new AlertDialog.Builder(this)
                        .setTitle(R.string.statistics)
                        .setView(dialogView)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
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
        Cryptogram cryptogram = CryptogramProvider.getInstance(this).getNext();
        updateCryptogram(cryptogram);
    }

    // Google Play Services
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLastConnectionError = 0;
        Log.d(TAG, "onConnected(): connected to Google APIs");

        updateGooglePlayGames();

        if (mGoogleApiClient.isConnected()) {
            // Submit score
            LeaderboardProvider.getInstance().submit(mGoogleApiClient);

            // Submit any achievements
            AchievementProvider.getInstance().check(mGoogleApiClient);
        }
    }

    // Google Play Services
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): attempting to connect");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed(): attempting to resolve");
        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed(): already resolving");
            return;
        }

        mLastConnectionError = connectionResult.getErrorCode();
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;
            try {
                connectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                Crashlytics.logException(e);
                showGmsError(0);
            }
        }
        updateGooglePlayGames();
    }

    private void updateGooglePlayGames() {
        if (mGoogleApiClient.isConnected()) {
            // Set the greeting appropriately on main menu
            Player p = Games.Players.getCurrentPlayer(mGoogleApiClient);
            String displayName;
            Uri imageUri, bannerUri;
            if (p == null) {
                displayName = "???";
                imageUri = null;
                bannerUri = null;
            } else {
                displayName = p.getDisplayName();
                imageUri = p.hasHiResImage() ? p.getHiResImageUri() : p.getIconImageUri();
                bannerUri = p.getBannerImageLandscapeUri();
            }
            Log.w(TAG, "onConnected(): current player is " + displayName);

            mIvGooglePlayGamesIcon.setVisibility(View.GONE);
            mIvGooglePlayGamesAvatar.setVisibility(View.VISIBLE);
            ImageManager.create(this).loadImage(mIvGooglePlayGamesAvatar, imageUri, R.drawable.im_avatar);
            mTvGooglePlayGames.setVisibility(View.GONE);
            mTvGooglePlayGamesName.setVisibility(View.VISIBLE);
            mTvGooglePlayGamesName.setText(displayName);
        } else {
            mIvGooglePlayGamesIcon.setVisibility(View.VISIBLE);
            mIvGooglePlayGamesAvatar.setVisibility(View.GONE);
            mTvGooglePlayGames.setVisibility(View.VISIBLE);
            mTvGooglePlayGamesName.setVisibility(View.GONE);
        }
    }

    private void showGmsError(int errorCode) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.google_play_games_connection_failure, mLastConnectionError, errorCode))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

}
