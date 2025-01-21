package com.example.minizoomapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.minizoomapp.databinding.ActivityMainBinding
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val myAppId =
        Constant.appId
    private val channelName = "minizoomapp"
    private val token = Constant.token
    private var localUid = 0 // Default local UID
    private var remoteUid: Int? = null

    private var isClientJoined = false
    private var agoraEngine: RtcEngine? = null
    private var localSurfaceView: SurfaceView? = null
    private var remoteSurfaceView: SurfaceView? = null

    private val PERMISSION_ID = 1001
    private val REQUESTED_PERMISSIONS = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check permissions
        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_ID)
        }

        // Initialize Agora SDK
        setUpVideoSDKEngine()

        binding.startCall.setOnClickListener { startVideoCall() }
        binding.endCall.setOnClickListener { endVideoCall() }
    }

    private fun checkPermissions(): Boolean {
        return REQUESTED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startVideoCall() {
        if (!checkPermissions()) {
            showMessage("Permissions not granted")
            return
        }

        // Set channel options
        val options = ChannelMediaOptions().apply {
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        }

        setUpLocalVideo()
        agoraEngine?.startPreview()
        agoraEngine?.joinChannel(token, channelName, localUid, options)
    }

    private fun endVideoCall() {
        if (!isClientJoined) {
            showMessage("You are not in a call")
            return
        }

        agoraEngine?.leaveChannel()
        isClientJoined = false
        showMessage("Left the channel")

        localSurfaceView?.visibility = View.GONE
        remoteSurfaceView?.visibility = View.GONE
    }

    private fun setUpVideoSDKEngine() {
        try {
            val config = RtcEngineConfig().apply {
                mContext = applicationContext
                mAppId = myAppId
                mEventHandler = rtcEventHandler
            }
            agoraEngine = RtcEngine.create(config)
            agoraEngine?.enableVideo()
            agoraEngine?.enableAudio()

            agoraEngine?.setVideoEncoderConfiguration(
                VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VideoDimensions(1280, 720),
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
                )
            )
        } catch (e: Exception) {
            showMessage("Error initializing Agora SDK: ${e.message}")
        }
    }

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                showMessage("Remote user joined: $uid")
                remoteUid = uid
                setUpRemoteVideo(uid)
            }
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            runOnUiThread {
                isClientJoined = true
                showMessage("Joined channel: $channel as UID: $uid")
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                showMessage("User offline: $uid")
                remoteSurfaceView?.visibility = View.GONE
            }
        }
    }

    private fun setUpLocalVideo() {
        if (localSurfaceView == null) {
            localSurfaceView = SurfaceView(this)
            binding.backVideoViewContainer.addView(localSurfaceView)
        }

        agoraEngine?.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                localUid
            )
        )
        localSurfaceView?.visibility = View.VISIBLE
    }

    private fun setUpRemoteVideo(uid: Int) {
        if (remoteSurfaceView == null) {
            remoteSurfaceView = SurfaceView(this).apply {
                setZOrderMediaOverlay(true) // Ensures proper layering of remote video
            }
            binding.remoteVideoViewContainer.addView(remoteSurfaceView)
        }

        agoraEngine?.setupRemoteVideo(
            VideoCanvas(
                remoteSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                uid
            )
        )
        remoteSurfaceView?.visibility = View.VISIBLE
    }

    private fun showMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine?.stopPreview()
        agoraEngine?.leaveChannel()
        RtcEngine.destroy()
        agoraEngine = null
    }
}
