package com.r42914lg.broadcastandplay_kt.mvp

import java.util.*

/**
 * MVP contract
 */
interface Contract {
    interface View {
        fun init()
        fun showProgress(percentReady: Int)
        fun showToast(text: String)
        fun enableEdit(enableFlag: Boolean)
        fun enablePlay(enableFlag: Boolean)
        fun enableCancel(enableFlag: Boolean)
        fun enablePauseAndResume(enableFlag: Boolean)
        fun unbind()
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun onPlayCurrent()
        fun onStartDownload(url: String)
        fun onCancelDownload()
        fun onInitiateDownload(urlText: String)
        fun setFileLength(fileLength: Int)
        fun setDownloadedLength(downloadedLength: Int)
        fun setDownloadCompleted()
        fun setDownloadCanceled()
        fun onStopFromServiceNotification()
    }

    interface Model {
        var fileSize: Int
        var downloadInProgress: Boolean
        var workRequestId: UUID
    }
}