package com.pixplicity.cryptogram.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.activities.HowToPlayActivity;

import butterknife.BindView;
import butterknife.OnClick;


public class LandingFragment extends BaseFragment {

    @BindView(R.id.vg_help)
    protected ViewGroup mVgHelp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_landing, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO
        //inflater.inflate(R.menu.menu_, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // TODO
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.vg_help)
    protected void onClickHelp() {
        startActivity(new Intent(getContext(), HowToPlayActivity.class));
    }

    @OnClick(R.id.bt_help_dismiss)
    protected void onClickHelpDismiss() {
        // TODO save preference
        // Animation is determined by `animateLayoutChanges` of view parent
        mVgHelp.setVisibility(View.GONE);
    }

}
