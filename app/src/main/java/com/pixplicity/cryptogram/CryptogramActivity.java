package com.pixplicity.cryptogram;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.cryptogram.utils.CryptogramProvider;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CryptogramActivity extends AppCompatActivity {

    @BindView(R.id.vg_author)
    protected TextView mTvAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cryptogram);
        ButterKnife.bind(this);

        Cryptogram cryptogram = CryptogramProvider.getInstance(this).getCurrent();
        if (cryptogram != null) {
            // TODO
            cryptogram.getQuote();
            mTvAuthor.setText(cryptogram.getAuthor());
        } else {
            // TODO
        }
    }

}
