package com.soylentispeople.datashare.datashare

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.Closeable
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

/**
 * Created by Yuval Shabtai on 1/20/2018.
 */
class BTClient : Closeable {

    var callingActivity: Activity? = null

    var listener: BTClientCallbacks? = null

    /**
     * Socket is always null when not connected
     */
    var socket: BluetoothSocket? = null
    var connectingThread: Thread? = null
    var receivingThread: Thread? = null

    constructor(activity: Activity) {
        this.callingActivity = activity
    }

    /**
     * Sends a connection request to the given device with the given
     * UUID
     */
    fun connect(device: BluetoothDevice, uuid: UUID) {
        //If already in process of connecting, cancel
        if(connectingThread != null && connectingThread!!.isAlive) {
            connectingThread!!.interrupt()
        }

        //Socket is never null while connected
        if(socket != null) {
            socket!!.close()
            socket = null
        }

        connectingThread = Thread({
            try {
                socket = device.createRfcommSocketToServiceRecord(uuid)
                socket!!.connect()
                //Connection successful, dispatch onConnected from listener
                callingActivity!!.runOnUiThread({listener!!.onConnected()})

                //Start receiving messages
                receivingThread = Thread({
                    readMessages()
                })
                receivingThread!!.start()
            } catch(e: IOException) {
                //If any part of the connection process fails, dispatch onConnectionFail
                callingActivity!!.runOnUiThread({listener!!.onConnectionFail()})
            } catch(e1: InterruptedException) {
                //If interrupted, simply return
                Log.w("Bluetooth Connection", "Warning: bluetooth connection interrupted")
            }
        })

        //Start thread
        connectingThread!!.start()
    }

    fun disconnect() {
        if(socket != null) {
            socket!!.close()
            socket = null
        }
    }

    fun sendMessage(message: String) {
        if(socket == null) {
            return
        }

        Thread({
            try {
                socket!!.outputStream.write(message.toByteArray(Charset.forName("US-ASCII")))
            } catch(e: IOException) {
                if(socket != null) {
                    socket!!.close()
                    socket = null
                    callingActivity!!.runOnUiThread({listener!!.onDisconnect()})
                }
            } catch(e1: Exception) {
                if(socket != null) {
                    socket!!.close()
                    socket = null
                    callingActivity!!.runOnUiThread({listener!!.onDisconnect()})
                }
            }
        }).start()
    }

    override fun close() {
        if(connectingThread != null && connectingThread!!.isAlive) {
            connectingThread!!.interrupt()
        }
    }

    private fun readMessages() {
        try {
            val inS = socket!!.inputStream
            var messageBuilder = StringBuilder(256)

            var input: Int = 0
            /*
         * Forever:
         *  Read a character
         *  If -1, stream is terminated, terminate general communication
         *  If 0, message is fully received, dispatch onMessageReceived
         *  Anything else is just another message character
         */
            while (true) {
                input = inS.read()
                if (input == -1) {
                    //Stream terminated, initiate connecting severing protocol
                    break
                } else if (input == 0) {
                    //dispatch message received and reset builder
                    callingActivity!!.runOnUiThread({ listener!!.onMessageReceived(messageBuilder.toString()) })
                    messageBuilder = StringBuilder(256)
                } else {
                    //Append message to messageBuilder
                    messageBuilder.append(input as Char)
                }
            }

            //Loop terminated, connection severing initiated
            if (socket != null) {
                socket!!.close()
                socket = null
            }
            callingActivity!!.runOnUiThread({ listener!!.onDisconnect() })
        } catch(e: InterruptedException) {
            if (socket != null) {
                socket!!.close()
                socket = null
            }
        } catch(e1: IOException) {
            if (socket != null) {
                socket!!.close()
                socket = null
            }
        }
    }
}