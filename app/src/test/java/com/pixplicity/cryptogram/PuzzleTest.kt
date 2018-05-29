package com.pixplicity.cryptogram

import android.annotation.SuppressLint
import android.util.Log
import com.pixplicity.cryptogram.models.Puzzle
import com.pixplicity.cryptogram.models.PuzzleProgress
import com.pixplicity.cryptogram.stringsimilarity.Levenshtein
import com.pixplicity.cryptogram.utils.PuzzleProvider
import com.pixplicity.cryptogram.views.CryptogramView
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(PowerMockRunner::class)
@PrepareForTest(Log::class)
class PuzzleTest {

    companion object {
        private val VERBOSE = false
    }

    @Before
    fun setup() {
        PowerMockito.mockStatic(Log::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun validProvider() {
        println("Total puzzles: " + PuzzleProvider.getInstance(null).count)
    }

    @Test
    @Throws(Exception::class)
    fun validCryptogramMapping() {
        for (seed in 0L..100L - 1) {
            if (VERBOSE) {
                print("seed $seed:")
            }
            PuzzleProgress.setRandomSeed(seed)
            val puzzle = Puzzle.Mock()
            val progress = PuzzleProgress()
            val mapping = progress.getCharMapping(puzzle)
            for (key in mapping.keys) {
                val value = mapping[key]
                if (VERBOSE) {
                    print("  $key/$value")
                }
                if (key == value) {
                    throw AssertionError("Key and value maps to same character for seed $seed")
                }
            }
            if (VERBOSE) {
                println()
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun noEmptyOrDuplicateCryptograms() {
        val levenshtein = Levenshtein()
        @SuppressLint("UseSparseArrays") val hashes = HashMap<Int, Puzzle>()
        val errors = ArrayList<String>()
        for (puzzle in PuzzleProvider.getInstance(null).all) {
            val id = puzzle.id
            val text = puzzle.text
            val author = puzzle.author
            val topic = puzzle.topic
            if (VERBOSE) {
                println("puzzle $puzzle")
            }
            // Ensure there's content
            if (text!!.trim { it <= ' ' }.length == 0) {
                errors.add("No content: $puzzle")
            } else {
                // Ensure there aren't single quotes (replace with ’)
                if (text.indexOf('\'') >= 0) {
                    errors.add("Contains single quote; replace with '‘' or '’': $puzzle")
                }
                // Ensure there aren't single quotes (replace with “/”)
                if (text.indexOf('"') >= 0) {
                    errors.add("Contains single quote; replace with '“' or '”': $puzzle")
                }
                // Ensure there aren't simple hyphens (replace with —)
                if (text.replace("[-–] ".toRegex(), "")
                                .replace(" [-–]".toRegex(), "")
                                .replace("--".toRegex(), "")
                                .length < text.length) {
                    errors.add("Contains simple hyphen; replace with '—': $puzzle")
                }
                // Ensure em dashes are surrounded with spaces
                if (text.replace("[\\w]—".toRegex(), "")
                                .replace("—[\\w]".toRegex(), "")
                                .length < text.length) {
                    errors.add("Contains em dash without surrounding spaces: $puzzle")
                }
                // Ensure there aren't simple hyphens (replace with —)
                if (text.contains("..")) {
                    errors.add("Contains expanded ellipsis; replace with '…': $puzzle")
                }
                // Ensure there aren't simple hyphens (replace with —)
                val given = puzzle.given
                if (given != null && given != given.toUpperCase(Locale.ENGLISH)) {
                    errors.add("Contains lowercase given characters: $puzzle")
                }
                // Ensure there aren't duplicates
                for (otherPuzzle in hashes.values) {
                    if (otherPuzzle.text == null) {
                        errors.add("Puzzle has null text: $otherPuzzle")
                        continue
                    }
                    val distance = levenshtein.distance(text, otherPuzzle.text!!)
                    if (distance < 10) {
                        errors.add("Levenshtein distance of $puzzle is $distance to $otherPuzzle")
                    }
                }
                if (CryptogramView.ENABLE_HYPHENATION) {
                    for (origWord in puzzle.words) {
                        if (origWord == null) {
                            errors.add("Null word in puzzle")
                            continue
                        }
                        val word = origWord.replace("[^a-zA-Z\u00AD\\-]".toRegex(), "")
                        for (wordPart in word.split("[\u00AD\\-]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                            if (wordPart.length > 8) {
                                errors.add("Contains word of length >8 without hyphen or soft-hyphen ('\u00AD'): '$word' in $puzzle")
                            }
                        }
                    }
                }
            }
            if (!puzzle.isInstruction) {
                // Ensure there's an author
                if (author == null || author.trim { it <= ' ' }.length < 3) {
                    errors.add("No author for $puzzle")
                }
            }
            if (topic != null && topic.replace("[^\\s\\w]".toRegex(), "")
                            .length < topic.length) {
                errors.add("Contains invalid character in topic for $puzzle")
            }
            hashes[id] = puzzle
            if (errors.size > 10) {
                // Fail early
                break
            }
        }
        if (errors.size > 0) {
            for (i in 0 until Math.min(10, errors.size)) {
                System.err.println("-\t" + errors[i])
            }
            if (errors.size > 10) {
                System.err.println("-\t(and more)")
            }
            throw AssertionError("Errors regarding puzzle quality")
        }
    }

    @Test
    fun hyphenation() {
        if (CryptogramView.ENABLE_HYPHENATION) {
            val puzzle = PuzzleProvider.getInstance(null)[0]
            val lineWidthInChars = 12
            for (i in 0 until lineWidthInChars) {
                print('=')
            }
            println()
            for (wordPart in puzzle!!.getWordsForLineWidth(lineWidthInChars)) {
                println(wordPart)
            }
        }
    }

}
