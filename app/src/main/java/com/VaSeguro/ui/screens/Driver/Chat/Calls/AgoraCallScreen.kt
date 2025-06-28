package com.VaSeguro.ui.screens.Driver.Chat.Calls
import android.annotation.SuppressLint
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.VaSeguro.BuildConfig
import com.VaSeguro.ui.screens.Driver.Chat.Calls.AgoraManager

@SuppressLint("DefaultLocale")
@Composable
fun AgoraCallScreen(
    channelName: String,
    token: String? = null,
    onCallEnd: () -> Unit
) {
    val context = LocalContext.current
    val appId = BuildConfig.AGORA_APP_ID
    val agoraManager = remember { AgoraManager(context, appId) }

    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }
    var callDuration by remember { mutableStateOf(0) }

    // Call duration timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callDuration++
        }
    }

    DisposableEffect(Unit) {
        agoraManager.joinChannel(token, channelName)
        onDispose {
            agoraManager.leaveChannel()
            agoraManager.destroy()
            onCallEnd()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User avatar (placeholder)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text("U", style = MaterialTheme.typography.displayLarge, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "In call: $channelName", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format(
                    "%02d:%02d",
                    callDuration / 60,
                    callDuration % 60
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute/unmute button
                IconButton(
                    onClick = {
                        isMuted = !isMuted
                        agoraManager.setMuted(isMuted) // Implement in AgoraManager
                    }
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isMuted) "Unmute" else "Mute"
                    )
                }
                // Speaker on/off button
                IconButton(
                    onClick = {
                        isSpeakerOn = !isSpeakerOn
                        agoraManager.setSpeakerOn(isSpeakerOn) // Implement in AgoraManager
                    }
                ) {
                    Icon(
                        imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (isSpeakerOn) "Speaker Off" else "Speaker On"
                    )
                }
                // End call button
                Button(
                    onClick = { onCallEnd() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Hang up", color = Color.White)
                }
            }
        }
    }
}