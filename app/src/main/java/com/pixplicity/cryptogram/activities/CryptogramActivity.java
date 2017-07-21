package com.pixplicity.cryptogram.activities;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
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
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.crashlytics.android.answers.ShareEvent;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.Snapshots;
import com.pixplicity.cryptogram.BuildConfig;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.adapters.PuzzleAdapter;
import com.pixplicity.cryptogram.events.PuzzleEvent;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.utils.AchievementProvider;
import com.pixplicity.cryptogram.utils.EventProvider;
import com.pixplicity.cryptogram.utils.LeaderboardProvider;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.pixplicity.cryptogram.utils.PuzzleProvider;
import com.pixplicity.cryptogram.utils.SavegameManager;
import com.pixplicity.cryptogram.utils.StringUtils;
import com.pixplicity.cryptogram.utils.StyleUtils;
import com.pixplicity.cryptogram.views.CryptogramLayout;
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
    private static final int RC_PLAY_GAMES = 1001;
    private static final int RC_SAVED_GAMES = 1002;

    private static final int ONBOARDING_PAGES = 2;

    public static final String EXTRA_LAUNCH_SETTINGS = "launch_settings";

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

    @BindView(R.id.vg_google_play_games_actions)
    protected ViewGroup mVgGooglePlayGamesActions;

    @BindView(R.id.rv_drawer)
    protected RecyclerView mRvDrawer;

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

    private PuzzleAdapter mAdapter;

    private Rate mRate;

    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = false;

    private int mLastConnectionError;

    private boolean mDarkTheme;
    private boolean mFreshInstall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDarkTheme = PrefsUtils.getDarkTheme();
        if (mDarkTheme) {
            setTheme(R.style.AppTheme_Dark);
            getWindow().setBackgroundDrawableResource(R.drawable.bg_activity_dark);
        }
        setContentView(R.layout.activity_cryptogram);
        if (mDarkTheme) {
            mVgStats.setBackgroundResource(R.drawable.bg_statistics_dark);
        }

        final PuzzleProvider puzzleProvider = PuzzleProvider.getInstance(this);

        // Create the Google Api Client with access to Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                .build();

        mRate = new Rate.Builder(this)
                .setTriggerCount(10)
                .setMinimumInstallTime((int) TimeUnit.DAYS.toMillis(2))
                .setMessage(getString(R.string.rating, getString(R.string.app_name)))
                .setFeedbackAction(Uri.parse("mailto:paul+cryptogram@pixplicity.com"))
                .build();

        mRvDrawer.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PuzzleAdapter(this, new PuzzleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (mDrawerLayout != null) {
                    mDrawerLayout.closeDrawers();
                }
                updateCryptogram(puzzleProvider.get(position));
            }
        });
        mRvDrawer.setAdapter(mAdapter);

        mVgCryptogram.setCrytogramView(mCryptogramView);
        mCryptogramView.setOnPuzzleProgressListener(new CryptogramView.OnPuzzleProgressListener() {
            @Override
            public void onPuzzleProgress(Puzzle puzzle) {
                onCryptogramUpdated(puzzle);
            }
        });
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
                                getString(R.string.highlight_hyphenation_description),
                                1200
                        );
                        break;
                    case PrefsUtils.TYPE_HIGHLIGHT_TOUCH_INPUT:
                        if (mFreshInstall) {
                            PrefsUtils.setHighlighted(type, true);
                        } else {
                            showHighlight(PrefsUtils.TYPE_HIGHLIGHT_TOUCH_INPUT, point,
                                    getString(R.string.highlight_touch_input_title),
                                    getString(R.string.highlight_touch_input_description),
                                    1200
                            );
                        }
                        break;
                }
            }
        });

        updateCryptogram(puzzleProvider.getCurrent());

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getBooleanExtra(EXTRA_LAUNCH_SETTINGS, false)) {
                startActivity(SettingsActivity.create(this));
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();

        final PuzzleProvider puzzleProvider = PuzzleProvider.getInstance(this);
        Puzzle puzzle = puzzleProvider.getCurrent();
        if (puzzle != null) {
            puzzle.onResume();
        }

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

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        final PuzzleProvider puzzleProvider = PuzzleProvider.getInstance(this);
        Puzzle puzzle = puzzleProvider.getCurrent();
        if (puzzle != null) {
            puzzle.onPause();
        }

        EventProvider.getBus().unregister(this);
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
        Log.d(TAG, "onActivityResult: " + requestCode);
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case RC_PLAY_GAMES: {
                Log.d(TAG, "onActivityResult: resolution result");
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                switch (resultCode) {
                    case RESULT_OK: {
                        // Logged in
                        Answers.getInstance().logLogin(new LoginEvent().putSuccess(true));
                        mGoogleApiClient.connect();
                    }
                    break;
                    case GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED: {
                        // Logged out
                        if (mGoogleApiClient.isConnected()) {
                            mGoogleApiClient.disconnect();
                        }
                    }
                    break;
                    case RESULT_CANCELED: {
                        // Canceled; do nothing
                    }
                    break;
                    default: {
                        // Assume some error
                        showGmsError(resultCode);
                    }
                }
            }
            case RC_SAVED_GAMES:
                if (mDrawerLayout != null) {
                    mDrawerLayout.closeDrawers();
                }
                if (intent != null) {
                    if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_METADATA)) {
                        // Load a snapshot.
                        final ProgressDialog pd = new ProgressDialog(this);
                        pd.setMessage("Loading saved game...");
                        pd.show();
                        final SnapshotMetadata snapshotMetadata = intent.getParcelableExtra(Snapshots.EXTRA_SNAPSHOT_METADATA);
                        PuzzleProvider.getInstance(this).load(mGoogleApiClient, snapshotMetadata,
                                new SavegameManager.OnLoadResult() {
                                    @Override
                                    public void onLoadSuccess() {
                                        updateCryptogram(PuzzleProvider.getInstance(CryptogramActivity.this)
                                                                       .getCurrent());
                                        showSnackbar("Game loaded.");
                                        pd.dismiss();
                                    }

                                    @Override
                                    public void onLoadFailure() {
                                        showSnackbar("Sorry, the game state couldn't be restored.");
                                        pd.dismiss();
                                    }
                                });
                    } else if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_NEW)) {
                        PuzzleProvider.getInstance(this).save(mGoogleApiClient,
                                new SavegameManager.OnSaveResult() {
                                    @Override
                                    public void onSaveSuccess() {
                                        showSnackbar("Game saved.");
                                    }

                                    @Override
                                    public void onSaveFailure() {
                                        showSnackbar("Game couldn't be saved at this time.");
                                    }
                                });
                    }
                }
                break;
        }
    }

    private boolean hasOnBoardingPages() {
        return PrefsUtils.getOnboarding() < ONBOARDING_PAGES - 1;
    }

    private void showHighlight(final int type, PointF point, final String title,
                               final String description, int delayMillis) {
        Rect viewRect = new Rect();
        mCryptogramView.getGlobalVisibleRect(viewRect);
        final int targetX = (int) (point.x + viewRect.left);
        final int targetY = (int) (point.y + viewRect.top);
        final int targetRadius = 48;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final long showTime = System.currentTimeMillis();
                TapTargetView.showFor(
                        CryptogramActivity.this,
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
            }
        }, delayMillis);
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
        vgRatio.setRatio(RatioDatumMode.DATUM_WIDTH, videoW, videoH);

        final EasyVideoPlayer player = customView.findViewById(R.id.player);
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
            ImageView ivVideo = customView.findViewById(R.id.iv_still_frame);
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
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                        PrefsUtils.setOnboarding(page);
                        showOnboarding(page + 1);
                    }
                })
                .show();
    }

    @OnClick(R.id.vg_google_play_games)
    protected void onClickGooglePlayGames() {
        if (mGoogleApiClient.isConnected()) {
            // Connected; show gameplay options
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_google_play_games, null);
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .show();

            Button btLeaderboards = dialogView.findViewById(R.id.bt_leaderboards);
            btLeaderboards.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_LEADERBOARDS));
                    try {
                        startActivityForResult(
                                Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, getString(R.string.leaderboard_scoreboard)),
                                RC_UNUSED);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(CryptogramActivity.this, R.string.google_play_games_not_installed, Toast.LENGTH_LONG).show();
                    }
                }
            });

            Button btAchievements = dialogView.findViewById(R.id.bt_achievements);
            btAchievements.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_ACHIEVEMENTS));
                    try {
                        startActivityForResult(
                                Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                                RC_UNUSED);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(CryptogramActivity.this, R.string.google_play_games_not_installed, Toast.LENGTH_LONG).show();
                    }
                }
            });

            Button btRestoreSavedGames = dialogView.findViewById(R.id.bt_restore_saved_games);
            btRestoreSavedGames.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    int maxNumberOfSavedGamesToShow = 5;
                    Intent savedGamesIntent = Games.Snapshots.getSelectSnapshotIntent(mGoogleApiClient,
                            "See My Saves", true, true, maxNumberOfSavedGamesToShow);
                    startActivityForResult(savedGamesIntent, RC_SAVED_GAMES);
                }
            });

            Button btSignOut = dialogView.findViewById(R.id.bt_sign_out);
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

    private void updateCryptogram(Puzzle puzzle) {
        if (puzzle != null) {
            PuzzleProvider provider = PuzzleProvider.getInstance(this);
            provider.setCurrentId(puzzle.getId());
            mRvDrawer.scrollToPosition(provider.getCurrentIndex());
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
                mTvAuthor.setText(getString(R.string.quote, author));
            }
            String topic = puzzle.getTopic();
            if ((!PrefsUtils.getShowTopic() && !puzzle.isCompleted()) || topic == null) {
                mTvTopic.setVisibility(View.GONE);
            } else {
                mTvTopic.setVisibility(View.VISIBLE);
                mTvTopic.setText(getString(R.string.topic, topic));
            }
            if (puzzle.isInstruction() || puzzle.isNoScore()) {
                mToolbar.setSubtitle(puzzle.getTitle(this));
            } else {
                mToolbar.setSubtitle(getString(
                        R.string.puzzle_number,
                        puzzle.getNumber()));
            }
            // Invoke various events
            onCryptogramUpdated(puzzle);
            puzzle.onResume();
        } else {
            mTvError.setVisibility(View.VISIBLE);
            mVgCryptogram.setVisibility(View.GONE);
            mToolbar.setSubtitle(null);
        }
    }

    private void onGameplayReady() {
        mCryptogramView.requestFocus();
    }

    public void onCryptogramUpdated(Puzzle puzzle) {
        // Update the HintView as the puzzle updates
        mHintView.setPuzzle(puzzle);
        mAdapter.notifyDataSetChanged();
        if (puzzle.isCompleted()) {
            mHintView.setVisibility(View.GONE);
            mVgStats.setVisibility(View.VISIBLE);
            long durationMs = puzzle.getDuration();
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
            mVgStatsScore.setVisibility(View.GONE);
            Float score = puzzle.getScore();
            if (score != null) {
                mVgStatsScore.setVisibility(View.VISIBLE);
                mTvStatsScore.setText(String.format(
                        Locale.ENGLISH,
                        "%.1f%%",
                        score * 100));
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
    public void onPuzzleStyleChanged(PuzzleEvent.PuzzleStyleChanged event) {
        // Just recreate the activity
        recreate();
    }

    @Subscribe
    public void onPuzzleStarted(PuzzleEvent.PuzzleStartedEvent event) {
        if (mGoogleApiClient.isConnected()) {
            // Submit any achievements
            AchievementProvider.getInstance().onCryptogramStart(mGoogleApiClient);
        }
    }

    @Subscribe
    public void onPuzzleCompleted(PuzzleEvent.PuzzleCompletedEvent event) {
        updateCryptogram(event.getPuzzle());

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

        // Attempt to save the game to Google Play Saved Games
        PuzzleProvider.getInstance(this).save(mGoogleApiClient, null);
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
            MenuItem item = menu.findItem(R.id.action_dark_theme);
            item.setChecked(PrefsUtils.getDarkTheme());
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
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog,
                                                        @NonNull DialogAction which) {
                                        PrefsUtils.setNeverAskRevealLetter(dialog.isPromptCheckBoxChecked());
                                        mCryptogramView.revealCharacterMapping(
                                                mCryptogramView.getSelectedCharacter());
                                    }
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
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog,
                                                    @NonNull DialogAction which) {
                                    PrefsUtils.setNeverAskRevealMistakes(dialog.isPromptCheckBoxChecked());
                                    mCryptogramView.revealMistakes();
                                }
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
                    mCryptogramView.invalidate();
                } else {
                    throw new IllegalStateException("Only applicable to debug builds");
                }
            }
            return true;
            case R.id.action_reset: {
                if (puzzle != null) {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.reset_puzzle)
                            .setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    puzzle.reset();
                                    mCryptogramView.reset();
                                    onCryptogramUpdated(puzzle);
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
                if (puzzle == null) {
                    if (mDrawerLayout != null) {
                        mDrawerLayout.openDrawer(GravityCompat.START);
                    }
                    break;
                }
                String prefilledText = null;
                int currentId = puzzle.getNumber();
                if (currentId > 0) {
                    prefilledText = String.valueOf(currentId);
                }
                new MaterialDialog.Builder(this)
                        .content(R.string.go_to_puzzle_content)
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input(null, prefilledText, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                MDButton button = dialog.getActionButton(DialogAction.POSITIVE);
                                try {
                                    button.setEnabled(Integer.parseInt(input.toString()) > 0);
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
                            public void onClick(@NonNull MaterialDialog dialog,
                                                @NonNull DialogAction which) {
                                //noinspection ConstantConditions
                                Editable input = dialog.getInputEditText().getText();
                                try {
                                    int puzzleNumber = Integer.parseInt(input.toString());
                                    PuzzleProvider provider = PuzzleProvider
                                            .getInstance(CryptogramActivity.this);
                                    Puzzle puzzle = provider.getByNumber(puzzleNumber);
                                    if (puzzle == null) {
                                        showSnackbar(getString(R.string.puzzle_nonexistant, puzzleNumber));
                                    } else {
                                        updateCryptogram(puzzle);
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
                onCryptogramUpdated(puzzle);
            }
            return true;
            case R.id.action_dark_theme: {
                mDarkTheme = !mDarkTheme;
                PrefsUtils.setDarkTheme(mDarkTheme);
                item.setChecked(mDarkTheme);
                // Relaunch as though launched from home screen
                Intent i = getBaseContext().getPackageManager()
                                           .getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
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
                            puzzle.getAuthor(),
                            getString(R.string.share_url));
                }
                intent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(intent, getString(R.string.share)));
                // Log the event
                Answers.getInstance().logShare(
                        new ShareEvent()
                                .putContentId(String.valueOf(puzzle.getId()))
                                .putContentType("puzzle")
                                .putContentName(text)
                );
            }
            return true;
            case R.id.action_stats: {
                // Log the event
                Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_STATISTICS));
                // Compose the dialog
                TableLayout dialogView = (TableLayout) LayoutInflater.from(this).inflate(R.layout.dialog_statistics, null);
                if (puzzle != null) {
                    puzzle.save();
                }
                PuzzleProvider provider = PuzzleProvider.getInstance(this);
                int count = 0, scoreCount = 0;
                float score = 0f;
                long shortestDurationMs = 0, totalDurationMs = 0;
                for (Puzzle c : provider.getAll()) {
                    long duration = c.getProgress().getDuration();
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
                                    provider.getLastNumber()));
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
            case R.id.action_settings: {
                startActivity(SettingsActivity.create(this));
            }
            return true;
            case R.id.action_about: {
                startActivity(AboutActivity.create(this));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSnackbar(String text) {
        final Snackbar snackbar = Snackbar.make(mVgRoot, text, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();

        // Set background
        @ColorInt int colorPrimary = StyleUtils.getColor(this, R.attr.colorPrimary);
        snackBarView.setBackgroundColor(colorPrimary);

        // Set foreground
        @ColorInt int textColor = StyleUtils.getColor(this, R.attr.textColorOnPrimary);
        TextView textView = snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(textColor);

        snackbar.show();
    }

    private void nextPuzzle() {
        Puzzle puzzle = PuzzleProvider.getInstance(this).getNext();
        updateCryptogram(puzzle);
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
        Log.d(TAG, "onConnectionFailed: attempting to resolve");
        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed: already resolving");
            return;
        }

        mLastConnectionError = connectionResult.getErrorCode();
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;
            boolean noResolution = true;
            if (connectionResult.hasResolution()) {
                try {
                    Log.d(TAG, "onConnectionFailed: offering resolution");
                    connectionResult.startResolutionForResult(this, RC_PLAY_GAMES);
                    noResolution = false;
                } catch (IntentSender.SendIntentException e) {
                    Crashlytics.logException(e);
                    Log.e(TAG, "onConnectionFailed: couldn't resolve", e);
                }
            }
            if (noResolution) {
                Log.e(TAG, "onConnectionFailed: no resolution for: " + connectionResult.toString());
                mResolvingConnectionFailure = false;
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
            Uri imageUri;
            if (p == null) {
                displayName = getString(R.string.google_play_games_player_unknown);
                imageUri = null;
            } else {
                displayName = p.getDisplayName();
                imageUri = p.hasHiResImage() ? p.getHiResImageUri() : p.getIconImageUri();
                //bannerUri = p.getBannerImageLandscapeUri();
            }
            Log.w(TAG, "onConnected(): current player is " + displayName);

            mIvGooglePlayGamesIcon.setVisibility(View.GONE);
            mIvGooglePlayGamesAvatar.setVisibility(View.VISIBLE);
            ImageManager.create(this).loadImage(mIvGooglePlayGamesAvatar, imageUri, R.drawable.im_avatar);
            mTvGooglePlayGames.setVisibility(View.GONE);
            mTvGooglePlayGamesName.setVisibility(View.VISIBLE);
            mTvGooglePlayGamesName.setText(displayName);
            mVgGooglePlayGamesActions.setVisibility(View.VISIBLE);
        } else {
            mIvGooglePlayGamesIcon.setVisibility(View.VISIBLE);
            mIvGooglePlayGamesAvatar.setVisibility(View.GONE);
            mTvGooglePlayGames.setVisibility(View.VISIBLE);
            mTvGooglePlayGamesName.setVisibility(View.GONE);
            mVgGooglePlayGamesActions.setVisibility(View.GONE);
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
