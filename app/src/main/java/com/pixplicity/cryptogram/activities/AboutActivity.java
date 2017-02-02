package com.pixplicity.cryptogram.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.utils.PrefsUtils;

public class AboutActivity extends BaseActivity {

    public static Intent create(Context context) {
        return new Intent(context, AboutActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Boolean mDarkTheme = PrefsUtils.getDarkTheme();
        if(mDarkTheme)
        {
            setTheme(R.style.darkAppTheme);
            // Replace any splash screen image
            getWindow().setBackgroundDrawableResource(R.drawable.bg_dark_activity);
        }
        else
            getWindow().setBackgroundDrawableResource(R.drawable.bg_activity);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mToolbar.setTitle(R.string.about);

        setHomeButtonEnabled(true);
    }

}
