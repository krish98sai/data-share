package com.ys.bluetooth

import android.bluetooth.BluetoothDevice;
import android.content.Intent

import android.os.Bundle;
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.soylentispeople.datashare.datashare.*
import java.util.*

class MainActivity : BTActivity(), BTClientCallbacks {
    override fun onConnected() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionFail() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDisconnect() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMessageReceived(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, BTServerActivity::class.java)
        startActivity(intent)
        setContentView(R.layout.activity_main)

        /*val btClient = BTClient(this, this)
        btClient.connect(null, UUID.fromString(getString(R.string.GLOBAL_UUID)))
        btClient.sendMessage("Hello World")*/
    }

    override fun onBluetoothDiscover(device: BluetoothDevice) {
        val textView = TextView(this)
        textView.text = device.name
        findViewById<LinearLayout>(R.id.list).addView(textView)
        Log.e("Bluetooth device: ", device.toString())
    }
}