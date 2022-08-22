package com.r42914lg.broadcastandplay_kt.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.r42914lg.broadcastandplay_kt.Constants
import com.r42914lg.broadcastandplay_kt.log

/**
 * This receiver handles BOOT_COMPLETED action
 */
class BootCompletedIntentReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Constants.LOG) log(".onReceive $intent")
        // we just log intent here
        // the idea is to start BackgroundService (in foreground) here, but we do not do that -
        // the only purpose of this receiver is to start the application
        // and the BackgroundService  is started in MyAop.onCreate
    }
}