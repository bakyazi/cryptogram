package com.pixplicity.cryptogram;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.cryptogram.utils.CryptogramProvider;
import com.pixplicity.cryptogram.views.CryptogramView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CryptogramActivity extends AppCompatActivity {

    @BindView(R.id.vg_author)
    protected TextView mTvAuthor;

    @BindView(R.id.cryptogram)
    protected CryptogramView mCryptogramView;

    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cryptogram);
        ButterKnife.bind(this);

        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);

        Cryptogram cryptogram = CryptogramProvider.getInstance(this).getCurrent();
        if (cryptogram != null) {
            mCryptogramView.setCryptogram(cryptogram);
            mTvAuthor.setText(cryptogram.getAuthor());
        } else {
            // TODO
        }
    }

}
