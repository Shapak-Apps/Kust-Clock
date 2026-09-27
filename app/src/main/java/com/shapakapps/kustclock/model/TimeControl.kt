package com.shapakapps.kustclock.model

enum class IncrementType(val label: String) {
    NONE("None"),
    FISCHER("Fischer"),
    BRONSTEIN("Bronstein"),
    DELAY("Delay")
}

data class Stage(
    val moves: Int,
    val durationMillis: Long
)

data class TimeControl(
    val name: String,
    val stages: List<Stage>,
    val incrementType: IncrementType,
    val incrementMillis: Long
)

data class CustomTimeControl(
    val id: Long,
    val name: String,
    val one: TimeControl,
    val two: TimeControl
)
