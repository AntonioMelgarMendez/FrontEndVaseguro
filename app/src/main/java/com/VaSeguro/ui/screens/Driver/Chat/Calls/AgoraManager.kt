package com.VaSeguro.ui.screens.Driver.Chat.Calls

import android.content.Context
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine

class AgoraManager(context: Context, private val appId: String) {
    private var rtcEngine: RtcEngine? = null

    init {
        rtcEngine = RtcEngine.create(context, appId, object : IRtcEngineEventHandler() {})
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