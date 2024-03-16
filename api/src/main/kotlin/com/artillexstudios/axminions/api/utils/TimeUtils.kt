package com.artillexstudios.axminions.api.utils

import com.artillexstudios.axminions.api.config.Config
import com.artillexstudios.axminions.api.config.Messages
import java.time.Duration

object TimeUtils {

    fun format(time: Long): String {
        if (time < 0) return "---"

        val remainingTime: Duration = Duration.ofMillis(time)
        val total: Long = remainingTime.seconds
        val days = total / 86400
        val hours = (total % 86400) / 3600
        val minutes = (total % 3600) / 60
        val seconds = total % 60

        when (Config.TIMER_FORMAT()) {
            1 -> {
                if (days > 0) return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds)
                if (hours > 0) return String.format("%02d:%02d:%02d", hours, minutes, seconds)
                return String.format("%02d:%02d", minutes, seconds)
            }

            2 -> {
                if (days > 0) return days.toString() + Messages.TIME_DAY()
                if (hours > 0) return hours.toString() + Messages.TIME_HOUR()
                if (minutes > 0) return minutes.toString() + Messages.TIME_MINUTE()
                return seconds.toString() + Messages.TIME_SECOND()
            }

            else -> {
                if (days > 0) return java.lang.String.format(
                    ((("%02d" + Messages.TIME_DAY()) + " %02d" + Messages.TIME_HOUR()) + " %02d" + Messages.TIME_MINUTE()) + " %02d" + Messages.TIME_SECOND(),
                    days,
                    hours,
                    minutes,
                    seconds
                )
                if (hours > 0) return java.lang.String.format(
                    (("%02d" + Messages.TIME_HOUR()) + " %02d" + Messages.TIME_MINUTE()) + " %02d" + Messages.TIME_SECOND(),
                    hours,
                    minutes,
                    seconds
                )
                return java.lang.String.format(
                    ("%02d" + Messages.TIME_MINUTE()) + " %02d" + Messages.TIME_SECOND(),
                    minutes,
                    seconds
                )
            }
        }
    }
}