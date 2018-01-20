package com.ys.bluetooth

import android.bluetooth.BluetoothDevice;
import android.content.Intent

import android.os.Bundle;
import android.util.Log
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.soylentispeople.datashare.datashare.BTActivity
import com.soylentispeople.datashare.datashare.BTClientActivity
import com.soylentispeople.datashare.datashare.BTServerActivity
import com.soylentispeople.datashare.datashare.R

class MainActivity : BTActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, BTServerActivity::class.java)
        startActivity(intent)
        setContentView(R.layout.activity_main)

    }

    override fun onBluetoothDiscover(device: BluetoothDevice) {
        val textView = TextView(this)
        textView.text = device.name
        findViewById<LinearLayout>(R.id.list).addView(textView)
        Log.e("Bluetooth device: ", device.toString())
    }
}