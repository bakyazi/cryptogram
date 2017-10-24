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

import com.afollestad.materialdialogs.MaterialDialog;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.activities.BaseActivity;
import com.pixplicity.cryptogram.activities.CryptogramActivity;
import com.pixplicity.cryptogram.events.PuzzleEvent;
import com.pixplicity.cryptogram.utils.EventProvider;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.pixplicity.cryptogram.utils.PuzzleProvider;
import com.pixplicity.cryptogram.utils.StyleUtils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;


public class SettingsFragment extends BaseFragment {

    @BindView(R.id.vg_content)
    protected ViewGroup mVgContent;

    @BindView(R.id.vg_busy)
    protected ViewGroup mVgBusy;

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

    @BindView(R.id.rb_keyboard_builtin)
    protected RadioButton mRbKeyboardBuiltin;

    @BindView(R.id.rb_keyboard_system)
    protected RadioButton mRbKeyboardSystem;

    @BindView(R.id.cb_randomize)
    protected CheckBox mCbRandomize;

    @BindView(R.id.cb_show_hints)
    protected CheckBox mCbShowHints;

    @BindView(R.id.cb_show_topic)
    protected CheckBox mCbShowTopic;

    @BindView(R.id.cb_auto_advance)
    protected CheckBox mCbAutoAdvance;

    @BindView(R.id.cb_skip_filled_cells)
    protected CheckBox mCbSkipFilledCells;

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
        updateCompoundButton(mRbThemeLight, !PrefsUtils.getDarkTheme(), (compoundButton, checked) -> {
            if (checked) {
                setTheme(false);
            }
        });
        updateCompoundButton(mRbThemeDark, PrefsUtils.getDarkTheme(), (compoundButton, checked) -> {
            if (checked) {
                setTheme(true);
            }
        });
        updateCompoundButton(mRbTextSizeSmall, PrefsUtils.getTextSize() == -1, (compoundButton, checked) -> {
            if (checked) {
                setTextSize(-1);
            }
        });
        updateCompoundButton(mRbTextSizeNormal, PrefsUtils.getTextSize() == 0, (compoundButton, checked) -> {
            if (checked) {
                setTextSize(0);
            }
        });
        updateCompoundButton(mRbTextSizeLarge, PrefsUtils.getTextSize() == 1, (compoundButton, checked) -> {
            if (checked) {
                setTextSize(1);
            }
        });
        updateCompoundButton(mRbKeyboardBuiltin, !PrefsUtils.getUseSystemKeyboard(), (compoundButton, checked) -> {
            if (checked) {
                setUseSystemKeyboard(false);
            }
        });
        updateCompoundButton(mRbKeyboardSystem, PrefsUtils.getUseSystemKeyboard(), (compoundButton, checked) -> {
            if (checked) {
                // Show warning that it works for shit
                new MaterialDialog.Builder(getActivity())
                        .content(R.string.keyboard_system_dialog)
                        .positiveText(R.string.keyboard_system_dialog_ok)
                        .dismissListener(dialogInterface -> setUseSystemKeyboard(true))
                        .show();
            }
        });
        updateCompoundButton(mCbRandomize, PrefsUtils.getRandomize(),
                (compoundButton, checked) -> PrefsUtils.setRandomize(checked));
        updateCompoundButton(mCbShowHints, PrefsUtils.getShowHints(),
                (compoundButton, checked) -> PrefsUtils.setShowHints(checked));
        updateCompoundButton(mCbShowTopic, PrefsUtils.getShowTopic(),
                (compoundButton, checked) -> PrefsUtils.setShowTopic(checked));
        updateCompoundButton(mCbAutoAdvance, PrefsUtils.getAutoAdvance(),
                (compoundButton, checked) -> PrefsUtils.setAutoAdvance(checked));
        updateCompoundButton(mCbSkipFilledCells, PrefsUtils.getSkipFilledCells(),
                (compoundButton, checked) -> PrefsUtils.setSkipFilledCells(checked));

        mBtResetDialogs.setEnabled(PrefsUtils.getNeverAskRevealLetter() || PrefsUtils.getNeverAskRevealMistakes());
    }

    private void setTextSize(int textSize) {
        PrefsUtils.setTextSize(textSize);
        StyleUtils.reset();
        EventProvider.postEvent(new PuzzleEvent.PuzzleStyleChangedEvent());
    }

    private void setTheme(boolean theme) {
        PrefsUtils.setDarkTheme(theme);
        relaunch();
    }

    private void setUseSystemKeyboard(boolean useSystemKeyboard) {
        PrefsUtils.setUseSystemKeyboard(useSystemKeyboard);
        relaunch();
    }

    private void relaunch() {
        // Show progress as it takes a moment to relaunch
        mVgBusy.setVisibility(View.VISIBLE);
        mVgContent.setVisibility(View.GONE);
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

    @OnClick(R.id.bt_reset_all_puzzles)
    protected void onClickResetAllPuzzles() {
        String keyword = getString(R.string.reset_all_puzzles_keyword);
        new MaterialDialog.Builder(getActivity())
                .content(getString(R.string.reset_all_puzzles_confirmation, keyword))
                .positiveText(R.string.reset)
                .input(null, null, (dialog, input) -> {
                    BaseActivity activity = (BaseActivity) getActivity();
                    if (input.toString().trim().toLowerCase(Locale.ENGLISH).equals(keyword)) {
                        PuzzleProvider.getInstance(getContext()).resetAll();
                        activity.showSnackbar(getString(R.string.reset_all_puzzles_executed));
                    } else {
                        activity.showSnackbar(getString(R.string.reset_all_puzzles_cancelled));
                    }
                })
                .show();
    }

}
