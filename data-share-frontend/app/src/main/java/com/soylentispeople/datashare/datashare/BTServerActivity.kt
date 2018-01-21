package com.soylentispeople.datashare.datashare

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
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
        if(message.length > 10 && message.substring(0,10).equals("ClientID: ")){
            var user_id = message.substring(10)
            URLLookUp().execute("http://get-data-share.com/payments/get_usable_bytes?uid="+ user_id);
            //TODO send server the client id here. Ask sai for what exactly to send.
        }else if(message.length > 9 && message.substring(0,9).equals("BDevice: ")){
            var BDevice = message.substring(9)
            Toast.makeText(this, BDevice, Toast.LENGTH_LONG).show();
        }
    }

    override fun onBluetoothDiscover(device: BluetoothDevice) {

    }

    override fun onDestroy() {
        btServer!!.close()
        super.onDestroy()
    }

    var client = OkHttpClient()

    fun run(url: String): JSONObject {
        lateinit var request: Request
        request = Request.Builder()
              .header("access-token", PreferenceManager.getDefaultSharedPreferences(this).getString("AuthenticationToken", ""))
              .header("uid", PreferenceManager.getDefaultSharedPreferences(this).getString("uid", ""))
              .header("client", PreferenceManager.getDefaultSharedPreferences(this).getString("client", ""))
              .url(url)
              .build()

        val response = client.newCall(request).execute()
        var str = response.body()!!.string()

        var obj = JSONObject(str)
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("bytes", obj!!.get("bytes").toString()).apply()



        return obj
    }
    private inner class URLLookUp : AsyncTask<String, Void, JSONObject>() {
        override fun doInBackground(vararg str: String): JSONObject? {
            return run(str[0]);
        }

        override fun onPostExecute(result: JSONObject?) {


        }
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