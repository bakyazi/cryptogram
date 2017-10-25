package com.pixplicity.cryptogram.models;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.R;
import com.pixplicity.cryptogram.providers.PuzzleProvider;
import com.pixplicity.cryptogram.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.UUID;

public class Puzzle {

    @SerializedName("id")
    protected int mId;

    protected transient String mFirebaseId;

    @SerializedName("uuid")
    protected String mUuid;

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

    @SerializedName("explicit")
    protected boolean mIsExplicit;

    @SerializedName("language")
    protected String mLanguage;

    // TODO serialize
    protected Date mDate;

    private transient String[] mWords;

    protected transient boolean mIsMock;

    private transient PuzzleProgress mProgress;

    private transient boolean mLoadedProgress;

    public Puzzle() {
        if (mUuid == null) {
            mUuid = UUID.randomUUID().toString();
        }
    }


    public static class Suggestion extends Puzzle {

        @SuppressWarnings("unused")
        public Suggestion() {
            // Empty constructor for Firebase
        }

        public Suggestion(String text, String author, String topic, String language, boolean isExplicit) {
            mText = text;
            mAuthor = author;
            mTopic = topic;
            mLanguage = language;
            mIsExplicit = isExplicit;
        }

        public void setText(String text) {
            mText = text;
        }

        public void setAuthor(String author) {
            mAuthor = author;
        }

        public void setTopic(String topic) {
            mTopic = topic;
        }

    }

    public Date getDate() {
        return mDate;
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
            mDate = new Date();
        }

    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getFirebaseId() {
        return mFirebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        mFirebaseId = firebaseId;
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

    @SuppressWarnings("unused")
    public String getText() {
        if (mText == null) {
            mText = "";
        }
        return mText;
    }

    @SuppressWarnings("unused")
    public String getAuthor() {
        return mAuthor;
    }

    @SuppressWarnings("unused")
    public String getTopic() {
        return mTopic;
    }

    @SuppressWarnings("unused")
    public boolean isExplicit() {
        return mIsExplicit;
    }

    @SuppressWarnings("unused")
    public String getLanguage() {
        return mLanguage;
    }

    public boolean hasTopic(@Nullable Topic topic) {
        if (topic == null) {
            return true;
        }
        if (mTopic == null) {
            return false;
        }
        String puzzleTopic = mTopic.toLowerCase(Locale.ENGLISH);
        for (String topicName : topic.getTopics()) {
            if (topicName.equals(puzzleTopic)) {
                return true;
            }
        }
        return false;
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
        Word[] words = Word.from(getWords());
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
            mProgress = PuzzleProvider.getInstance(CryptogramApp.getInstance()).getProgress().get(mId);
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
            final PuzzleProvider puzzleProvider = PuzzleProvider.getInstance(CryptogramApp.getInstance());
            final PuzzleProgress progress = getProgress();
            if (progress != null) {
                puzzleProvider.setProgress(progress.getId(), progress);
                puzzleProvider.saveLocal();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Puzzle)) {
            return false;
        }

        Puzzle puzzle = (Puzzle) o;

        if (mId != puzzle.mId) {
            return false;
        }
        return mFirebaseId != null
                ? mFirebaseId.equals(puzzle.mFirebaseId)
                : puzzle.mFirebaseId == null;
    }

    @Override
    public int hashCode() {
        int result = mId;
        result = 31 * result + (mFirebaseId != null ? mFirebaseId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "#" + getId() + ": " + getText().length() + " chars, author '" + getAuthor() + "' (“" + StringUtils.ellipsize(mText.replace("\u00AD", ""), 40) + "”)";
    }

}
