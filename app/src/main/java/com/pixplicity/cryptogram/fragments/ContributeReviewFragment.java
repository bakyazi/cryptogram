package com.pixplicity.cryptogram.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.utils.Database;


public class ContributeReviewFragment extends BaseFragment {

    private static final String TAG = ContributeReviewFragment.class.getSimpleName();

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
                for (DataSnapshot puzzles : dataSnapshot.getChildren()) {
                    Puzzle.Suggestion puzzle = puzzles.getValue(Puzzle.Suggestion.class);
                    Log.d(TAG, "Value is: " + puzzle);
                }
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

}
