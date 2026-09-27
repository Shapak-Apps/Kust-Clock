package com.shapakapps.kustclock.engine

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shapakapps.kustclock.audio.SoundManager
import com.shapakapps.kustclock.model.IncrementType
import com.shapakapps.kustclock.model.TimeControl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ClockPhase { IDLE, RUNNING, PAUSED, FINISHED }

data class PlayerClock(
    val control: TimeControl,
    val remainingMillis: Long,
    val movesMade: Int,
    val stageIndex: Int
) {
    companion object {
        fun initial(control: TimeControl): PlayerClock =
            PlayerClock(control, control.stages.first().durationMillis, 0, 0)
    }
}

class ClockEngine(
    private val controlOne: TimeControl,
    private val controlTwo: TimeControl,
    private val onSound: (SoundManager.AppSound) -> Unit
) {

    var phase by mutableStateOf(ClockPhase.IDLE)
        private set
    var activePlayer by mutableStateOf(-1)
        private set
    var winner by mutableStateOf<Int?>(null)
        private set
    var playerOne by mutableStateOf(PlayerClock.initial(controlOne))
        private set
    var playerTwo by mutableStateOf(PlayerClock.initial(controlTwo))
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var ticker: Job? = null
    private var lastStamp = 0L
    private val turnStart = longArrayOf(0L, 0L)

    init {
        turnStart[0] = playerOne.remainingMillis
        turnStart[1] = playerTwo.remainingMillis
    }

    fun start() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                tick()
                delay(TICK_INTERVAL_MS)
            }
        }
    }

    fun dispose() {
        ticker?.cancel()
        scope.cancel()
    }

    fun press(playerIndex: Int) {
        if (phase == ClockPhase.FINISHED || phase == ClockPhase.PAUSED) return
        if (phase == ClockPhase.RUNNING && playerIndex != activePlayer) return
        val now = SystemClock.elapsedRealtime()
        if (phase == ClockPhase.RUNNING) {
            tick(now)
        }
        var player = playerAt(playerIndex)
        val control = player.control
        player = player.copy(movesMade = player.movesMade + 1)
        when (control.incrementType) {
            IncrementType.FISCHER -> player = player.copy(remainingMillis = player.remainingMillis + control.incrementMillis)
            IncrementType.BRONSTEIN -> {
                val used = (turnStart[playerIndex] - player.remainingMillis).coerceIn(0, control.incrementMillis)
                player = player.copy(remainingMillis = player.remainingMillis + used)
            }
            else -> Unit
        }
        val stage = control.stages[player.stageIndex]
        if (stage.moves > 0 && player.movesMade >= stage.moves && player.stageIndex < control.stages.size - 1) {
            player = player.copy(
                stageIndex = player.stageIndex + 1,
                remainingMillis = player.remainingMillis + control.stages[player.stageIndex + 1].durationMillis
            )
        }
        writePlayer(playerIndex, player)
        turnStart[playerIndex] = player.remainingMillis
        val opponentIndex = 1 - playerIndex
        val opponent = playerAt(opponentIndex)
        activePlayer = opponentIndex
        phase = ClockPhase.RUNNING
        lastStamp = now + delayMillisFor(opponent.control)
        turnStart[opponentIndex] = opponent.remainingMillis
        onSound(if (playerIndex == 0) SoundManager.AppSound.SWITCH_ONE else SoundManager.AppSound.SWITCH_TWO)
    }

    fun pause() {
        if (phase != ClockPhase.RUNNING) return
        tick()
        phase = ClockPhase.PAUSED
        onSound(SoundManager.AppSound.PAUSE)
    }

    fun resume() {
        if (phase != ClockPhase.PAUSED) return
        lastStamp = maxOf(lastStamp, SystemClock.elapsedRealtime())
        phase = ClockPhase.RUNNING
    }

    fun reset() {
        playerOne = PlayerClock.initial(controlOne)
        playerTwo = PlayerClock.initial(controlTwo)
        turnStart[0] = playerOne.remainingMillis
        turnStart[1] = playerTwo.remainingMillis
        activePlayer = -1
        winner = null
        lastStamp = 0L
        phase = ClockPhase.IDLE
        onSound(SoundManager.AppSound.RESET)
    }

    fun adjustTime(playerIndex: Int, deltaMillis: Long) {
        if (phase == ClockPhase.FINISHED) return
        val player = playerAt(playerIndex)
        val updated = player.copy(remainingMillis = (player.remainingMillis + deltaMillis).coerceAtLeast(0))
        writePlayer(playerIndex, updated)
        if (playerIndex == activePlayer) {
            turnStart[playerIndex] = maxOf(turnStart[playerIndex], updated.remainingMillis)
        }
    }

    private fun tick(now: Long = SystemClock.elapsedRealtime()) {
        if (phase != ClockPhase.RUNNING) return
        val delta = (now - lastStamp).coerceAtLeast(0)
        lastStamp = maxOf(lastStamp, now)
        if (delta <= 0L || activePlayer < 0) return
        val player = playerAt(activePlayer)
        val updated = player.copy(remainingMillis = player.remainingMillis - delta)
        if (updated.remainingMillis <= 0L) {
            writePlayer(activePlayer, updated.copy(remainingMillis = 0L))
            winner = 1 - activePlayer
            phase = ClockPhase.FINISHED
            onSound(SoundManager.AppSound.TIME_ENDED)
        } else {
            writePlayer(activePlayer, updated)
        }
    }

    private fun delayMillisFor(control: TimeControl): Long =
        if (control.incrementType == IncrementType.DELAY) control.incrementMillis else 0L

    private fun playerAt(index: Int): PlayerClock = if (index == 0) playerOne else playerTwo

    private fun writePlayer(index: Int, player: PlayerClock) {
        if (index == 0) playerOne = player else playerTwo = player
    }

    companion object {
        private const val TICK_INTERVAL_MS = 50L
    }
}
