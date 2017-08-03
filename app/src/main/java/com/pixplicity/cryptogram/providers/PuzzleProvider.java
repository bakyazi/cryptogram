package com.pixplicity.cryptogram.providers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pixplicity.cryptogram.BuildConfig;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.models.PuzzleProgress;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.pixplicity.cryptogram.views.CryptogramView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

public class PuzzleProvider extends AssetProvider {

    private static final String TAG = PuzzleProvider.class.getSimpleName();

    private static final String ASSET_FILENAME = "cryptograms.json";

    private static PuzzleProvider sInstance;

    private int mCurrentIndex = -1;
    private Puzzle[] mPuzzles;
    private HashMap<Integer, Integer> mPuzzleIds;
    private SparseArray<PuzzleProgress> mPuzzleProgress;

    private int mLastPuzzleId = -1;

    private static final Gson mGson = new Gson();
    private final Random mRandom = new Random();
    private ArrayList<Integer> mRandomIndices;

    @NonNull
    public static PuzzleProvider getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PuzzleProvider(context);
        }
        return sInstance;
    }

    private PuzzleProvider(@Nullable Context context) {
        super(context);
    }

    @NonNull
    @Override
    public String getAssetFilename() {
        return ASSET_FILENAME;
    }

    @Override
    protected void onLoadFailure(Context context, IOException e) {
        Toast.makeText(context, R.string.error_puzzles_load_failure, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onLoad(Context context, InputStream is) {
        long start = System.nanoTime();
        mPuzzles = GsonProvider.getGson().fromJson(new InputStreamReader(is), Puzzle[].class);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("readStream: parsed Json in %.2fms", (System.nanoTime() - start) / 1000000f));
            start = System.nanoTime();
        }
        int index = 0, nextId = 0;
        mPuzzleIds = new HashMap<>();
        if (BuildConfig.DEBUG) {
            LinkedList<Puzzle> puzzles = new LinkedList<>();
            if (CryptogramView.ENABLE_HYPHENATION) {
                puzzles.add(new Puzzle.Mock(
                        "AAAAAAAA\u00ADBBB\u00ADCCCCCCC\u00ADDDDDDDDDD\u00ADEEEE\u00ADFFFFFFFFFFFFF\u00ADGGGG\u00ADHHHHHHHHHH\u00ADIIIIII.",
                        null, null));
                puzzles.add(new Puzzle.Mock(
                        "JJJJJJJJ KKK LLLLLLL MMMMMMMMM NNNN OOOOOOOOOOOOO PPPP\u00ADQQQQQQQQQQ\u00ADRRRRRR.",
                        null, null));
            }
            puzzles.addAll(Arrays.asList(mPuzzles));
            mPuzzles = puzzles.toArray(new Puzzle[puzzles.size()]);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, String.format("readStream: added test puzzles in %.2fms", (System.nanoTime() - start) / 1000000f));
                start = System.nanoTime();
            }
        }
        for (Puzzle puzzle : mPuzzles) {
            int id = puzzle.getId();
            if (id == 0) {
                while (mPuzzleIds.get(nextId) != null) {
                    // Locate the next vacant spot
                    nextId++;
                }
                id = nextId;
                puzzle.setId(id);
            }
            if (id > mLastPuzzleId) {
                mLastPuzzleId = id;
            }
            mPuzzleIds.put(id, index);
            index++;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("readStream: performed ID mapping in %.2fms", (System.nanoTime() - start) / 1000000f));
        }
    }

    public Puzzle[] getAll() {
        return mPuzzles;
    }

    /**
     * @return last puzzle ID
     */
    public int getLastNumber() {
        return mLastPuzzleId + 1;
    }

    public int getCount() {
        return getAll().length;
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    private int getIndexFromId(int id) {
        Integer index = mPuzzleIds.get(id);
        if (index == null) {
            return -1;
        }
        return index;
    }

    private int getIdFromIndex(int index) {
        return mPuzzles[index].getId();
    }

    @Nullable
    public Puzzle getCurrent() {
        if (mCurrentIndex < 0) {
            mCurrentIndex = getIndexFromId(PrefsUtils.getCurrentId());
        }
        if (mCurrentIndex < 0) {
            return getNext();
        }
        return get(mCurrentIndex);
    }

    public void setCurrentIndex(int index) {
        mCurrentIndex = index;
        PrefsUtils.setCurrentId(getIdFromIndex(index));
    }

    public void setCurrentId(int id) {
        mCurrentIndex = getIndexFromId(id);
        PrefsUtils.setCurrentId(id);
    }

    @Nullable
    public Puzzle getNext() {
        int oldIndex = mCurrentIndex;
        int newIndex = -1;
        int count = getCount();
        if (count == 0) {
            return null;
        }
        if (PrefsUtils.getRandomize()) {
            if (mRandomIndices == null) {
                mRandomIndices = new ArrayList<>();
                for (int i = 0; i < getAll().length; i++) {
                    mRandomIndices.add(i);
                }
                Collections.shuffle(mRandomIndices, mRandom);
            }
            boolean chooseNext = false;
            Iterator<Integer> iter = mRandomIndices.iterator();
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
            if (newIndex < 0 && !mRandomIndices.isEmpty()) {
                newIndex = mRandomIndices.get(0);
            }
        } else if (mCurrentIndex + 1 < getCount()) {
            newIndex = mCurrentIndex + 1;
        }
        if (newIndex > -1) {
            setCurrentIndex(newIndex);
        }
        return get(mCurrentIndex);
    }

    @Nullable
    public Puzzle get(int index) {
        if (index < 0 || index >= mPuzzles.length) {
            return null;
        }
        return mPuzzles[index];
    }

    @Nullable
    public Puzzle getByNumber(int number) {
        for (Puzzle puzzle : mPuzzles) {
            if (puzzle.getNumber() == number) {
                return puzzle;
            }
        }
        return null;
    }

    public long getTotalScore() {
        long score = 0;
        for (Puzzle puzzle : mPuzzles) {
            if (!puzzle.isCompleted()) {
                continue;
            }
            PuzzleProgress progress = puzzle.getProgress();
            if (!progress.hasScore(puzzle)) {
                continue;
            }
            score += Math.round(100f * progress.getScore(puzzle));
        }
        return score;
    }

    @NonNull
    public SparseArray<PuzzleProgress> getProgress() {
        if (mPuzzleProgress == null) {
            int failures = 0;
            mPuzzleProgress = new SparseArray<>();
            Set<String> progressStrSet = PrefsUtils.getProgress();
            if (progressStrSet != null) {
                for (String progressStr : progressStrSet) {
                    try {
                        PuzzleProgress progress = mGson.fromJson(progressStr, PuzzleProgress.class);
                        mPuzzleProgress.put(progress.getId(), progress);
                    } catch (JsonSyntaxException e) {
                        Crashlytics.setString("progressStr", progressStr);
                        Crashlytics.logException(new RuntimeException("Failed reading progress string", e));
                        progressStrSet.remove(progressStr);
                        failures++;
                    }
                }
            }
            if (failures > 0) {
                // Remove any corrupted data
                PrefsUtils.setProgress(progressStrSet);
            }
        }
        return mPuzzleProgress;
    }

    public void setProgress(PuzzleProgress progress) {
        SparseArray<PuzzleProgress> progressList = getProgress();
        progressList.put(progress.getId(), progress);

        // Now store everything
        Set<String> progressStrSet = new LinkedHashSet<>();
        for (int i = 0; i < progressList.size(); i++) {
            progressStrSet.add(mGson.toJson(progressList.valueAt(i)));
        }
        PrefsUtils.setProgress(progressStrSet);
    }

}
