package com.shapakapps.kustclock

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.shapakapps.kustclock.model.TimeControlRepository
import com.shapakapps.kustclock.ui.KustClockApp
import com.shapakapps.kustclock.util.LocaleHelper

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val tmpPrefs = newBase.getSharedPreferences("kust_clock_preferences", MODE_PRIVATE)
        val mode = tmpPrefs.getString("app_language", LocaleHelper.LANG_SYSTEM)
            ?: LocaleHelper.LANG_SYSTEM
        super.attachBaseContext(LocaleHelper.wrap(newBase, mode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TimeControlRepository.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            KustClockApp(onLanguageChange = { recreate() })
        }
    }
}

