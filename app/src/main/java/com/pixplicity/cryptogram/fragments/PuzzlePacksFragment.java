package com.pixplicity.cryptogram.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.adapters.GridSpacingItemDecoration;
import com.pixplicity.cryptogram.adapters.PackAdapter;
import com.pixplicity.cryptogram.models.Topic;
import com.pixplicity.cryptogram.providers.TopicProvider;

import butterknife.BindView;
import butterknife.OnClick;

public class PuzzlePacksFragment extends BaseFragment {

    @BindView(R.id.rv_packs)
    protected RecyclerView mRvPacks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_packs, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int spanCount = 3;
        mRvPacks.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        mRvPacks.addItemDecoration(new GridSpacingItemDecoration(spanCount,
                getResources().getDimensionPixelSize(R.dimen.grid_padding), false));
        mRvPacks.setAdapter(new PackAdapter(getContext(), position -> {
            // TODO open pack
            Topic[] topics = TopicProvider.getInstance(getContext()).getTopics();
            Toast.makeText(getContext(), "TODO " + topics[position], Toast.LENGTH_SHORT).show();

        }));

        ViewCompat.setNestedScrollingEnabled(mRvPacks, false);
    }

    @OnClick(R.id.bt_more)
    protected void onClickMore() {
        // TODO show more packs
        Toast.makeText(getContext(), "TODO", Toast.LENGTH_SHORT).show();
    }

}
