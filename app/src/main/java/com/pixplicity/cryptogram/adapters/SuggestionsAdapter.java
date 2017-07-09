package com.pixplicity.cryptogram.adapters;

import android.content.Context;
import android.support.annotation.Nullable;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Puzzle;

public class SuggestionsAdapter extends PuzzleAdapter {

    public SuggestionsAdapter(Context context, OnItemClickListener onItemClickListener) {
        super(context, onItemClickListener);
    }

    protected int getLayoutResId(int viewType) {
        switch (viewType) {
            case TYPE_NORMAL:
                if (isDarkTheme()) {
                    return R.layout.item_puzzle;
                } else {
                    return R.layout.item_puzzle_dark;
                }
            case TYPE_SELECTED:
                if (isDarkTheme()) {
                    return R.layout.item_puzzle_selected;
                } else {
                    return R.layout.item_puzzle_selected_dark;
                }
        }
        throw new IllegalStateException("Unexpected view type " + viewType);
    }

    @Override
    protected int getSelection() {
        return -1;
    }

    @Override
    public void setPuzzles(@Nullable Puzzle[] puzzles) {
        mPuzzles = puzzles;
    }

    @Override
    public Puzzle[] getPuzzles() {
        return mPuzzles;
    }

}
