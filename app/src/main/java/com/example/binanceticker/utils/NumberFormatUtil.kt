package com.example.binanceticker.utils

import java.util.Locale

object NumberFormatUtil {
    fun Float.toAbbreviatedFormat(): String {
        val locale = Locale.ROOT
        return when {
            this >= 1_000_000_000 -> String.format(locale,"%.3fB", this / 1_000_000_000)
            this >= 1_000_000 -> String.format(locale, "%.3fM", this / 1_000_000)
            this >= 1_000 -> String.format(locale, "%.3fK", this / 1_000)
            else -> String.format(locale, "%.3f", this)
        }
    }
}