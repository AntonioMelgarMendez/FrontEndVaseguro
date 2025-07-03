package com.VaSeguro.ui.screens.Driver.Chat.Calls

import android.content.Context
import android.media.MediaPlayer
import android.os.Vibrator
import android.os.VibrationEffect
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import com.VaSeguro.BuildConfig
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AgoraCallScreen(
    personName: String,
    personPhotoUrl: String?,
    channelName: String,
    callerOneSignalId: String,
    calleeOneSignalId: String,
    onCallEnd: () -> Unit,
    callViewModel: CallViewModel = viewModel()
) {
    val context = LocalContext.current
    val appId = BuildConfig.AGORA_APP_ID
    val agoraManager = remember { AgoraManager(context, appId) }
    var mediaPlayerReleased by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }
    var callDuration by remember { mutableStateOf(0) }
    var isRinging by remember { mutableStateOf(true) }
    val callStatus by agoraManager.callStatus.collectAsState()
    var callActive by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        Log.d("AgoraCallScreen", "Creating call with caller: $callerOneSignalId, callee: $calleeOneSignalId")
        callViewModel.createCall(
            callerOneSignalId,
            calleeOneSignalId
        )
    }
    val mediaPlayer = remember {
        MediaPlayer.create(context, android.provider.Settings.System.DEFAULT_RINGTONE_URI)
    }
    LaunchedEffect(callStatus) {
        if (callStatus == "Calling...") {
            isRinging = true
            mediaPlayer.isLooping = true
            mediaPlayer.start()
        } else {
            isRinging = false
            if (!mediaPlayerReleased && mediaPlayer.isPlaying) mediaPlayer.stop()
            if (!mediaPlayerReleased) {
                mediaPlayer.release()
                mediaPlayerReleased = true
            }
        }
    }

    LaunchedEffect(callStatus) {
        if (callStatus == "In call") {
            while (callActive) {
                delay(1000)
                callDuration += 1
            }
        } else {
            callDuration = 0
        }
    }

    // Only join channel after call is created
    DisposableEffect(callViewModel.callId) {
        if (callViewModel.callId != null) {
            agoraManager.joinChannel("", channelName)
        }
        onDispose {
            agoraManager.leaveChannel()
            agoraManager.destroy()
            if (!mediaPlayerReleased && mediaPlayer.isPlaying) mediaPlayer.stop()
            if (!mediaPlayerReleased) {
                mediaPlayer.release()
                mediaPlayerReleased = true
            }
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            // Do not call onCallEnd() here
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        when {
            callViewModel.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            callViewModel.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${callViewModel.error}")
                }
            }
            callViewModel.callId != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = callStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(
                                if (personPhotoUrl.isNullOrBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!personPhotoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = personPhotoUrl,
                                contentDescription = "User photo",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            val initial = personName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: ""
                            Text(
                                text = initial,
                                color = Color.White,
                                style = MaterialTheme.typography.displayLarge
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = personName,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (callStatus == "In call") {
                        Text(
                            text = String.format("%02d:%02d", callDuration / 60, callDuration % 60),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                isMuted = !isMuted
                                agoraManager.setMuted(isMuted)
                            }
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = if (isMuted) "Unmute" else "Mute"
                            )
                        }
                        IconButton(
                            onClick = {
                                isSpeakerOn = !isSpeakerOn
                                agoraManager.setSpeakerOn(isSpeakerOn)
                            }
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = if (isSpeakerOn) "Speaker Off" else "Speaker On"
                            )
                        }
                        Button(
                            onClick = {
                                callActive = false
                                callViewModel.callId = null
                                onCallEnd()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Colgar", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}