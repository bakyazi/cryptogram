package com.pixplicity.cryptogram.providers;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.pixplicity.cryptogram.BuildConfig;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.events.PuzzleEvent;
import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.models.PuzzleProgress;
import com.pixplicity.cryptogram.models.PuzzleProgressState;
import com.pixplicity.cryptogram.models.Topic;
import com.pixplicity.cryptogram.utils.EventProvider;
import com.pixplicity.cryptogram.utils.Logger;
import com.pixplicity.cryptogram.utils.PrefsUtils;
import com.pixplicity.cryptogram.utils.SavegameManager;
import com.pixplicity.cryptogram.views.CryptogramView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PuzzleProvider extends AssetProvider {

    private static final String ASSET_FILENAME = "cryptograms.json";

    private static PuzzleProvider sInstance;

    private int mCurrentIndex = -1;

    private Map<String, Topic> mTopics;
    private Puzzle[] mPuzzles;
    private HashMap<Integer, Integer> mPuzzleIds;
    private SparseArray<PuzzleProgress> mPuzzleProgress;

    private int mLastPuzzleId = -1;

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
        Type type = new TypeToken<Map<String, Topic>>() {
        }.getType();
        mTopics = GsonProvider.getGson().fromJson(new InputStreamReader(is), type);
        if (BuildConfig.DEBUG) {
            Logger.d("parsing", String.format(Locale.ENGLISH, "readStream: parsed json in %.2fms", (System.nanoTime() - start) / 1000000f));
            start = System.nanoTime();
        }

        LinkedList<Puzzle> puzzles = new LinkedList<>();
        for (String topicId : mTopics.keySet()) {
            Topic topic = mTopics.get(topicId);
            puzzles.addAll(Arrays.asList(topic.getPuzzles()));
        }
        if (BuildConfig.DEBUG) {
            if (CryptogramView.ENABLE_HYPHENATION) {
                puzzles.add(new Puzzle.Mock(
                        "AAAAAAAA\u00ADBBB\u00ADCCCCCCC\u00ADDDDDDDDDD\u00ADEEEE\u00ADFFFFFFFFFFFFF\u00ADGGGG\u00ADHHHHHHHHHH\u00ADIIIIII.",
                        null, null));
                puzzles.add(new Puzzle.Mock(
                        "JJJJJJJJ KKK LLLLLLL MMMMMMMMM NNNN OOOOOOOOOOOOO PPPP\u00ADQQQQQQQQQQ\u00ADRRRRRR.",
                        null, null));
            }
        }
        mPuzzles = puzzles.toArray(new Puzzle[puzzles.size()]);
        if (BuildConfig.DEBUG) {
            Logger.d("parsing", String.format(Locale.ENGLISH, "readStream: remapped puzzles in %.2fms", (System.nanoTime() - start) / 1000000f));
            start = System.nanoTime();
        }

        int index = 0, nextId = 0, lastId = -1;
        mPuzzleIds = new HashMap<>();
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
            Logger.d("parsing", String.format(Locale.ENGLISH, "readStream: performed ID mapping in %.2fms", (System.nanoTime() - start) / 1000000f));
        }
    }

    public Map<String, Topic> getTopics() {
        return mTopics;
    }

    @Nullable
    public Topic getTopicById(@Nullable String topicId) {
        if (topicId != null) {
            mTopics.get(topicId);
        }
        return null;
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
                        PuzzleProgress progress = GsonProvider.getGson().fromJson(progressStr, PuzzleProgress.class);
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

        // Forget the current puzzle
        PrefsUtils.clearCurrentId();
        // Jump back to the first puzzle
        setCurrentIndex(0);
        EventProvider.postEventDelayed(
                new PuzzleEvent.PuzzleResetEvent());
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
                    Logger.e("loading", "game state failed loading from Google Play Games");
                    if (onLoadResult != null) {
                        onLoadResult.onLoadFailure();
                    }
                } else {
                    Logger.d("loading", "game state loaded from Google Play Games");
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
                    Logger.e("saving", "game state failed saving to Google Play Games");
                    if (onSaveResult != null) {
                        onSaveResult.onSaveFailure();
                    }
                } else {
                    Logger.d("saving", "game state saved to Google Play Games");
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
            progressStrSet.add(GsonProvider.getGson().toJson(progressList.valueAt(i)));
        }
        PrefsUtils.setProgress(progressStrSet);
    }

    public String getProgressJson() {
        SparseArray<PuzzleProgress> progressList = getProgress();
        PuzzleProgressState resultList = new PuzzleProgressState();
        for (int i = 0; i < progressList.size(); i++) {
            resultList.addProgress(progressList.valueAt(i));
        }
        // Store the index of the current puzzle
        resultList.setCurrentId(PrefsUtils.getCurrentId());
        return GsonProvider.getGson().toJson(resultList);
    }

    public void setProgressFromJson(String json) {
        PuzzleProgressState state = GsonProvider.getGson().fromJson(json, PuzzleProgressState.class);
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
            final Integer currentId = state.getCurrentId();
            if (currentId != null) {
                // Select the current puzzle by its ID
                PrefsUtils.setCurrentId(currentId);
            }
        }
        saveLocal();
    }

}
