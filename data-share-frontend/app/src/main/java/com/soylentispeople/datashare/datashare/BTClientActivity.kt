package com.soylentispeople.datashare.datashare

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by Yuval Shabtai on 1/20/2018.
 */
class BTClientActivity: BTActivity(), BTClientCallbacks {

    var selectedDevice: BluetoothDevice? = null
    var btClient: BTClient? = null

    var deviceMap: HashMap<TextView, BluetoothDevice>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_btclient)

        btClient = BTClient(this, this)
        deviceMap = HashMap()
    }

    fun connect(view: View) {
        if(selectedDevice == null) {
            Toast.makeText(this, "No device selected", Toast.LENGTH_SHORT).show()
            return
        }
        btClient!!.connect(selectedDevice!!, UUID.fromString(getString(R.string.GLOBAL_UUID)))
    }

    fun disconnect(view: View) {
        btClient!!.disconnect()
    }

    override fun onConnected() {
        Log.i("BTClient", "Successful connection established")
    }

    override fun onConnectionFail() {
        Log.i("BTClient", "Connection failed")
    }

    override fun onDisconnect() {
        Log.i("BTClient", "Disconnected")
    }

    override fun onMessageReceived(message: String) {
        Log.i("BTClient", "Message received. Content: " + message)
    }

    override fun onBluetoothDiscover(device: BluetoothDevice) {
        val deviceView = TextView(this)
        deviceView.setText(if(device.name == null) device.address else device.name)

        deviceMap!!.put(deviceView, device)


        deviceView.setOnClickListener({
            var layout = findViewById<LinearLayout>(R.id.device_list)
            val childCount = layout.childCount

            for(i in 0..childCount-1){
                layout.getChildAt(i).setBackgroundColor(0xFFFFFFFF.toInt())
            }

            selectedDevice = deviceMap!!.get(it as TextView)
            it.setBackgroundColor(0xFFCDCDCD.toInt())
        })

        findViewById<LinearLayout>(R.id.device_list).addView(deviceView)
    }

    override fun onDestroy() {
        btClient!!.close()
        super.onDestroy()
    }

    fun scan(view: View){
        findViewById<LinearLayout>(R.id.device_list).removeAllViews()
        deviceMap!!.clear()
        super.scan()
    }

    fun sendMessage(view: View) {
        val textBox = findViewById<EditText>(R.id.message_text)
        btClient!!.sendMessage(textBox.text.toString())
    }
}