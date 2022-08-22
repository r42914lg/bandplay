package com.r42914lg.broadcastandplay_kt

import android.util.Log

inline fun <reified T> T.log(message: String) =
    Log.d("LG>" + T::class.java.simpleName, message)