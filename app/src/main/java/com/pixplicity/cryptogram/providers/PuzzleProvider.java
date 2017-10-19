package com.pixplicity.cryptogram.providers;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pixplicity.cryptogram.BuildConfig;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.models.PuzzleList;
import com.pixplicity.cryptogram.models.PuzzleProgress;
import com.pixplicity.cryptogram.models.PuzzleProgressState;
import com.pixplicity.cryptogram.models.Topic;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.pixplicity.cryptogram.utils.SavegameManager;
import com.pixplicity.cryptogram.views.CryptogramView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

public class PuzzleProvider extends AssetProvider {

    private static final String TAG = PuzzleProvider.class.getSimpleName();

    private static final String ASSET_FILENAME = "cryptograms.json";

    private static PuzzleProvider sInstance;

    private Puzzle[] mPuzzles;
    private HashMap<Integer, Integer> mPuzzleIds;
    private SparseArray<PuzzleProgress> mPuzzleProgress;

    private int mLastPuzzleId;

    private static final Gson mGson = new Gson();

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
        int index = 0, nextId = 0, lastId = -1;
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
            if (id > lastId) {
                lastId = id;
            }
            mPuzzleIds.put(id, index);
            index++;
        }
        mLastPuzzleId = lastId + 1;
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("readStream: performed ID mapping in %.2fms", (System.nanoTime() - start) / 1000000f));
        }
    }

    public Puzzle[] getAll() {
        return mPuzzles;
    }

    public Puzzle[] getAllForTopic(@Nullable Topic topic) {
        if (topic == null) {
            return getAll();
        }
        ArrayList<Puzzle> puzzles = new ArrayList<>();
        for (Puzzle puzzle : mPuzzles) {
            if (puzzle.hasTopic(topic)) {
                puzzles.add(puzzle);
            }
        }
        return puzzles.toArray(new Puzzle[puzzles.size()]);
    }

    /**
     * @return last puzzle ID
     */
    public int getLastNumber() {
        return mLastPuzzleId;
    }

    public int getCount() {
        return getAll().length;
    }

    @Nullable
    public Puzzle getCurrent(final PuzzleList puzzlesList) {
        final int currentIndex = puzzlesList.getCurrentIndex();
        return puzzlesList.get(currentIndex < 0 ? 0 : currentIndex);
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

    public void setProgress(int puzzleId, @Nullable PuzzleProgress progress) {
        // Ensure that we've loaded all puzzle progress
        getProgress();
        mPuzzleProgress.put(puzzleId, progress);
    }

    /**
     * Resets all puzzles.
     */
    public void resetAll() {
        for (Puzzle puzzle : mPuzzles) {
            puzzle.reset(false);
        }
        mPuzzleProgress.clear();
        saveLocal();

        // Jump back to the first puzzle
        // FIXME get the first puzzle from the current puzzle list
        // FIXME maybe just reset the puzzle topics?
        /*
        setCurrentIndex(0);
        EventProvider.postEventDelayed(
                new PuzzleEvent.PuzzleResetEvent(getCurrent()));
        */
    }

    public void load(@Nullable final GoogleApiClient googleApiClient,
                     @NonNull final SnapshotMetadata snapshotMetadata,
                     @Nullable final SavegameManager.OnLoadResult onLoadResult) {
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            // No connection; cannot load
            if (onLoadResult != null) {
                onLoadResult.onLoadFailure();
            }
            return;
        }
        new AsyncTask<Void, Void, Snapshot>() {

            @Override
            protected Snapshot doInBackground(Void... voids) {
                return SavegameManager.load(googleApiClient, snapshotMetadata.getUniqueName());
            }

            @Override
            protected void onPostExecute(Snapshot snapshot) {
                if (snapshot == null) {
                    Log.e(TAG, "game state failed loading from Google Play Games");
                    if (onLoadResult != null) {
                        onLoadResult.onLoadFailure();
                    }
                } else {
                    Log.d(TAG, "game state loaded from Google Play Games");
                    if (onLoadResult != null) {
                        onLoadResult.onLoadSuccess();
                    }
                }
            }

        }.execute();
    }

    public void save(@Nullable final GoogleApiClient googleApiClient,
                     @Nullable final SavegameManager.OnSaveResult onSaveResult) {
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            // No connection; cannot save
            if (onSaveResult != null) {
                onSaveResult.onSaveFailure();
            }
            return;
        }
        // Create a new snapshot named with a unique string
        new AsyncTask<Void, Void, SnapshotMetadata>() {

            @Override
            protected SnapshotMetadata doInBackground(Void... voids) {
                return SavegameManager.save(googleApiClient);
            }

            @Override
            protected void onPostExecute(SnapshotMetadata snapshot) {
                if (snapshot == null) {
                    Log.e(TAG, "game state failed saving to Google Play Games");
                    if (onSaveResult != null) {
                        onSaveResult.onSaveFailure();
                    }
                } else {
                    Log.d(TAG, "game state saved to Google Play Games");
                    if (onSaveResult != null) {
                        onSaveResult.onSaveSuccess();
                    }
                }
            }

        }.execute();
    }

    public void saveLocal() {
        SparseArray<PuzzleProgress> progressList = getProgress();
        // Now store everything
        Set<String> progressStrSet = new LinkedHashSet<>();
        for (int i = 0; i < progressList.size(); i++) {
            progressStrSet.add(mGson.toJson(progressList.valueAt(i)));
        }
        PrefsUtils.setProgress(progressStrSet);
    }

    public String getProgressJson() {
        SparseArray<PuzzleProgress> progressList = getProgress();
        PuzzleProgressState resultList = new PuzzleProgressState();
        for (int i = 0; i < progressList.size(); i++) {
            resultList.addProgress(progressList.valueAt(i));
        }
        // FIXME store the current puzzle
        // resultList.setCurrentId(getIdFromIndex(getCurrentIndex()));
        return mGson.toJson(resultList);
    }

    public void setProgressFromJson(String json) {
        PuzzleProgressState state = mGson.fromJson(json, PuzzleProgressState.class);
        if (state != null) {
            for (PuzzleProgress puzzleProgress : state.getProgress()) {
                if (puzzleProgress != null) {
                    final int puzzleId = puzzleProgress.getId();
                    setProgress(puzzleId, puzzleProgress);
                    Integer index = mPuzzleIds.get(puzzleId);
                    if (index != null && index >= 0) {
                        mPuzzles[index].unload();
                    }
                }
            }
            // FIXME resume the current puzzle from state.getCurrentId()
            /*
            final Integer currentId = state.getCurrentId();
            if (currentId != null) {
                // Select the current puzzle by its ID
                setCurrentId(currentId);
            } else {
                // Select the first puzzle
                setCurrentIndex(0);
            }
            */
        }
        saveLocal();
    }

}
