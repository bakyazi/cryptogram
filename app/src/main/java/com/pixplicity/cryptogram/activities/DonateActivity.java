package com.pixplicity.cryptogram.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;

public class DonateActivity extends BaseActivity {

    public static Intent create(Context context) {
        return new Intent(context, DonateActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        CryptogramApp.getInstance().getFirebaseAnalytics().setCurrentScreen(this, CryptogramApp.CONTENT_DONATE, null);
        Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_DONATE));
    }

}
