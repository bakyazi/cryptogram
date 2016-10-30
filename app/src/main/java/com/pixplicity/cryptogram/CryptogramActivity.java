package com.pixplicity.cryptogram;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.cryptogram.utils.CryptogramProvider;
import com.pixplicity.cryptogram.views.CryptogramView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CryptogramActivity extends AppCompatActivity {

    @BindView(R.id.vg_cryptogram)
    protected ViewGroup mVgCryptogram;

    @BindView(R.id.tv_author)
    protected TextView mTvAuthor;

    @BindView(R.id.cryptogram)
    protected CryptogramView mCryptogramView;

    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    @BindView(R.id.tv_error)
    protected TextView mTvError;

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
            mTvError.setVisibility(View.GONE);
            mVgCryptogram.setVisibility(View.VISIBLE);
            mCryptogramView.setCryptogram(cryptogram);
            mTvAuthor.setText(cryptogram.getAuthor());
            mToolbar.setSubtitle(getString(R.string.puzzle_number, cryptogram.getId()));
        } else {
            mTvError.setVisibility(View.VISIBLE);
            mVgCryptogram.setVisibility(View.GONE);
            mToolbar.setSubtitle(null);
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
                new AlertDialog.Builder(this)
                        .setMessage(R.string.skip_puzzle)
                        .setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Cryptogram cryptogram = CryptogramProvider.getInstance(CryptogramActivity.this).getNext();
                                updateCryptogram(cryptogram);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
            }
            return true;
            case R.id.action_reset: {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.reset_puzzle)
                        .setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Cryptogram cryptogram = mCryptogramView.getCryptogram();
                                if (cryptogram != null) {
                                    cryptogram.reset();
                                    mCryptogramView.invalidate();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
