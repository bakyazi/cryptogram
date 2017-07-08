package com.pixplicity.cryptogram.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.fragments.ContributeFragment;
import com.pixplicity.cryptogram.fragments.ContributeReviewFragment;
import com.pixplicity.cryptogram.fragments.ContributeSuggestFragment;

import butterknife.BindView;

public class ContributeActivity extends BaseActivity {

    private static final String EXTRA_MODE = "mode";

    public static final int MODE_SELECT = 0;
    public static final int MODE_SUGGEST = 1;
    public static final int MODE_REVIEW = 2;

    @BindView(R.id.vg_root)
    protected ViewGroup mVgRoot;

    public static Intent create(Context context, int mode) {
        Intent intent = new Intent(context, ContributeActivity.class);
        intent.putExtra(EXTRA_MODE, mode);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contribute);

        mToolbar.setTitle(R.string.contribute);

        setHomeButtonEnabled(true);

        Fragment fragment = null;
        switch (getIntent().getIntExtra(EXTRA_MODE, 0)) {
            case MODE_SELECT:
                fragment = ContributeFragment.create();
                break;
            case MODE_SUGGEST:
                fragment = ContributeSuggestFragment.create();
                break;
            case MODE_REVIEW:
                fragment = ContributeReviewFragment.create();
                break;
        }
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.vg_root, fragment)
                                   .commit();

        Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_CONTRIBUTE));
    }

}
