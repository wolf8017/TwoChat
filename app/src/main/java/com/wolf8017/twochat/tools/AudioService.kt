package com.wolf8017.twochat.tools

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Button
import java.io.IOException

class AudioService(private var context: Context) {

    private var tmpMediaPlayer: MediaPlayer? = null
    private lateinit var onPlayCallBack: OnPlayCallBack


    fun playAudioFromUrl(url: String?, onPlayCallBack: OnPlayCallBack) {
//        var mediaPlayer = MediaPlayer()

//        if (tmpMediaPlayer != null) {
//            tmpMediaPlayer!!.stop()
//            tmpMediaPlayer!!.reset()
//            tmpMediaPlayer!!.release() // Release the media player
//            tmpMediaPlayer = null
//        }
//
//        try {
//            mediaPlayer.reset()
//            mediaPlayer.setDataSource(url)
//            mediaPlayer.prepare()
//            mediaPlayer.start()
//
//            tmpMediaPlayer = mediaPlayer
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }

        var mediaPlayer: MediaPlayer?
        val uri =  Uri.parse(url)
        mediaPlayer = MediaPlayer.create(context, uri).apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build()
            )
            setOnPreparedListener {
                onPlayCallBack.onStarted()
            }
            start()
        }

        mediaPlayer?.setOnCompletionListener {
            it.release()
            mediaPlayer = null
            onPlayCallBack.onFinished()
        }
    }



    interface OnPlayCallBack {
        fun onStarted()
        fun onFinished()
    }
}
