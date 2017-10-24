package com.pixplicity.cryptogram.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.activities.AboutActivity;
import com.pixplicity.cryptogram.utils.HtmlCompat;
import com.pixplicity.cryptogram.utils.VideoUtils;

import net.soulwolf.widget.ratiolayout.widget.RatioFrameLayout;

import butterknife.BindView;


public class HowToPlayFragment extends BaseFragment {

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_how_to_play, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_how_to_play, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isDarkTheme()) {
            invert(mIvInstructions1);
            invert(mIvInstructions2);
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about: {
                startActivity(AboutActivity.create(getContext()));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
