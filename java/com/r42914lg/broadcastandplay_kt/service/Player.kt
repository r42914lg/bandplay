package com.r42914lg.broadcastandplay_kt.service

import android.media.MediaPlayer
import com.r42914lg.broadcastandplay_kt.Constants
import com.r42914lg.broadcastandplay_kt.log
import java.io.File

/**
 * Wrapper around MediaPlayer - adds some simple logic
 * used in PlayService
 */
class Player(private val dir: File?, val f: () -> Unit) {

    private lateinit var mp: MediaPlayer
    private var isPlaying = false
    private var isOnPause = false

    fun initMediaPlayer() {
        mp = MediaPlayer()
        mp.setOnCompletionListener { f() }
        isPlaying = true

        if (Constants.LOG) log(".initMediaPlayer MediaPlayer ref = $mp")
    }

    fun onPlay() {
        if (Constants.LOG) log(".onPlay isPlaying old value = $isPlaying")

        if (isPlaying) {
            mp.stop()
            mp.release()
            initMediaPlayer()
        }
        try {
            val file = File(dir, Constants.FILE_NAME)
            mp.setDataSource(file.canonicalPath)
            mp.prepare()
            mp.start()
            isPlaying = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun pauseOrResume() {
        if (Constants.LOG) log(".pauseOrResume")

        if (isOnPause) {
            mp.start()
        } else {
            mp.pause()
        }

        isOnPause = !isOnPause
    }

    fun stopAndRelease() {
        mp.stop()
        mp.release()
    }
}