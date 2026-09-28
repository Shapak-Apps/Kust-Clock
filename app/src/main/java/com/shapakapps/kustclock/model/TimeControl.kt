package com.shapakapps.kustclock.model

enum class IncrementType {
    NONE,
    FISCHER,
    BRONSTEIN,
    DELAY
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
