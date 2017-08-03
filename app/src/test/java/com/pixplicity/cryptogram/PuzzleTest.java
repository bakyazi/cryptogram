package com.pixplicity.cryptogram;

import android.annotation.SuppressLint;
import android.util.Log;

import com.pixplicity.cryptogram.models.Puzzle;
import com.pixplicity.cryptogram.models.PuzzleProgress;
import com.pixplicity.cryptogram.stringsimilarity.Levenshtein;
import com.pixplicity.cryptogram.utils.PuzzleProvider;
import com.pixplicity.cryptogram.views.CryptogramView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class PuzzleTest {

    private static final boolean VERBOSE = false;

    @Before
    public void setup() {
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void validProvider() throws Exception {
        System.out.println("Total puzzles: " + PuzzleProvider.getInstance(null).getCount());
    }

    @Test
    public void validCryptogramMapping() throws Exception {
        for (long seed = 0L; seed < 100L; seed++) {
            if (VERBOSE) {
                System.out.print("seed " + seed + ":");
            }
            PuzzleProgress.setRandomSeed(seed);
            Puzzle puzzle = new Puzzle.Mock();
            PuzzleProgress progress = new PuzzleProgress();
            HashMap<Character, Character> mapping = progress.getCharMapping(puzzle);
            for (Character key : mapping.keySet()) {
                Character value = mapping.get(key);
                if (VERBOSE) {
                    System.out.print("  " + key + "/" + value);
                }
                if (key.equals(value)) {
                    throw new AssertionError("Key and value maps to same character for seed " + seed);
                }
            }
            if (VERBOSE) {
                System.out.println();
            }
        }
    }

    @Test
    public void noEmptyOrDuplicateCryptograms() throws Exception {
        Levenshtein levenshtein = new Levenshtein();
        @SuppressLint("UseSparseArrays") HashMap<Integer, Puzzle> hashes = new HashMap<>();
        ArrayList<String> errors = new ArrayList<>();
        for (Puzzle puzzle : PuzzleProvider.getInstance(null).getAll()) {
            int id = puzzle.getId();
            String text = puzzle.getText();
            String author = puzzle.getAuthor();
            String topic = puzzle.getTopic();
            if (VERBOSE) {
                System.out.println("puzzle " + puzzle);
            }
            // Ensure there's content
            if (text.trim().length() == 0) {
                errors.add("No content: " + puzzle);
            } else {
                // Ensure there aren't single quotes (replace with ’)
                if (text.indexOf('\'') >= 0) {
                    errors.add("Contains single quote; replace with '‘' or '’': " + puzzle);
                }
                // Ensure there aren't single quotes (replace with “/”)
                if (text.indexOf('"') >= 0) {
                    errors.add("Contains single quote; replace with '“' or '”': " + puzzle);
                }
                // Ensure there aren't simple hyphens (replace with —)
                if (text.contains(" - ") || text.contains("--")) {
                    errors.add("Contains simple hyphen; replace with '—': " + puzzle);
                }
                // Ensure em dashes are surrounded with spaces
                if (text.replaceAll("[\\w]—", "").replaceAll("—[\\w]", "").length() < text.length()) {
                    errors.add("Contains em dash without surrounding spaces: " + puzzle);
                }
                // Ensure there aren't simple hyphens (replace with —)
                if (text.contains("...")) {
                    errors.add("Contains expanded ellipsis; replace with '…': " + puzzle);
                }
                // Ensure there aren't simple hyphens (replace with —)
                String given = puzzle.getGiven();
                if (given != null && !given.equals(given.toUpperCase(Locale.ENGLISH))) {
                    errors.add("Contains lowercase given characters: " + puzzle);
                }
                // Ensure there aren't duplicates
                for (Puzzle otherPuzzle : hashes.values()) {
                    double distance = levenshtein.distance(text, otherPuzzle.getText());
                    if (distance < 10) {
                        errors.add("Levenshtein distance of " + puzzle + " is " + distance + " to " + otherPuzzle);
                    }
                }
                if (CryptogramView.ENABLE_HYPHENATION) {
                    for (String word : puzzle.getWords()) {
                        word = word.replaceAll("[^a-zA-Z\u00AD\\-]", "");
                        for (String wordPart : word.split("[\u00AD\\-]")) {
                            if (wordPart.length() > 8) {
                                errors.add("Contains word of length >8 without hyphen or soft-hyphen ('\u00AD'): '" + word + "' in " + puzzle);
                            }
                        }
                    }
                }
            }
            if (!puzzle.isInstruction()) {
                // Ensure there's an author
                if (author == null || author.trim().length() == 0) {
                    errors.add("No author: " + puzzle);
                }
            }
            if (author != null && author.contains("[^\\s\\w]")) {
                errors.add("Contains invalid character in author");
            }
            if (topic != null && topic.contains("[^\\s\\w]")) {
                errors.add("Contains invalid character in topic");
            }
            hashes.put(id, puzzle);
        }
        if (errors.size() > 0) {
            for (int i = 0; i < Math.min(10, errors.size()); i++) {
                System.err.println("-\t" + errors.get(i));
            }
            if (errors.size() > 10) {
                System.err.println("-\t(and " + errors.size() + " more)");
            }
            throw new AssertionError(errors.size() + " errors regarding puzzle quality");
        }
    }

    @Test
    public void hyphenation() {
        if (CryptogramView.ENABLE_HYPHENATION) {
            Puzzle puzzle = PuzzleProvider.getInstance(null).get(0);
            int lineWidthInChars = 12;
            for (int i = 0; i < lineWidthInChars; i++) {
                System.out.print('=');
            }
            System.out.println();
            for (String wordPart : puzzle.getWordsForLineWidth(lineWidthInChars)) {
                System.out.println(wordPart);
            }
        }
    }

}
