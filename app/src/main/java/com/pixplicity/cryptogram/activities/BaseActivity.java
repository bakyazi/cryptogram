package com.pixplicity.cryptogram.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.utils.PrefsUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    @BindView(R.id.content)
    protected View mVgContent;

    @BindView(R.id.coordinator)
    protected View mVgCoordinator;

    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    @Nullable
    @BindView(R.id.drawer_layout)
    protected DrawerLayout mDrawerLayout;

    protected ActionBarDrawerToggle mDrawerToggle;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        // Replace any splash screen image
        getWindow().setBackgroundDrawableResource(R.drawable.bg_activity);
        Boolean mDarkTheme = PrefsUtils.getDarkTheme();
        if(mDarkTheme)
        {
            setTheme(R.style.darkAppTheme);
            // Replace any splash screen image
            getWindow().setBackgroundDrawableResource(R.drawable.bg_dark_activity);
        }
        else
            getWindow().setBackgroundDrawableResource(R.drawable.bg_activity);
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
            if(mDarkTheme)
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorDarkPrimaryDark));
            else
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
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

    protected void onDrawerOpened(View drawerView) {
    }

    protected void onDrawerClosed(View drawerView) {
    }

    protected void onDrawerMoving() {
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
}
