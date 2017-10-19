package com.pixplicity.cryptogram.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.SignUpEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.events.SubmissionEvent;
import com.pixplicity.cryptogram.fragments.ContributeFragment;
import com.pixplicity.cryptogram.fragments.ContributeReviewFragment;
import com.pixplicity.cryptogram.fragments.ContributeSuggestFragment;
import com.pixplicity.cryptogram.fragments.SignInFragment;
import com.pixplicity.cryptogram.utils.EventProvider;
import com.squareup.otto.Subscribe;

import butterknife.BindView;

public class ContributeActivity extends BaseActivity {

    private static final String EXTRA_MODE = "mode";

    public static final int MODE_SELECT = 0;
    public static final int MODE_SUGGEST = 1;
    public static final int MODE_REVIEW = 2;

    @BindView(R.id.vg_root)
    protected ViewGroup mVgRoot;

    private FirebaseAuth mAuth;

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

        mAuth = FirebaseAuth.getInstance();

        Answers.getInstance().logContentView(new ContentViewEvent().putContentName(CryptogramApp.CONTENT_CONTRIBUTE));
    }

    @Nullable
    @Override
    protected Class<? extends Activity> getHierarchicalParent() {
        if (getIntent().getIntExtra(EXTRA_MODE, MODE_SELECT) == MODE_SELECT) {
            return LandingActivity.class;
        } else {
            return ContributeActivity.class;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventProvider.getBus().register(this);
        onSignInEvent(null);
    }

    @Override
    protected void onStop() {
        EventProvider.getBus().unregister(this);

        super.onStop();
    }

    @Subscribe
    public void onSignInEvent(SignUpEvent event) {
        // Check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Subscribe
    public void onSubmissionEvent(SubmissionEvent event) {
        finish();
    }

    private void updateUI(FirebaseUser user) {
        Fragment fragment = null;
        if (user != null) {
            switch (getIntent().getIntExtra(EXTRA_MODE, MODE_SELECT)) {
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
        } else {
            fragment = SignInFragment.create();
        }
        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.vg_root, fragment)
                                   .commit();
    }

}
