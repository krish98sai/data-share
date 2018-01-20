package com.ys.bluetooth;

import android.bluetooth.BluetoothDevice;

import android.os.Bundle;
import android.util.Log
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.soylentispeople.datashare.datashare.BTActivity
import com.soylentispeople.datashare.datashare.R

class MainActivity : BTActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scan();
    }

    override fun onBluetoothDiscover(device: BluetoothDevice) {
        val textView = TextView(this);
        textView.setText(device.name);
        findViewById<LinearLayout>(R.id.list).addView(textView);
        Log.e("Bluetooth device: ", device.toString());
    }
}