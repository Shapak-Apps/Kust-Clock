package com.shapakapps.kustclock.util

import android.content.Context
import com.shapakapps.kustclock.R
import com.shapakapps.kustclock.model.IncrementType
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

fun durationText(context: Context, millis: Long): String {
    val res = context.resources
    val hours = millis / 3_600_000
    val minutes = (millis % 3_600_000) / 60_000
    val seconds = (millis % 60_000) / 1_000
    return when {
        hours > 0 && minutes > 0 -> res.getString(
            R.string.duration_hr_min,
            hours, res.getString(R.string.unit_hr),
            minutes, res.getString(R.string.unit_min)
        )
        hours > 0 -> res.getString(R.string.duration_single, hours, res.getString(R.string.unit_hr))
        minutes > 0 -> res.getString(R.string.duration_single, minutes, res.getString(R.string.unit_min))
        else -> res.getString(R.string.duration_single, seconds, res.getString(R.string.unit_sec))
    }
}

fun incrementLabel(context: Context, type: IncrementType): String {
    val res = context.resources
    return res.getString(
        when (type) {
            IncrementType.NONE -> R.string.increment_none
            IncrementType.FISCHER -> R.string.increment_fischer
            IncrementType.BRONSTEIN -> R.string.increment_bronstein
            IncrementType.DELAY -> R.string.increment_delay
        }
    )
}

fun summarizeControl(context: Context, control: TimeControl): String {
    val res = context.resources
    if (control.stages.size > 1) {
        val parts = control.stages.joinToString(" + ") { stage ->
            if (stage.moves > 0) res.getString(
                R.string.summary_stage_moves,
                durationText(context, stage.durationMillis),
                stage.moves,
                res.getString(R.string.unit_moves)
            )
            else durationText(context, stage.durationMillis)
        }
        return "$parts · ${incrementLabel(context, control.incrementType)}"
    }
    val base = durationText(context, control.stages.first().durationMillis)
    return if (control.incrementMillis > 0) res.getString(
        R.string.summary_increment,
        base, control.incrementMillis / 1000, res.getString(R.string.unit_sec)
    ) else base
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
