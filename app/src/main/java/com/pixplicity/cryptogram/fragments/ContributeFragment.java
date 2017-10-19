package com.pixplicity.cryptogram.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.activities.ContributeActivity;
import com.pixplicity.cryptogram.utils.TintUtils;

import butterknife.BindView;
import butterknife.OnClick;


public class ContributeFragment extends BaseFragment {

    private static final String TAG = ContributeFragment.class.getSimpleName();

    public static Fragment create() {
        return new ContributeFragment();
    }

    @BindView(R.id.tv_suggest)
    protected TextView mTvSuggest;

    @BindView(R.id.tv_review)
    protected TextView mTvReview;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contribute, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int color;
        if (isDarkTheme()) {
            color = getResources().getColor(R.color.colorDarkTextPrimary);
        } else {
            color = getResources().getColor(R.color.colorTextPrimary);
        }
        TintUtils.tint(color, mTvSuggest);
        TintUtils.tint(color, mTvReview);
    }

    @OnClick(R.id.bt_suggest)
    protected void onClickSuggest() {
        startActivity(ContributeActivity.create(getContext(), ContributeActivity.MODE_SUGGEST));
    }

    @OnClick(R.id.bt_review)
    protected void onClickReview() {
        startActivity(ContributeActivity.create(getContext(), ContributeActivity.MODE_REVIEW));
    }

}
