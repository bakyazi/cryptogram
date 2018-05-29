package com.pixplicity.cryptogram.models

import java.util.LinkedList

class Word(wordString: String) {

    companion object {

        private val SOFT_HYPHEN = "\u00AD"

        fun from(wordStrings: Array<String?>): Array<Word?> {
            val words = arrayOfNulls<Word>(wordStrings.size)
            for (i in wordStrings.indices) {
                val wordString = wordStrings[i] ?: continue
                words[i] = Word(wordString)
            }
            return words
        }
    }

    private val mParts: Array<String>

    val whole: String
        get() {
            val whole = StringBuilder()
            for (part in mParts) {
                whole.append(part)
            }
            return whole.toString()
        }

    init {
        mParts = wordString.split(SOFT_HYPHEN.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    fun getForSpace(chars: Int): String? {
        var wordPart: StringBuilder? = null
        var remaining = mParts.size
        for (part in mParts) {
            remaining--
            var length = part.length
            if (remaining > 0) {
                length++
            }
            if (length <= chars - (if (wordPart == null) 0 else wordPart.length)) {
                if (wordPart == null) {
                    wordPart = StringBuilder(part)
                } else {
                    wordPart.append(part)
                }
            } else {
                // Nothing else fits; quit
                break
            }
        }
        if (wordPart != null && remaining > 0) {
            wordPart.append(SOFT_HYPHEN)
        }
        return if (wordPart == null) null else wordPart.toString()
    }

    fun fillForSpace(wordParts: LinkedList<String>, lineRemaining: Int,
                     lineWidthInChars: Int): Int {
        var lineRemaining = lineRemaining
        var i = 0
        var remaining = mParts.size
        while (remaining > 0) {
            var wordPart: StringBuilder? = null
            while (i < mParts.size) {
                val part = mParts[i]
                remaining--
                var length = part.length
                if (remaining > 0) {
                    length++
                }
                if (length <= lineRemaining - (if (wordPart == null) 0 else wordPart.length)) {
                    if (wordPart == null) {
                        wordPart = StringBuilder(part)
                    } else {
                        wordPart.append(part)
                    }
                } else {
                    // Nothing else fits; quit
                    break
                }
                i++
            }
            if (wordPart != null && remaining > 0) {
                wordPart.append(SOFT_HYPHEN)
            }
            if (wordPart == null) {
                if (lineRemaining == lineWidthInChars) {
                    // Edge case: nothing fits at all, allow it to exceed the line
                    wordParts.add(whole)
                    lineRemaining = 0
                    break
                }
                lineRemaining = lineWidthInChars
            } else {
                val part = wordPart.toString()
                wordParts.add(part)
                if (remaining < 0) {
                    lineRemaining = lineWidthInChars
                } else {
                    lineRemaining -= part.length
                }
            }
        }
        return lineRemaining
    }

    fun length(): Int {
        var length = 0
        for (part in mParts) {
            length += part.length
        }
        return length
    }

}
