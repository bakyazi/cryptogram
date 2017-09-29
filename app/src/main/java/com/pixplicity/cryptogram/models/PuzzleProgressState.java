package com.pixplicity.cryptogram.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PuzzleProgressState {

    @SerializedName("current")
    private Integer mCurrentPuzzleId;

    @SerializedName("progress")
    private ArrayList<PuzzleProgress> mProgress;

    @Nullable
    public Integer getCurrentId() {
        return mCurrentPuzzleId;
    }

    public void setCurrentId(int puzzleId) {
        mCurrentPuzzleId = puzzleId;
    }

    @NonNull
    public ArrayList<PuzzleProgress> getProgress() {
        if (mProgress == null) {
            mProgress = new ArrayList<>();
        }
        return mProgress;
    }

    public void addProgress(PuzzleProgress puzzleProgress) {
        getProgress().add(puzzleProgress);
    }

}
