package com.VaSeguro.ui.screens.Driver.Chat.Calls


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class AgoraCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val personName = intent.getStringExtra("personName") ?: ""
        val personPhotoUrl = intent.getStringExtra("personPhotoUrl")
        val channelName = intent.getStringExtra("channelName") ?: ""
        val token = intent.getStringExtra("token")
        setContent {
            AgoraCallScreen(
                personName = personName,
                personPhotoUrl = personPhotoUrl,
                channelName = channelName,
                token = token,
                onCallEnd = { finish() }
            )
        }
    }
}