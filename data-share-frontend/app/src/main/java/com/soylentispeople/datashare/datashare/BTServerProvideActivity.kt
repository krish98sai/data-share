package com.soylentispeople.datashare.datashare

/**
 * Created by krish98sai on 1/21/2018.
 */

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Toast
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.*
import org.json.JSONException
import java.net.URL


class BTServerProvideActivity: BTActivity(), BTServerCallbacks {

    var btServer: BTServer? = null
    var used_bytes = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val myDevice = BluetoothAdapter.getDefaultAdapter()
        val simpleAlert = AlertDialog.Builder(this).create()
        simpleAlert.setTitle("Bluetooth Settings Change")
        simpleAlert.setMessage("Your bluetooth name will be changed to: Data-Share Provider")
        simpleAlert.setButton(AlertDialog.BUTTON_POSITIVE, "AGREE", { _, _ ->
            Toast.makeText(applicationContext, "Bluetooth name changed", Toast.LENGTH_SHORT).show()
            val deviceName = myDevice.name
            Toast.makeText(applicationContext, "Your current device name: " + deviceName, Toast.LENGTH_LONG).show()
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

    fun sendMessage(response: String) {
        btServer!!.sendMessage(response)
    }

    override fun onConnected() {
        Log.i("BTServer", "Successful connection established")
    }

    override fun onConnectionFail() {
        Log.i("BTServer", "Connection failed")
    }

    override fun onDisconnect() {
        Log.i("BTServer", "Disconnected")
        URLLookUp().execute("http://get-data-share.com/payments/execute_transaction")
    }

    override fun onMessageReceived(message: String) {
        val json = JSONObject(message)
        if(json.has("url")){
            val url = json.get("url").toString()
            URLLookUp().execute(url)
        }
        else if (json.has("uid")){
            val uid = json.get("uid").toString()
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("receiver-uid", uid).apply()
            URLLookUp().execute("http://get-data-share.com/payments/get_usable_bytes?uid=" + uid)
        }

        Log.i("BTServer", "Message received. Contents: " + message)
        if(message.length > 10 && message.substring(0,10).equals("ClientID: ")){
            var user_id = message.substring(10)
            URLLookUp().execute("http://get-data-share.com/payments/get_usable_bytes?uid="+ user_id)
            //TODO send server the client id here. Ask sai for what exactly to send.
        }else if(message.length > 9 && message.substring(0,9).equals("BDevice: ")){
            var BDevice = message.substring(9)
            Toast.makeText(this, BDevice, Toast.LENGTH_LONG).show()
        }
    }

    override fun onBluetoothDiscover(device: BluetoothDevice) {

    }

    override fun onDestroy() {
        btServer!!.close()
        super.onDestroy()
    }

    var client = OkHttpClient()

    fun run(url: String): Response {
        if (url.substring(0..51) == "http://get-data-share.com/payments/get_usable_bytes"){
            val request: Request = Request.Builder()
                    .header("access-token", PreferenceManager.getDefaultSharedPreferences(this).getString("AuthenticationToken", ""))
                    .header("uid", PreferenceManager.getDefaultSharedPreferences(this).getString("uid", ""))
                    .header("client", PreferenceManager.getDefaultSharedPreferences(this).getString("client", ""))
                    .url(url)
                    .build()

            val response = client.newCall(request).execute()
            val str = response.body()!!.string()

            val obj = JSONObject(str)
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("receiver_bytes", obj!!.get("bytes").toString()).apply()

            return response
        }
        else if (url == "http://get-data-share.com/payments/execute_transaction"){
            val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("uid", PreferenceManager.getDefaultSharedPreferences(this).getString("receiver_uid", ""))
                    .addFormDataPart("bytes", used_bytes.toString())
                    .build()

            val request = Request.Builder()
                    .header("access-token", PreferenceManager.getDefaultSharedPreferences(this).getString("AuthenticationToken", ""))
                    .header("uid", PreferenceManager.getDefaultSharedPreferences(this).getString("uid", ""))
                    .header("client", PreferenceManager.getDefaultSharedPreferences(this).getString("client", ""))
                    .url(url)
                    .post(requestBody)
                    .build()

            return client.newCall(request).execute()
        }
        else{
            val request: Request = Request.Builder()
                    .url(url)
                    .build()

            return client.newCall(request).execute()
        }
    }
    private inner class URLLookUp : AsyncTask<String, Void, Response>() {
        override fun doInBackground(vararg str: String): Response? {
            return run(str[0])
        }

        override fun onPostExecute(result: Response?) {
            try {
                JSONObject(result!!.body().toString())
            } catch (ex: JSONException) {
                sendMessage(result!!.headers().toString() + "\n" + result.body()!!.string())
            }
        }
    }
}