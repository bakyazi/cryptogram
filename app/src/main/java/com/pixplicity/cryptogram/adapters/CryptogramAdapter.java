package com.pixplicity.cryptogram.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.cryptogram.utils.CryptogramProvider;
import com.pixplicity.cryptogram.utils.PrefsUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CryptogramAdapter extends RecyclerView.Adapter<CryptogramAdapter.ViewHolder> {

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_SELECTED = 1;

    boolean mDarkTheme = PrefsUtils.getDarkTheme();
    private final Context mContext;
    private final OnItemClickListener mOnItemClickListener;
    private Cryptogram[] mData;

    public CryptogramAdapter(Context context, OnItemClickListener onItemClickListener) {
        mContext = context;
        mOnItemClickListener = onItemClickListener;

        CryptogramProvider provider = CryptogramProvider.getInstance(mContext);
        mData = provider.getAll();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutResId;
        switch (viewType) {
            default:
            case TYPE_NORMAL:
                if(mDarkTheme)
                    layoutResId = R.layout.item_puzzle_dark;
                else
                    layoutResId = R.layout.item_puzzle;
                break;
            case TYPE_SELECTED:
                if(mDarkTheme)
                    layoutResId = R.layout.item_puzzle_selected_dark;
                else
                    layoutResId = R.layout.item_puzzle_selected;
                break;
        }
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                                            .inflate(layoutResId, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        if (CryptogramProvider.getInstance(mContext).getCurrentIndex() == position) {
            return TYPE_SELECTED;
        }
        return TYPE_NORMAL;
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        Cryptogram cryptogram = mData[position];
        vh.setPosition(position);
        vh.tvPuzzleId.setText(cryptogram.getTitle(mContext));
        String author = cryptogram.getAuthor();
        if (author == null) {
            vh.tvAuthor.setVisibility(View.GONE);
        } else {
            vh.tvAuthor.setVisibility(View.VISIBLE);
            vh.tvAuthor.setText(author);
        }
        vh.ivCompleted.setVisibility(cryptogram.isCompleted() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return mData.length;
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
