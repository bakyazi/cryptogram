package com.pixplicity.cryptogram.adapters;

import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class SimpleAdapter<T> extends RecyclerView.Adapter<SimpleAdapter.ViewHolder> implements
        ListAdapter, SpinnerAdapter {

    private List<T> mItems;

    public SimpleAdapter(List<T> items) {
        setItems(items);
    }

    public void setItems(List<T> items) {
        mItems = items;
    }

    @Override
    public SimpleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        return new SimpleAdapter.ViewHolder(
                LayoutInflater.from(parent.getContext())
                              .inflate(android.R.layout.simple_list_item_1, parent, false));
    }

    @Override
    public void onBindViewHolder(final SimpleAdapter.ViewHolder holder, int position) {
        holder.mText1.setText(getText(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public int getCount() {
        return getItemCount();
    }

    @Override
    public T getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SimpleAdapter.ViewHolder holder;
        if (convertView == null) {
            holder = onCreateViewHolder(parent, position);
            convertView = holder.itemView;
            convertView.setTag(holder);
        } else {
            holder = (SimpleAdapter.ViewHolder) convertView.getTag();
        }
        onBindViewHolder(holder, position);
        return convertView;
    }

    @Override
    public View getDropDownView(int i, View view, ViewGroup viewGroup) {
        return getView(i, view, viewGroup);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public abstract String getText(int position);

    public void onItemClick(int position, T item, View view) {
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(android.R.id.text1)
        protected TextView mText1;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}
