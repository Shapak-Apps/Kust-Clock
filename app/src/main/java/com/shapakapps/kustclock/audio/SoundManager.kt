package com.shapakapps.kustclock.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.shapakapps.kustclock.R

class SoundManager(context: Context) {

    enum class AppSound { SWITCH_ONE, SWITCH_TWO, TIME_ENDED, PAUSE, RESET }

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds: Map<AppSound, Int> = mapOf(
        AppSound.SWITCH_ONE to soundPool.load(context, R.raw.kust_switch1, 1),
        AppSound.SWITCH_TWO to soundPool.load(context, R.raw.kust_switch2, 1),
        AppSound.TIME_ENDED to soundPool.load(context, R.raw.kust_time_ended, 1),
        AppSound.PAUSE to soundPool.load(context, R.raw.kust_pause, 1),
        AppSound.RESET to soundPool.load(context, R.raw.kust_reset, 1)
    )

    fun play(sound: AppSound) {
        soundIds[sound]?.let { id ->
            soundPool.play(id, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}
