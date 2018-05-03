package com.pixplicity.cryptogram.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;

public class AboutActivity extends BaseActivity {

    public static Intent create(Context context) {
        return new Intent(context, AboutActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mToolbar.setTitle(R.string.about);

        setHomeButtonEnabled(true);

        CryptogramApp.getInstance().getFirebaseAnalytics().setCurrentScreen(this, CryptogramApp.CONTENT_ABOUT, null);
        Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_ABOUT));
    }

}
