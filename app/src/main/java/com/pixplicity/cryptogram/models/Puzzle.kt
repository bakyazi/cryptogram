package com.pixplicity.cryptogram.models

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.pixplicity.cryptogram.CryptogramApp
import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.utils.PrefsUtils
import com.pixplicity.cryptogram.utils.PuzzleProvider
import com.pixplicity.cryptogram.utils.StringUtils
import java.util.*

open class Puzzle {

    protected var mIsMock: Boolean = false

    @SerializedName("id")
    var id: Int = 0

    @SerializedName("number")
    protected var mNumber: Int? = null

    @SerializedName("text")
    var text: String? = null
        protected set

    @SerializedName("author")
    var author: String? = null
        protected set

    @SerializedName("topic")
    var topic: String? = null
        protected set

    @SerializedName("given")
    var given: String? = null
        get() =
            if (PrefsUtils.hardcoreMode) null
            else field
        protected set

    @SerializedName("noscore")
    var isNoScore: Boolean = false
        protected set

    @Transient
    private var mWords: Array<String?>? = null

    private var mProgress: PuzzleProgress? = null
    private var mLoadedProgress: Boolean = false

    var number: Int
        get() = if (mNumber == null) {
            id + 1
        } else mNumber!!
        set(number) {
            mNumber = number
        }

    // FIXME really this should be Array<String>
    val words: Array<String?>
        get() {
            if (mWords == null) {
                if (text == null || text!!.length == 0) {
                    mWords = arrayOfNulls(0)
                } else {
                    mWords = text!!.split("\\s".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                }
            }
            return mWords!!
        }

    // Ensure we've attempted to load the data
    val progress: PuzzleProgress
        get() {
            load()
            if (mProgress == null) {
                mProgress = PuzzleProgress(this)
            }
            return mProgress!!
        }

    val charMapping: HashMap<Char, Char>
        get() = progress.getCharMapping(this)

    val characterList: ArrayList<Char>
        get() = progress.getCharacterList(this)

    val userChars: Collection<Char>
        get() = progress.getUserChars(this)

    val mistakeCount: Int
        get() = progress.getMistakeCount(this)

    val isInstruction: Boolean
        get() = id < 0

    val isInProgress: Boolean
        get() = progress.isInProgress(this)

    val isCompleted: Boolean
        get() = progress.isCompleted(this)

    fun checkCompleted() = progress.checkCompleted(this)

    val reveals: Int
        get() = progress.reveals

    val excessCount: Int
        get() = progress.getExcessCount(this)

    /**
     * Returns the duration of the user's play time on this puzzle in milliseconds.
     */
    // Don't measure the duration for puzzles with given characters
    val durationMs: Long
        get() = if (isNoScore) {
            0
        } else progress.durationMs

    val score: Float?
        get() = if (isInstruction) {
            null
        } else progress.getScore(this)

    class Mock @JvmOverloads constructor(text: String = "Bright vixens jump; dozy fowl quack.", author: String? = "Paul Lammertsma", topic: String? = "Other") : Puzzle() {

        init {
            this.text = text
            this.author = author
            this.topic = topic
            mIsMock = true
        }

    }

    /**
     * Creates a mock cryptogram.
     */

    fun getTitle(context: Context): String {
        if (isInstruction) {
            return context.getString(R.string.puzzle_number_instruction)
        } else if (isNoScore) {
            return context.getString(R.string.puzzle_number_practice, number)
        }
        return context.getString(R.string.puzzle_number, number)
    }

    fun getWordsForLineWidth(lineWidthInChars: Int): Array<String> {
        val wordParts = LinkedList<String>()
        val words = Word.from(words)
        var lineRemaining = lineWidthInChars
        for (word in words) {
            if (word == null) continue
            lineRemaining = word.fillForSpace(wordParts, lineRemaining, lineWidthInChars)
        }
        return wordParts.toTypedArray()
    }

    fun getCharacterForMapping(mappedChar: Char): Char? {
        return charMapping[mappedChar]
    }

    fun getMappedCharacter(inputChar: Char): Char? {
        val charMapping = charMapping
        for (character in charMapping.keys) {
            if (charMapping[character] == inputChar) {
                return character
            }
        }
        return null
    }

    fun isUserCharInput(inputChar: Char): Boolean {
        return userChars.contains(inputChar) || isGiven(inputChar)
    }

    fun getUserChar(c: Char): Char? {
        return progress.getUserChar(this, c)
    }

    fun setUserChar(selectedCharacter: Char, c: Char): Boolean {
        val changed = progress.setUserChar(this, selectedCharacter, c)
        save()
        return changed
    }

    fun hasUserChars(): Boolean {
        for (c in userChars) {
            if (c.toInt() != 0) {
                return true
            }
        }
        return false
    }

    fun isInputChar(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z'
    }

    fun isGiven(matchChar: Char): Boolean {
        given?.let {
            for (j in 0 until it.length) {
                val c = it[j]
                if (c == matchChar) {
                    return true
                }
            }
        }
        return false
    }

    fun reveal(c: Char) {
        if (!isInputChar(c)) {
            // Not applicable
            return
        }
        progress.reveal(c)
        save()
    }

    fun revealedMistakes() {
        progress.incrementRevealedMistakes()
        save()
    }

    fun revealPuzzle() {
        val charMapping = charMapping
        for (c in charMapping.keys) {
            progress.setUserChar(this, c, c)
        }
        save()
    }

    fun isRevealed(c: Char): Boolean {
        return if (given != null && given!!.indexOf(c) > -1) {
            true
        } else progress.isRevealed(c)
    }

    fun onResume() {
        progress.onResume(this)
    }

    fun onPause() {
        progress.onPause()
        save()
    }

    fun unload() {
        mLoadedProgress = false
    }

    private fun load() {
        if (!mLoadedProgress && !mIsMock) {
            mProgress = PuzzleProvider.getInstance(CryptogramApp.instance).progress.get(id)
            if (mProgress != null) {
                mProgress!!.sanitize(this)
            }
        }
        mLoadedProgress = true
    }

    fun reset(save: Boolean) {
        progress.reset(if (save) this else null)
        if (save) {
            save()
        }
    }

    fun save() {
        if (!mIsMock) {
            val puzzleProvider = PuzzleProvider.getInstance(CryptogramApp.instance)
            val progress = progress
            puzzleProvider.setProgress(progress.id, progress)
            puzzleProvider.saveLocal()
        }
    }

    override fun toString(): String {
        return "#" + id + ": " + text!!.length + " chars, author '" + author + "' (“" + StringUtils.ellipsize(text!!.replace("\u00AD", ""), 40) + "”)"
    }

}
