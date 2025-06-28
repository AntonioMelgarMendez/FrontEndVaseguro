package com.VaSeguro.ui.screens.Driver.Chat.Calls

import android.content.Context
import android.util.Log
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine

class AgoraManager(context: Context, private val appId: String) {
    private var rtcEngine: RtcEngine? = null

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d("Agora", "Remote user joined: $uid")
        }
        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d("Agora", "Remote user offline: $uid")
        }
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d("Agora", "Joined channel: $channel, uid: $uid")
        }
    }

    init {
        rtcEngine = RtcEngine.create(context, appId, rtcEventHandler)
        rtcEngine?.setChannelProfile(io.agora.rtc2.Constants.CHANNEL_PROFILE_COMMUNICATION)
        rtcEngine?.enableAudio()
    }

    fun joinChannel(token: String?, channelName: String, uid: Int = 0) {
        rtcEngine?.joinChannel(token, channelName, "", uid)
    }

    fun leaveChannel() {
        rtcEngine?.leaveChannel()
    }

    fun setMuted(muted: Boolean) {
        rtcEngine?.muteLocalAudioStream(muted)
    }

    fun setSpeakerOn(enabled: Boolean) {
        rtcEngine?.setEnableSpeakerphone(enabled)
    }

    fun destroy() {
        RtcEngine.destroy()
    }
}