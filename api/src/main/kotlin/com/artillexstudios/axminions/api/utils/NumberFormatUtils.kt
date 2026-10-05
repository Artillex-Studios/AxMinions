package com.artillexstudios.axminions.api.utils

import com.artillexstudios.axminions.api.config.Config
import java.math.RoundingMode
import java.text.CompactNumberFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object NumberFormatUtils {

    fun format(value: Double): String {
        val roundingMode = runCatching { RoundingMode.valueOf(Config.NUMBER_FORMATTING_ROUNDING()) }
            .getOrDefault(RoundingMode.HALF_EVEN)

        return when (Config.NUMBER_FORMATTING_MODE()) {
            0 -> {
                val format = DecimalFormat(Config.NUMBER_FORMATTING_PATTERN())
                format.roundingMode = roundingMode
                format.format(value)
            }

            1 -> {
                val format = NumberFormat.getCompactNumberInstance(parseLocale(Config.NUMBER_FORMATTING_SHORT_LOCALE()), NumberFormat.Style.SHORT)
                if (format is CompactNumberFormat) {
                    format.roundingMode = roundingMode
                }
                format.format(value)
            }

            else -> value.toString()
        }
    }

    private fun parseLocale(tag: String): Locale {
        val parts = tag.split("_")
        return if (parts.size >= 2) Locale.of(parts[0], parts[1]) else Locale.forLanguageTag(tag.replace("_", "-"))
    }
}
