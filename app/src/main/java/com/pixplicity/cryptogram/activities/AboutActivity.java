package com.pixplicity.cryptogram.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

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
    }

}
