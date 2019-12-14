package com.radiocore.player

interface StreamMetadataListener {
    fun onMetadataReceived(metadata: String)
}