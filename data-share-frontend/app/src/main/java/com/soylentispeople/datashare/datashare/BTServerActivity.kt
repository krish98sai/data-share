package com.soylentispeople.datashare.datashare

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
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

        val myDevice = BluetoothAdapter.getDefaultAdapter()
        val simpleAlert = AlertDialog.Builder(this).create()
        simpleAlert.setTitle("Bluetooth Settings Change")
        simpleAlert.setMessage("Your bluetooth name will be changed to: Data-Share Provider")
        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "AGREE", { dialogInterface, i ->
            Toast.makeText(applicationContext, "Bluetooth name changed", Toast.LENGTH_SHORT).show()
            val deviceName = myDevice.name
            Toast.makeText(applicationContext, "Your current device name:" + deviceName, Toast.LENGTH_LONG).show()
        })

        if (myDevice != null) {
            myDevice.name = "Data-Share Provider"
        }
        simpleAlert.show()



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

        //Survey paired devices for name
        for(device in mBTAdapter!!.bondedDevices) {
            if(device.name.equals(getString(R.string.receiver_bt_name))) {
                var classBluetoothPan = Class.forName("android.bluetooth.BluetoothPan")
                var mBTPanConnect = classBluetoothPan.getDeclaredMethod("connect", BluetoothDevice::class.java)
                var BTPanConstructor = classBluetoothPan.getDeclaredConstructor(
                        Context::class.java,
                        BluetoothProfile.ServiceListener::class.java)
                BTPanConstructor.setAccessible(true)
                var BTSrvInstance = BTPanConstructor.newInstance(this, BTPanServiceListener(this))

                mBTPanConnect.invoke(BTSrvInstance, device)
                break
            }
        }
    }

    override fun onConnectionFail() {
        Log.i("BTServer", "Connection failed")
    }

    override fun onDisconnect() {
        Log.i("BTServer", "Disconnected")
    }

    override fun onMessageReceived(message: String) {
        Log.i("BTServer", "Message received. Contents: " + message)
    }

    override fun onBluetoothDiscover(device: BluetoothDevice) {

    }

    override fun onDestroy() {
        btServer!!.close()
        super.onDestroy()
    }

    inner class BTPanServiceListener(private val context: Context) : BluetoothProfile.ServiceListener {

        override fun onServiceConnected(profile: Int,
                                        proxy: BluetoothProfile) {
            //Some code must be here or the compiler will optimize away this callback.
            Log.e("MyApp", "BTPan proxy connected")
        }

        override fun onServiceDisconnected(profile: Int) {}
    }
}