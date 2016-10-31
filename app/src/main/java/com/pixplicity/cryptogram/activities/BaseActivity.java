package com.pixplicity.cryptogram.activities;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.pixplicity.cryptogram.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    @BindView(R.id.content)
    protected View mVgContent;

    @BindView(R.id.coordinator)
    protected View mVgCoordinator;

    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        ab.setElevation(6);
        ab.setTitle(R.string.app_name);
        ab.setDisplayShowTitleEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        // Show 'up' navigation
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });
    }

}
