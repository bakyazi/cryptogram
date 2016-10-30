package com.pixplicity.cryptogram;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
        updateCryptogram(cryptogram);
    }

    private void updateCryptogram(Cryptogram cryptogram) {
        if (cryptogram != null) {
            mCryptogramView.setCryptogram(cryptogram);
            mTvAuthor.setText(cryptogram.getAuthor());
        } else {
            // TODO
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cryptogram, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next: {
                // TODO ask confirmation
                Cryptogram cryptogram = CryptogramProvider.getInstance(this).getNext();
                updateCryptogram(cryptogram);
            }
            return true;
            case R.id.action_reset: {
                // TODO ask confirmation
                Cryptogram cryptogram = mCryptogramView.getCryptogram();
                if (cryptogram != null) {
                    cryptogram.reset();
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
