package com.pixplicity.cryptogram.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.utils.HtmlCompat;
import com.pixplicity.cryptogram.utils.VideoUtils;

import net.soulwolf.widget.ratiolayout.widget.RatioFrameLayout;

import butterknife.BindView;


public class HowToPlayFragment extends BaseFragment {

    private static final String TAG = HowToPlayFragment.class.getSimpleName();

    @BindView(R.id.tv_how_to_play_1)
    protected TextView mTvHowToPlay1;

    @BindView(R.id.tv_how_to_play_2)
    protected TextView mTvHowToPlay2;

    @BindView(R.id.rf_video1)
    protected RatioFrameLayout mRfVideo1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_how_to_play, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTvHowToPlay1.setText(
                HtmlCompat.fromHtml(getString(R.string.how_to_play_1)));
        mTvHowToPlay2.setText(
                HtmlCompat.fromHtml(getString(R.string.how_to_play_2)));

        VideoUtils.setup(getActivity(), mRfVideo1, VideoUtils.VIDEO_INSTRUCTION);
    }

}
