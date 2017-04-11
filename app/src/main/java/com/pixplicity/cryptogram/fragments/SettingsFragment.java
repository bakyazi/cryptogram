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

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.activities.CryptogramActivity;
import com.pixplicity.cryptogram.utils.PrefsUtils;

import butterknife.BindView;
import butterknife.OnClick;


public class SettingsFragment extends BaseFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @BindView(R.id.cb_randomize)
    protected CheckBox mCbRandomize;

    @BindView(R.id.cb_hints)
    protected CheckBox mCbHints;

    @BindView(R.id.cb_dark_theme)
    protected CheckBox mCbDarkTheme;

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

    private void update() {
        updateCheckBox(mCbRandomize, PrefsUtils.getRandomize(), new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                PrefsUtils.setRandomize(checked);
            }
        });
        updateCheckBox(mCbHints, PrefsUtils.getShowHints(), new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                PrefsUtils.setShowHints(checked);
            }
        });
        updateCheckBox(mCbDarkTheme, PrefsUtils.getDarkTheme(), new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                PrefsUtils.setDarkTheme(checked);

                // Relaunch as though launched from home screen
                Context context = getActivity().getBaseContext();
                Intent i = context.getPackageManager()
                                  .getLaunchIntentForPackage(context.getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra(CryptogramActivity.EXTRA_LAUNCH_SETTINGS, true);
                startActivity(i);
                getActivity().finish();
            }
        });

        mBtResetDialogs.setEnabled(PrefsUtils.getNeverAskRevealLetter() || PrefsUtils.getNeverAskRevealMistakes());
    }

    private void updateCheckBox(CheckBox checkBox, boolean checked, CompoundButton.OnCheckedChangeListener listener) {
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(checked);
        checkBox.setOnCheckedChangeListener(listener);
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
