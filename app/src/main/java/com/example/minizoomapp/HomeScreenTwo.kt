package com.example.minizoomapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.minizoomapp.databinding.ActivityHomeScreenTwoBinding
import io.agora.rtc2.RtcEngine

class HomeScreenTwo : AppCompatActivity() {
    private lateinit var binding: ActivityHomeScreenTwoBinding


    private lateinit var agoraEngine: RtcEngine
    private val appId = "<YOUR_AGORA_APP_ID>"  // Replace with your Agora App ID
    private val channelName = "<YOUR_CHANNEL_NAME>"  // Replace with your Agora Channel Name
    private val token = "<YOUR_AGORA_TOKEN>"  // Replace with your Agora Token if required

    private var frontCameraTrackId: Int? = null
    private var backCameraTrackId: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenTwoBinding.inflate(layoutInflater)
        setContentView(binding.root)






    }
}