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
        findViewById<Button>(R.id.register).setOnClickListener { //TODO HTTP requests
            var intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)

        }
        if(PreferenceManager.getDefaultSharedPreferences(this).getString("AuthenticationToken", "").equals("")){
            setContentView(R.layout.login_page);


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

    fun run(url: String): Response {
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
        return response
    }

    private inner class URLLookUp : AsyncTask<String, Void, Response>() {
        override fun doInBackground(vararg str: String): Response? {
            return run(str[0]);
        }

        override fun onPostExecute(result: Response?) {
            Log.e("something1",result!!.body().toString())
            if(!PreferenceManager.getDefaultSharedPreferences(this@LoginActivity).getString("AuthenticationToken", "").equals("")) {
                var obj = JSONObject(result.body()!!.string())
                if(obj.get("status").toString().equals("success")){
                    var intent = Intent(this@LoginActivity, MenuActivity::class.java)
                    startActivity(intent)
                }
            }else{
                var obj = JSONObject(result.body()!!.string())
                if( obj.has("errors") && (obj.get("errors") as JSONArray).get(0).equals("Invalid login credentials. Please try again.")){

                }else{
                    PreferenceManager.getDefaultSharedPreferences(this@LoginActivity).edit().putString("AuthenticationToken", result.header("access-token").toString()).apply();
                    PreferenceManager.getDefaultSharedPreferences(this@LoginActivity).edit().putString("uid", result.header("uid").toString()).apply();
                    var intent = Intent(this@LoginActivity, MenuActivity::class.java)
                    startActivity(intent)
                }

            }
        }
    }

}