package com.r42914lg.broadcastandplay_kt.mvp

import com.r42914lg.broadcastandplay_kt.R
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.r42914lg.broadcastandplay_kt.MyApp
import com.r42914lg.broadcastandplay_kt.PermissionHelper
import com.r42914lg.broadcastandplay_kt.service.PlayService
import com.r42914lg.broadcastandplay_kt.service.PlayService.MyBinder


class MainActivity : AppCompatActivity(), Contract.View {

    private lateinit var presenter: Contract.Presenter
    private lateinit var sConn: ServiceConnection
    private lateinit var playerControls: Controls
    private var bound = false

    private lateinit var playButton: Button
    private lateinit var cancelButton: Button
    private lateinit var editText: EditText
    private lateinit var pauseResumeButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionHelper(this)
        prepareToBindPlayerService()
    }

    override fun onResume() {
        super.onResume()
        attachPresenter()
    }

    override fun onPause() {
        super.onPause()
        presenter.detachView()
    }

    private fun attachPresenter() {
        presenter = (application as MyApp).getPresenterRef()
        presenter.attachView(this)
    }

    private fun prepareToBindPlayerService() {
        intent = Intent(this, PlayService::class.java)
        sConn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                bound = true
                playerControls = (binder as MyBinder).getService()
            }
            override fun onServiceDisconnected(name: ComponentName) {
                bound = false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(intent, sConn, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(sConn)
            bound = false
        }
    }

    override fun init() {
        playButton = findViewById(R.id.play_button)
        cancelButton = findViewById(R.id.cancel_button)
        editText = findViewById(R.id.url_text)
        progressBar = findViewById(R.id.progress_horizontal)
        pauseResumeButton = findViewById(R.id.resume_pause_button)
        val goButton = findViewById<Button>(R.id.download_button)

        goButton.setOnClickListener { presenter.onInitiateDownload(editText.text.toString()) }
        pauseResumeButton.setOnClickListener {playerControls.pauseOrResume() }
        playButton.setOnClickListener { presenter.onPlayCurrent() }
        cancelButton.setOnClickListener { presenter.onCancelDownload() }

        editText.isEnabled = true
        playButton.isEnabled = false
        cancelButton.isEnabled = false
        pauseResumeButton.isEnabled = false
        progressBar.progress = 0
    }

    override fun showProgress(percentReady: Int) {
        progressBar.progress = percentReady
    }

    override fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun enableEdit(enableFlag: Boolean) {
        editText.isEnabled = enableFlag
    }

    override fun enablePlay(enableFlag: Boolean) {
        playButton.isEnabled = enableFlag
    }

    override fun enableCancel(enableFlag: Boolean) {
        cancelButton.isEnabled = enableFlag
    }

    override fun enablePauseAndResume(enableFlag: Boolean) {
        pauseResumeButton.isEnabled = enableFlag
    }

    override fun unbind() {
        if (bound) {
            unbindService(sConn)
            bound = false
        }
    }
}