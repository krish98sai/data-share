package com.soylentispeople.datashare.datashare

/**
 * Created by Yuval Shabtai on 1/20/2018.
 */
interface BTServerCallbacks {
    fun onConnected()
    fun onConnectionFail()
    fun onDisconnect()
    fun onMessageReceived(message: String)
}