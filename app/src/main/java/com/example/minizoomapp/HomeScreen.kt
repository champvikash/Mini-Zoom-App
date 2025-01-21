package com.example.minizoomapp

import android.os.Bundle
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.minizoomapp.databinding.ActivityHomeScreenBinding
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas

class HomeScreen : AppCompatActivity() {
    private lateinit var binding: ActivityHomeScreenBinding
    private val frontAppId = ""
    private val backAppId = ""
    private val frontToken = ""
    private val backToken = ""
    private val frontChannelName = "minizoomapp"
    private val backChannelName = "minizoomapp"

    private val frontCameraUid = 1001
    private val backCameraUid = 1002

    private var agoraEngineFront: RtcEngine? = null
    private var agoraEngineBack: RtcEngine? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpAgoraEngines()
        binding.startCall.setOnClickListener {
            startFrontCamera()
            startBackCamera()
        }
        binding.endCall.setOnClickListener { endDualCameraStream() }


    }


    private fun setUpAgoraEngines() {
        try {
            // Initialize Agora for the front camera
            val frontConfig = RtcEngineConfig().apply {
                mContext = baseContext
                mAppId = frontAppId
                mEventHandler = object : IRtcEngineEventHandler() {
                    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                        runOnUiThread { showMessage("Front camera joined channel: $channel") }
                    }

                    override fun onError(err: Int) {
                        runOnUiThread { showMessage("Front camera error: $err") }
                    }
                }
            }
            agoraEngineFront = RtcEngine.create(frontConfig)
            agoraEngineFront?.enableVideo()

            // Initialize Agora for the back camera
            val backConfig = RtcEngineConfig().apply {
                mContext = baseContext
                mAppId = backAppId
                mEventHandler = object : IRtcEngineEventHandler() {
                    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                        runOnUiThread { showMessage("Back camera joined channel: $channel") }
                    }

                    override fun onError(err: Int) {
                        runOnUiThread { showMessage("Back camera error: $err") }
                    }
                }
            }
            agoraEngineBack = RtcEngine.create(backConfig)
            agoraEngineBack?.enableVideo()

        } catch (e: Exception) {
            showMessage("Error initializing Agora SDK: ${e.message}")
        }
    }

    private fun showMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun endDualCameraStream() {
        // Stop and leave the front camera stream
        agoraEngineFront?.stopPreview()
        agoraEngineFront?.leaveChannel()
        binding.frontVideoViewContainer.removeAllViews()

        // Stop and leave the back camera stream
        agoraEngineBack?.stopPreview()
        agoraEngineBack?.leaveChannel()
        binding.backVideoViewContainer.removeAllViews()
        showMessage("Dual camera stream ended")
    }

//    private fun startDualCameraStream() {
//        // Front camera setup
//        val frontSurfaceView = SurfaceView(this)
//        binding.frontVideoViewContainer.addView(frontSurfaceView)
//        agoraEngineFront?.setupLocalVideo(
//            VideoCanvas(
//                frontSurfaceView,
//                VideoCanvas.RENDER_MODE_HIDDEN,
//                frontCameraUid
//            )
//        )
//        agoraEngineFront?.startPreview()
//
//        val frontOptions = ChannelMediaOptions().apply {
//            channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
//            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
//        }
//        agoraEngineFront?.joinChannel(frontToken, frontChannelName, frontCameraUid, frontOptions)
//
//        // Back camera setup
//        val backSurfaceView = SurfaceView(this)
//        binding.backVideoViewContainer.addView(backSurfaceView)
//        agoraEngineBack?.setupLocalVideo(
//            VideoCanvas(
//                backSurfaceView,
//                VideoCanvas.RENDER_MODE_HIDDEN,
//                backCameraUid
//            )
//        )
//        agoraEngineBack?.switchCamera() // Switch to back camera
//        agoraEngineBack?.startPreview()
//
//        val backOptions = ChannelMediaOptions().apply {
//            channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
//            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
//        }
//        agoraEngineBack?.joinChannel(backToken, backChannelName, backCameraUid, backOptions)
//    }

    private fun startDualCameraStream() {
        // Front camera setup
        val frontSurfaceView = SurfaceView(this)
        binding.frontVideoViewContainer.removeAllViews() // Clear any previous views
        binding.frontVideoViewContainer.addView(frontSurfaceView)

        agoraEngineFront?.setupLocalVideo(
            VideoCanvas(
                frontSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                frontCameraUid
            )
        )
        agoraEngineFront?.startPreview()

        val frontOptions = ChannelMediaOptions().apply {
            channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        }
        agoraEngineFront?.joinChannel(frontToken, frontChannelName, frontCameraUid, frontOptions)

        // Back camera setup
        val backSurfaceView = SurfaceView(this)
        binding.backVideoViewContainer.removeAllViews() // Clear any previous views
        binding.backVideoViewContainer.addView(backSurfaceView)

        agoraEngineBack?.setupLocalVideo(
            VideoCanvas(
                backSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                backCameraUid
            )
        )

        // Switch to the back camera
        agoraEngineBack?.switchCamera()

        agoraEngineBack?.startPreview()

        val backOptions = ChannelMediaOptions().apply {
            channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        }
        agoraEngineBack?.joinChannel(backToken, backChannelName, backCameraUid, backOptions)

        showMessage("Dual camera stream started")
    }


    override fun onDestroy() {
        super.onDestroy()
        endDualCameraStream()
        RtcEngine.destroy()
        agoraEngineFront = null
        agoraEngineBack = null
    }


    private fun startFrontCamera() {
        // Front camera setup
        val frontSurfaceView = SurfaceView(this)
        binding.frontVideoViewContainer.removeAllViews() // Clear any previous views
        binding.frontVideoViewContainer.addView(frontSurfaceView)

        agoraEngineFront?.setupLocalVideo(
            VideoCanvas(
                frontSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                frontCameraUid
            )
        )
        agoraEngineFront?.startPreview()

        val frontOptions = ChannelMediaOptions().apply {
            channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        }
        agoraEngineFront?.joinChannel(frontToken, frontChannelName, frontCameraUid, frontOptions)
    }


    private fun startBackCamera() {
        // Back camera setup
        val backSurfaceView = SurfaceView(this)// Clear any previous views
        binding.backVideoViewContainer.addView(backSurfaceView)

        agoraEngineBack?.setupLocalVideo(
            VideoCanvas(
                backSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                backCameraUid
            )
        )

        // Switch to the back camera (ensure it's set to the back camera before preview)
        agoraEngineBack?.switchCamera()

        agoraEngineBack?.startPreview()

        val backOptions = ChannelMediaOptions().apply {
            channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        }
        agoraEngineBack?.joinChannel(backToken, backChannelName, backCameraUid, backOptions)
    }

}