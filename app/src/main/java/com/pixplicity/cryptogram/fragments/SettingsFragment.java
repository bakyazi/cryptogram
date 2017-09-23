package com.pixplicity.cryptogram.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.activities.CryptogramActivity;
import com.pixplicity.cryptogram.events.PuzzleEvent;
import com.pixplicity.cryptogram.utils.EventProvider;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.pixplicity.cryptogram.utils.StyleUtils;

import butterknife.BindView;
import butterknife.OnClick;


public class SettingsFragment extends BaseFragment {

    @BindView(R.id.rb_theme_light)
    protected RadioButton mRbThemeLight;

    @BindView(R.id.rb_theme_dark)
    protected RadioButton mRbThemeDark;

    @BindView(R.id.rb_text_size_small)
    protected RadioButton mRbTextSizeSmall;

    @BindView(R.id.rb_text_size_normal)
    protected RadioButton mRbTextSizeNormal;

    @BindView(R.id.rb_text_size_large)
    protected RadioButton mRbTextSizeLarge;

    @BindView(R.id.cb_randomize)
    protected CheckBox mCbRandomize;

    @BindView(R.id.cb_show_hints)
    protected CheckBox mCbShowHints;

    @BindView(R.id.cb_show_topic)
    protected CheckBox mCbShowTopic;

    @BindView(R.id.cb_auto_advance)
    protected CheckBox mCbAutoAdvance;

    @BindView(R.id.bt_reset_dialogs)
    protected Button mBtResetDialogs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_settings, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        update();
    }

    @OnClick(R.id.iv_theme_light)
    protected void onClickIvThemeLight() {
        mRbThemeLight.setChecked(true);
    }

    @OnClick(R.id.iv_theme_dark)
    protected void onClickIvThemeDark() {
        mRbThemeDark.setChecked(true);
    }

    private void update() {
        updateCompoundButton(mRbThemeLight, !PrefsUtils.getDarkTheme(), new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    setTheme(false);
                }
            }
        });
        updateCompoundButton(mRbThemeDark, PrefsUtils.getDarkTheme(), new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    setTheme(true);
                }
            }
        });
        updateCompoundButton(mRbTextSizeSmall, PrefsUtils.getTextSize() == -1, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    setTextSize(-1);
                }
            }
        });
        updateCompoundButton(mRbTextSizeNormal, PrefsUtils.getTextSize() == 0, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    setTextSize(0);
                }
            }
        });
        updateCompoundButton(mRbTextSizeLarge, PrefsUtils.getTextSize() == 1, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    setTextSize(1);
                }
            }
        });
        updateCompoundButton(mCbRandomize, PrefsUtils.getRandomize(), new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                PrefsUtils.setRandomize(checked);
            }
        });
        updateCompoundButton(mCbShowHints, PrefsUtils.getShowHints(), new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                PrefsUtils.setShowHints(checked);
            }
        });
        updateCompoundButton(mCbShowTopic, PrefsUtils.getShowTopic(), new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                PrefsUtils.setShowTopic(checked);
            }
        });
        updateCompoundButton(mCbAutoAdvance, PrefsUtils.getAutoAdvance(), new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                PrefsUtils.setAutoAdvance(checked);
            }
        });

        mBtResetDialogs.setEnabled(PrefsUtils.getNeverAskRevealLetter() || PrefsUtils.getNeverAskRevealMistakes());
    }

    private void setTextSize(int textSize) {
        PrefsUtils.setTextSize(textSize);
        StyleUtils.reset();
        EventProvider.postEvent(new PuzzleEvent.PuzzleStyleChanged());
    }

    private void setTheme(boolean theme) {
        PrefsUtils.setDarkTheme(theme);
        relaunch();
    }

    private void relaunch() {
        // Relaunch as though launched from home screen
        Context context = getActivity().getBaseContext();
        Intent i = context.getPackageManager()
                          .getLaunchIntentForPackage(context.getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(CryptogramActivity.EXTRA_LAUNCH_SETTINGS, true);
        startActivity(i);
        getActivity().finish();
    }

    private void updateCompoundButton(CompoundButton compoundButton, boolean checked,
                                      CompoundButton.OnCheckedChangeListener listener) {
        compoundButton.setOnCheckedChangeListener(null);
        compoundButton.setChecked(checked);
        compoundButton.setOnCheckedChangeListener(listener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.bt_reset_dialogs)
    protected void onClickResetDialogs() {
        PrefsUtils.setNeverAskRevealLetter(false);
        PrefsUtils.setNeverAskRevealMistakes(false);
        update();
    }

}
