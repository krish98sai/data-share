package com.soylentispeople.datashare.datashare

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


/**
 * Created by adity on 1/20/2018.
 */
class LoginActivity : Activity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(PreferenceManager.getDefaultSharedPreferences(this).getString("AuthenticationToken", "").equals("")){
            setContentView(R.layout.login_page);
            (findViewById<Button>(R.id.register) as Button).setOnClickListener { //TODO HTTP requests
                var intent = Intent(this, SignupActivity::class.java)
                startActivity(intent)

            }

            findViewById<Button>(R.id.login).setOnClickListener {
                URLLookUp().execute("http://get-data-share.com/auth/sign_in")

            }
        }else{
            //TODO make more secure by checking token if internet connection
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if(activeNetworkInfo == null || !(activeNetworkInfo.isConnected)){
                Toast.makeText(applicationContext, "Not connected to internet", Toast.LENGTH_SHORT).show()
                //redirect to receiver page
                var intent = Intent(this, BTClientActivity::class.java)
                startActivity(intent)
            }else {
                Toast.makeText(applicationContext, "Connected to internet", Toast.LENGTH_SHORT).show()
                URLLookUp().execute("http://get-data-share.com/check_token")
            }


        }
    }

    var client = OkHttpClient()

    fun run(url: String): JSONObject {
        lateinit var request: Request
        if(!PreferenceManager.getDefaultSharedPreferences(this).getString("AuthenticationToken", "").equals("")) {


            request = Request.Builder()
                    .header("access-token", PreferenceManager.getDefaultSharedPreferences(this).getString("AuthenticationToken", ""))
                    .header("uid", PreferenceManager.getDefaultSharedPreferences(this).getString("uid", ""))
                    .header("client", PreferenceManager.getDefaultSharedPreferences(this).getString("client", ""))
                    .url(url)
                    .build()
        }else{
            val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("email", (findViewById<EditText>(R.id.email) as EditText).text.toString())
                    .addFormDataPart("password", (findViewById<EditText>(R.id.password) as EditText).text.toString())
                    .build()

            request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()
        }
        val response = client.newCall(request).execute()
        var str = response.body()!!.string()
        Log.e("Something123", str)
        var obj = JSONObject(str)

        if(!PreferenceManager.getDefaultSharedPreferences(this).getString("AuthenticationToken", "").equals("")) {

            if(obj!!.get("status").toString().equals("success")){
                var intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
            }
        }else{

            if( obj!!.has("errors") && (obj.get("errors") as JSONArray).get(0).equals("Invalid login credentials. Please try again.")){
                Log.e("what is happening?", "THis is happpeing")
            }else{
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("AuthenticationToken", response.header("access-token").toString()).apply()
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("uid", response.header("uid").toString()).apply()
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("client", response.header("client").toString()).apply()
                var intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
            }

        }


        return obj
    }

    private inner class URLLookUp : AsyncTask<String, Void, JSONObject>() {
        override fun doInBackground(vararg str: String): JSONObject? {
            return run(str[0])
        }

        override fun onPostExecute(result: JSONObject?) {


        }
    }

}