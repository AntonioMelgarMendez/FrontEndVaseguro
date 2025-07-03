// File: app/src/main/java/com/VaSeguro/ui/screens/Driver/Chat/Calls/IncomingCallReceiver.kt
package com.VaSeguro.ui.screens.Driver.Chat.Calls

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class IncomingCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.VaSeguro.ACTION_ACCEPT_CALL" -> {
                val callIntent = Intent(context, AgoraCallActivity::class.java)
                callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                // Pass extras as needed
                callIntent.putExtra("personName", intent.getStringExtra("personName"))
                callIntent.putExtra("personPhotoUrl", intent.getStringExtra("personPhotoUrl"))
                callIntent.putExtra("channelName", intent.getStringExtra("channelName"))
                callIntent.putExtra("token", intent.getStringExtra("token"))
                context.startActivity(callIntent)
            }
            "com.VaSeguro.ACTION_DECLINE_CALL" -> {
                Toast.makeText(context, "Call declined", Toast.LENGTH_SHORT).show()
            }
        }
    }
}