package com.soylentispeople.datashare.datashare

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.webkit.WebView
import android.view.inputmethod.EditorInfo
import android.widget.*
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
        if(mBTAdapter!!.isDiscovering){
            mBTAdapter!!.cancelDiscovery()
        }
        setContentView(R.layout.webview_screen)
        findViewById<ImageButton>(R.id.submit).setOnClickListener{
            var url = (findViewById<EditText>(R.id.search_bar) as EditText).text.toString()
            search(url)
        }
//        findViewById<EditText>(R.id.search_bar).setOnEditorActionListener(){ v, actionId, event ->
//            if(actionId == EditorInfo.IME_ACTION_DONE) {
//                val str = (findViewById<EditText>(R.id.search_bar) as EditText).text.toString()
//                search(str)
//                true
//            }
//            false
//        }

        btClient!!.sendMessage("{ \"uid\": \"" +
                PreferenceManager.getDefaultSharedPreferences(this).getString("uid", "") + "\"}")
    }

    override fun onConnectionFail() {
        Toast.makeText(this, "Bluetooth connection failed", Toast.LENGTH_SHORT)
    }

    override fun onDisconnect() {
        setContentView(R.layout.client_connecting_screen)
    }

    override fun onMessageReceived(message: String) {
        (findViewById<WebView>(R.id.webview) as WebView).loadData(message, "text/html", null)
    }

    fun scan(view: View) {
        scan()
    }

    fun connect(view: View) {
        btClient!!.connect(selectedDevice!!, UUID.fromString(getString(R.string.GLOBAL_UUID)))
    }

    fun returnToScanScreen(view: View) {
        btClient!!.disconnect()
    }

    fun search(url: String) {
        btClient!!.sendMessage("{\"url\":\"" + url +"\"}")
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