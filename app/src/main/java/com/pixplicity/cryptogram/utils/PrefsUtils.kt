package com.pixplicity.cryptogram.utils

import com.pixplicity.cryptogram.CryptogramApp
import com.pixplicity.easyprefs.library.Prefs

object PrefsUtils {

    private const val KEY_CURRENT_ID = "current_puzzle_index"
    private const val KEY_PROGRESS = "puzzle_progress"
    private const val KEY_RANDOMIZE = "randomize"
    private const val KEY_ONBOARDING = "onboarding"
    private const val KEY_SHOW_USED_LETTERS = "show_used_letters"
    private const val KEY_SHOW_TOPIC = "show_topic"
    private const val KEY_SHOW_SCORE = "show_score"
    private const val KEY_DARK_THEME = "dark_theme"
    private const val KEY_TEXT_SIZE = "text_size"
    private const val KEY_AUTO_ADVANCE = "auto_advance"
    private const val KEY_SKIP_FILLED_CELLS = "skip_filled_cells"
    private const val KEY_NEVER_ASK_REVEAL_LETTER = "never_ask_reveal_letter"
    private const val KEY_NEVER_ASK_REVEAL_MISTAKES = "never_ask_reveal_mistakes"
    private const val KEY_HIGHLIGHTED_HYPHENATION = "highlighted_hyphenation"
    private const val KEY_HIGHLIGHTED_TOUCH_INPUT = "highlighted_touch_input"
    private const val KEY_SAVEGAME_NAME = "savegame_name"
    private const val KEY_USE_SYSTEM_KEYBOARD = "use_system_keyboard"
    private const val KEY_LAST_VERSION = "last_version"
    private const val KEY_DONATION_FEEDBACK = "donation_feedback"

    const val TYPE_HIGHLIGHT_HYPHENATION = 0
    const val TYPE_HIGHLIGHT_TOUCH_INPUT = 1


    var currentId: Int
        get() = Prefs.getInt(PrefsUtils.KEY_CURRENT_ID, -2)
        set(currentId) = Prefs.putInt(PrefsUtils.KEY_CURRENT_ID, currentId)

    var progress: Set<String>?
        get() = if (CryptogramApp.instance == null) {
            null
        } else Prefs.getOrderedStringSet(PrefsUtils.KEY_PROGRESS, null)
        set(progressStrSet) {
            if (CryptogramApp.instance == null) {
                return
            }
            Prefs.putOrderedStringSet(PrefsUtils.KEY_PROGRESS, progressStrSet)
        }

    var randomize: Boolean
        get() = Prefs.getBoolean(KEY_RANDOMIZE, false)
        set(randomize) = Prefs.putBoolean(KEY_RANDOMIZE, randomize)

    var onboarding: Int
        get() = Prefs.getInt(KEY_ONBOARDING, -1)
        set(page) = Prefs.putInt(KEY_ONBOARDING, page)

    var showUsedChars: Boolean
        get() = Prefs.getBoolean(KEY_SHOW_USED_LETTERS, true)
        set(show) = Prefs.putBoolean(KEY_SHOW_USED_LETTERS, show)

    var showTopic: Boolean
        get() = Prefs.getBoolean(KEY_SHOW_TOPIC, true)
        set(show) = Prefs.putBoolean(KEY_SHOW_TOPIC, show)

    var showScore: Boolean
        get() = Prefs.getBoolean(KEY_SHOW_SCORE, true)
        set(show) = Prefs.putBoolean(KEY_SHOW_SCORE, show)

    var darkTheme: Boolean
        get() = Prefs.getBoolean(KEY_DARK_THEME, false)
        set(theme) = Prefs.putBoolean(KEY_DARK_THEME, theme)

    var textSize: Int
        get() = Prefs.getInt(KEY_TEXT_SIZE, 0)
        set(textSize) = Prefs.putInt(KEY_TEXT_SIZE, textSize)

    var autoAdvance: Boolean
        get() = Prefs.getBoolean(KEY_AUTO_ADVANCE, false)
        set(show) = Prefs.putBoolean(KEY_AUTO_ADVANCE, show)

    var skipFilledCells: Boolean
        get() = Prefs.getBoolean(KEY_SKIP_FILLED_CELLS, true)
        set(skillFilledCells) = Prefs.putBoolean(KEY_SKIP_FILLED_CELLS, skillFilledCells)

    var neverAskRevealMistakes: Boolean
        get() = Prefs.getBoolean(KEY_NEVER_ASK_REVEAL_MISTAKES, false)
        set(neverAsk) = Prefs.putBoolean(KEY_NEVER_ASK_REVEAL_MISTAKES, neverAsk)

    var neverAskRevealLetter: Boolean
        get() = Prefs.getBoolean(KEY_NEVER_ASK_REVEAL_LETTER, false)
        set(neverAsk) = Prefs.putBoolean(KEY_NEVER_ASK_REVEAL_LETTER, neverAsk)

    var useSystemKeyboard: Boolean
        get() = Prefs.getBoolean(KEY_USE_SYSTEM_KEYBOARD, false)
        set(useSystemKeyboard) = Prefs.putBoolean(KEY_USE_SYSTEM_KEYBOARD, useSystemKeyboard)

    var lastSavegameName: String?
        get() = Prefs.getString(KEY_SAVEGAME_NAME, null)
        set(savegameName) = Prefs.putString(KEY_SAVEGAME_NAME, savegameName)

    // This isn't a fresh install
    var lastVersion: Int
        get() {
            var lastVersion = Prefs.getInt(KEY_LAST_VERSION, -1)
            if (lastVersion == -1 && Prefs.contains(PrefsUtils.KEY_CURRENT_ID)) {
                lastVersion = 0
            }
            return lastVersion
        }
        set(lastVersion) = Prefs.putInt(KEY_LAST_VERSION, lastVersion)

    private fun getHighlightKey(type: Int): String? {
        when (type) {
            TYPE_HIGHLIGHT_HYPHENATION -> return KEY_HIGHLIGHTED_HYPHENATION
            TYPE_HIGHLIGHT_TOUCH_INPUT -> return KEY_HIGHLIGHTED_TOUCH_INPUT
            else -> return null
        }
    }

    fun getHighlighted(type: Int): Boolean {
        val key = getHighlightKey(type) ?: return false
        return Prefs.getBoolean(key, false)
    }

    fun setHighlighted(type: Int, highlighted: Boolean) {
        val key = getHighlightKey(type)
        if (key != null) {
            Prefs.putBoolean(key, highlighted)
        }
    }

}
