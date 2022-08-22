package com.r42914lg.broadcastandplay_kt.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentFilter.SYSTEM_HIGH_PRIORITY
import android.graphics.Color
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.r42914lg.broadcastandplay_kt.Constants
import com.r42914lg.broadcastandplay_kt.R
import com.r42914lg.broadcastandplay_kt.log
import com.r42914lg.broadcastandplay_kt.mvp.MainActivity
import com.r42914lg.broadcastandplay_kt.receiver.SMSReceiver

/**
 * This service is started from MyApp.onCreate() either directly
 * or indirectly via BootCompletedIntentReceiver
 * It's key task is to register/unregister SMS receiver, it starts as foreground,
 * shows notification allowing user to terminate the service and keeps working
 * until terminated by user or system
 */
class BackgroundService : Service() {

    companion object {
        const val ONGOING_NOTIFICATION_ID = 2
        const val CHANNEL_ID = "aux_service"
        const val CHANNEL_NAME = "My Background Service"
        const val ACTION_START = "P_START"
        const val ACTION_CLOSE = "P_CLOSE"
    }

    private val smsReceiver = SMSReceiver()
    private var registeredFlag = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        if (Constants.LOG) log(".onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Constants.LOG) log(".onDestroy - unregistering SMS br $smsReceiver")

        unregisterReceiver(smsReceiver)
    }

    private fun initForeground(channelId: String, channelName: String) {

        val resultIntent = Intent(this, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(resultIntent)
        val resultPendingIntent: PendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)

        val closeIntent = Intent(this, BackgroundService::class.java)
        closeIntent.action = ACTION_CLOSE
        val stopPendingIntent =
            PendingIntent.getService(this, 0, closeIntent, PendingIntent.FLAG_IMMUTABLE)

        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(getString(R.string.notification_title_sms))
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(resultPendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                getString(R.string.action_close),
                stopPendingIntent
            )
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(ONGOING_NOTIFICATION_ID, notificationBuilder.build())

        startForeground(ONGOING_NOTIFICATION_ID, notification)

        if (Constants.LOG) log(".initForeground DONE")
    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (Constants.LOG) log(".onStartCommand ACTION = ${intent.action}")

        when (intent.action) {
            ACTION_START -> {
                initForeground(CHANNEL_ID, CHANNEL_NAME)
                registerSmsReceiver()
            }
            ACTION_CLOSE -> onTerminate()
            else -> throw UnsupportedOperationException()
        }

        return START_STICKY
    }

    private fun registerSmsReceiver() {
        if (!registeredFlag) {
            val filter = IntentFilter()
            filter.priority = SYSTEM_HIGH_PRIORITY
            filter.addAction("android.provider.Telephony.SMS_RECEIVED")
            registerReceiver(smsReceiver, filter)
            registeredFlag = true
        }
    }

    private fun onTerminate() {
        if (Constants.LOG) log(".onTerminate")

        stopForeground(true)
        stopSelf()
    }
}