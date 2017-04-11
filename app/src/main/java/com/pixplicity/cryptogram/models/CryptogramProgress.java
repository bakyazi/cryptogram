package com.pixplicity.cryptogram.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LevelEndEvent;
import com.crashlytics.android.answers.LevelStartEvent;
import com.google.gson.annotations.SerializedName;
import com.pixplicity.cryptogram.events.CryptogramEvent;
import com.pixplicity.cryptogram.utils.EventProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class CryptogramProgress {

    private static final String TAG = CryptogramProgress.class.getSimpleName();

    private static final List<Character> ALPHABET = new ArrayList<>(26);

    static {
        for (int i = 'A'; i <= 'Z'; i++) {
            ALPHABET.add((char) i);
        }
    }

    private static Long sRandomSeed;

    public CryptogramProgress() {
    }

    public CryptogramProgress(@NonNull Cryptogram cryptogram) {
        mId = cryptogram.getId();
        // Apply mappings for any given characters
        String given = cryptogram.getGiven();
        if (given != null) {
            for (int j = 0; j < given.length(); j++) {
                char c = given.charAt(j);
                setUserChar(cryptogram, c, c);
            }
        }
    }

    @SerializedName("id")
    private int mId;

    /**
     * Maps from correct characters to user inputted characters.
     */
    @SerializedName("user")
    private HashMap<Character, Character> mUserChars;

    /**
     * Maps from correct characters to randomized characters.
     */
    @SerializedName("shuffle")
    private HashMap<Character, Character> mCharMapping;

    /**
     * Temporary ordered list of characters.
     */
    private transient ArrayList<Character> mCharacterList;

    /**
     * List of characters that have been revealed.
     */
    @SerializedName("revealed")
    private List<Character> mRevealed;

    /**
     * Timestamp of (corrected) puzzle start time.
     */
    @SerializedName("start_time")
    private Long mStartTime;

    /**
     * Timestamp of puzzle completion time, or time at which playing was stopped.
     */
    @SerializedName("stop_time")
    private Long mStopTime;

    /**
     * Total times characters were input by the user.
     */
    @SerializedName("inputs")
    private Integer mInputs;

    /**
     * Total times characters were input by the user.
     */
    @SerializedName("completed")
    private Boolean mCompleted;

    /**
     * Whether this cryptogram was played using hints.
     */
    @SerializedName("had_hints")
    private Boolean mHadHints;

    /**
     * Total times mistakes were revealed by the user.
     */
    @SerializedName("revealed_mistakes")
    private Integer mRevealedMistakes;

    private transient Boolean mPlaying;
    private transient boolean mSanitized;

    public static void setRandomSeed(Long randomSeed) {
        sRandomSeed = randomSeed;
    }

    public int getId() {
        return mId;
    }

    @NonNull
    public synchronized HashMap<Character, Character> getCharMapping(@NonNull Cryptogram cryptogram) {
        // Ensure we've attempted to load the data
        if (mCharMapping == null) {
            // Populate from the character list
            getCharacterList(cryptogram);
            Random r;
            if (sRandomSeed == null) {
                r = new Random();
            } else {
                r = new Random(sRandomSeed);
            }
            List<Character> alphabet = new ArrayList<>(ALPHABET.size());
            for (Character c : ALPHABET) {
                alphabet.add(c);
            }
            int remaining = mCharMapping.size();
            // Figure out what the last character would be
            char lastChar = 0;
            for (Character c : mCharMapping.keySet()) {
                lastChar = c;
            }
            // Create mappings
            for (Character c : mCharMapping.keySet()) {
                int i = r.nextInt(alphabet.size());
                char mappedC = alphabet.get(i);
                if (mappedC == c) {
                    i = (i + 1) % alphabet.size();
                }
                // Special case for the last two mappings to ensure a solution
                if (remaining == 2) {
                    // Check if by selecting this character, we have a solution for the last character
                    int j = (i + 1) % alphabet.size();
                    if (alphabet.get(j) == lastChar) {
                        // We don't; select the other instead
                        i = (i + 1) % alphabet.size();
                    }
                }
                mappedC = alphabet.get(i);
                alphabet.remove(i);
                mCharMapping.put(c, mappedC);
                remaining--;
            }
            // We've generated the mappings, save it
            cryptogram.save();
        }
        return mCharMapping;
    }

    public synchronized ArrayList<Character> getCharacterList(@NonNull Cryptogram cryptogram) {
        if (mCharacterList == null || mCharMapping == null) {
            // May need to be regenerated as the field is transient
            boolean resetCharMapping = false;
            if (mCharMapping == null) {
                mCharMapping = new HashMap<>();
                resetCharMapping = true;
            }
            mCharacterList = new ArrayList<>();
            for (String word : cryptogram.getWords()) {
                for (int i = 0; i < word.length(); i++) {
                    char c = Character.toUpperCase(word.charAt(i));
                    if (cryptogram.isInputChar(c)) {
                        if (resetCharMapping) {
                            mCharMapping.put(c, (char) 0);
                        }
                        if (!mCharacterList.contains(c)) {
                            mCharacterList.add(c);
                        }
                    }
                }
            }
        }
        return mCharacterList;
    }

    public synchronized void setCharMapping(HashMap<Character, Character> charMapping) {
        mCharMapping = charMapping;
    }

    @NonNull
    private synchronized HashMap<Character, Character> getUserCharsMapping(@NonNull Cryptogram cryptogram) {
        if (mUserChars == null) {
            mUserChars = new HashMap<>();
            for (String word : cryptogram.getWords()) {
                for (int i = 0; i < word.length(); i++) {
                    char c = Character.toUpperCase(word.charAt(i));
                    if (cryptogram.isInputChar(c)) {
                        mUserChars.put(c, (char) 0);
                    }
                }
            }
        }
        return mUserChars;
    }

    public synchronized Collection<Character> getUserChars(@NonNull Cryptogram cryptogram) {
        return getUserCharsMapping(cryptogram).values();
    }

    @Nullable
    public synchronized Character getUserChar(@NonNull Cryptogram cryptogram, char c) {
        return getUserCharsMapping(cryptogram).get(c);
    }

    public synchronized void setUserChar(@NonNull Cryptogram cryptogram, char selectedCharacter, char c) {
        char previousChar = getUserCharsMapping(cryptogram).get(selectedCharacter);
        char userChar = Character.toUpperCase(c);
        if (previousChar != userChar && userChar != 0) {
            if (mInputs == null) {
                mInputs = 1;
            } else {
                mInputs++;
            }
        }
        getUserCharsMapping(cryptogram).put(selectedCharacter, userChar);
    }

    public synchronized int getExcessCount(@NonNull Cryptogram cryptogram) {
        if (mInputs == null) {
            return -1;
        }
        // Start with total number of inputs
        int count = mInputs;
        for (Character c : getUserCharsMapping(cryptogram).values()) {
            if (c != 0) {
                // Subtract any filled in characters
                count--;
            }
        }
        return count;
    }

    public synchronized boolean isCompleted(@NonNull Cryptogram cryptogram) {
        if (mCompleted == null || !mCompleted) {
            mCompleted = true;
            HashMap<Character, Character> userChars = getUserCharsMapping(cryptogram);
            for (Character character : userChars.keySet()) {
                // In order to be correct, the key and value must be identical
                if (character != userChars.get(character) && !cryptogram.isGiven(character)) {
                    mCompleted = false;
                    break;
                }
            }
            if (mCompleted) {
                onCompleted(cryptogram);
                onPause();
            }
        }
        return mCompleted;
    }

    public synchronized void reveal(char c) {
        if (mRevealed == null) {
            mRevealed = new ArrayList<>();
        } else if (mRevealed.contains(c)) {
            return;
        }
        mRevealed.add(c);
    }

    public synchronized boolean isRevealed(char c) {
        return mRevealed != null && mRevealed.contains(c);
    }

    public synchronized int getReveals() {
        return mRevealed == null ? 0 : mRevealed.size();
    }

    public synchronized Integer getRevealedMistakes() {
        return mRevealedMistakes == null ? 0 : mRevealedMistakes;
    }

    public synchronized void incrementRevealedMistakes() {
        if (mRevealedMistakes == null) {
            mRevealedMistakes = 1;
        } else {
            mRevealedMistakes++;
        }
    }

    public synchronized boolean isPlaying() {
        return mPlaying != null && mPlaying;
    }

    public synchronized void onResume(Cryptogram cryptogram) {
        if (!isPlaying() && !isCompleted(cryptogram)) {
            if (mStartTime == null || mStartTime == 0) {
                onStart(cryptogram);
            }
            // Only resume playing if the puzzle wasn't completed
            setTimes();
            mPlaying = true;
        }
    }

    public synchronized void onPause() {
        if (isPlaying()) {
            setTimes();
            mPlaying = false;
        }
    }

    private synchronized void onStart(Cryptogram cryptogram) {
        int puzzleNumber = cryptogram.getNumber();
        Answers.getInstance().logLevelStart(
                new LevelStartEvent()
                        .putLevelName("Puzzle #" + puzzleNumber));

        EventProvider.postEvent(
                new CryptogramEvent.CryptogramStartedEvent(cryptogram));
    }

    private synchronized void onCompleted(@NonNull Cryptogram cryptogram) {
        int puzzleNumber = cryptogram.getNumber();
        LevelEndEvent event = new LevelEndEvent()
                .putLevelName("Puzzle #" + puzzleNumber)
                .putSuccess(true);
        Float score = getScore(cryptogram);
        if (score != null) {
            event.putScore(score);
        }
        Answers.getInstance().logLevelEnd(
                event);

        EventProvider.postEvent(
                new CryptogramEvent.CryptogramCompletedEvent(cryptogram));
    }

    private synchronized void setTimes() {
        long stopTime = System.currentTimeMillis();
        mStartTime = stopTime - getDuration();
        mStopTime = stopTime;
    }

    public synchronized long getStartTime() {
        if (mStartTime == null || mStartTime == 0) {
            return 0;
        }
        return mStartTime;
    }

    public synchronized long getDuration() {
        if (mStartTime == null || mStartTime == 0) {
            return 0;
        }
        if (!isPlaying()) {
            if (mStopTime == null || mStopTime == 0) {
                return 0;
            }
            return mStopTime - mStartTime;
        }
        return System.currentTimeMillis() - mStartTime;
    }

    public synchronized boolean hasScore(@NonNull Cryptogram cryptogram) {
        long duration = cryptogram.getDuration();
        int excessCount = getExcessCount(cryptogram);
        if (duration == 0 || excessCount < 0) {
            return false;
        }
        return true;
    }

    public synchronized Float getScore(@NonNull Cryptogram cryptogram) {
        if (!hasScore(cryptogram)) {
            return null;
        }
        long duration = getDuration();
        int excessCount = getExcessCount(cryptogram);
        float score = 1;
        score = addScore(score, (float) duration / 120f);
        score = addScore(score, (float) Math.pow(0.75f, getRevealedMistakes()));
        score = addScore(score, (6f - getReveals()) / 6f);
        score = addScore(score, (26f - excessCount) / 26f);
        // Never return a score below 0.0% or above 100.0%
        return Math.max(0f, Math.min(1f, score));
    }

    private float addScore(float score, float addition) {
        addition = Math.max(-1f, Math.min(1f, addition));
        if (score < 0 && addition < 0) {
            return score * -addition;
        }
        return score * addition;
    }

    public synchronized void setHadHints(boolean hadHints) {
        mHadHints = hadHints;
    }

    public synchronized boolean hadHints() {
        if (mHadHints == null) {
            mHadHints = false;
        }
        return mHadHints;
    }

    public synchronized void sanitize(@NonNull Cryptogram cryptogram) {
        if (mSanitized) {
            return;
        }
        mSanitized = true;
        // Ensure that only input characters have user mappings
        Iterator<Character> i = getUserCharsMapping(cryptogram).keySet().iterator();
        while (i.hasNext()) {
            Character c = i.next();
            if (!cryptogram.isInputChar(c)) {
                i.remove();
            }
        }
        ArrayList<Character> characterList = getCharacterList(cryptogram);
        HashMap<Character, Character> charMapping = getCharMapping(cryptogram);
        Log.w(TAG, "check for invalid mappings in " + cryptogram);
        for (Character c : characterList) {
            if (charMapping.get(c) == null || charMapping.get(c) == 0) {
                // Whoops! Puzzle has a broken character mapping
                mUserChars = null;
                mCharMapping = null;
                getCharMapping(cryptogram);
                Log.w(TAG, "invalid character mapping for " + cryptogram + "; reset mappings");
                break;
            }
        }
        // Apply mappings for any revealed characters
        if (mRevealed != null) {
            for (Character c : mRevealed) {
                setUserChar(cryptogram, c, c);
            }
        }
    }

    public synchronized void reset(@NonNull Cryptogram cryptogram) {
        mUserChars = null;
        mCharMapping = null;
        mStartTime = null;
        mStopTime = null;
        mCompleted = null;
        if (isPlaying()) {
            mPlaying = null;
            onResume(cryptogram);
        }
        sanitize(cryptogram);
    }

}
