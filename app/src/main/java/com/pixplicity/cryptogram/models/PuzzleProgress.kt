package com.pixplicity.cryptogram.models

import android.os.Bundle
import android.util.Log
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.LevelEndEvent
import com.crashlytics.android.answers.LevelStartEvent
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.annotations.SerializedName
import com.pixplicity.cryptogram.CryptogramApp
import com.pixplicity.cryptogram.events.PuzzleEvent
import com.pixplicity.cryptogram.utils.EventProvider
import com.pixplicity.cryptogram.utils.PrefsUtils
import java.util.*

class PuzzleProgress {

    @SerializedName("id")
    var id: Int = 0

    /**
     * Maps from correct characters to user inputted characters.
     */
    @SerializedName("user")
    private var mUserChars: HashMap<Char, Char>? = null

    /**
     * Maps from correct characters to randomized characters.
     */
    @SerializedName("shuffle")
    private var mCharMapping: HashMap<Char, Char>? = null

    /**
     * Temporary ordered list of characters.
     */
    @Transient
    private var mCharacterList: ArrayList<Char>? = null

    /**
     * List of characters that have been revealed.
     */
    @SerializedName("revealed")
    private var mRevealed: MutableList<Char>? = null

    /**
     * Timestamp of (corrected) puzzle start time.
     */
    @SerializedName("start_time")
    private var mStartTime: Long? = null

    /**
     * Timestamp of puzzle completion time, or time at which playing was stopped.
     */
    @SerializedName("stop_time")
    private var mStopTime: Long? = null

    /**
     * Total times characters were input by the user.
     */
    @SerializedName("inputs")
    private var mInputs: Int? = null

    /**
     * Total times characters were input by the user.
     */
    @SerializedName("completed")
    private var mCompleted: Boolean? = null

    /**
     * Whether this cryptogram was played using hints.
     */
    @SerializedName("had_hints")
    private val mHadHints: Boolean? = null

    /**
     * Total times mistakes were revealed by the user.
     */
    @SerializedName("revealed_mistakes")
    private var mRevealedMistakes: Int? = null

    @Transient
    private var mPlaying: Boolean? = null
    @Transient
    private var mSanitized: Boolean = false

    val reveals: Int
        @Synchronized get() = if (mRevealed == null) 0 else mRevealed!!.size

    val revealedMistakes: Int?
        @Synchronized get() = if (mRevealedMistakes == null) 0 else mRevealedMistakes

    val isPlaying: Boolean
        @Synchronized get() = mPlaying != null && mPlaying!!

    val startTime: Long
        @Synchronized get() = if (mStartTime == null || mStartTime == 0L) {
            0
        } else mStartTime!!

    val durationMs: Long
        @Synchronized get() {
            if (mStartTime == null || mStartTime == 0L) {
                return 0
            }
            return if (!isPlaying) {
                if (mStopTime == null || mStopTime == 0L) {
                    0
                } else mStopTime!! - mStartTime!!
            } else System.currentTimeMillis() - mStartTime!!
        }

    constructor() {}

    constructor(puzzle: Puzzle) {
        id = puzzle.id
        // Apply mappings for any given characters
        val given = puzzle.given
        if (given != null) {
            for (j in 0 until given.length) {
                val c = given[j]
                setUserChar(puzzle, c, c)
            }
        }
    }

    @Synchronized
    fun getCharMapping(puzzle: Puzzle): HashMap<Char, Char> {
        // Ensure we've attempted to load the data
        if (mCharMapping == null) {
            // Populate from the character list
            getCharacterList(puzzle)
            val r: Random
            if (sRandomSeed == null) {
                r = Random()
            } else {
                r = Random(sRandomSeed!!)
            }
            val alphabet = ArrayList<Char>(ALPHABET.size)
            for (c in ALPHABET) {
                alphabet.add(c)
            }
            var remaining = mCharMapping!!.size
            // Figure out what the last character would be
            var lastChar: Char = 0.toChar()
            for (c in mCharMapping!!.keys) {
                lastChar = c
            }
            // Create mappings
            for (c in mCharMapping!!.keys) {
                var i = r.nextInt(alphabet.size)
                var mappedC = alphabet[i]
                if (mappedC == c) {
                    i = (i + 1) % alphabet.size
                }
                // Special case for the last two mappings to ensure a solution
                if (remaining == 2) {
                    // Check if by selecting this character, we have a solution for the last character
                    val j = (i + 1) % alphabet.size
                    if (alphabet[j] == lastChar) {
                        // We don't; select the other instead
                        i = (i + 1) % alphabet.size
                    }
                }
                mappedC = alphabet[i]
                alphabet.removeAt(i)
                mCharMapping!![c] = mappedC
                remaining--
            }
            // We've generated the mappings, save it
            puzzle.save()
        }
        return mCharMapping!!
    }

    @Synchronized
    fun getCharacterList(puzzle: Puzzle): ArrayList<Char> {
        if (mCharacterList == null || mCharMapping == null) {
            // May need to be regenerated as the field is transient
            var resetCharMapping = false
            if (mCharMapping == null) {
                mCharMapping = HashMap()
                resetCharMapping = true
            }
            mCharacterList = ArrayList()
            for (word in puzzle.words) {
                if (word == null) continue
                for (i in 0 until word.length) {
                    val c = Character.toUpperCase(word[i])
                    if (puzzle.isInputChar(c)) {
                        if (resetCharMapping) {
                            mCharMapping!![c] = 0.toChar()
                        }
                        if (!mCharacterList!!.contains(c)) {
                            mCharacterList!!.add(c)
                        }
                    }
                }
            }
        }
        return mCharacterList!!
    }

    @Synchronized
    fun setCharMapping(charMapping: HashMap<Char, Char>) {
        mCharMapping = charMapping
    }

    @Synchronized
    private fun getUserCharsMapping(puzzle: Puzzle): HashMap<Char, Char> {
        if (mUserChars == null) {
            mUserChars = HashMap()
            for (word in puzzle.words) {
                if (word == null) continue
                for (i in 0 until word.length) {
                    val c = Character.toUpperCase(word[i])
                    if (puzzle.isInputChar(c)) {
                        mUserChars!![c] = 0.toChar()
                    }
                }
            }
        }
        return mUserChars!!
    }

    @Synchronized
    fun getUserChars(puzzle: Puzzle): Collection<Char> {
        return getUserCharsMapping(puzzle).values
    }

    @Synchronized
    fun getUserChar(puzzle: Puzzle, c: Char): Char? {
        return getUserCharsMapping(puzzle)[c]
    }

    /**
     * Sets a selected hint to a character.
     *
     * @return If the character was changed from a previous assignment; i.e. 'corrected' by the user.
     */
    @Synchronized
    fun setUserChar(puzzle: Puzzle, selectedCharacter: Char,
                    c: Char): Boolean {
        var changed = false
        var previousChar: Char? = getUserCharsMapping(puzzle)[selectedCharacter]
        if (previousChar == null) {
            previousChar = 0.toChar()
        }
        val userChar = Character.toUpperCase(c)
        if (previousChar != userChar && userChar.toInt() != 0) {
            if (previousChar.toInt() != 0) {
                // User made a correction
                changed = true
            }
            (mInputs ?: 0).let {
                mInputs = it + 1
            }
        }
        getUserCharsMapping(puzzle)[selectedCharacter] = userChar
        return changed
    }

    private fun getUserCharsCount(puzzle: Puzzle): Int {
        var count = 0
        val userCharsMapping = getUserCharsMapping(puzzle)
        for (c in userCharsMapping.keys) {
            if (puzzle.isGiven(c)) {
                // This character is given by the puzzle
                continue
            }
            val userChar = userCharsMapping[c]
            if (userChar != null && userChar.toInt() != 0) {
                // This is a user filled character
                count++
            }
        }
        return count
    }

    @Synchronized
    fun getExcessCount(puzzle: Puzzle): Int {
        return if (mInputs == null) {
            -1
        } else mInputs!! - getUserCharsCount(puzzle)
        // Start with total number of inputs
    }

    @Synchronized
    fun isInProgress(puzzle: Puzzle): Boolean {
        return if (isCompleted(puzzle)) {
            false
        } else getUserCharsCount(puzzle) > 0
        // Dumb approach of simply checking on inputs
    }

    @Synchronized
    fun isCompleted(puzzle: Puzzle): Boolean {
        if (mCompleted != true) {
            mCompleted = true
            val userChars = getUserCharsMapping(puzzle)
            if (userChars.size <= 5) {
                Log.w(TAG, "User character mapping has an unexpectedly small size (that's what she said)")
                userChars.clear()
                mCompleted = false
            } else {
                for (character in userChars.keys) {
                    // In order to be correct, the key and value must be identical
                    if (character != null && character !== userChars[character] && !puzzle.isGiven(character)) {
                        mCompleted = false
                        break
                    }
                }
            }
            if (mCompleted!!) {
                onCompleted(puzzle)
                onPause()
            }
        }
        return mCompleted!!
    }

    @Synchronized
    fun reveal(c: Char) {
        if (mRevealed == null) {
            mRevealed = ArrayList()
        } else if (mRevealed!!.contains(c)) {
            return
        }
        mRevealed!!.add(c)
    }

    @Synchronized
    fun isRevealed(c: Char): Boolean {
        return mRevealed != null && mRevealed!!.contains(c)
    }

    @Synchronized
    fun incrementRevealedMistakes() {
        (mRevealedMistakes ?: 0).let {
            mRevealedMistakes = it + 1
        }
    }

    @Synchronized
    fun onResume(puzzle: Puzzle) {
        if (!isPlaying && !isCompleted(puzzle)) {
            if (mStartTime == null || mStartTime == 0L) {
                onStart(puzzle)
            }
            // Only resume playing if the puzzle wasn't completed
            setTimes()
            mPlaying = true
        }
    }

    @Synchronized
    fun onPause() {
        if (isPlaying) {
            setTimes()
            mPlaying = false
        }
    }

    @Synchronized
    private fun onStart(puzzle: Puzzle) {
        run {
            // Analytics
            val puzzleNumber = puzzle.number
            val puzzleId = puzzle.id.toString()
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.LEVEL, puzzleId)
            CryptogramApp.instance!!.firebaseAnalytics.logEvent(CryptogramApp.EVENT_LEVEL_START, bundle)
            Answers.getInstance().logLevelStart(
                    LevelStartEvent()
                            .putLevelName("Puzzle #$puzzleNumber"))
        }

        EventProvider.postEvent(
                PuzzleEvent.PuzzleStartedEvent(puzzle))
    }

    @Synchronized
    private fun onCompleted(puzzle: Puzzle) {
        run {
            // Analytics
            val puzzleNumber = puzzle.number
            val score = getScore(puzzle)
            val puzzleId = puzzle.id.toString()
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.LEVEL, puzzleId)
            val event = LevelEndEvent()
                    .putLevelName("Puzzle #$puzzleNumber")
                    .putSuccess(true)
            if (score != null) {
                bundle.putFloat(FirebaseAnalytics.Param.SCORE, score)
                event.putScore(score)
            }
            CryptogramApp.instance!!.firebaseAnalytics.logEvent(CryptogramApp.EVENT_LEVEL_END, bundle)
            Answers.getInstance().logLevelEnd(event)
        }

        EventProvider.postEventDelayed(PuzzleEvent.PuzzleCompletedEvent(puzzle))
    }

    @Synchronized
    private fun setTimes() {
        val stopTime = System.currentTimeMillis()
        mStartTime = stopTime - durationMs
        mStopTime = stopTime
    }

    @Synchronized
    fun hasScore(puzzle: Puzzle): Boolean {
        val duration = puzzle.durationMs
        return if (duration == 0L) {
            false
        } else true
    }

    @Synchronized
    fun getScore(puzzle: Puzzle): Float? {
        if (!hasScore(puzzle)) {
            return null
        }
        val duration = durationMs / 1000f
        var score = 1f
        score = addScore(score, TARGET_DURATION / duration)
        score = addScore(score, Math.pow(0.75, revealedMistakes!!.toDouble()).toFloat())
        score = addScore(score, (MAX_REVEALS - reveals) / 6f)
        // Never return a score below 0.0% or above 100.0%
        return Math.max(0f, Math.min(1f, score))
    }

    private fun addScore(score: Float, addition: Float): Float {
        var addition = addition
        addition = Math.max(-1f, Math.min(1f, addition))
        return if (score < 0 && addition < 0) {
            score * -addition
        } else score * addition
    }

    @Synchronized
    fun sanitize(puzzle: Puzzle) {
        if (mSanitized) {
            return
        }
        mSanitized = true
        // Ensure that only input characters have user mappings
        val i = getUserCharsMapping(puzzle).keys.iterator()
        while (i.hasNext()) {
            val c = i.next()
            if (c == null || !puzzle.isInputChar(c)) {
                i.remove()
            }
        }
        if (mStartTime == null || mStopTime == null) {
            // This is a hacky fix for dealing with broken completion states
            mCompleted = false
        }
        val characterList = getCharacterList(puzzle)
        val charMapping = getCharMapping(puzzle)
        Log.w(TAG, "check for invalid mappings in $puzzle")
        for (c in characterList) {
            if (charMapping[c]?.toInt() ?: 0 == 0) {
                // Whoops! Puzzle has a broken character mapping
                mUserChars = null
                mCharMapping = null
                mCompleted = false
                getCharMapping(puzzle)
                Log.w(TAG, "invalid character mapping for $puzzle; reset mappings")
                break
            }
        }
        // Apply mappings for any revealed characters
        if (mRevealed != null) {
            for (c in mRevealed!!) {
                setUserChar(puzzle, c, c)
            }
        }
    }

    @Synchronized
    fun reset(puzzle: Puzzle?) {
        mUserChars = null
        mCharMapping = null
        mStartTime = null
        mStopTime = null
        mCompleted = null
        if (isPlaying) {
            mPlaying = null
            if (puzzle != null) {
                onResume(puzzle)
            }
        }
        if (puzzle != null) {
            sanitize(puzzle)
        }
    }

    companion object {

        private val TAG = PuzzleProgress::class.java.simpleName

        private val ALPHABET = ArrayList<Char>(26)

        private val TARGET_DURATION = 3 * 60f
        private val MAX_REVEALS = 6f
        private val MAX_EXCESS_INPUT = 26f

        init {
            var i: Int = 'A'.toInt()
            while (i <= 'Z'.toInt()) {
                ALPHABET.add(i.toChar())
                i++
            }
        }

        private var sRandomSeed: Long? = null

        fun setRandomSeed(randomSeed: Long?) {
            sRandomSeed = randomSeed
        }
    }

}
