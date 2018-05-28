package com.pixplicity.cryptogram.models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.utils.PuzzleProvider;
import com.pixplicity.cryptogram.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class Puzzle {

    protected boolean mIsMock;

    @SerializedName("id")
    protected int mId;

    @SerializedName("number")
    protected Integer mNumber;

    @SerializedName("text")
    protected String mText;

    @SerializedName("author")
    protected String mAuthor;

    @SerializedName("topic")
    protected String mTopic;

    @SerializedName("given")
    protected String mGiven;

    @SerializedName("noscore")
    protected boolean mNoScore;

    private transient String[] mWords;

    private PuzzleProgress mProgress;
    private boolean mLoadedProgress;

    public Puzzle() {
    }

    public static class Mock extends Puzzle {

        /**
         * Creates a mock cryptogram.
         */
        public Mock() {
            this("Bright vixens jump; dozy fowl quack.",
                    "Paul Lammertsma",
                    "Other");
        }

        public Mock(String text, String author, String topic) {
            mText = text;
            mAuthor = author;
            mTopic = topic;
            mIsMock = true;
        }

    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getNumber() {
        if (mNumber == null) {
            return mId + 1;
        }
        return mNumber;
    }

    public void setNumber(int number) {
        mNumber = number;
    }

    public String getTitle(Context context) {
        if (isInstruction()) {
            return context.getString(R.string.puzzle_number_instruction);
        } else if (isNoScore()) {
            return context.getString(R.string.puzzle_number_practice, getNumber());
        }
        return context.getString(R.string.puzzle_number, getNumber());
    }

    public String getText() {
        return mText;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getTopic() {
        return mTopic;
    }

    @NonNull
    public String[] getWords() {
        if (mWords == null) {
            if (mText == null || mText.length() == 0) {
                mWords = new String[0];
            } else {
                mWords = mText.split("\\s");
            }
        }
        return mWords;
    }

    @NonNull
    public String[] getWordsForLineWidth(int lineWidthInChars) {
        LinkedList<String> wordParts = new LinkedList<>();
        Word[] words = Word.Companion.from(getWords());
        int lineRemaining = lineWidthInChars;
        for (Word word : words) {
            lineRemaining = word.fillForSpace(wordParts, lineRemaining, lineWidthInChars);
        }
        return wordParts.toArray(new String[wordParts.size()]);
    }

    public PuzzleProgress getProgress() {
        // Ensure we've attempted to load the data
        load();
        if (mProgress == null) {
            mProgress = new PuzzleProgress(this);
        }
        return mProgress;
    }

    @NonNull
    public HashMap<Character, Character> getCharMapping() {
        return getProgress().getCharMapping(this);
    }

    public ArrayList<Character> getCharacterList() {
        return getProgress().getCharacterList(this);
    }

    public Character getCharacterForMapping(char mappedChar) {
        return getCharMapping().get(mappedChar);
    }

    public Character getMappedCharacter(char inputChar) {
        HashMap<Character, Character> charMapping = getCharMapping();
        for (Character character : charMapping.keySet()) {
            if (charMapping.get(character) == inputChar) {
                return character;
            }
        }
        return null;
    }

    public Collection<Character> getUserChars() {
        return getProgress().getUserChars(this);
    }

    public boolean isUserCharInput(char inputChar) {
        return getUserChars().contains(inputChar);
    }

    public Character getUserChar(char c) {
        return getProgress().getUserChar(this, c);
    }

    public boolean setUserChar(char selectedCharacter, char c) {
        boolean changed = getProgress().setUserChar(this, selectedCharacter, c);
        save();
        return changed;
    }

    public boolean hasUserChars() {
        for (Character c : getUserChars()) {
            if (c != null && c != 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isInputChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public boolean isInstruction() {
        return mId < 0;
    }

    public boolean isInProgress() {
        return getProgress().isInProgress(this);
    }

    public boolean isCompleted() {
        return getProgress().isCompleted(this);
    }

    public boolean isNoScore() {
        return mNoScore;
    }

    @Nullable
    public String getGiven() {
        return mGiven;
    }

    public boolean isGiven(char matchChar) {
        if (mGiven != null) {
            for (int j = 0; j < mGiven.length(); j++) {
                char c = mGiven.charAt(j);
                if (c == matchChar) {
                    return true;
                }
            }
        }
        return false;
    }

    public void reveal(char c) {
        if (!isInputChar(c)) {
            // Not applicable
            return;
        }
        getProgress().reveal(c);
        save();
    }

    public void revealedMistakes() {
        getProgress().incrementRevealedMistakes();
        save();
    }

    public void revealPuzzle() {
        HashMap<Character, Character> charMapping = getCharMapping();
        for (Character c : charMapping.keySet()) {
            getProgress().setUserChar(this, c, c);
        }
        save();
    }

    public boolean isRevealed(char c) {
        if (mGiven != null && mGiven.indexOf(c) > -1) {
            return true;
        }
        return getProgress().isRevealed(c);
    }

    public int getReveals() {
        return getProgress().getReveals();
    }

    public int getExcessCount() {
        return getProgress().getExcessCount(this);
    }

    /**
     * Returns the duration of the user's play time on this puzzle in milliseconds.
     */
    public long getDurationMs() {
        if (isNoScore()) {
            // Don't measure the duration for puzzles with given characters
            return 0;
        }
        return getProgress().getDurationMs();
    }

    @Nullable
    public Float getScore() {
        if (isInstruction()) {
            return null;
        }
        return getProgress().getScore(this);
    }

    public void onResume() {
        getProgress().onResume(this);
    }

    public void onPause() {
        getProgress().onPause();
        save();
    }

    public void unload() {
        mLoadedProgress = false;
    }

    private void load() {
        if (!mLoadedProgress && !mIsMock) {
            mProgress = PuzzleProvider.Companion.getInstance(CryptogramApp.instance).getProgress().get(mId);
            if (mProgress != null) {
                mProgress.sanitize(this);
            }
        }
        mLoadedProgress = true;
    }

    public void reset(boolean save) {
        getProgress().reset(save ? this : null);
        if (save) {
            save();
        }
    }

    public void save() {
        if (!mIsMock) {
            final PuzzleProvider puzzleProvider = PuzzleProvider.Companion.getInstance(CryptogramApp.instance);
            final PuzzleProgress progress = getProgress();
            if (progress != null) {
                puzzleProvider.setProgress(progress.getId(), progress);
                puzzleProvider.saveLocal();
            }
        }
    }

    @Override
    public String toString() {
        return "#" + getId() + ": " + getText().length() + " chars, author '" + getAuthor() + "' (“" + StringUtils.INSTANCE.ellipsize(mText.replace("\u00AD", ""), 40) + "”)";
    }

}
