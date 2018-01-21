package com.soylentispeople.datashare.datashare

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by krish98sai on 1/21/2018.
 */
class BTClientReceiveActivity : BTActivity(), BTClientCallbacks {

    var btClient: BTClient? = null

    var deviceMap: HashMap<TextView, BluetoothDevice>? = null
    var selectedDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.client_connecting_screen)

        btClient = BTClient(this, this)
        deviceMap = HashMap()
    }

    override fun onConnected() {
        setContentView(R.layout.webview_screen)
    }

    override fun onConnectionFail() {
        //TODO notify user
    }

    override fun onDisconnect() {
        //TODO notify user
    }

    override fun onMessageReceived(message: String) {
        //TODO: Protocol goes here
    }

    fun scan(view: View) {
        scan()
    }

    fun connect(view: View) {
        btClient!!.connect(selectedDevice!!, UUID.fromString(getString(R.string.GLOBAL_UUID)))
    }

    fun returnToScanScreen(view: View) {
        btClient!!.disconnect()
        setContentView(R.layout.client_connecting_screen)
    }

    fun search(view: View) {

    }

    override fun onBluetoothDiscover(device: BluetoothDevice) {
        //Avoid duplicates
        for(key in deviceMap!!.keys) {
            if(device.address.equals(deviceMap!!.get(key)!!.address)) {
                return
            }
        }

        val deviceView = TextView(this)
        deviceView.setText(if(device.name == null) device.address else device.name)

        deviceMap!!.put(deviceView, device)

        deviceView.setOnClickListener({
            var layout = findViewById<LinearLayout>(R.id.device_list)
            val childCount = layout.childCount

            for(i in 0..childCount-1){
                layout.getChildAt(i).setBackgroundColor(0x00000000.toInt())
            }

            selectedDevice = deviceMap!!.get(it as TextView)
            it.setBackgroundColor(0x30000000.toInt())
        })

        findViewById<LinearLayout>(R.id.device_list).addView(deviceView)
    }

}