package com.pixplicity.cryptogram.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.utils.HtmlCompat;

import butterknife.BindView;


public class HowToPlayFragment extends BaseFragment {

    private static final String TAG = HowToPlayFragment.class.getSimpleName();

    @BindView(R.id.tv_how_to_play)
    protected TextView mTvHowToPlay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_how_to_play, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTvHowToPlay.setText(
                HtmlCompat.fromHtml(getString(R.string.how_to_play_1)));
    }

}
