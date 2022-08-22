package com.r42914lg.broadcastandplay_kt.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.r42914lg.broadcastandplay_kt.Constants
import com.r42914lg.broadcastandplay_kt.MyApp
import com.r42914lg.broadcastandplay_kt.log

/**
 * Receiver to handle incoming SMS messages
 */
class SMSReceiver : BroadcastReceiver() {
    companion object {
        const val KEY = "LOAD_AUDIO="
        const val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
    }

    private var ctx: Context? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (Constants.LOG) log(".onReceive $intent")
        ctx = context

        if (intent!!.action !== SMS_RECEIVED) {
            if (Constants.LOG) log(".onReceive - not SMS action")
            return
        }

        val messages: Array<SmsMessage> = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (i in messages.indices) {
            val smsText: String = messages[i].messageBody
            if (Constants.LOG) log(".onReceive SMS txt = $smsText")
            if (smsText.contains(KEY)) {
                kickService(smsText.substring(11, smsText.length))
                break
            }
        }
    }

    private fun kickService(url: String) {
        if (Constants.LOG) log(".kickService $url")
        val presenter = (ctx?.applicationContext as MyApp).getPresenterRef()
        presenter.onStartDownload(url)
    }
}