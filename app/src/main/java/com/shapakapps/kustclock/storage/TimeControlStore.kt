package com.shapakapps.kustclock.storage

import android.content.Context
import com.shapakapps.kustclock.model.CustomTimeControl
import com.shapakapps.kustclock.model.IncrementType
import com.shapakapps.kustclock.model.Stage
import com.shapakapps.kustclock.model.TimeControl
import org.json.JSONArray
import org.json.JSONObject

class TimeControlStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): List<CustomTimeControl> {
        val raw = prefs.getString(KEY_CUSTOM, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { parseCustom(array.getJSONObject(it)) }
        }.getOrDefault(emptyList())
    }

    fun save(list: List<CustomTimeControl>) {
        val array = JSONArray()
        list.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("name", item.name)
                    .put("one", controlToJson(item.one))
                    .put("two", controlToJson(item.two))
            )
        }
        prefs.edit().putString(KEY_CUSTOM, array.toString()).apply()
    }

    private fun controlToJson(control: TimeControl): JSONObject {
        val stages = JSONArray()
        control.stages.forEach { stage ->
            stages.put(JSONObject().put("moves", stage.moves).put("duration", stage.durationMillis))
        }
        return JSONObject()
            .put("name", control.name)
            .put("type", control.incrementType.name)
            .put("increment", control.incrementMillis)
            .put("stages", stages)
    }

    private fun parseControl(json: JSONObject): TimeControl {
        val stagesJson = json.getJSONArray("stages")
        val stages = (0 until stagesJson.length()).map {
            val stage = stagesJson.getJSONObject(it)
            Stage(stage.optInt("moves"), stage.optLong("duration"))
        }
        val type = IncrementType.entries.firstOrNull { it.name == json.optString("type") } ?: IncrementType.FISCHER
        return TimeControl(json.optString("name"), stages, type, json.optLong("increment"))
    }

    private fun parseCustom(json: JSONObject): CustomTimeControl =
        CustomTimeControl(
            id = json.optLong("id"),
            name = json.optString("name"),
            one = parseControl(json.getJSONObject("one")),
            two = parseControl(json.getJSONObject("two"))
        )

    companion object {
        private const val PREFS_NAME = "kust_clock_store"
        private const val KEY_CUSTOM = "custom_time_controls"
    }
}
