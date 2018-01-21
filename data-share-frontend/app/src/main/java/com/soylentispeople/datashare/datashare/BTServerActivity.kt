package com.soylentispeople.datashare.datashare

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import java.util.*

/**
 * Created by Yuval Shabtai on 1/20/2018.
 */
class BTServerActivity: BTActivity(), BTServerCallbacks {

    var btServer: BTServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btserver)
        btServer = BTServer(this, mBTAdapter!!, this)
    }

    fun acceptConnection(view: View) {
        btServer!!.acceptConnection(UUID.fromString(getString(R.string.GLOBAL_UUID)))
    }

    fun cancelAcceptance(view: View) {
        btServer!!.cancelAcceptance()
    }

    fun disconnect(view: View) {
        btServer!!.disconnect()
    }

    fun sendMessage(view: View) {
        val messageBox = findViewById<EditText>(R.id.message_text)
        btServer!!.sendMessage(messageBox.text.toString())
    }

    override fun onConnected() {
        Log.i("BTServer", "Successful connection established")
    }

    override fun onConnectionFail() {
        Log.i("BTServer", "Connection failed")
    }

    override fun onDisconnect() {
        Log.i("BTServer", "Disconnected")
    }

    override fun onMessageReceived(message: String) {
        Log.i("BTServer", "Message received. Contents: " + message)
        if(message.length > 10 && message.substring(0,10).equals("ClientID: ")){
            var user_id = message.substring(10);
            Toast.makeText(this, user_id, Toast.LENGTH_LONG).show();
            //TODO send server the client id here. Ask sai for what exactly to send.
        }
    }

    override fun onBluetoothDiscover(device: BluetoothDevice) {

    }

    override fun onDestroy() {
        btServer!!.close()
        super.onDestroy()
    }

}