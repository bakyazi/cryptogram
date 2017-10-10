package com.pixplicity.cryptogram.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.Snapshots;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.events.PuzzleEvent;
import com.pixplicity.cryptogram.providers.PuzzleProvider;
import com.pixplicity.cryptogram.utils.AchievementProvider;
import com.pixplicity.cryptogram.utils.EventProvider;
import com.pixplicity.cryptogram.utils.LeaderboardProvider;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.pixplicity.cryptogram.utils.SavegameManager;
import com.pixplicity.cryptogram.utils.StyleUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = BaseActivity.class.getSimpleName();

    protected static final int RC_UNUSED = 1000;
    protected static final int RC_PLAY_GAMES = 1001;
    protected static final int RC_SAVED_GAMES = 1002;

    @Nullable
    @BindView(R.id.vg_root)
    protected View mVgRoot;

    @Nullable
    @BindView(R.id.drawer_layout)
    protected DrawerLayout mDrawerLayout;

    @BindView(R.id.coordinator)
    protected View mVgCoordinator;

    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    protected ActionBarDrawerToggle mDrawerToggle;

    protected boolean mDarkTheme;

    private GoogleApiClient mGoogleApiClient;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = false;

    private int mLastConnectionError;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the Google Api Client with access to Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
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
                                        EventProvider.postEvent(new PuzzleEvent.PuzzlesLoaded());
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

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            // Sync the toggle state after onRestoreInstanceState has occurred.
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle if it's present
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            // If it returns true, then it has handled the option item selection event
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent up = new Intent(BaseActivity.this, PuzzleActivity.class);
                if (NavUtils.shouldUpRecreateTask(BaseActivity.this, up)) {
                    TaskStackBuilder.create(BaseActivity.this)
                                    .addNextIntent(up)
                                    .startActivities();
                    finish();
                } else {
                    NavUtils.navigateUpTo(BaseActivity.this, up);
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        // Replace any splash screen image
        getWindow().setBackgroundDrawableResource(R.drawable.bg_activity);
        mDarkTheme = PrefsUtils.getDarkTheme();
        if (mDarkTheme) {
            setTheme(R.style.AppTheme_Dark);
        }
        super.setContentView(layoutResID);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mToolbar.setContentInsetStartWithNavigation(0);

        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        ab.setElevation(6);
        ab.setTitle(R.string.app_name);
        ab.setDisplayShowTitleEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mDarkTheme) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorDarkPrimaryDark));
            } else {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            }
        }

        if (mDrawerLayout != null) {
            // Apply side navigation
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    mDrawerLayout,         /* DrawerLayout object */
                    R.string.drawer_open,  /* "open drawer" description */
                    R.string.drawer_close  /* "close drawer" description */
            ) {
                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    BaseActivity.this.onDrawerOpened(drawerView);
                }

                /** Called when a drawer has settled in a completely closed state. */
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    BaseActivity.this.onDrawerClosed(drawerView);
                }
            };

            // Set the drawer toggle as the DrawerListener
            mDrawerLayout.addDrawerListener(mDrawerToggle);
            mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                }

                @Override
                public void onDrawerStateChanged(int newState) {
                    switch (newState) {
                        case DrawerLayout.STATE_IDLE:
                            break;
                        case DrawerLayout.STATE_SETTLING:
                        case DrawerLayout.STATE_DRAGGING:
                            BaseActivity.this.onDrawerMoving();
                            break;
                    }
                }
            });

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    protected void onDrawerOpened(View drawerView) {
    }

    protected void onDrawerClosed(View drawerView) {
    }

    protected void onDrawerMoving() {
    }

    protected View getViewRoot() {
        return mVgRoot == null ? mDrawerLayout : mVgRoot;
    }

    @NonNull
    @Override
    public ActionBar getSupportActionBar() {
        ActionBar actionBar = super.getSupportActionBar();
        assert actionBar != null;
        return actionBar;
    }

    protected void setHomeButtonEnabled(boolean enabled) {
        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(enabled);
        ab.setDisplayShowHomeEnabled(enabled);
        ab.setDisplayHomeAsUpEnabled(enabled);
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    protected void onGoogleSignIn() {
        // Start the sign-in flow
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    protected void onGoogleSignOut() {
        mSignInClicked = false;
        Games.signOut(mGoogleApiClient);
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        onGoogleApiClientConnectionChange();
    }

    // Google Play Services
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLastConnectionError = 0;

        onGoogleApiClientConnectionChange();

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

        onGoogleApiClientConnectionChange();
    }

    protected void onGoogleApiClientConnectionChange() {
        // By default do nothing
    }

    private void showGmsError(int errorCode) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.google_play_games_connection_failure, mLastConnectionError, errorCode))
                .setPositiveButton(android.R.string.ok, (dialog, i) -> dialog.dismiss())
                .show();
    }

    protected void showSnackbar(String text) {
        final Snackbar snackbar = Snackbar.make(getViewRoot(), text, Snackbar.LENGTH_SHORT);
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

    public boolean isDarkTheme() {
        return mDarkTheme;
    }

}
