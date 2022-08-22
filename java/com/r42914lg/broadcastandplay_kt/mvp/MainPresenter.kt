package com.r42914lg.broadcastandplay_kt.mvp

import android.content.Context
import android.content.Intent
import android.webkit.URLUtil
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.r42914lg.broadcastandplay_kt.Constants
import com.r42914lg.broadcastandplay_kt.R
import com.r42914lg.broadcastandplay_kt.log
import com.r42914lg.broadcastandplay_kt.service.DownloadWorker
import com.r42914lg.broadcastandplay_kt.service.PlayService

/**
 * Presenter implementation
 */
class MainPresenter(private val model: Contract.Model,
                    private val context: Context
) :  Contract.Presenter {

    private var mainView: Contract.View? = null

    override fun attachView(view: Contract.View) {
        if (Constants.LOG) log(".attachView $view")

        mainView = view
        mainView?.init()
    }

    override fun detachView() {
        if (Constants.LOG) log(".detachView $mainView")
        mainView = null
    }

    /**
     * (1) start PlayService (2) update UI (if the activity is not visible we just start PlayService)
     */
    override fun onPlayCurrent() {
        if (Constants.LOG) log(".onPlayCurrent")

        val intent = Intent(context, PlayService::class.java)
        intent.action = PlayService.ACTION_PLAY

        context.startForegroundService(intent)

        mainView?.enablePlay(false)
        mainView?.enablePauseAndResume(true)
    }

    /**
     * Starts download for given URL via WorkManager - DownloadWorker
     */
    override fun onStartDownload(url: String) {
        if (Constants.LOG) log(".onStartDownload $url")

        val data = Data.Builder()
            .putString(Constants.URL_TO_LOAD, url)
            .build()

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(data)
            .build()

        // keep work request ID in model (we will need it if cancel will be requested by user)
        model.workRequestId = downloadRequest.id

        WorkManager
            .getInstance(context)
            .enqueue(downloadRequest)
    }

    /**
     * Cancels current download request
     */
    override fun onCancelDownload() {
        if (Constants.LOG) log(".onCancelDownload")

        WorkManager.getInstance(context)
            .cancelWorkById(model.workRequestId)
    }

    /**
     * (1) check if given URL is valid otherwise do nothing
     * (2) if we have active download in progress show toast and return
     * (3) update UI state (if Activity not visible we skip this step)
     * (4) start download
     */
    override fun onInitiateDownload(urlText: String) {
        if (!URLUtil.isValidUrl(urlText)) {
            if (Constants.LOG) log(".onInitiateDownload -> " +  context.getString(R.string.wrong_url_message))
            mainView?.showToast(context.getString(R.string.wrong_url_message))
            return
        }

        if (model.downloadInProgress) {
            if (Constants.LOG) log(".onInitiateDownload - > " +  context.getString(R.string.wrong_url_message))
            mainView?.showToast(context.getString(R.string.download_in_progress_msg))
            return
        }

        // store flag in model
        model.downloadInProgress = true

        mainView?.enableEdit(false)
        mainView?.enablePlay(false)
        mainView?.enableCancel(true)

        onStartDownload(urlText)
    }

    /**
     * This method is call from worker - sets file size to be downloaded
     */
    override fun setFileLength(fileLength: Int) {
        if (Constants.LOG) log(".setFileLength $fileLength")
        model.fileSize = fileLength
    }

    /**
     * This method is call from worker - sets # of already downloaded bytes
     * we calculate % complete and update UI to show actual download progress
     */
    override fun setDownloadedLength(downloadedLength: Int) {
        mainView?.showProgress(((1f * downloadedLength / model.fileSize) * 100).toInt())
    }

    /**
     * This method is call from worker on download completion:
     * (1) clear download in progress flag
     * (2) update UI state
     * (3) if Activity is NOT visible we automatically start playing
     */
    override fun setDownloadCompleted() {
        if (Constants.LOG) log(".setDownloadCompleted")

        model.downloadInProgress = false

        mainView?.enableEdit(true)
        mainView?.enablePlay(true)
        mainView?.enablePauseAndResume(false)
        mainView?.enableCancel(false)
        mainView?.showToast(context.getString(R.string.download_complete_msg))

        if (mainView == null) { // Activity not started
            if (Constants.LOG) log(".setDownloadCompleted -> no activity, calling omPlayCurrent...")
            onPlayCurrent()
        }
    }

    /**
     * This method is call from worker on download cancellation:
     * (1) clear download in progress flag
     * (2) update UI state
     */
    override fun setDownloadCanceled() {
        if (Constants.LOG) log(".setDownloadCanceled")

        model.downloadInProgress = false

        mainView?.enableEdit(true)
        mainView?.enablePlay(false)
        mainView?.enableCancel(false)
        mainView?.showProgress(0)
    }

    /**
     * Called from PlayService when user requested to stop playing -
     * we need to unbind from service so it an be destroyed
     */
    override fun onStopFromServiceNotification() {
        if (Constants.LOG) log(".onStopFromServiceNotification")

        mainView?.unbind()
        mainView?.enablePlay(true)
        mainView?.enablePauseAndResume(false)
    }
}