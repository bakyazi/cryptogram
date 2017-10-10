package com.pixplicity.cryptogram.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.activities.PuzzleActivity;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.views.CryptogramView;

import butterknife.BindView;
import butterknife.OnClick;

public class ContinuePuzzleFragment extends BaseFragment {

    @BindView(R.id.cryptogram)
    protected CryptogramView mCryptogramView;

    @BindView(R.id.tv_author)
    protected TextView mTvAuthor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_continue_puzzle, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        {
            // TODO attach to last played puzzle
            Puzzle.Mock puzzle = new Puzzle.Mock();
            mCryptogramView.setPuzzle(puzzle);
            String author = puzzle.getAuthor();
            if (TextUtils.isEmpty(author)) {
                mTvAuthor.setText(null);
            } else {
                mTvAuthor.setText(getString(R.string.quote_by, author));
            }
        }
    }

    @OnClick(R.id.cv_puzzle)
    protected void onClickPuzzle() {
        startActivity(PuzzleActivity.create(getContext()));
    }

}
