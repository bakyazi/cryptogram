package com.pixplicity.cryptogram.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.pixplicity.cryptogram.utils.PuzzleProvider;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PuzzleAdapter extends RecyclerView.Adapter<PuzzleAdapter.ViewHolder> {

    protected static final int TYPE_NORMAL = 0;
    protected static final int TYPE_SELECTED = 1;

    private final Context mContext;
    private final OnItemClickListener mOnItemClickListener;
    protected Puzzle[] mPuzzles;

    private boolean mDarkTheme = PrefsUtils.getDarkTheme();

    public PuzzleAdapter(Context context, OnItemClickListener onItemClickListener) {
        mContext = context;
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                                            .inflate(getLayoutResId(viewType), parent, false));
    }

    protected int getLayoutResId(int viewType) {
        switch (viewType) {
            case TYPE_NORMAL:
                if (isDarkTheme()) {
                    return R.layout.item_puzzle_dark;
                } else {
                    return R.layout.item_puzzle;
                }
            case TYPE_SELECTED:
                if (isDarkTheme()) {
                    return R.layout.item_puzzle_selected_dark;
                } else {
                    return R.layout.item_puzzle_selected;
                }
        }
        throw new IllegalStateException("Unexpected view type " + viewType);
    }

    protected boolean isDarkTheme() {
        return mDarkTheme;
    }

    @Override
    public int getItemViewType(int position) {
        if (getSelection() == position) {
            return TYPE_SELECTED;
        }
        return TYPE_NORMAL;
    }

    protected int getSelection() {
        return PuzzleProvider.getInstance(mContext).getCurrentIndex();
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        Puzzle[] puzzles = getPuzzles();
        if (puzzles != null) {
            Puzzle puzzle = puzzles[position];
            vh.setPosition(position);
            vh.tvPuzzleId.setText(puzzle.getTitle(mContext));
            String author = puzzle.getAuthor();
            if (author == null) {
                vh.tvAuthor.setVisibility(View.GONE);
            } else {
                vh.tvAuthor.setVisibility(View.VISIBLE);
                vh.tvAuthor.setText(author);
            }
            vh.ivCompleted.setVisibility(puzzle.isCompleted() ? View.VISIBLE : View.GONE);
            vh.ivInProgress.setVisibility(puzzle.isInProgress() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        Puzzle[] puzzles = getPuzzles();
        if (puzzles == null) {
            return 0;
        }
        return puzzles.length;
    }

    public void setPuzzles(@Nullable Puzzle[] puzzles) {
        throw new IllegalStateException("This implementation obtains puzzles from the PuzzleProvider");
    }

    @Nullable
    public Puzzle[] getPuzzles() {
        PuzzleProvider provider = PuzzleProvider.getInstance(mContext);
        mPuzzles = provider.getAll();
        return mPuzzles;
    }

    public interface OnItemClickListener {

        void onItemClick(int position);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_puzzle_id)
        protected TextView tvPuzzleId;

        @BindView(R.id.tv_author)
        protected TextView tvAuthor;

        @BindView(R.id.iv_completed)
        protected ImageView ivCompleted;

        @BindView(R.id.iv_in_progress)
        protected ImageView ivInProgress;

        protected ViewGroup vgContainer;

        private int mPosition;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            vgContainer = (ViewGroup) itemView;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(mPosition);
                }
            });
        }

        public void setPosition(int position) {
            mPosition = position;
        }

    }

}
