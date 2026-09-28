package com.shapakapps.kustclock.model

import android.content.Context
import com.shapakapps.kustclock.R
import com.shapakapps.kustclock.storage.TimeControlStore
import com.shapakapps.kustclock.util.durationText

object TimeControlRepository {

    private lateinit var store: TimeControlStore
    private var initialized = false

    fun localizedPresets(context: Context): List<TimeControl> {
        val res = context.resources
        fun dur(millis: Long) = durationText(context, millis)
        return listOf(
            TimeControl(dur(60_000), listOf(Stage(0, 60_000)), IncrementType.FISCHER, 0),
            TimeControl("${dur(60_000)} | 1 ${res.getString(R.string.unit_sec)}", listOf(Stage(0, 60_000)), IncrementType.FISCHER, 1_000),
            TimeControl("${dur(120_000)} | 1 ${res.getString(R.string.unit_sec)}", listOf(Stage(0, 120_000)), IncrementType.FISCHER, 1_000),
            TimeControl(dur(180_000), listOf(Stage(0, 180_000)), IncrementType.FISCHER, 0),
            TimeControl("${dur(180_000)} | 2 ${res.getString(R.string.unit_sec)}", listOf(Stage(0, 180_000)), IncrementType.FISCHER, 2_000),
            TimeControl(dur(300_000), listOf(Stage(0, 300_000)), IncrementType.FISCHER, 0),
            TimeControl("${dur(300_000)} | 3 ${res.getString(R.string.unit_sec)}", listOf(Stage(0, 300_000)), IncrementType.FISCHER, 3_000),
            TimeControl(dur(600_000), listOf(Stage(0, 600_000)), IncrementType.FISCHER, 0),
            TimeControl("${dur(600_000)} | 5 ${res.getString(R.string.unit_sec)}", listOf(Stage(0, 600_000)), IncrementType.FISCHER, 5_000),
            TimeControl("${dur(900_000)} | 10 ${res.getString(R.string.unit_sec)}", listOf(Stage(0, 900_000)), IncrementType.FISCHER, 10_000),
            TimeControl(dur(1_800_000), listOf(Stage(0, 1_800_000)), IncrementType.FISCHER, 0),
            TimeControl("${dur(1_800_000)} | 20 ${res.getString(R.string.unit_sec)}", listOf(Stage(0, 1_800_000)), IncrementType.FISCHER, 20_000),
            TimeControl("${dur(3_600_000)} | 30 ${res.getString(R.string.unit_sec)}", listOf(Stage(0, 3_600_000)), IncrementType.FISCHER, 30_000),
            TimeControl(res.getString(R.string.preset_tournament), listOf(Stage(40, 7_200_000), Stage(0, 3_600_000)), IncrementType.DELAY, 5_000)
        )
    }

    val presets: List<TimeControl> = listOf(
        TimeControl("1 min", listOf(Stage(0, 60_000)), IncrementType.FISCHER, 0),
        TimeControl("1 min | 1 sec", listOf(Stage(0, 60_000)), IncrementType.FISCHER, 1_000),
        TimeControl("2 min | 1 sec", listOf(Stage(0, 120_000)), IncrementType.FISCHER, 1_000),
        TimeControl("3 min", listOf(Stage(0, 180_000)), IncrementType.FISCHER, 0),
        TimeControl("3 min | 2 sec", listOf(Stage(0, 180_000)), IncrementType.FISCHER, 2_000),
        TimeControl("5 min", listOf(Stage(0, 300_000)), IncrementType.FISCHER, 0),
        TimeControl("5 min | 3 sec", listOf(Stage(0, 300_000)), IncrementType.FISCHER, 3_000),
        TimeControl("10 min", listOf(Stage(0, 600_000)), IncrementType.FISCHER, 0),
        TimeControl("10 min | 5 sec", listOf(Stage(0, 600_000)), IncrementType.FISCHER, 5_000),
        TimeControl("15 min | 10 sec", listOf(Stage(0, 900_000)), IncrementType.FISCHER, 10_000),
        TimeControl("30 min", listOf(Stage(0, 1_800_000)), IncrementType.FISCHER, 0),
        TimeControl("30 min | 20 sec", listOf(Stage(0, 1_800_000)), IncrementType.FISCHER, 20_000),
        TimeControl("60 min | 30 sec", listOf(Stage(0, 3_600_000)), IncrementType.FISCHER, 30_000),
        TimeControl("Tournament", listOf(Stage(40, 7_200_000), Stage(0, 3_600_000)), IncrementType.DELAY, 5_000)
    )

    fun init(context: Context) {
        if (initialized) return
        store = TimeControlStore(context.applicationContext)
        initialized = true
    }

    fun customControls(): List<CustomTimeControl> = if (initialized) store.load() else emptyList()

    fun findCustom(id: Long): CustomTimeControl? = customControls().firstOrNull { it.id == id }

    fun saveCustom(item: CustomTimeControl) {
        if (!initialized) return
        store.save(customControls().filter { it.id != item.id } + item)
    }

    fun deleteCustom(id: Long) {
        if (!initialized) return
        store.save(customControls().filter { it.id != id })
    }

    fun restoreDefaults() {
        if (!initialized) return
        store.save(emptyList())
    }

    fun pairById(id: Long): Pair<TimeControl, TimeControl>? {
        if (id in 0 until presets.size) {
            val control = presets[id.toInt()]
            return control to control
        }
        return findCustom(id)?.let { it.one to it.two }
    }

    fun pairByIdLocalized(context: Context, id: Long): Pair<TimeControl, TimeControl>? {
        if (id >= 0) {
            val localized = localizedPresets(context)
            if (id < localized.size) {
                val control = localized[id.toInt()]
                return control to control
            }
        }
        return findCustom(id)?.let { it.one to it.two }
    }
}
