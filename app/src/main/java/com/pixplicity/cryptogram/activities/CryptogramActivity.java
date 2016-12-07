package com.pixplicity.cryptogram.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.adapters.CryptogramAdapter;
import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.cryptogram.utils.CryptogramProvider;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.pixplicity.cryptogram.views.CryptogramView;
import com.pixplicity.cryptogram.views.HintView;

import net.soulwolf.widget.ratiolayout.RatioDatumMode;
import net.soulwolf.widget.ratiolayout.widget.RatioFrameLayout;

import java.util.Locale;

import butterknife.BindView;

public class CryptogramActivity extends BaseActivity {

    private static final String TAG = CryptogramActivity.class.getSimpleName();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cryptogram);

        final CryptogramProvider cryptogramProvider = CryptogramProvider.getInstance(this);

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
                mCryptogramView.requestFocus();
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
    protected void onResume() {
        super.onResume();

        final CryptogramProvider cryptogramProvider = CryptogramProvider.getInstance(this);
        Cryptogram cryptogram = cryptogramProvider.getCurrent();
        if (cryptogram != null) {
            cryptogram.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        final CryptogramProvider cryptogramProvider = CryptogramProvider.getInstance(this);
        Cryptogram cryptogram = cryptogramProvider.getCurrent();
        if (cryptogram != null) {
            cryptogram.onPause();
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    private void updateCryptogram(Cryptogram cryptogram) {
        if (cryptogram != null) {
            CryptogramProvider provider = CryptogramProvider.getInstance(this);
            provider.setCurrent(cryptogram.getId());
            mRvDrawer.smoothScrollToPosition(
                    provider.getCurrentIndex());
            mTvError.setVisibility(View.GONE);
            mVgCryptogram.setVisibility(View.VISIBLE);
            // Apply the puzzle to the CryptogramView
            mCryptogramView.setCryptogram(cryptogram);
            // Show other puzzle details
            mTvAuthor.setText(getString(R.string.quote, cryptogram.getAuthor()));
            mTvTopic.setText(getString(R.string.topic, cryptogram.getTopic()));
            mToolbar.setSubtitle(getString(
                    R.string.puzzle_number,
                    cryptogram.getId() + 1,
                    provider.getCount()));
            // Invoke various events
            onCryptogramUpdated(cryptogram);
            cryptogram.onResume();
        } else {
            mTvError.setVisibility(View.VISIBLE);
            mVgCryptogram.setVisibility(View.GONE);
            mToolbar.setSubtitle(null);
        }
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
                int durationS = (int) (durationMs / 1000);
                mTvStatsTime.setText(String.format(
                        Locale.ENGLISH,
                        "%d:%02d:%02d",
                        durationS / 3600,
                        durationS % 3600 / 60,
                        durationS % 60));
            }
            int excessCount = cryptogram.getExcessCount();
            if (excessCount < 0) {
                mVgStatsExcess.setVisibility(View.GONE);
            } else {
                mVgStatsExcess.setVisibility(View.VISIBLE);
                mTvStatsExcess.setText(String.valueOf(excessCount));
            }
            mTvStatsReveals.setText(String.valueOf(cryptogram.getReveals()));
            float score = cryptogram.getScore();
            if (score < 0) {
                mVgStatsScore.setVisibility(View.GONE);
            } else {
                mVgStatsScore.setVisibility(View.VISIBLE);
                mTvStatsScore.setText(String.format(
                        Locale.ENGLISH,
                        "%.1f%%",
                        score * 100));
            }
        } else {
            mHintView.setVisibility(PrefsUtils.getShowHints() ? View.VISIBLE : View.GONE);
            mVgStats.setVisibility(View.GONE);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }

        final Cryptogram cryptogram = mCryptogramView.getCryptogram();
        switch (item.getItemId()) {
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
                String currentId = String.valueOf(cryptogram.getId() + 1);
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
                                    int id = Integer.parseInt(input.toString());
                                    CryptogramProvider provider = CryptogramProvider
                                            .getInstance(CryptogramActivity.this);
                                    Cryptogram cryptogram = provider.get(id - 1);
                                    if (cryptogram == null) {
                                        Snackbar.make(mVgContent, getString(R.string.puzzle_nonexistant, id),
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
            case R.id.action_about: {
                startActivity(AboutActivity.create(this));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void nextPuzzle() {
        Cryptogram cryptogram = CryptogramProvider.getInstance(CryptogramActivity.this).getNext();
        updateCryptogram(cryptogram);
    }

}
