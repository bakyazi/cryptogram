package com.pixplicity.cryptogram.models;

import android.support.annotation.NonNull;

import java.util.LinkedList;

public class Word {

    private static final String SOFT_HYPHEN = "\u00AD";

    private final String[] mParts;

    public static Word[] from(String[] wordStrings) {
        Word[] words = new Word[wordStrings.length];
        for (int i = 0; i < wordStrings.length; i++) {
            words[i] = new Word(wordStrings[i]);
        }
        return words;
    }

    public Word(@NonNull String wordString) {
        mParts = wordString.split(SOFT_HYPHEN);
    }

    public String getForSpace(int chars) {
        StringBuilder wordPart = null;
        int remaining = mParts.length;
        for (String part : mParts) {
            remaining--;
            int length = part.length();
            if (remaining > 0) {
                length++;
            }
            if (length <= chars - (wordPart == null ? 0 : wordPart.length())) {
                if (wordPart == null) {
                    wordPart = new StringBuilder(part);
                } else {
                    wordPart.append(part);
                }
            } else {
                // Nothing else fits; quit
                break;
            }
        }
        if (wordPart != null && remaining > 0) {
            wordPart.append(SOFT_HYPHEN);
        }
        return wordPart == null ? null : wordPart.toString();
    }

    public String getWhole() {
        StringBuilder whole = new StringBuilder();
        for (String part : mParts) {
            whole.append(part);
        }
        return whole.toString();
    }

    public int fillForSpace(LinkedList<String> wordParts, int lineRemaining,
                            int lineWidthInChars) {
        int i = 0;
        int remaining = mParts.length;
        while (remaining > 0) {
            StringBuilder wordPart = null;
            for (; i < mParts.length; i++) {
                String part = mParts[i];
                remaining--;
                int length = part.length();
                if (remaining > 0) {
                    length++;
                }
                if (length <= lineRemaining - (wordPart == null ? 0 : wordPart.length())) {
                    if (wordPart == null) {
                        wordPart = new StringBuilder(part);
                    } else {
                        wordPart.append(part);
                    }
                } else {
                    // Nothing else fits; quit
                    break;
                }
            }
            if (wordPart != null && remaining > 0) {
                wordPart.append(SOFT_HYPHEN);
            }
            if (wordPart == null) {
                if (lineRemaining == lineWidthInChars) {
                    // Edge case: nothing fits at all, allow it to exceed the line
                    wordParts.add(getWhole());
                    lineRemaining = 0;
                    break;
                }
                lineRemaining = lineWidthInChars;
            } else {
                String part = wordPart.toString();
                wordParts.add(part);
                if (remaining < 0) {
                    lineRemaining = lineWidthInChars;
                } else {
                    lineRemaining -= part.length();
                }
            }
        }
        return lineRemaining;
    }

    public int length() {
        int length = 0;
        for (String part : mParts) {
            length += part.length();
        }
        return length;
    }

}
