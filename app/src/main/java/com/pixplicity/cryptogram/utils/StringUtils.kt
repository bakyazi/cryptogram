package com.pixplicity.cryptogram.utils

import java.util.Locale

object StringUtils {

    private val NON_THIN = "[^iIl1\\.,']"

    private fun textWidth(str: String): Int {
        return str.length - str.replace(NON_THIN.toRegex(), "").length / 2
    }

    fun ellipsize(text: String, max: Int): String {
        if (textWidth(text) <= max) {
            return text
        }

        // Start by chopping off at the word before max
        // This is an over-approximation due to thin-characters…
        var end = text.lastIndexOf(' ', max - 1)

        // Just one long word. Chop it off.
        if (end == -1) {
            return text.substring(0, max - 1) + "…"
        }

        // Step forward as long as textWidth allows.
        var newEnd = end
        do {
            end = newEnd
            newEnd = text.indexOf(' ', end + 1)

            // No more spaces.
            if (newEnd == -1) {
                newEnd = text.length
            }

        } while (textWidth(text.substring(0, newEnd) + "…") < max)

        return text.substring(0, end) + "…"
    }

    fun getDurationString(durationMs: Long): String {
        val durationS = (durationMs / 1000).toInt()
        return String.format(
                Locale.ENGLISH,
                "%d:%02d:%02d",
                durationS / 3600,
                durationS % 3600 / 60,
                durationS % 60)
    }

}
