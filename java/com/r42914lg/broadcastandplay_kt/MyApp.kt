package com.r42914lg.broadcastandplay_kt

import android.app.Application
import android.content.Intent
import com.r42914lg.broadcastandplay_kt.mvp.Contract
import com.r42914lg.broadcastandplay_kt.mvp.MainPresenter
import com.r42914lg.broadcastandplay_kt.mvp.ModelImpl
import com.r42914lg.broadcastandplay_kt.service.BackgroundService


class MyApp : Application() {
    private val presenter: Contract.Presenter by lazy {
        MainPresenter(ModelImpl(), this)
    }

    fun getPresenterRef(): Contract.Presenter {
        return presenter
    }

    override fun onCreate() {
        super.onCreate()
        if (Constants.LOG) log(".onCreate ... starting service listen to SMS")

        val intent = Intent(this, BackgroundService::class.java)
        intent.action = BackgroundService.ACTION_START
        this.startForegroundService(intent)
    }

    override fun onTerminate() {
        super.onTerminate()
        if (Constants.LOG) log(".onTerminate")
    }
}