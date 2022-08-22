package com.r42914lg.broadcastandplay_kt.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.r42914lg.broadcastandplay_kt.Constants
import com.r42914lg.broadcastandplay_kt.Constants.Companion.URL_TO_LOAD
import com.r42914lg.broadcastandplay_kt.MyApp
import com.r42914lg.broadcastandplay_kt.log
import com.r42914lg.broadcastandplay_kt.mvp.Contract
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * This worker is to download the audio from internet
 */
class DownloadWorker(private val context: Context, private val workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val mHandler = Handler(Looper.getMainLooper())
    private val presenter: Contract.Presenter = (context.applicationContext as MyApp).getPresenterRef()

    override fun doWork(): Result {
        val url = workerParams.inputData.getString(URL_TO_LOAD)
        if (Constants.LOG) log(".doWork $url")

        url?.apply {
            downloadFile(url)
        }
        return Result.success()
    }

    private fun downloadFile(urlText: String): Result {
        try {
            val url = URL(urlText)
            val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection

            urlConnection.requestMethod = "POST"
            urlConnection.doOutput = true
            urlConnection.connect()

            val file = File(context.externalCacheDir, Constants.FILE_NAME)
            val fileOutput = FileOutputStream(file)
            val inputStream: InputStream = urlConnection.inputStream

            val totalSize: Int = urlConnection.contentLength
            mHandler.post { presenter.setFileLength(totalSize) }

            val buffer = ByteArray(1024)
            var bufferLength: Int
            var downloadedSize = 0
            var terminateFlag = false

            while (inputStream.read(buffer).also { bufferLength = it } > 0 && !terminateFlag) {
                fileOutput.write(buffer, 0, bufferLength)
                downloadedSize += bufferLength
                mHandler.post { presenter.setDownloadedLength(downloadedSize) }
                if (isStopped) {
                    terminateFlag = true
                    mHandler.post { presenter.setDownloadCanceled() }
                }
            }

            fileOutput.close()
            if (!terminateFlag) {
                if (Constants.LOG) log(".downloadFile POST CANCEL")
                mHandler.post { presenter.setDownloadCompleted() }
            }

            if (Constants.LOG) log(".downloadFile SUCCESS")
            return Result.success()

        } catch (e: Exception) {
            e.printStackTrace()

            if (Constants.LOG) log(".downloadFile FAILURE")
            return Result.failure()
        }
    }
}