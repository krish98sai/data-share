package com.soylentispeople.datashare.datashare

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import android.widget.Toast
import java.io.Closeable
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

/**
 * Created by Yuval Shabtai on 1/20/2018.
 */
class BTServer: Closeable {

    var mActivity: Activity
    var mBTAdapter: BluetoothAdapter
    var listener: BTServerCallbacks

    var connectingThread: Thread? = null
    var receivingThread: Thread? = null

    var serverSocket: BluetoothServerSocket? = null
    var socket: BluetoothSocket? = null

    constructor(activity: Activity, btAdapter: BluetoothAdapter, listener: BTServerCallbacks) {
        this.mActivity = activity
        this.mBTAdapter = btAdapter
        this.listener = listener
    }

    fun acceptConnection(uuid: UUID) {
        //If already connected, do nothing
        if(socket != null) {
            return
        }

        //Check for bluetooth availability
        if(!mBTAdapter.isEnabled) {
            Toast.makeText(mActivity, "Please enable bluetooth on your device", Toast.LENGTH_SHORT).show()
            return
        }

        //Already waiting for connection, do nothing
        if(connectingThread != null && connectingThread!!.isAlive) {
            return
        }

        connectingThread = Thread({
            try {
                serverSocket = mBTAdapter.listenUsingInsecureRfcommWithServiceRecord("Data-Share", uuid)
                socket = serverSocket!!.accept()

                mActivity.runOnUiThread({ listener.onConnected() })

                serverSocket!!.close()
                serverSocket = null

                receivingThread = Thread({
                    readMessages()
                })
                receivingThread!!.start()
            } catch(e: IOException) {
                if(serverSocket != null) {
                    serverSocket!!.close()
                    serverSocket = null
                }

                mActivity.runOnUiThread({ listener.onConnectionFail() })
            } catch(e1: InterruptedException) {
                if(serverSocket != null) {
                    serverSocket!!.close()
                    serverSocket = null
                }

                Log.w("Bluetooth Connection", "Warning: bluetooth connection interrupted")
            }
        })

        connectingThread!!.start()
    }

    fun cancelAcceptance() {
        if(serverSocket != null) {
            serverSocket!!.close()
            serverSocket = null
        }
    }

    override fun close() {
        if(connectingThread != null && connectingThread!!.isAlive) {
            connectingThread!!.interrupt()
            connectingThread = null
        }

        if(receivingThread != null && receivingThread!!.isAlive) {
            receivingThread!!.interrupt()
        }

        if(serverSocket != null) {
            serverSocket!!.close()
            serverSocket = null
        }

        if(socket != null) {
            socket!!.close()
            socket = null
        }
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
                //creates a  byte array with an extra 0 at the end as a flag for when the message ends
                var tempArr = message.toByteArray(Charset.forName("US-ASCII"))
                var arr2 = ByteArray(tempArr.size + 1, {i -> 0})
                for(i in 0..tempArr.size-1){
                    arr2[i] = tempArr[i]
                }
                socket!!.outputStream.write(arr2)


                //socket!!.outputStream.write(message.toByteArray(Charset.forName("US-ASCII")))
            } catch(e: IOException) {
                if(socket != null) {
                    socket!!.close()
                    socket = null
                    mActivity.runOnUiThread({listener.onDisconnect()})
                }
            } catch(e1: Exception) {
                if(socket != null) {
                    socket!!.close()
                    socket = null
                    mActivity.runOnUiThread({listener.onDisconnect()})
                }
            }
        }).start()
    }

    private fun readMessages() {
        try {
            val inS = socket!!.inputStream
            var messageBuilder = StringBuilder(256)

            var input: Int = 0
            /*
             * Forever:
             * Read a character
             * If -1, stream is terminated, terminate general communication
             * If 0, message is fully received, dispatch onMessageReceived
             * Anything else is just another message character
             */
            while (true) {
                input = inS.read()
                if (input == -1) {
                    //Stream terminated, initiate connecting severing protocol
                    break
                } else if (input == 0) {
                    //dispatch message received and reset builder
                    mActivity!!.runOnUiThread({ listener!!.onMessageReceived(messageBuilder.toString()) })
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
            mActivity!!.runOnUiThread({ listener!!.onDisconnect() })
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