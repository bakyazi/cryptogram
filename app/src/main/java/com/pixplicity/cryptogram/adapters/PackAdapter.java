package com.pixplicity.cryptogram.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Topic;
import com.pixplicity.cryptogram.providers.TopicProvider;

import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PackAdapter extends RecyclerView.Adapter<PackAdapter.ViewHolder> {

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_PURCHASABLE = 1;

    private final Context mContext;
    private final OnItemClickListener mOnItemClickListener;
    private Topic[] mPacks;

    public PackAdapter(Context context, OnItemClickListener onItemClickListener) {
        mContext = context;
        mOnItemClickListener = onItemClickListener;
        mPacks = TopicProvider.getInstance(context).getTopics();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutResId;
        switch (viewType) {
            default:
            case TYPE_NORMAL:
                layoutResId = R.layout.in_pack;
                break;
            case TYPE_PURCHASABLE:
                layoutResId = R.layout.in_pack;
                break;
        }
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                                            .inflate(layoutResId, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        if (false) {
            // TODO
            return TYPE_PURCHASABLE;
        }
        return TYPE_NORMAL;
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        Topic topic = mPacks[position];
        vh.setPosition(position);
        vh.tvTitle.setText(topic.getName());

        String cover = topic.getCover();
        if (cover != null) {
            try {
                InputStream ims = mContext.getAssets().open("covers/" + cover);
                Drawable d = Drawable.createFromStream(ims, null);
                vh.ivCover.setImageDrawable(d);
                ims.close();
            } catch (IOException ignored) {
                cover = null;
            }
        }
        if (cover == null) {
            vh.ivCover.setImageResource(R.drawable.im_puzzle1);
        }
    }

    @Override
    public int getItemCount() {
        return mPacks.length;
    }

    public interface OnItemClickListener {

        void onItemClick(int position);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_title)
        protected TextView tvTitle;

        @BindView(R.id.tv_progress)
        protected TextView tvProgress;

        @BindView(R.id.iv_cover)
        protected ImageView ivCover;

        protected ViewGroup vgContainer;

        private int mPosition;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            vgContainer = (ViewGroup) itemView;

            itemView.setOnClickListener(view -> mOnItemClickListener.onItemClick(mPosition));
        }

        public void setPosition(int position) {
            mPosition = position;
        }

    }

}
