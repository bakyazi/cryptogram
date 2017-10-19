package com.pixplicity.cryptogram.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.utils.StatisticsUtils;

import butterknife.BindView;
import butterknife.OnClick;

public class LandingActivity extends BaseActivity {

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

    @BindView(R.id.vg_stats)
    protected ViewGroup mVgStats;

    public static Intent create(Context context) {
        return new Intent(context, LandingActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_LANDING));

        StatisticsUtils.populateTable(mVgStats);

        Intent intent = getIntent();
        if (intent != null) {
            // Relaunch settings to resume where user left off
            if (intent.getBooleanExtra(EXTRA_LAUNCH_SETTINGS, false)) {
                startActivity(SettingsActivity.create(this));
            }
        }
    }

    @Nullable
    @Override
    protected Class<? extends Activity> getHierarchicalParent() {
        return null;
    }

    @OnClick(R.id.bt_settings)
    protected void onClickSettings() {
        startActivity(SettingsActivity.create(this));
    }

    @OnClick(R.id.bt_how_to_play)
    protected void onClickHowToPlay() {
        startActivity(HowToPlayActivity.create(this));
    }

    @OnClick(R.id.vg_google_play_games)
    protected void onClickGooglePlayGames() {
        if (getGoogleApiClient().isConnected()) {
            // Connected; show gameplay options
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_google_play_games, null);
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .show();

            Button btLeaderboards = dialogView.findViewById(R.id.bt_leaderboards);
            btLeaderboards.setOnClickListener(view -> {
                dialog.dismiss();
                Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_LEADERBOARDS));
                try {
                    startActivityForResult(
                            Games.Leaderboards.getLeaderboardIntent(getGoogleApiClient(), getString(R.string.leaderboard_scoreboard)),
                            RC_UNUSED);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(LandingActivity.this, R.string.google_play_games_not_installed, Toast.LENGTH_LONG).show();
                }
            });

            Button btAchievements = dialogView.findViewById(R.id.bt_achievements);
            btAchievements.setOnClickListener(view -> {
                dialog.dismiss();
                Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_ACHIEVEMENTS));
                try {
                    startActivityForResult(
                            Games.Achievements.getAchievementsIntent(getGoogleApiClient()),
                            RC_UNUSED);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(LandingActivity.this, R.string.google_play_games_not_installed, Toast.LENGTH_LONG).show();
                }
            });

            Button btRestoreSavedGames = dialogView.findViewById(R.id.bt_restore_saved_games);
            btRestoreSavedGames.setOnClickListener(view -> {
                dialog.dismiss();
                int maxNumberOfSavedGamesToShow = 5;
                Intent savedGamesIntent = Games.Snapshots.getSelectSnapshotIntent(getGoogleApiClient(),
                        "See My Saves", true, true, maxNumberOfSavedGamesToShow);
                startActivityForResult(savedGamesIntent, RC_SAVED_GAMES);
            });

            Button btSignOut = dialogView.findViewById(R.id.bt_sign_out);
            btSignOut.setOnClickListener(view -> {
                dialog.dismiss();
                onGoogleSignOut();
            });
        } else {
            onGoogleSignIn();
        }
    }

    @Override
    protected void onGoogleApiClientConnectionChange() {
        if (getGoogleApiClient().isConnected()) {
            // Set the greeting appropriately on main menu
            Player p = Games.Players.getCurrentPlayer(getGoogleApiClient());
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

}
