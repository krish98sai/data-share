package com.soylentispeople.datashare.datashare

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
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
            findViewById<Button>(R.id.register).setOnClickListener { //TODO HTTP requests
                var intent = Intent(this, SignupActivity::class.java)
                startActivity(intent)

            }

            findViewById<Button>(R.id.login).setOnClickListener {
                URLLookUp().execute("http://get-data-share.com/auth/sign_in")

            }
        }else{
            //TODO make more secure by checking tocken if internet connection

            //go to next Page
            var intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)



        }
    }

    var client = OkHttpClient()

    fun run(url: String): JSONObject {
        lateinit var request: Request
        if(!PreferenceManager.getDefaultSharedPreferences(this).getString("AuthenticationToken", "").equals("")) {
            val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .build()

            request = Request.Builder()
                    .header("access-token", PreferenceManager.getDefaultSharedPreferences(this).getString("AuthenticationToken", ""))
                    .header("uid", PreferenceManager.getDefaultSharedPreferences(this).getString("AuthenticationToken", ""))
                    .url(url)
                    .post(requestBody)
                    .build()
        }else{
            val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("email", (findViewById(R.id.email) as EditText).text.toString())
                    .addFormDataPart("password", (findViewById(R.id.password) as EditText).text.toString())
                    .build()

            request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()
        }
        val response = client.newCall(request).execute()
        var obj = JSONObject(response.body()!!.string())

        if(!PreferenceManager.getDefaultSharedPreferences(this).getString("AuthenticationToken", "").equals("")) {

            if(obj!!.get("status").toString().equals("success")){
                var intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
            }
        }else{

            if( obj!!.has("errors") && (obj.get("errors") as JSONArray).get(0).equals("Invalid login credentials. Please try again.")){
                Log.e("what is happening?", "THis is happpeing")
            }else{
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("AuthenticationToken", response.header("access-token").toString()).apply();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString("uid", response.header("uid").toString()).apply();
                var intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
            }

        }


        return obj
    }

    private inner class URLLookUp : AsyncTask<String, Void, JSONObject>() {
        override fun doInBackground(vararg str: String): JSONObject? {
            return run(str[0]);
        }

        override fun onPostExecute(result: JSONObject?) {


        }
    }

}