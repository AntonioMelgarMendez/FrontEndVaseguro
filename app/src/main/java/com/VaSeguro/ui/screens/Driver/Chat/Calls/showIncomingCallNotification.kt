package com.VaSeguro.ui.screens.Driver.Chat.Calls

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

fun showIncomingCallNotification(context: Context, personName: String) {
    val channelId = "call_channel"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(channelId, "Calls", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    val acceptIntent = Intent(context, IncomingCallReceiver::class.java).apply {
        action = "com.VaSeguro.ACTION_ACCEPT_CALL"
    }
    val declineIntent = Intent(context, IncomingCallReceiver::class.java).apply {
        action = "com.VaSeguro.ACTION_DECLINE_CALL"
    }

    val acceptPendingIntent = PendingIntent.getBroadcast(context, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    val declinePendingIntent = PendingIntent.getBroadcast(context, 1, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    val notification = NotificationCompat.Builder(context, channelId)
        .setContentTitle("Incoming call")
        .setContentText("Call from $personName")
        .setSmallIcon(android.R.drawable.sym_call_incoming)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_CALL)
        .addAction(android.R.drawable.ic_menu_call, "Accept", acceptPendingIntent)
        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Decline", declinePendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(1001, notification)
}