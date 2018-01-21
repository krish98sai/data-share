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
 * Raeeka Yusuf on 1/20/2018
 */
class BTClient : Closeable {

    var mActivity: Activity? = null
    var listener: BTClientCallbacks? = null

    /**
     * Socket is always null when not connected
     */
    var isConnected: Boolean = false
    var connectedDevice: BluetoothDevice? = null
    var socket: BluetoothSocket? = null
    var connectingThread: Thread? = null
    var receivingThread: Thread? = null

    constructor(activity: Activity, myListener: BTClientCallbacks) {
        this.mActivity = activity
        this.listener = myListener
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
        closeConnectionSequence()

        connectingThread = Thread({
            try {
                socket = device.createRfcommSocketToServiceRecord(uuid)
                socket!!.connect()
                //Connection successful, dispatch onConnected from listener
                connectedDevice = device
                isConnected = true
                mActivity!!.runOnUiThread({listener!!.onConnected()})

                //Start receiving messages
                receivingThread = Thread({
                    readMessages()
                })
                receivingThread!!.start()
            } catch(e: IOException) {
                //If any part of the connection process fails, dispatch onConnectionFail
                mActivity!!.runOnUiThread({listener!!.onConnectionFail()})
            } catch(e1: InterruptedException) {
                //If interrupted, simply return
                Log.w("Bluetooth Connection", "Warning: bluetooth connection interrupted")
            }
        })

        //Start thread
        connectingThread!!.start()
    }

    fun disconnect() {
        closeConnectionSequence()
    }

    fun sendMessage(message: String) {
        if(socket == null) {
            return
        }

        Thread({
            try {
                //creates a  byte array with an extra 0 at the end as a flag for when the message ends
                var tempArr = message.toByteArray(Charset.forName("US-ASCII"))
                var arr2 = ByteArray(tempArr.size + 1, {i -> 0})
                for(i in 0..tempArr.size-1){
                    arr2[i] = tempArr[i]
                }
                socket!!.outputStream.write(arr2)


            } catch(e: IOException) {
                closeConnectionSequence()
            } catch(e1: Exception) {
                closeConnectionSequence()
            }
        }).start()
    }

    override fun close() {
        if(connectingThread != null && connectingThread!!.isAlive) {
            connectingThread!!.interrupt()
        }

        if(receivingThread != null && receivingThread!!.isAlive) {
            receivingThread!!.interrupt()
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
                    val message = messageBuilder.toString()

                    //dispatch message received and reset builder
                    mActivity!!.runOnUiThread({ listener!!.onMessageReceived(message) })
                    messageBuilder = StringBuilder(256)
                } else {
                    //Append message to messageBuilder
                    messageBuilder.append(input.toChar())
                }
            }

            //Loop terminated, connection severing initiated
            closeConnectionSequence()
            mActivity!!.runOnUiThread({ listener!!.onDisconnect() })
        } catch(e: InterruptedException) {
            closeConnectionSequence()
        } catch(e1: IOException) {
            closeConnectionSequence()
        }
    }

    private fun closeConnectionSequence() {
        if (socket != null) {
            socket!!.close()
            socket = null
            connectedDevice = null
            mActivity!!.runOnUiThread({ listener!!.onDisconnect() })
            isConnected = false
        }
    }
}