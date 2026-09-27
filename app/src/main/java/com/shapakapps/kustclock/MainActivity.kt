package com.shapakapps.kustclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.shapakapps.kustclock.model.TimeControlRepository
import com.shapakapps.kustclock.ui.KustClockApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TimeControlRepository.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            KustClockApp()
        }
    }
}
