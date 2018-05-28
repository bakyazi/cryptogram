package com.pixplicity.cryptogram.utils

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.util.SparseArray
import android.widget.Toast

import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.games.snapshot.Snapshot
import com.google.android.gms.games.snapshot.SnapshotMetadata
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.pixplicity.cryptogram.BuildConfig
import com.pixplicity.cryptogram.events.PuzzleEvent
import com.pixplicity.cryptogram.models.Puzzle
import com.pixplicity.cryptogram.models.PuzzleProgress
import com.pixplicity.cryptogram.models.PuzzleProgressState
import com.pixplicity.cryptogram.views.CryptogramView

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.HashMap
import java.util.LinkedHashSet
import java.util.LinkedList
import java.util.Random

class PuzzleProvider @Throws(IOException::class) private constructor(context: Context?) {

    private var mCurrentIndex = -1
    var all: Array<Puzzle> = emptyArray()
        private set
    private var mPuzzleIds: HashMap<Int, Int>? = null
    private var mPuzzleProgress: SparseArray<PuzzleProgress>? = null

    private var mLastPuzzleId = -1

    private val mGson = Gson()
    private val mRandom = Random()
    private var mRandomIndices: ArrayList<Int>? = null

    /**
     * @return last puzzle ID
     */
    val lastNumber: Int
        get() = mLastPuzzleId + 1

    val count: Int
        get() = all.size

    var currentIndex: Int
        get() = mCurrentIndex
        set(index) {
            mCurrentIndex = index
            PrefsUtils.setCurrentId(getIdFromIndex(index))
        }

    val current: Puzzle?
        get() {
            if (mCurrentIndex < 0) {
                mCurrentIndex = getIndexFromId(PrefsUtils.getCurrentId())
            }
            return if (mCurrentIndex < 0) {
                next
            } else get(mCurrentIndex)
        }

    // No good; eliminate this candidate and find the next
    val next: Puzzle?
        get() {
            val oldIndex = mCurrentIndex
            var newIndex = -1
            val count = count
            if (count == 0) {
                return null
            }
            if (PrefsUtils.getRandomize()) {
                if (mRandomIndices == null) {
                    mRandomIndices = ArrayList()
                    for (i in 0 until all.size) {
                        mRandomIndices!!.add(i)
                    }
                    Collections.shuffle(mRandomIndices!!, mRandom)
                }
                var chooseNext = false
                val iter = mRandomIndices!!.iterator()
                while (iter.hasNext()) {
                    val index = iter.next()
                    var puzzle = get(index)
                    if (puzzle == null || puzzle.isCompleted) {
                        iter.remove()
                        puzzle = null
                    }
                    if (oldIndex == index) {
                        chooseNext = true
                    } else if (chooseNext && puzzle != null) {
                        newIndex = index
                        break
                    }
                }
                if (newIndex < 0 && !mRandomIndices!!.isEmpty()) {
                    newIndex = mRandomIndices!![0]
                }
            } else if (mCurrentIndex + 1 < count) {
                newIndex = mCurrentIndex + 1
            }
            if (newIndex > -1) {
                currentIndex = newIndex
            }
            return get(mCurrentIndex)
        }

    val totalScore: Long
        get() {
            var score: Long = 0
            for (puzzle in all) {
                if (!puzzle.isCompleted) {
                    continue
                }
                val progress = puzzle.progress
                if (!progress.hasScore(puzzle)) {
                    continue
                }
                score += Math.round(100f * progress.getScore(puzzle)!!).toLong()
            }
            return score
        }

    // Remove any corrupted data
    val progress: SparseArray<PuzzleProgress>
        get() {
            if (mPuzzleProgress == null) {
                var failures = 0
                mPuzzleProgress = SparseArray()
                val progressStrSet = PrefsUtils.getProgress()
                if (progressStrSet != null) {
                    for (progressStr in progressStrSet) {
                        try {
                            val progress = mGson.fromJson(progressStr, PuzzleProgress::class.java)
                            mPuzzleProgress!!.put(progress.id, progress)
                        } catch (e: JsonSyntaxException) {
                            Crashlytics.setString("progressStr", progressStr)
                            Crashlytics.logException(RuntimeException("Failed reading progress string", e))
                            progressStrSet.remove(progressStr)
                            failures++
                        }

                    }
                }
                if (failures > 0) {
                    PrefsUtils.setProgress(progressStrSet)
                }
            }
            return mPuzzleProgress!!
        }

    val progressJson: String
        get() {
            val progressList = progress
            val resultList = PuzzleProgressState()
            for (i in 0 until progressList.size()) {
                resultList.addProgress(progressList.valueAt(i))
            }
            resultList.setCurrentId(getIdFromIndex(currentIndex))
            return mGson.toJson(resultList)
        }

    init {
        if (context != null) {
            val `is` = context.assets.open(ASSET_FILENAME)
            readStream(`is`)
        } else {
            val `is` = this.javaClass.classLoader.getResourceAsStream("assets/$ASSET_FILENAME")
            if (`is` != null) {
                readStream(`is`)
                `is`.close()
            }
        }
    }

    private fun readStream(`is`: InputStream) {
        var start = System.nanoTime()
        all = mGson.fromJson(InputStreamReader(`is`), Array<Puzzle>::class.java)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("readStream: parsed Json in %.2fms", (System.nanoTime() - start) / 1000000f))
            start = System.nanoTime()
        }
        var index = 0
        var nextId = 0
        mPuzzleIds = HashMap()
        if (BuildConfig.DEBUG) {
            val puzzles = LinkedList<Puzzle>()
            if (CryptogramView.ENABLE_HYPHENATION) {
                puzzles.add(Puzzle.Mock(
                        "AAAAAAAA\u00ADBBB\u00ADCCCCCCC\u00ADDDDDDDDDD\u00ADEEEE\u00ADFFFFFFFFFFFFF\u00ADGGGG\u00ADHHHHHHHHHH\u00ADIIIIII.", null, null))
                puzzles.add(Puzzle.Mock(
                        "JJJJJJJJ KKK LLLLLLL MMMMMMMMM NNNN OOOOOOOOOOOOO PPPP\u00ADQQQQQQQQQQ\u00ADRRRRRR.", null, null))
            }
            puzzles.addAll(Arrays.asList(*all))
            all = puzzles.toTypedArray()
            if (BuildConfig.DEBUG) {
                Log.d(TAG, String.format("readStream: added test puzzles in %.2fms", (System.nanoTime() - start) / 1000000f))
                start = System.nanoTime()
            }
        }
        for (puzzle in all) {
            var id = puzzle.id
            if (id == 0) {
                while (mPuzzleIds!![nextId] != null) {
                    // Locate the next vacant spot
                    nextId++
                }
                id = nextId
                puzzle.id = id
            }
            if (id > mLastPuzzleId) {
                mLastPuzzleId = id
            }
            mPuzzleIds!![id] = index
            index++
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("readStream: performed ID mapping in %.2fms", (System.nanoTime() - start) / 1000000f))
        }
    }

    private fun getIndexFromId(id: Int): Int {
        val index = mPuzzleIds!![id] ?: return -1
        return index
    }

    private fun getIdFromIndex(index: Int): Int {
        return all[index].id
    }

    fun setCurrentId(id: Int) {
        mCurrentIndex = getIndexFromId(id)
        PrefsUtils.setCurrentId(id)
    }

    operator fun get(index: Int): Puzzle? {
        return if (index < 0 || index >= all.size) {
            null
        } else all[index]
    }

    fun getByNumber(number: Int): Puzzle? {
        for (puzzle in all) {
            if (puzzle.number == number) {
                return puzzle
            }
        }
        return null
    }

    fun setProgress(puzzleId: Int, progress: PuzzleProgress?) {
        // Ensure that we've loaded all puzzle progress
        mPuzzleProgress!!.put(puzzleId, progress)
    }

    /**
     * Resets all puzzles.
     */
    fun resetAll() {
        for (puzzle in all) {
            puzzle.reset(false)
        }
        mPuzzleProgress!!.clear()
        saveLocal()

        // Jump back to the first puzzle
        currentIndex = 0
        EventProvider.postEventDelayed(
                PuzzleEvent.PuzzleResetEvent(current))
    }

    fun load(googleApiClient: GoogleApiClient?,
             snapshotMetadata: SnapshotMetadata,
             onLoadResult: SavegameManager.OnLoadResult?) {
        if (googleApiClient == null || !googleApiClient.isConnected) {
            // No connection; cannot load
            onLoadResult?.onLoadFailure()
            return
        }
        object : AsyncTask<Void, Void, Snapshot>() {

            override fun doInBackground(vararg voids: Void): Snapshot? {
                return SavegameManager.load(googleApiClient, snapshotMetadata.uniqueName)
            }

            override fun onPostExecute(snapshot: Snapshot?) {
                if (snapshot == null) {
                    Log.e(TAG, "game state failed loading from Google Play Games")
                    onLoadResult?.onLoadFailure()
                } else {
                    Log.d(TAG, "game state loaded from Google Play Games")
                    onLoadResult?.onLoadSuccess()
                }
            }

        }.execute()
    }

    fun save(googleApiClient: GoogleApiClient?,
             onSaveResult: SavegameManager.OnSaveResult?) {
        if (googleApiClient == null || !googleApiClient.isConnected) {
            // No connection; cannot save
            onSaveResult?.onSaveFailure()
            return
        }
        // Create a new snapshot named with a unique string
        object : AsyncTask<Void, Void, SnapshotMetadata>() {

            override fun doInBackground(vararg voids: Void): SnapshotMetadata {
                return SavegameManager.save(googleApiClient)
            }

            override fun onPostExecute(snapshot: SnapshotMetadata?) {
                if (snapshot == null) {
                    Log.e(TAG, "game state failed saving to Google Play Games")
                    onSaveResult?.onSaveFailure()
                } else {
                    Log.d(TAG, "game state saved to Google Play Games")
                    onSaveResult?.onSaveSuccess()
                }
            }

        }.execute()
    }

    fun saveLocal() {
        val progressList = progress
        // Now store everything
        val progressStrSet = LinkedHashSet<String>()
        for (i in 0 until progressList.size()) {
            progressStrSet.add(mGson.toJson(progressList.valueAt(i)))
        }
        PrefsUtils.setProgress(progressStrSet)
    }

    fun setProgressFromJson(json: String) {
        val state = mGson.fromJson(json, PuzzleProgressState::class.java)
        if (state != null) {
            for (puzzleProgress in state.progress) {
                if (puzzleProgress != null) {
                    val puzzleId = puzzleProgress.id
                    setProgress(puzzleId, puzzleProgress)
                    val index = getIndexFromId(puzzleId)
                    if (index >= 0) {
                        all[index].unload()
                    }
                }
            }
            val currentId = state.currentId
            if (currentId != null) {
                // Select the current puzzle by its ID
                setCurrentId(currentId)
            } else {
                // Select the first puzzle
                currentIndex = 0
            }
        }
        saveLocal()
    }

    companion object {

        private val TAG = PuzzleProvider::class.java.simpleName

        private val ASSET_FILENAME = "cryptograms.json"

        private var sInstance: PuzzleProvider? = null

        fun getInstance(context: Context?): PuzzleProvider {
            if (sInstance == null) {
                try {
                    sInstance = PuzzleProvider(context)
                } catch (e: IOException) {
                    Log.e(TAG, "could not read puzzle file", e)
                    Toast.makeText(context, "Could not find any puzzles", Toast.LENGTH_LONG).show()
                }

            }
            return sInstance!!
        }
    }

}
