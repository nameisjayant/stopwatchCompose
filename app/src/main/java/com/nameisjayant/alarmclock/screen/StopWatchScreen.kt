package com.nameisjayant.alarmclock.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nameisjayant.alarmclock.constant.RESET_STOPWATCH
import com.nameisjayant.alarmclock.constant.START_STOPWATCH
import com.nameisjayant.alarmclock.constant.STOP_STOPWATCH
import com.nameisjayant.alarmclock.service.StopwatchService
import com.nameisjayant.alarmclock.service.StopwatchState
import com.nameisjayant.alarmclock.service.checkPostPermission
import com.nameisjayant.alarmclock.service.fromSecondToTimeFormat
import com.nameisjayant.alarmclock.service.triggerStopwatchService
import com.sujit.alarmclock.R


@Preview()
@Composable
fun StopWatchScreen(
    stopwatchService: StopwatchService
) {
    val timeInSeconds by stopwatchService.timeInSeconds
    val stopwatchState by stopwatchService.timerState
    val context = LocalContext.current
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(key1 = Unit) {
        if (!checkPostPermission(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        StopWatchAnimatedText(
            time = fromSecondToTimeFormat(timeInSeconds), modifier = Modifier.weight(
                1f
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = {
                when (stopwatchState) {
                    StopwatchState.STOP -> {
                        context.triggerStopwatchService(RESET_STOPWATCH)
                    }

                    else -> Unit
                }
            }, enabled = stopwatchState == StopwatchState.STOP) {
                Text(text = stringResource(id = R.string.reset))
            }
            Button(
                onClick = {
                    when (stopwatchState) {
                        StopwatchState.STARTED ->
                            context.triggerStopwatchService(STOP_STOPWATCH)

                        else ->
                            context.triggerStopwatchService(START_STOPWATCH)
                    }
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = when (stopwatchState) {
                        StopwatchState.STARTED -> Color(0xFFB3261E)
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Text(
                    text = when (stopwatchState) {
                        StopwatchState.IDLE -> stringResource(R.string.start)
                        StopwatchState.STARTED -> stringResource(id = R.string.stop)
                        StopwatchState.STOP -> stringResource(id = R.string.resume)
                    }

                )
            }
        }

    }

}

@Composable
fun StopWatchAnimatedText(
    modifier: Modifier = Modifier,
    time: String
) {
    var oldTime by remember { mutableStateOf("") }

    SideEffect {
        oldTime = time
    }

    Row(
        modifier = modifier
    ) {
        for (i in oldTime.indices) {
            val oldChar = oldTime.getOrNull(i)
            val newChar = time[i]
            val char = if (newChar == oldChar) oldChar else newChar
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    slideInVertically { it }.togetherWith(slideOutVertically { it })
                },
                label = ""
            ) {
                Text(
                    text = it.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.displayLarge,
                )
            }
        }
    }
}