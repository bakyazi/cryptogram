package com.pixplicity.cryptogram.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Topic;
import com.pixplicity.cryptogram.providers.TopicProvider;

import butterknife.OnClick;

public class PuzzlePacksFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_packs, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Topic[] topics = TopicProvider.getInstance(getContext()).getTopics();
        super.onViewCreated(view, savedInstanceState);
    }

    @OnClick(R.id.bt_more)
    protected void onClickMore() {
        // TODO
        Toast.makeText(getContext(), "TODO", Toast.LENGTH_SHORT).show();
    }

}
