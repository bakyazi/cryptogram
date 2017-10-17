package com.pixplicity.cryptogram.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.adapters.SuggestionsAdapter;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.utils.Database;

import java.util.ArrayList;

import butterknife.BindView;


public class ContributeReviewFragment extends BaseFragment {

    private static final String TAG = ContributeReviewFragment.class.getSimpleName();

    @BindView(R.id.rv_puzzles)
    protected RecyclerView mRvPuzzles;

    @BindView(R.id.pb_loading)
    protected ProgressBar mPbLoading;

    private boolean mPuzzlesLoaded;
    private ArrayList<Puzzle.Suggestion> mPuzzleList = new ArrayList<>();
    private SuggestionsAdapter mAdapter;

    public static Fragment create() {
        return new ContributeReviewFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Database.getInstance().getSuggestions().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mPuzzlesLoaded = true;
                Puzzle.Suggestion puzzle = dataSnapshot.getValue(Puzzle.Suggestion.class);
                if (puzzle != null) {
                    puzzle.setFirebaseId(dataSnapshot.getKey());
                    mPuzzleList.add(puzzle);
                }
                updateAdapter();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Puzzle.Suggestion puzzle = dataSnapshot.getValue(Puzzle.Suggestion.class);
                if (puzzle != null) {
                    puzzle.setFirebaseId(dataSnapshot.getKey());
                    for (int i = 0; i < mPuzzleList.size(); i++) {
                        if (puzzle.equals(mPuzzleList.get(i))) {
                            mPuzzleList.remove(i);
                            mPuzzleList.add(i, puzzle);
                        }
                    }
                }
                updateAdapter();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Puzzle.Suggestion puzzle = dataSnapshot.getValue(Puzzle.Suggestion.class);
                if (puzzle != null) {
                    puzzle.setFirebaseId(dataSnapshot.getKey());
                    mPuzzleList.remove(puzzle);
                }
                updateAdapter();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                mPuzzlesLoaded = true;
                showSnackbar(getString(R.string.error_no_puzzles));
                updateAdapter();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contribute_review, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new SuggestionsAdapter(getContext(), position -> {
            Puzzle[] puzzles = mAdapter.getPuzzles();
            if (puzzles != null) {
                Puzzle puzzle = puzzles[position];
                showSnackbar("Reviewing puzzles will be available in an upcoming version");
                // TODO something with the puzzle
            }
        });
        updateAdapter();
        mRvPuzzles.setAdapter(mAdapter);
    }

    private void updateAdapter() {
        if (mAdapter != null) {
            mAdapter.setPuzzles(mPuzzleList.toArray(new Puzzle.Suggestion[mPuzzleList.size()]));
            mAdapter.notifyDataSetChanged();
        }
        mPbLoading.setVisibility(mPuzzlesLoaded ? View.GONE : View.VISIBLE);
    }

}
