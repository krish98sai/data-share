package com.soylentispeople.datashare.datashare

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.R.string.cancel
import android.content.DialogInterface
import android.app.AlertDialog;
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.wifi.WifiManager
import android.net.wifi.WifiInfo
import com.soylentispeople.datashare.datashare.R.id.back


/**
 * Created by raeekayusuf on 1/20/18.
 */
class ProviderSettings : Activity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.provider_settings)

        /******* CHECK WIFI *************/

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected){
            Toast.makeText(applicationContext, "Connected to internet", Toast.LENGTH_SHORT).show()
            //redirect to main menu
        }else{
            Toast.makeText(applicationContext, "Not connected to internet", Toast.LENGTH_SHORT).show()
            //redirect to receiver
        }
        /********************************/


        bluetoothNameChange()
        //back()
    }

    /*fun back(){
        var button = findViewById<Button>(R.id.back) as Button
        button.setOnClickListener {
            setContentView(R.layout.main_menu_activity)
        }
    }*/

    fun bluetoothNameChange(){
        val myDevice = BluetoothAdapter.getDefaultAdapter()

        var button = findViewById<Button>(R.id.on_off) as Button
        button.setOnClickListener {
            val simpleAlert = AlertDialog.Builder(this).create()
            simpleAlert.setTitle("Bluetooth Settings Change")
            simpleAlert.setMessage("Your bluetooth name will be changed to: Data-Share Provider")
            simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "AGREE", { dialogInterface, i ->
                Toast.makeText(applicationContext, "Bluetooth name changed", Toast.LENGTH_SHORT).show()
            })

            val myDevice = BluetoothAdapter.getDefaultAdapter()
            if (myDevice != null) {
                myDevice.name = "Data-Share Provider"
            }
            val deviceName = myDevice.name
            Toast.makeText(applicationContext, "Your current device name:" + deviceName, Toast.LENGTH_LONG).show()

            simpleAlert.show()

            var intent = Intent(this, BTServerActivity::class.java)
            startActivity(intent)
        }
    }

}