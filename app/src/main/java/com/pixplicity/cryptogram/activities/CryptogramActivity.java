package com.pixplicity.cryptogram.activities;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixplicity.cryptogram.BuildConfig;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.adapters.PuzzleAdapter;
import com.pixplicity.cryptogram.events.PuzzleEvent;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.models.PuzzleList;
import com.pixplicity.cryptogram.models.Topic;
import com.pixplicity.cryptogram.providers.PuzzleProvider;
import com.pixplicity.cryptogram.providers.TopicProvider;
import com.pixplicity.cryptogram.utils.AchievementProvider;
import com.pixplicity.cryptogram.utils.EventProvider;
import com.pixplicity.cryptogram.utils.LeaderboardProvider;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.pixplicity.cryptogram.utils.StatisticsUtils;
import com.pixplicity.cryptogram.utils.SavegameManager;
import com.pixplicity.cryptogram.utils.StringUtils;
import com.pixplicity.cryptogram.utils.UpdateManager;
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
import butterknife.OnClick;

public class CryptogramActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = CryptogramActivity.class.getSimpleName();

    private static final int RC_UNUSED = 1000;
    private static final int RC_PLAY_GAMES = 1001;
    private static final int RC_SAVED_GAMES = 1002;

    private static final int ONBOARDING_PAGES = 2;

    public static final String EXTRA_LAUNCH_SETTINGS = "launch_settings";
    public static final int HIGHLIGHT_DELAY = 1200;

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

    @BindView(R.id.sp_categories)
    protected Spinner mSpCategories;

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

    @BindView(R.id.vg_stats_time)
    protected ViewGroup mVgStatsTime;

    @BindView(R.id.tv_stats_time)
    protected TextView mTvStatsTime;

    @BindView(R.id.vg_stats_reveals)
    protected ViewGroup mVgStatsReveals;

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

    private PuzzleList mPuzzles;
    private PuzzleAdapter mPuzzleAdapter;

    private Rate mRate;

    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = false;

    private int mLastConnectionError;

    private boolean mFreshInstall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
            getWindow().setBackgroundDrawableResource(R.drawable.bg_activity_dark);
        }
        setContentView(R.layout.activity_cryptogram);
        if (isDarkTheme()) {
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

        final String topicId = PrefsUtils.getCurrentTopic();
        Topic topic = TopicProvider.getInstance(this).getTopicById(topicId);
        PuzzleProvider provider = PuzzleProvider.getInstance(CryptogramActivity.this);
        mPuzzles = new PuzzleList(provider.getAllForTopic(topic));
        mPuzzleAdapter = new PuzzleAdapter(this, new PuzzleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (mDrawerLayout != null) {
                    mDrawerLayout.closeDrawers();
                }
                onPuzzleChanged(puzzleProvider.get(position), false);
            }
        }, mPuzzles);
        mRvDrawer.setAdapter(mPuzzleAdapter);

        final ArrayAdapter<Topic> topicAdapter = new TopicAdapter(this);
        mSpCategories.setAdapter(topicAdapter);
        mSpCategories.setSelection(topicAdapter.getPosition(topic));
        mSpCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            private boolean mFirstTime = true;

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                if (mFirstTime) {
                    mFirstTime = false;
                    return;
                }
                final Topic topic = topicAdapter.getItem(position);
                PuzzleProvider provider = PuzzleProvider.getInstance(CryptogramActivity.this);
                mPuzzles = new PuzzleList(provider.getAllForTopic(topic));
                mPuzzleAdapter.setPuzzleList(mPuzzles);
                PrefsUtils.setCurrentTopic(topic);
                // Display the current puzzle
                updateCryptogram(provider.getCurrent(mPuzzles));
                // Show the topic info
                final String topicName =
                        topic == null ? getString(R.string.all_topics) : topic.getName();
                final String topicDescription =
                        topic == null
                                ? getString(R.string.all_topics_description)
                                : topic.getDescription();
                new MaterialDialog.Builder(CryptogramActivity.this)
                        .title(topicName)
                        .content(topicDescription)
                        .positiveText(R.string.play)
                        .show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

        });

        mVgCryptogram.setCryptogramView(mCryptogramView);
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
                        showHighlight(type, createTapTargetFromPoint(
                                point,
                                getString(R.string.highlight_hyphenation_title),
                                getString(R.string.highlight_hyphenation_description)),
                                1200
                        );
                        break;
                    case PrefsUtils.TYPE_HIGHLIGHT_TOUCH_INPUT:
                        if (mFreshInstall) {
                            PrefsUtils.setHighlighted(type, true);
                        } else {
                            showHighlight(PrefsUtils.TYPE_HIGHLIGHT_TOUCH_INPUT, createTapTargetFromPoint(
                                    point,
                                    getString(R.string.highlight_touch_input_title),
                                    getString(R.string.highlight_touch_input_description)),
                                    1200
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

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getBooleanExtra(EXTRA_LAUNCH_SETTINGS, false)) {
                startActivity(SettingsActivity.Companion.create(this));
            }
        }

        if (UpdateManager.consumeEnabledShowUsedLetters()) {
            showHighlight(-1, TapTarget.forToolbarOverflow(
                    mToolbar,
                    getString(R.string.highlight_used_letters_title),
                    getString(R.string.highlight_used_letters_description)),
                    1200
            );
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventProvider.getBus().register(this);

        mGoogleApiClient.connect();

        final PuzzleProvider puzzleProvider = PuzzleProvider.getInstance(this);
        Puzzle puzzle = puzzleProvider.getCurrent(mPuzzles);
        if (puzzle != null) {
            puzzle.onResume();
        }
        showHintView(puzzle);

        if (hasOnBoardingPages()) {
            showOnboarding(0);
        } else {
            onGameplayReady();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        final PuzzleProvider puzzleProvider = PuzzleProvider.getInstance(this);
        Puzzle puzzle = puzzleProvider.getCurrent(mPuzzles);
        onPuzzleChanged(puzzle, true);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        final PuzzleProvider puzzleProvider = PuzzleProvider.getInstance(this);
        Puzzle puzzle = puzzleProvider.getCurrent(mPuzzles);
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
        if (mVwKeyboard != null && mVwKeyboard.isShown()) {
            mCryptogramView.hideSoftInput();
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
                        {
                            // Analytics
                            CryptogramApp.getInstance().getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.LOGIN, null);
                            Answers.getInstance().logLogin(new LoginEvent().putSuccess(true));
                        }
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
                                        onPuzzleChanged(PuzzleProvider.getInstance(CryptogramActivity.this)
                                                                       .getCurrent(mPuzzles), false);
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

    private TapTarget createTapTargetFromPoint(PointF point, final String title,
                                               final String description) {
        Rect viewRect = new Rect();
        mCryptogramView.getGlobalVisibleRect(viewRect);
        final int targetX = (int) (point.x + viewRect.left);
        final int targetY = (int) (point.y + viewRect.top);
        final int targetRadius = 48;
        return TapTarget.forBounds(new Rect(targetX - targetRadius, targetY - targetRadius, targetX + targetRadius, targetY + targetRadius),
                title, description);
    }

    private void showHighlight(final int type, final TapTarget tapTarget, int delayMillis) {
        new Handler().postDelayed(() -> {
            final long showTime = System.currentTimeMillis();
            TapTargetView.showFor(
                    CryptogramActivity.this,
                    tapTarget
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
                            if (type >= 0 && System.currentTimeMillis() - showTime >= 1500) {
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
                video = VideoUtils.INSTANCE.getVIDEO_INSTRUCTION();
                break;
            case 1:
                titleStringResId = R.string.intro2_title;
                textStringResId = R.string.intro2_text;
                actionStringResId = R.string.intro_done;
                video = VideoUtils.INSTANCE.getVIDEO_HELP();
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
        EasyVideoPlayer player = VideoUtils.INSTANCE.setup(this, vgRatio, video);

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

    @OnClick(R.id.vg_google_play_games)
    protected void onClickGooglePlayGames() {
        if (mGoogleApiClient.isConnected()) {
            // Connected; show gameplay options
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_google_play_games, null);
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .show();

            Button btLeaderboards = dialogView.findViewById(R.id.bt_leaderboards);
            btLeaderboards.setOnClickListener(view -> {
                dialog.dismiss();
                {
                    // Analytics
                    CryptogramApp.getInstance().getFirebaseAnalytics().logEvent(CryptogramApp.CONTENT_LEADERBOARDS, null);
                    Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_LEADERBOARDS));
                }
                try {
                    startActivityForResult(
                            Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, getString(R.string.leaderboard_scoreboard)),
                            RC_UNUSED);
                } catch (SecurityException e) {
                    // Not sure why we're still seeing errors about the connection state, but here we are
                    Crashlytics.logException(e);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(CryptogramActivity.this, R.string.google_play_games_not_installed, Toast.LENGTH_LONG).show();
                }
            });

            Button btAchievements = dialogView.findViewById(R.id.bt_achievements);
            btAchievements.setOnClickListener(view -> {
                dialog.dismiss();
                {
                    // Analytics
                    CryptogramApp.getInstance().getFirebaseAnalytics().logEvent(CryptogramApp.CONTENT_ACHIEVEMENTS, null);
                    Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_ACHIEVEMENTS));
                }
                try {
                    startActivityForResult(
                            Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                            RC_UNUSED);
                } catch (SecurityException e) {
                    // Not sure why we're still seeing errors about the connection state, but here we are
                    Crashlytics.logException(e);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(CryptogramActivity.this, R.string.google_play_games_not_installed, Toast.LENGTH_LONG).show();
                }
            });

            Button btRestoreSavedGames = dialogView.findViewById(R.id.bt_restore_saved_games);
            btRestoreSavedGames.setOnClickListener(view -> {
                dialog.dismiss();
                int maxNumberOfSavedGamesToShow = 5;
                Intent savedGamesIntent = Games.Snapshots.getSelectSnapshotIntent(mGoogleApiClient,
                        "See My Saves", true, true, maxNumberOfSavedGamesToShow);
                startActivityForResult(savedGamesIntent, RC_SAVED_GAMES);
            });

            Button btSignOut = dialogView.findViewById(R.id.bt_sign_out);
            btSignOut.setOnClickListener(view -> {
                dialog.dismiss();
                mSignInClicked = false;
                Games.signOut(mGoogleApiClient);
                if (mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
                updateGooglePlayGames();
            });
        } else {
            // start the sign-in flow
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
    }

    private void updateCryptogram(Puzzle puzzle) {
        if (puzzle != null) {
            mPuzzles.setCurrentId(puzzle.getId());
            final int currentIndex = mPuzzles.getCurrentIndex();
            if (currentIndex >= 0 && currentIndex < mRvDrawer.getAdapter().getItemCount()) {
                mRvDrawer.scrollToPosition(currentIndex);
            } else if (mRvDrawer.getAdapter().getItemCount() > 0) {
                mRvDrawer.scrollToPosition(0);
            }
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
                setToolbarSubtitle(puzzle.getTitle(this));
            } else {
                setToolbarSubtitle(getString(
                        R.string.puzzle_number,
                        puzzle.getNumber()));
            }
            // Invoke various events
            showPuzzleState(puzzle);
            puzzle.onResume();
        } else {
            mTvError.setVisibility(View.VISIBLE);
            mVgCryptogram.setVisibility(View.GONE);
            setToolbarSubtitle(null);
        }
    }

    private void onGameplayReady() {
        mCryptogramView.requestFocus();
        if (UpdateManager.consumeScoreExcludesExcessInputs()) {
            new MaterialDialog.Builder(this)
                    .title(R.string.scoring_changed_title)
                    .content(R.string.scoring_changed_message)
                    .cancelable(false)
                    .positiveText(R.string.scoring_changed_ok)
                    .show();
        }
    }

    public void showPuzzleState(Puzzle puzzle) {
        // Update the HintView as the puzzle updates
        mPuzzleAdapter.notifyDataSetChanged();
        if (puzzle.isCompleted()) {
            mVgStats.setVisibility(View.VISIBLE);
            long durationMs = puzzle.getDurationMs();
            if (durationMs <= 0) {
                mVgStatsTime.setVisibility(View.GONE);
            } else {
                mVgStatsTime.setVisibility(View.VISIBLE);
                mTvStatsTime.setText(StringUtils.getDurationString(durationMs));
            }
            int reveals = -1;
            Float score = null;
            if (PrefsUtils.getShowScore()) {
                reveals = puzzle.getReveals();
                score = puzzle.getScore();
            }
            if (reveals < 0) {
                mVgStatsReveals.setVisibility(View.GONE);
            } else {
                mVgStatsReveals.setVisibility(View.VISIBLE);
                mTvStatsReveals.setText(String.valueOf(reveals));
            }
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
            mVgStats.setVisibility(View.GONE);
        }
        showHintView(puzzle);
    }

    protected void showHintView(@Nullable Puzzle puzzle) {
        mHintView.setVisibility(puzzle != null && !puzzle.isCompleted()
                && PrefsUtils.getShowUsedChars() && PrefsUtils.getUseSystemKeyboard()
                ? View.VISIBLE : View.GONE);
    }

    public void onPuzzleChanged(Puzzle puzzle, boolean delayEvent) {
        updateCryptogram(puzzle);
        if (delayEvent) {
            EventProvider.postEventDelayed(new PuzzleEvent.PuzzleProgressEvent(puzzle), 200);
        } else {
            EventProvider.postEvent(new PuzzleEvent.PuzzleProgressEvent(puzzle));
        }
    }

    @Subscribe
    public void onPuzzleProgress(PuzzleEvent.PuzzleProgressEvent event) {
        showPuzzleState(event.getPuzzle());
    }

    @Subscribe
    public void onPuzzleStarted(PuzzleEvent.PuzzleStartedEvent event) {
        if (mGoogleApiClient.isConnected()) {
            // Submit any achievements
            AchievementProvider.getInstance().onCryptogramStart(mGoogleApiClient);
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

        if (mGoogleApiClient.isConnected()) {
            // Submit score
            LeaderboardProvider.getInstance().submit(mGoogleApiClient);

            // Submit any achievements
            AchievementProvider.getInstance().onCryptogramCompleted(mGoogleApiClient);
        }

        // Attempt to save the game to Google Play Saved Games
        PuzzleProvider.getInstance(this).save(mGoogleApiClient, null);
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
                                showPuzzleState(puzzle);
                                onPuzzleChanged(puzzle, false);
                            })
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
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
                        .input(null, prefilledText, (dialog, input) -> {
                            MDButton button = dialog.getActionButton(DialogAction.POSITIVE);
                            try {
                                button.setEnabled(Integer.parseInt(input.toString()) > 0);
                            } catch (NumberFormatException ignored) {
                                button.setEnabled(false);
                            }
                        })
                        .alwaysCallInputCallback()
                        .showListener(dialogInterface -> {
                            MaterialDialog dialog = (MaterialDialog) dialogInterface;
                            //noinspection ConstantConditions
                            dialog.getInputEditText().selectAll();
                        })
                        .onPositive((dialog, which) -> {
                            //noinspection ConstantConditions
                            Editable input = dialog.getInputEditText().getText();
                            try {
                                int puzzleNumber = Integer.parseInt(input.toString());
                                PuzzleProvider provider = PuzzleProvider
                                        .getInstance(CryptogramActivity.this);
                                Puzzle newPuzzle = provider.getByNumber(puzzleNumber);
                                if (newPuzzle == null) {
                                    showSnackbar(getString(R.string.puzzle_nonexistant, puzzleNumber));
                                } else {
                                    onPuzzleChanged(newPuzzle, false);
                                }
                            } catch (NumberFormatException ignored) {
                            }
                        }).show();
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
                {
                    // Analytics
                    String puzzleId = puzzle == null
                            ? null
                            : String.valueOf(puzzle.getId());
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.LEVEL, puzzleId);
                    bundle.putString(FirebaseAnalytics.Param.CONTENT, text);
                    CryptogramApp.getInstance().getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.SHARE, bundle);
                    Answers.getInstance().logShare(
                            new ShareEvent()
                                    .putContentId(puzzleId)
                                    .putContentType("puzzle")
                                    .putContentName(text)
                    );
                }
            }
            return true;
            case R.id.action_stats: {
                if (puzzle != null) {
                    // Make sure to save the puzzle first
                    puzzle.save();
                }
                // Now show the stats
                StatisticsUtils.showDialog(this);
            }
            return true;
            case R.id.action_settings: {
                startActivity(SettingsActivity.Companion.create(this));
            }
            return true;
            case R.id.action_how_to_play: {
                startActivity(HowToPlayActivity.Companion.create(this));
            }
            return true;
            case R.id.action_about: {
                startActivity(AboutActivity.Companion.create(this));
            }
            return true;
            case R.id.action_donate: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("bitcoin:" + BuildConfig.BITCOIN_ADDRESS));

                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.donate_title)
                        .setMessage(R.string.donate_message)
                        .setPositiveButton(R.string.donate_copy_address, (dialogInterface, i) -> {
                            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            if (clipboardManager != null) {
                                clipboardManager.setPrimaryClip(ClipData.newPlainText("text", BuildConfig.BITCOIN_ADDRESS));
                                Toast.makeText(this, R.string.donate_copy_success, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, R.string.donate_copy_failure, Toast.LENGTH_SHORT).show();
                                Crashlytics.logException(new IllegalStateException("Failed copying bitcoin address"));
                            }
                        })
                        .setNegativeButton(R.string.donate_launch_wallet, ((dialog1, which) -> {
                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException ignore) {
                                String installPackageName = "de.schildbach.wallet";
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + installPackageName)));
                                } catch (ActivityNotFoundException ignore2) {
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + installPackageName)));
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(this, R.string.donate_launch_failure, Toast.LENGTH_SHORT).show();
                                        Crashlytics.logException(new IllegalStateException("Failed launching Google Play", e));
                                    }
                                }
                            }
                        }))
                        .show();

                PackageManager packageManager = getPackageManager();
                if (intent.resolveActivity(packageManager) == null) {
                    // No intent available to handle action
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText(R.string.install_bitcoin_wallet);
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void nextPuzzle() {
        Puzzle puzzle = mPuzzles.getNext();
        onPuzzleChanged(puzzle, false);
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
        if (isFinishing()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.google_play_games_connection_failure, mLastConnectionError, errorCode))
                .setPositiveButton(android.R.string.ok, (dialog, i) -> dialog.dismiss())
                .show();
    }

}
