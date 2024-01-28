package com.nameisjayant.alarmclock.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.nameisjayant.alarmclock.constant.NOTIFICATION_ID
import com.nameisjayant.alarmclock.constant.RESET_STOPWATCH
import com.nameisjayant.alarmclock.constant.START_STOPWATCH
import com.nameisjayant.alarmclock.constant.STOP_STOPWATCH
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

@AndroidEntryPoint
class StopwatchService : Service() {

    private var timer: Timer? = null
    var timeInSeconds = mutableIntStateOf(0)
        private set
    var timerState = mutableStateOf(StopwatchState.IDLE)
        private set

    @Inject
    lateinit var notificationManager: NotificationManager


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            when (it) {
                START_STOPWATCH -> {
                    startForegroundService()
                    setStopButton()
                    startStopwatch()
                }

                STOP_STOPWATCH -> {
                    setResumeButton()
                    stopStopwatch()
                }
                RESET_STOPWATCH -> {
                    cancelStopwatch()
                    stopForegroundService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder = StopwatchBinder()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(getNotificationChannel())
        }
    }

    private fun startForegroundService() {
        createNotificationChannel()
        val notification = getStopwatchWithStopAction(timeInSeconds.intValue)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopForegroundService() {
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun setStopButton() {
        val notification = getStopwatchWithStopAction(timeInSeconds.intValue)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun setResumeButton() {
        val notification = getStopwatchWithResumeAction(timeInSeconds.intValue)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun stopStopwatch() {
        timer?.cancel()
        timerState.value = StopwatchState.STOP
    }

    private fun cancelStopwatch() {
        timer = null
        timeInSeconds.intValue = 0
        timerState.value = StopwatchState.IDLE
    }

    private fun startStopwatch() {
        timerState.value = StopwatchState.STARTED
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            timeInSeconds.intValue++
            updateNotification()
        }
    }

    private fun updateNotification() {
        val notification = getStopwatchWithStopAction(timeInSeconds.intValue)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    inner class StopwatchBinder : Binder() {
        fun getService(): StopwatchService = this@StopwatchService
    }
}