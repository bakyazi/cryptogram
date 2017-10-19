package com.pixplicity.cryptogram.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;

public class SettingsActivity extends BaseActivity {

    public static Intent create(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar.setTitle(R.string.settings);

        Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_SETTINGS));
    }

    @Nullable
    @Override
    protected Class<? extends Activity> getHierarchicalParent() {
        return LandingActivity.class;
    }

}
