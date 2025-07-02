package com.VaSeguro
import android.app.Application
import android.content.Intent
import android.util.Log
import com.VaSeguro.data.AppProvider
import com.google.android.libraries.places.api.Places
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.VaSeguro.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.onesignal.notifications.INotificationClickListener
import com.onesignal.notifications.INotificationClickEvent
import com.onesignal.notifications.INotificationLifecycleListener
import com.onesignal.notifications.INotificationWillDisplayEvent

class MyApplication : Application() {
    val appProvider by lazy { AppProvider(this) }

    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(this, BuildConfig.MAPS_API_KEY)
        }
        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)

        // Click listener
        val clickListener = object : INotificationClickListener {
            override fun onClick(event: INotificationClickEvent) {
                val data: JSONObject? = event.notification.additionalData
                val notificationType = data?.optString("type")
                val actionId = event.result?.actionId

                if (notificationType == "call") {
                    when (actionId) {
                        "answer" -> {
                            val intent = Intent(applicationContext, com.VaSeguro.ui.screens.Driver.Chat.Calls.AgoraCallActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            applicationContext.startActivity(intent)
                        }
                        "hangup" -> {

                        }
                    }
                }
            }
        }
        OneSignal.Notifications.addClickListener(clickListener)

        // Foreground lifecycle listener (optional)
        val lifecycleListener = object : INotificationLifecycleListener {
            override fun onWillDisplay(event: INotificationWillDisplayEvent) {
                Log.d("OneSignal", "Foreground notification: ${event.notification.title}")
                // Uncomment to suppress notification in foreground
                // event.preventDefault()
            }
        }
        OneSignal.Notifications.addForegroundLifecycleListener(lifecycleListener)

        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }
    }
}