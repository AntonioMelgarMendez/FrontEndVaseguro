package com.VaSeguro.ui.screens.Driver.Chat.Calls

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.Constants

class AgoraManager(context: Context, private val appId: String) {
    private var rtcEngine: RtcEngine? = null

    private val _callStatus = MutableStateFlow("Calling...")
    val callStatus: StateFlow<String> = _callStatus

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d("Agora", "Remote user joined: $uid")
            _callStatus.value = "In call"
        }
        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d("Agora", "Remote user offline: $uid")
            _callStatus.value = "User left"
        }
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d("Agora", "Joined channel: $channel, uid: $uid")
        }
        override fun onError(err: Int) {
            Log.e("Agora", "Error: $err")
        }

    }

    init {
        rtcEngine = RtcEngine.create(context, appId, rtcEventHandler)
        rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
        rtcEngine?.enableAudio()
        rtcEngine?.muteLocalAudioStream(false)
        rtcEngine?.setEnableSpeakerphone(true)
        rtcEngine?.enableLocalAudio(true)
        rtcEngine?.adjustPlaybackSignalVolume(100)
        rtcEngine?.adjustRecordingSignalVolume(100)
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