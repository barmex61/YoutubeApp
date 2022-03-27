package com.fatih.youtube.view

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import com.fatih.youtube.databinding.ActivityPublishContentBinding

class PublishContentActivity: AppCompatActivity() {

    private lateinit var videoUrl:Uri
    private lateinit var mediaController: MediaController

    private lateinit var binding:ActivityPublishContentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPublishContentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        doInit()
    }
    private fun doInit(){

        mediaController= MediaController(this)
        intent.data?.let {
           videoUrl= it
           prepareVideoView(videoUrl)
        }
    }
    private fun prepareVideoView(uri: Uri){
        val videoView=binding.videoView
        videoView.setVideoURI(uri)
        videoView.setMediaController(mediaController)
        videoView.setOnPreparedListener {_->
            mediaController.setAnchorView(videoView)
            videoView.start()
        }
    }
}