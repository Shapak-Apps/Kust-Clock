package com.shapakapps.kustclock.util

import com.shapakapps.kustclock.model.TimeControl
import java.util.Locale

data class Hms(val hours: Int, val minutes: Int, val seconds: Int)

fun splitHms(millis: Long): Hms =
    Hms(
        hours = (millis / 3_600_000).toInt(),
        minutes = ((millis % 3_600_000) / 60_000).toInt(),
        seconds = ((millis % 60_000) / 1_000).toInt()
    )

fun formatClockTime(millis: Long): String {
    val clamped = millis.coerceAtLeast(0)
    val hours = (clamped / 3_600_000).toInt()
    val minutes = ((clamped % 3_600_000) / 60_000).toInt()
    val seconds = ((clamped % 60_000) / 1_000).toInt()
    val tenths = ((clamped % 1_000) / 100).toInt()
    return when {
        hours > 0 -> String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        clamped >= 20_000 -> String.format(Locale.US, "%d:%02d", minutes, seconds)
        else -> String.format(Locale.US, "%d:%02d.%d", minutes, seconds, tenths)
    }
}

fun durationText(millis: Long): String {
    val hours = millis / 3_600_000
    val minutes = (millis % 3_600_000) / 60_000
    val seconds = (millis % 60_000) / 1_000
    return when {
        hours > 0 && minutes > 0 -> "$hours hr $minutes min"
        hours > 0 -> "$hours hr"
        minutes > 0 -> "$minutes min"
        else -> "$seconds sec"
    }
}

fun summarizeControl(control: TimeControl): String {
    if (control.stages.size > 1) {
        val parts = control.stages.joinToString(" + ") { stage ->
            if (stage.moves > 0) "${durationText(stage.durationMillis)} / ${stage.moves} moves"
            else durationText(stage.durationMillis)
        }
        return "$parts · ${control.incrementType.label}"
    }
    val base = durationText(control.stages.first().durationMillis)
    return if (control.incrementMillis > 0) "$base | ${control.incrementMillis / 1000} sec" else base
}

fun controlGlyph(control: TimeControl): String {
    if (control.stages.size > 1) return "♛\uFE0E"
    val seconds = control.stages.first().durationMillis / 1000
    return when {
        seconds < 180 -> "♟\uFE0E"
        seconds < 480 -> "♞\uFE0E"
        seconds < 1500 -> "♝\uFE0E"
        else -> "♜\uFE0E"
    }
}
