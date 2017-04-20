package com.pixplicity.cryptogram.fragments;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.utils.HtmlCompat;

import butterknife.BindView;


public class AboutFragment extends BaseFragment {

    private static final String TAG = AboutFragment.class.getSimpleName();
    public static final String FEEDBACK_EMAIL = "paul@pixplicity.com";

    @BindView(R.id.tv_version)
    protected TextView mTvVersion;

    @BindView(R.id.tv_about_this_app_1)
    protected TextView mTvAboutThisApp1;

    @BindView(R.id.tv_about_this_app_2)
    protected TextView mTvAboutThisApp2;

    @BindView(R.id.tv_disclaimer)
    protected TextView mTvDisclaimer;

    @BindView(R.id.tv_licenses)
    protected TextView mTvLicenses;

    @BindView(R.id.artwork)
    protected TextView mTvArtwork;

    @BindView(R.id.bt_website)
    protected Button mBtWebsite;

    @BindView(R.id.iv_labs)
    protected ImageView mIvLabs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_about, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // App version
        String versionString = getVersionString();
        mTvVersion.setText(versionString);

        // About this app
        String appName = getString(R.string.app_name);
        mTvAboutThisApp1.setText(
                HtmlCompat.fromHtml(getString(R.string.about_this_app_1, appName)));
        mTvAboutThisApp2.setText(
                HtmlCompat.fromHtml(getString(R.string.about_this_app_2)));

        // Legal
        mTvDisclaimer.setText(HtmlCompat.fromHtml(getString(R.string.disclaimer)));

        // Licenses
        mTvLicenses.setMovementMethod(LinkMovementMethod.getInstance());
        mTvLicenses.setText(
                HtmlCompat.fromHtml(getString(R.string.licenses)));

        // Artwork
        mTvArtwork.setMovementMethod(LinkMovementMethod.getInstance());
        mTvArtwork.setText(
                HtmlCompat.fromHtml(getString(R.string.artwork)));

        // Website
        final View.OnClickListener launchWebsite = new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getString(R.string.url)));
                startActivity(i);
            }
        };
        mBtWebsite.setOnClickListener(launchWebsite);
        mIvLabs.setOnClickListener(launchWebsite);
    }

    @Nullable
    private String getVersionString() {
        String versionString = null;
        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            versionString = getString(R.string.version, info.versionName, info.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package not found", e);
        }
        return versionString;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_feedback: {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", FEEDBACK_EMAIL, null));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{FEEDBACK_EMAIL});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
                emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedback_body, getVersionString()));
                emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(emailIntent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
