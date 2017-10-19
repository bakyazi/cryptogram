package com.pixplicity.cryptogram.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.pixplicity.cryptogram.utils.StyleUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    @Nullable
    @BindView(R.id.vg_root)
    protected View mVgRoot;

    @Nullable
    @BindView(R.id.drawer_layout)
    protected DrawerLayout mDrawerLayout;

    @BindView(R.id.coordinator)
    protected View mVgCoordinator;

    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    @Nullable
    @BindView(R.id.tv_toolbar_subtitle)
    protected TextView mTvToolbarSubtitle;

    protected ActionBarDrawerToggle mDrawerToggle;

    protected boolean mDarkTheme;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        // Replace any splash screen image
        getWindow().setBackgroundDrawableResource(R.drawable.bg_activity_light);
        mDarkTheme = PrefsUtils.getDarkTheme();
        if (mDarkTheme) {
            setTheme(R.style.AppTheme_Dark);
            // Replace any splash screen image
            getWindow().setBackgroundDrawableResource(R.drawable.bg_activity_dark);
        } else {
            getWindow().setBackgroundDrawableResource(R.drawable.bg_activity_light);
        }
        super.setContentView(layoutResID);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mToolbar.setContentInsetStartWithNavigation(0);

        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        ab.setElevation(6);
        ab.setTitle(R.string.app_name);
        ab.setDisplayShowTitleEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mDarkTheme) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorDarkPrimaryDark));
            } else {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            }
        }

        if (mDrawerLayout != null) {
            // Apply side navigation
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    mDrawerLayout,         /* DrawerLayout object */
                    R.string.drawer_open,  /* "open drawer" description */
                    R.string.drawer_close  /* "close drawer" description */
            ) {
                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    BaseActivity.this.onDrawerOpened(drawerView);
                }

                /** Called when a drawer has settled in a completely closed state. */
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    BaseActivity.this.onDrawerClosed(drawerView);
                }
            };

            // Set the drawer toggle as the DrawerListener
            mDrawerLayout.addDrawerListener(mDrawerToggle);
            mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                }

                @Override
                public void onDrawerStateChanged(int newState) {
                    switch (newState) {
                        case DrawerLayout.STATE_IDLE:
                            break;
                        case DrawerLayout.STATE_SETTLING:
                        case DrawerLayout.STATE_DRAGGING:
                            BaseActivity.this.onDrawerMoving();
                            break;
                    }
                }
            });

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    protected void setToolbarSubtitle(String subtitle) {
        if (mTvToolbarSubtitle != null) {
            mTvToolbarSubtitle.setText(subtitle);
        } else {
            mToolbar.setSubtitle(subtitle);
        }
    }

    protected void onDrawerOpened(View drawerView) {
    }

    protected void onDrawerClosed(View drawerView) {
    }

    protected void onDrawerMoving() {
    }

    protected View getViewRoot() {
        return mVgRoot == null ? mDrawerLayout : mVgRoot;
    }

    @NonNull
    @Override
    public ActionBar getSupportActionBar() {
        ActionBar actionBar = super.getSupportActionBar();
        assert actionBar != null;
        return actionBar;
    }

    protected void setHomeButtonEnabled(boolean enabled) {
        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(enabled);
        ab.setDisplayShowHomeEnabled(enabled);
        ab.setDisplayHomeAsUpEnabled(enabled);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            // Sync the toggle state after onRestoreInstanceState has occurred.
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle if it's present
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            // If it returns true, then it has handled the option item selection event
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home: {
                Intent up = new Intent(BaseActivity.this, CryptogramActivity.class);
                if (NavUtils.shouldUpRecreateTask(BaseActivity.this, up)) {
                    TaskStackBuilder.create(BaseActivity.this)
                                    .addNextIntent(up)
                                    .startActivities();
                    finish();
                } else {
                    NavUtils.navigateUpTo(BaseActivity.this, up);
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isDarkTheme() {
        return mDarkTheme;
    }

    public void showSnackbar(String text) {
        final Snackbar snackbar = Snackbar.make(getViewRoot(), text, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();

        // Set background
        @ColorInt int colorPrimary = StyleUtils.getColor(this, R.attr.colorPrimary);
        snackBarView.setBackgroundColor(colorPrimary);

        // Set foreground
        @ColorInt int textColor = StyleUtils.getColor(this, R.attr.textColorOnPrimary);
        TextView textView = snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(textColor);

        snackbar.show();
    }

}
