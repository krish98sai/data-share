package com.soylentispeople.datashare.datashare

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by adity on 1/20/2018.
 */
class SignupActivity: Activity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_page);
        findViewById<Button>(R.id.register).setOnClickListener { //TODO HTTP requests
            if((findViewById(R.id.password)as EditText).text.toString().equals((findViewById(R.id.password2)as EditText).text.toString())){
                if((findViewById(R.id.password)as EditText).text.toString().length > 7){
                    
                }
            }

        }
    }


    var client = OkHttpClient()

    fun run(url: String): Response {
        lateinit var request: Request

            val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("email", (findViewById(R.id.email) as EditText).text.toString())
                    .addFormDataPart("password", (findViewById(R.id.password) as EditText).text.toString())
                    .addFormDataPart("phone", (findViewById(R.id.phoneNumber) as EditText).text.toString())
                    .build()

            request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

        val response = client.newCall(request).execute()
        return response
    }

    private inner class URLLookUp : AsyncTask<String, Void, Response>() {
        override fun doInBackground(vararg str: String): Response? {
            return run(str[0]);
        }

        override fun onPostExecute(result: Response?) {
            var obj = JSONObject(result!!.body()!!.string())
            if( obj.has("errors") ){

            }else{
                var intent = Intent(this@SignupActivity, MenuActivity::class.java)
                startActivity(intent)
            }
        }
    }
}