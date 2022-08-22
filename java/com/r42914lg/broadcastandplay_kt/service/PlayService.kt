package com.r42914lg.broadcastandplay_kt.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.r42914lg.broadcastandplay_kt.Constants
import com.r42914lg.broadcastandplay_kt.MyApp
import com.r42914lg.broadcastandplay_kt.R
import com.r42914lg.broadcastandplay_kt.log
import com.r42914lg.broadcastandplay_kt.mvp.Contract
import com.r42914lg.broadcastandplay_kt.mvp.Controls
import com.r42914lg.broadcastandplay_kt.mvp.MainActivity

/**
 * Service which plays audio in foreground,
 * started as foreground service, notification allows to pause/resume and stop playback
 */
class PlayService : Service(), Controls {

    companion object {
        const val ONGOING_NOTIFICATION_ID = 1
        const val CHANNEL_ID = "play_service"
        const val CHANNEL_NAME = "My Background Service"
        const val ACTION_PLAY = "P_PLAY"
        const val ACTION_STOP = "P_STOP"
        const val ACTION_RESUME = "P_RESUME"
    }

    inner class MyBinder : Binder() {
        fun getService(): Controls {
            return this@PlayService
        }
    }

    private lateinit var presenter: Contract.Presenter
    private val mp by lazy  { Player(externalCacheDir, ::onTerminate) }
    private val binder = MyBinder()

    override fun onCreate() {
        presenter = (application as MyApp).getPresenterRef()
        if (Constants.LOG) log(".onCreate presenter ref = $presenter")

        mp.initMediaPlayer()
    }

    private fun initForeground(channelId: String, channelName: String) {

        val resultIntent = Intent(this, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(resultIntent)
        val resultPendingIntent: PendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, PlayService::class.java)
        stopIntent.action = ACTION_STOP
        val stopPendingIntent =
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val resumeIntent = Intent(this, PlayService::class.java)
        resumeIntent.action = ACTION_RESUME
        val resumePendingIntent =
            PendingIntent.getService(this, 0, resumeIntent, PendingIntent.FLAG_IMMUTABLE)

        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(getString(R.string.notification_title_play))
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentIntent(resultPendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                getString(R.string.action_stop),
                stopPendingIntent
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                getString(R.string.action_pause_resume),
                resumePendingIntent
            )
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(ONGOING_NOTIFICATION_ID, notificationBuilder.build())

        startForeground(ONGOING_NOTIFICATION_ID, notification)

        if (Constants.LOG) log(".initForeground DONE")
    }

    private fun onTerminate() {
        if (Constants.LOG) log(".onTerminate")

        presenter.onStopFromServiceNotification()

        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (Constants.LOG) log(".onStartCommand ACTION = ${intent.action}")

        when (intent.action) {
            ACTION_PLAY -> {
                initForeground(CHANNEL_ID, CHANNEL_NAME)
                mp.onPlay()
            }
            ACTION_STOP -> onTerminate()
            ACTION_RESUME -> pauseOrResume()
            else -> throw UnsupportedOperationException()
        }
        return START_STICKY
    }

    override fun onBind(arg0: Intent?): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    override fun onDestroy() {
        if (Constants.LOG) log(".onDestroy... about to stop/release MP ref $mp")
        mp.stopAndRelease()
        super.onDestroy()
    }

    override fun pauseOrResume() {
        if (Constants.LOG) log(".pauseOrResume")
        mp.pauseOrResume()
    }
}
