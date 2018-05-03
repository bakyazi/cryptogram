package com.pixplicity.cryptogram.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;

public class HowToPlayActivity extends BaseActivity {

    public static Intent create(Context context) {
        return new Intent(context, HowToPlayActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);

        mToolbar.setTitle(R.string.how_to_play);

        setHomeButtonEnabled(true);

        CryptogramApp.getInstance().getFirebaseAnalytics().setCurrentScreen(this, CryptogramApp.CONTENT_HOW_TO_PLAY, null);
        Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_HOW_TO_PLAY));
    }

}
