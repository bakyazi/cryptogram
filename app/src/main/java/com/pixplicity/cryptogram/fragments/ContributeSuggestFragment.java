package com.pixplicity.cryptogram.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.utils.Database;

import butterknife.BindView;


public class ContributeSuggestFragment extends BaseFragment {

    private static final String TAG = ContributeSuggestFragment.class.getSimpleName();

    @BindView(R.id.et_text)
    protected EditText mEtText;

    @BindView(R.id.et_author)
    protected EditText mEtAuthor;

    @BindView(R.id.et_topic)
    protected EditText mEtTopic;

    public static Fragment create() {
        return new ContributeSuggestFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contribute_suggest, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_suggest, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_submit: {
                onSubmit();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSubmit() {
        Puzzle puzzle = new Puzzle.Suggestion(
                mEtText.getText().toString(),
                mEtAuthor.getText().toString(),
                mEtTopic.getText().toString());
        Database.getInstance()
                .getSuggestions()
                .push()
                .setValue(puzzle);
    }

}
