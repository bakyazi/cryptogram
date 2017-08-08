package com.pixplicity.cryptogram.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.models.PuzzleList;
import com.pixplicity.cryptogram.utils.PrefsUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PuzzleAdapter extends RecyclerView.Adapter<PuzzleAdapter.ViewHolder> {

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_SELECTED = 1;

    private final Context mContext;
    private final OnItemClickListener mOnItemClickListener;
    private PuzzleList mPuzzles;

    private boolean mDarkTheme = PrefsUtils.getDarkTheme();

    public PuzzleAdapter(Context context, OnItemClickListener onItemClickListener, PuzzleList puzzleList) {
        mContext = context;
        mOnItemClickListener = onItemClickListener;
        mPuzzles = puzzleList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutResId;
        switch (viewType) {
            default:
            case TYPE_NORMAL:
                if (mDarkTheme) {
                    layoutResId = R.layout.item_puzzle_dark;
                } else {
                    layoutResId = R.layout.item_puzzle;
                }
                break;
            case TYPE_SELECTED:
                if (mDarkTheme) {
                    layoutResId = R.layout.item_puzzle_selected_dark;
                } else {
                    layoutResId = R.layout.item_puzzle_selected;
                }
                break;
        }
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                                            .inflate(layoutResId, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        if (mPuzzles.getCurrentIndex() == position) {
            return TYPE_SELECTED;
        }
        return TYPE_NORMAL;
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        Puzzle puzzle = mPuzzles.get(position);
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
    }

    @Override
    public int getItemCount() {
        return mPuzzles.getCount();
    }

    public void setPuzzleList(PuzzleList puzzleList) {
        mPuzzles = puzzleList;
        notifyDataSetChanged();
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
