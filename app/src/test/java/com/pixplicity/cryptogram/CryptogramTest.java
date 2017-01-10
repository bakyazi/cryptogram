package com.pixplicity.cryptogram;

import com.pixplicity.cryptogram.models.Cryptogram;
import com.pixplicity.cryptogram.models.CryptogramProgress;

import org.junit.Test;

import java.util.HashMap;

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

}