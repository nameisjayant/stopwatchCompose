package com.nameisjayant.alarmclock.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.DecimalFormat
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.nameisjayant.alarmclock.MainActivity
import com.nameisjayant.alarmclock.constant.CHANNEL_ID
import com.nameisjayant.alarmclock.constant.CHANNEL_NAME
import com.nameisjayant.alarmclock.constant.RESET_STOPWATCH
import com.nameisjayant.alarmclock.constant.START_STOPWATCH
import com.nameisjayant.alarmclock.constant.STOP_STOPWATCH
import com.sujit.alarmclock.R


@RequiresApi(Build.VERSION_CODES.O)
fun getNotificationChannel(): NotificationChannel =
    NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)

fun Context.getNotificationBuilder(timeInSecond: Int): NotificationCompat.Builder {
    return NotificationCompat.Builder(this, CHANNEL_ID)
        .apply {
            setContentTitle(getString(R.string.stopwatch))
            setContentText(fromSecondToTimeFormat(timeInSecond))
            setSmallIcon(R.drawable.ic_timer)
            setOngoing(true)
            setContentIntent(getPendingIntent())
        }
}

private fun Context.getPendingIntent(): PendingIntent {
    val intent = Intent(this, MainActivity::class.java)
    return PendingIntent.getActivity(
        this,
        0,
        intent,
        getPendingIntentFlag()
    )
}

fun Context.getStopwatchWithStopAction(timeInSecond: Int): Notification {
    return getNotificationBuilder(timeInSecond)
        .addAction(0, getString(R.string.stop), getStopPendingIntent())
        .build()

}

private fun Context.getStopPendingIntent(): PendingIntent {
    val intent = Intent(this, StopwatchService::class.java).apply {
        action = STOP_STOPWATCH
    }
    return PendingIntent.getService(
        this,
        0,
        intent,
        getPendingIntentFlag()
    )
}

fun Context.getStopwatchWithResumeAction(timeInSecond: Int): Notification {
    return getNotificationBuilder(timeInSecond)
        .addAction(0, getString(R.string.reset), getResetPendingIntent())
        .addAction(0, getString(R.string.resume), getResumePendingIntent())
        .build()
}

private fun Context.getResetPendingIntent(): PendingIntent {
    val intent = Intent(this, StopwatchService::class.java).apply {
        action = RESET_STOPWATCH
    }
    return PendingIntent.getService(
        this,
        0,
        intent,
        getPendingIntentFlag()
    )
}

private fun Context.getResumePendingIntent(): PendingIntent {
    val intent = Intent(this, StopwatchService::class.java).apply {
        action = START_STOPWATCH
    }
    return PendingIntent.getService(
        this,
        0,
        intent,
        getPendingIntentFlag()
    )
}

fun getPendingIntentFlag() = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

fun checkPostPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.checkCallingOrSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true
}

fun fromSecondToTimeFormat(timeInSecond: Int): String {
    val f = DecimalFormat("00")
    val seconds = timeInSecond % 60
    val minutes = (timeInSecond % 3600) / 60
    val hours = timeInSecond / 3600
    return "${f.format(hours)}:${f.format(minutes)}:${f.format(seconds)}"
}


fun Context.triggerStopwatchService(action: String) {
    Intent(this, StopwatchService::class.java).apply {
        this.action = action
        this@triggerStopwatchService.startService(this)
    }
}
