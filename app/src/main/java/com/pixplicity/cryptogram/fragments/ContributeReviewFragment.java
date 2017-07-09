package com.pixplicity.cryptogram.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.adapters.PuzzleAdapter;
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

        Database.getInstance().getSuggestions().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                mPuzzlesLoaded = true;
                for (DataSnapshot puzzles : dataSnapshot.getChildren()) {
                    Puzzle.Suggestion puzzle = puzzles.getValue(Puzzle.Suggestion.class);
                    Log.d(TAG, "Value is: " + puzzle);
                    mPuzzleList.add(puzzle);
                }
                updateAdapter();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
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

        mAdapter = new SuggestionsAdapter(getContext(), new PuzzleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Puzzle[] puzzles = mAdapter.getPuzzles();
                if (puzzles != null) {
                    Puzzle puzzle = puzzles[position];
                    Snackbar.make(mRvPuzzles, "Reviewing puzzles will be available in an upcoming version", Snackbar.LENGTH_SHORT).show();
                    // TODO something with the puzzle
                }
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
