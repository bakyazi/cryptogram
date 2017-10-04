package com.pixplicity.cryptogram.fragments;

import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.utils.HtmlCompat;
import com.pixplicity.cryptogram.utils.VideoUtils;

import net.soulwolf.widget.ratiolayout.widget.RatioFrameLayout;

import butterknife.BindView;


public class HowToPlayFragment extends BaseFragment {

    private static final String TAG = HowToPlayFragment.class.getSimpleName();

    /**
     * Color matrix that flips the components (<code>-1.0f * c + 255 = 255 - c</code>)
     * and keeps the alpha intact.
     */
    private static final float[] NEGATIVE = {
            -1.0f,     0,     0,    0, 255, // red
            0, -1.0f,     0,    0, 255, // green
            0,     0, -1.0f,    0, 255, // blue
            0,     0,     0, 1.0f,   0  // alpha
    };

    @BindView(R.id.tv_how_to_play_1)
    protected TextView mTvHowToPlay1;

    @BindView(R.id.tv_how_to_play_2)
    protected TextView mTvHowToPlay2;

    @BindView(R.id.tv_how_to_play_3)
    protected TextView mTvHowToPlay3;

    @BindView(R.id.tv_how_to_play_4)
    protected TextView mTvHowToPlay4;

    @BindView(R.id.iv_instructions1)
    protected ImageView mIvInstructions1;

    @BindView(R.id.iv_instructions2)
    protected ImageView mIvInstructions2;

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

        if (isDarkTheme()) {
            mIvInstructions1.getDrawable().setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
            mIvInstructions2.getDrawable().setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
        }

        mTvHowToPlay1.setText(
                HtmlCompat.fromHtml(getString(R.string.how_to_play_1)));
        mTvHowToPlay2.setText(
                HtmlCompat.fromHtml(getString(R.string.how_to_play_2)));
        mTvHowToPlay3.setText(
                HtmlCompat.fromHtml(getString(R.string.how_to_play_3)));
        mTvHowToPlay4.setText(
                HtmlCompat.fromHtml(getString(R.string.how_to_play_4)));

        VideoUtils.setup(getActivity(), mRfVideo1, VideoUtils.VIDEO_INSTRUCTION);
    }

}
