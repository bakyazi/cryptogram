package com.pixplicity.cryptogram;

import android.annotation.SuppressLint;

import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.cryptogram.models.CryptogramProgress;
import com.pixplicity.cryptogram.utils.CryptogramProvider;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class CryptogramTest {

    @Test
    public void validCryptogramMapping() throws Exception {
        for (long seed = 0L; seed < 100L; seed++) {
            System.out.print("seed " + seed + ":");
            CryptogramProgress.setRandomSeed(seed);
            Cryptogram cryptogram = new Cryptogram.Mock();
            CryptogramProgress progress = new CryptogramProgress();
            HashMap<Character, Character> mapping = progress.getCharMapping(cryptogram);
            for (Character key : mapping.keySet()) {
                Character value = mapping.get(key);
                System.out.print("  " + key + "/" + value);
                assertNotEquals(key, value);
            }
            System.out.println();
        }
    }

    @Test
    public void noEmptyOrDuplicateCryptograms() throws Exception {
        @SuppressLint("UseSparseArrays") HashMap<Integer, Integer> hashes = new HashMap<>();
        for (Cryptogram cryptogram : CryptogramProvider.getInstance(null).getAll()) {
            int id = cryptogram.getId();
            String text = cryptogram.getText().trim().toLowerCase();
            String author = cryptogram.getAuthor();
            int hash = text.hashCode();
            System.out.println("cryptogram " + id + ": " + text.length() + " chars, author '" + author + "'");
            // Ensure there's content
            assertNotEquals(0, text.length());
            // Ensure there aren't single quotes (replace with ’)
            assertEquals(-1, text.indexOf('\''));
            // Ensure there aren't simple hyphens (replace with —)
            assertEquals(-1, text.indexOf(" - "));
            // Ensure there's an author
            assertNotEquals(0, author.length());
            // Ensure there aren't duplicates
            assertEquals(false, hashes.containsKey(hash));
            hashes.put(id, hash);
        }
    }

}
