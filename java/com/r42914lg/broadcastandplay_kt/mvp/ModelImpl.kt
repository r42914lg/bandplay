package com.r42914lg.broadcastandplay_kt.mvp

import java.util.*


class ModelImpl : Contract.Model {
    // file size to b downloaded
    override var fileSize = 0
    // flag
    override var downloadInProgress = false
    // corresponds to download request ID (we use it to cancel if requested by user)
    override lateinit var workRequestId: UUID
}