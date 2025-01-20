package com.example.minizoomapp


import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.minizoomapp.databinding.ActivityViewerBinding
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas

class ViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewerBinding

    private val myAppId = "ad42da4a4d8d48d8ab16b0308abca45c"
    private val channelName = "minizoomapp"
    private val token =
        "007eJxTYOjjWXIr/tHcvFN/uSvulFy8Ov/5pPLdgvVHuLsLncxOXFmiwJCYYmKUkmiSaJJikWJikWKRmGRolmRgbABkJCeamCazLO5JbwhkZKhZspmZkQECQXxuhtzMvMyq/PzcxIICBgYAOhIlgw=="

    private var agoraEngine: RtcEngine? = null
    private var remoteSurfaceView: SurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpVideoSDKEngine()

        // Join the live broadcast
        binding.joinLive.setOnClickListener { joinLive() }
    }

    private fun setUpVideoSDKEngine() {
        try {
            val config = RtcEngineConfig().apply {
                mContext = baseContext
                mAppId = myAppId
                mEventHandler = rtcEventHandler
            }
            agoraEngine = RtcEngine.create(config)
            agoraEngine?.enableVideo()
        } catch (e: Exception) {
            showMessage("Error initializing Agora SDK: ${e.message}")
        }
    }

    private fun joinLive() {
        val options = ChannelMediaOptions().apply {
            channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
        }
        agoraEngine?.joinChannel(token, channelName, 0, options)
    }

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                showMessage("User joined: $uid")
                setUpRemoteVideo(uid) // Set up remote video when a user joins
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                showMessage("User offline: $uid")
                remoteSurfaceView?.visibility = View.GONE
            }
        }
    }

    private fun setUpRemoteVideo(uid: Int) {
        if (remoteSurfaceView == null) {
            remoteSurfaceView = SurfaceView(this).apply {
                setZOrderMediaOverlay(true)
            }
            binding.remoteVideoViewContainer.addView(remoteSurfaceView)
        }

        agoraEngine?.setupRemoteVideo(
            VideoCanvas(
                remoteSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
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
        agoraEngine?.leaveChannel()
        RtcEngine.destroy()
        agoraEngine = null
    }
}
