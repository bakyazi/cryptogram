package com.pixplicity.cryptogram.utils;

import com.pixplicity.cryptogram.CryptogramApp;
import com.pixplicity.cryptogram.models.Topic;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Set;

public class PrefsUtils {

    private static final String KEY_CURRENT_ID = "current_puzzle_index";
    private static final String KEY_PROGRESS = "puzzle_progress";
    private static final String KEY_RANDOMIZE = "randomize";
    private static final String KEY_ONBOARDING = "onboarding";
    private static final String KEY_SHOW_HINTS = "show_hints";
    private static final String KEY_SHOW_TOPIC = "show_topic";
    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_TEXT_SIZE = "text_size";
    private static final String KEY_AUTO_ADVANCE = "auto_advance";
    private static final String KEY_SKIP_FILLED_CELLS = "skip_filled_cells";
    private static final String KEY_CURRENT_TOPIC = "current_topic";
    private static final String KEY_NEVER_SHOW_HELP = "never_show_help";
    private static final String KEY_NEVER_ASK_REVEAL_LETTER = "never_ask_reveal_letter";
    private static final String KEY_NEVER_ASK_REVEAL_MISTAKES = "never_ask_reveal_mistakes";
    private static final String KEY_HIGHLIGHTED_HYPHENATION = "highlighted_hyphenation";
    private static final String KEY_HIGHLIGHTED_TOUCH_INPUT = "highlighted_touch_input";
    private static final String KEY_SAVEGAME_NAME = "savegame_name";
    private static final String KEY_USE_SYSTEM_KEYBOARD = "use_system_keyboard";

    public static final int TYPE_HIGHLIGHT_HYPHENATION = 0;
    public static final int TYPE_HIGHLIGHT_TOUCH_INPUT = 1;


    public static int getCurrentId() {
        return Prefs.getInt(PrefsUtils.KEY_CURRENT_ID, -2);
    }

    public static void setCurrentId(int currentId) {
        Prefs.putInt(PrefsUtils.KEY_CURRENT_ID, currentId);
    }

    public static void clearCurrentId() {
        Prefs.remove(PrefsUtils.KEY_CURRENT_ID);
    }

    public static Set<String> getProgress() {
        if (CryptogramApp.getInstance() == null) {
            return null;
        }
        return Prefs.getOrderedStringSet(PrefsUtils.KEY_PROGRESS, null);
    }

    public static void setProgress(Set<String> progressStrSet) {
        if (CryptogramApp.getInstance() == null) {
            return;
        }
        Prefs.putOrderedStringSet(PrefsUtils.KEY_PROGRESS, progressStrSet);
    }

    public static boolean getRandomize() {
        return Prefs.getBoolean(KEY_RANDOMIZE, false);
    }

    public static void setRandomize(boolean randomize) {
        Prefs.putBoolean(KEY_RANDOMIZE, randomize);
    }

    public static void setOnboarding(int page) {
        Prefs.putInt(KEY_ONBOARDING, page);
    }

    public static int getOnboarding() {
        return Prefs.getInt(KEY_ONBOARDING, -1);
    }

    public static boolean getShowHints() {
        return Prefs.getBoolean(KEY_SHOW_HINTS, false);
    }

    public static void setShowHints(boolean show) {
        Prefs.putBoolean(KEY_SHOW_HINTS, show);
    }

    public static boolean getShowTopic() {
        return Prefs.getBoolean(KEY_SHOW_TOPIC, true);
    }

    public static void setShowTopic(boolean show) {
        Prefs.putBoolean(KEY_SHOW_TOPIC, show);
    }

    public static boolean getDarkTheme() {
        return Prefs.getBoolean(KEY_DARK_THEME, false);
    }

    public static void setDarkTheme(boolean theme) {
        Prefs.putBoolean(KEY_DARK_THEME, theme);
    }

    public static int getTextSize() {
        return Prefs.getInt(KEY_TEXT_SIZE, 0);
    }

    public static void setTextSize(int textSize) {
        Prefs.putInt(KEY_TEXT_SIZE, textSize);
    }

    public static boolean getAutoAdvance() {
        return Prefs.getBoolean(KEY_AUTO_ADVANCE, false);
    }

    public static void setAutoAdvance(boolean show) {
        Prefs.putBoolean(KEY_AUTO_ADVANCE, show);
    }

    public static boolean getSkipFilledCells() {
        return Prefs.getBoolean(KEY_SKIP_FILLED_CELLS, true);
    }

    public static void setSkipFilledCells(boolean skillFilledCells) {
        Prefs.putBoolean(KEY_SKIP_FILLED_CELLS, skillFilledCells);
    }

    public static String getCurrentTopic() {
        return Prefs.getString(KEY_CURRENT_TOPIC, null);
    }

    public static void setCurrentTopic(Topic topic) {
        Prefs.putString(KEY_CURRENT_TOPIC, topic == null ? null : topic.getId());
    }

    public static void setNeverAskRevealMistakes(boolean neverAsk) {
        Prefs.putBoolean(KEY_NEVER_ASK_REVEAL_MISTAKES, neverAsk);
    }

    public static boolean getNeverAskRevealMistakes() {
        return Prefs.getBoolean(KEY_NEVER_ASK_REVEAL_MISTAKES, false);
    }

    public static void setNeverShowHelp(boolean neverShowHelp) {
        Prefs.putBoolean(KEY_NEVER_SHOW_HELP, neverShowHelp);
    }

    public static boolean getNeverShowHelp() {
        return Prefs.getBoolean(KEY_NEVER_SHOW_HELP, false);
    }

    public static void setNeverAskRevealLetter(boolean neverAsk) {
        Prefs.putBoolean(KEY_NEVER_ASK_REVEAL_LETTER, neverAsk);
    }

    public static boolean getNeverAskRevealLetter() {
        return Prefs.getBoolean(KEY_NEVER_ASK_REVEAL_LETTER, false);
    }

    public static void setUseSystemKeyboard(boolean useSystemKeyboard) {
        Prefs.putBoolean(KEY_USE_SYSTEM_KEYBOARD, useSystemKeyboard);
    }

    public static boolean getUseSystemKeyboard() {
        return Prefs.getBoolean(KEY_USE_SYSTEM_KEYBOARD, false);
    }

    private static String getHighlightKey(int type) {
        switch (type) {
            case TYPE_HIGHLIGHT_HYPHENATION:
                return KEY_HIGHLIGHTED_HYPHENATION;
            case TYPE_HIGHLIGHT_TOUCH_INPUT:
                return KEY_HIGHLIGHTED_TOUCH_INPUT;
            default:
                return null;
        }
    }

    public static boolean getHighlighted(int type) {
        String key = getHighlightKey(type);
        if (key == null) {
            return false;
        }
        return Prefs.getBoolean(key, false);
    }

    public static void setHighlighted(int type, boolean highlighted) {
        String key = getHighlightKey(type);
        if (key != null) {
            Prefs.putBoolean(key, highlighted);
        }
    }

    public static String getLastSavegameName() {
        return Prefs.getString(KEY_SAVEGAME_NAME, null);
    }

    public static void setLastSavegameName(String savegameName) {
        Prefs.putString(KEY_SAVEGAME_NAME, savegameName);
    }

}
