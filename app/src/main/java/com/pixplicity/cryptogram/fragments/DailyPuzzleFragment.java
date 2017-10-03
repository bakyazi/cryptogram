package com.pixplicity.cryptogram.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.views.CryptogramView;

import java.util.Date;

import butterknife.BindView;
import butterknife.OnClick;

public class DailyPuzzleFragment extends BaseFragment {

    @BindView(R.id.cryptogram)
    protected CryptogramView mCryptogramView;

    @BindView(R.id.vg_subscribe)
    protected ViewGroup mVgSubscribe;

    @BindView(R.id.vg_calendar)
    protected ViewGroup mVgCalendar;

    @BindView(R.id.vg_daily_date)
    protected ViewGroup mVgDate;

    @BindView(R.id.tv_daily_month)
    protected TextView mTvDailyMonth;

    @BindView(R.id.tv_daily_day)
    protected TextView mTvDailyDay;

    @BindView(R.id.tv_daily_author)
    protected TextView mTvDailyAuthor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_daily, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        {
            // TODO attach to today's puzzle
            Puzzle.Mock puzzle = new Puzzle.Mock();
            mCryptogramView.setPuzzle(puzzle);
            mTvDailyAuthor.setText(puzzle.getAuthor());
            Date date = puzzle.getDate();
            if (date == null) {
                mVgDate.setVisibility(View.INVISIBLE);
            } else {
                mVgDate.setVisibility(View.VISIBLE);
                mTvDailyMonth.setText(DateFormat.format("MMM", date));
                mTvDailyDay.setText(DateFormat.format("d", date));
            }
        }
        if (hasSubscription()) {
            mVgSubscribe.setVisibility(View.GONE);
            mVgCalendar.setVisibility(View.VISIBLE);
        } else {
            mVgSubscribe.setVisibility(View.VISIBLE);
            mVgCalendar.setVisibility(View.GONE);
        }
    }

    private boolean hasSubscription() {
        return false;
    }

    @OnClick(R.id.bt_subscribe)
    protected void onClickSubscribe() {
        // TODO
        Toast.makeText(getContext(), "TODO", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.bt_archive)
    protected void onClickArchive() {
        // TODO
        Toast.makeText(getContext(), "TODO", Toast.LENGTH_SHORT).show();
    }

}
