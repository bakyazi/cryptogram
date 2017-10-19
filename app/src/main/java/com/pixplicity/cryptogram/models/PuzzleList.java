package com.pixplicity.cryptogram.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pixplicity.cryptogram.utils.PrefsUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

@Deprecated
public class PuzzleList {

    private static final Random sRandom = new Random();

    private Puzzle[] mPuzzles;
    private HashMap<Integer, Integer> mPuzzleIds;

    private ArrayList<Integer> mRandomIndices;

    private int mCurrentIndex = -1;

    public PuzzleList(@NonNull Puzzle[] puzzles) {
        mPuzzles = puzzles;
        mPuzzleIds = new HashMap<>();

        int index = 0;
        for (Puzzle puzzle : mPuzzles) {
            mPuzzleIds.put(puzzle.getId(), index);
            index++;
        }
        setCurrentId(PrefsUtils.getCurrentId());
    }

    public int getCount() {
        return mPuzzles.length;
    }

    public Puzzle[] getAll() {
        return mPuzzles;
    }

    public Puzzle get(int index) {
        if (index < 0 || index >= mPuzzles.length) {
            return null;
        }
        return mPuzzles[index];
    }

    @Nullable
    public Puzzle getNext() {
        final int count = getAll().length;
        int oldIndex = getCurrentIndex();
        int newIndex = -1;
        if (count == 0) {
            return null;
        }
        if (PrefsUtils.getRandomize()) {
            boolean chooseNext = false;
            Iterator<Integer> iter = getRandomIndices().iterator();
            while (iter.hasNext()) {
                Integer index = iter.next();
                Puzzle puzzle = get(index);
                if (puzzle == null || puzzle.isCompleted()) {
                    // No good; eliminate this candidate and find the next
                    iter.remove();
                    puzzle = null;
                }
                if (oldIndex == index) {
                    chooseNext = true;
                } else if (chooseNext && puzzle != null) {
                    newIndex = index;
                    break;
                }
            }
            if (newIndex < 0 && !getRandomIndices().isEmpty()) {
                newIndex = getRandomIndices().get(0);
            }
        } else if (getCurrentIndex() + 1 < count) {
            newIndex = getCurrentIndex() + 1;
        }
        if (newIndex < 0) {
            newIndex = 0;
        }
        setCurrentIndex(newIndex);
        return get(getCurrentIndex());
    }

    public ArrayList<Integer> getRandomIndices() {
        mRandomIndices = new ArrayList<>();
        for (int i = 0; i < getAll().length; i++) {
            mRandomIndices.add(i);
        }
        Collections.shuffle(mRandomIndices, sRandom);
        return mRandomIndices;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        mCurrentIndex = currentIndex;
        PrefsUtils.setCurrentId(get(currentIndex).getId());
    }

    public void setCurrentId(int currentId) {
        mCurrentIndex = getIndexFromId(currentId);
        PrefsUtils.setCurrentId(currentId);
    }

    private int getIndexFromId(int id) {
        Integer index = mPuzzleIds.get(id);
        if (index == null) {
            return -1;
        }
        return index;
    }

}
