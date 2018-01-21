package com.soylentispeople.datashare.datashare

/**
 * Created by krish98sai on 1/21/2018.
 */

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.Toast
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Created by adity on 1/20/2018.
 */
class CreditActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit)
        val bGetBalance = findViewById<Button>(R.id.get_balance)
        val bAddCredit = findViewById<Button>(R.id.add_credit)
        val bRedeemCredit = findViewById<Button>(R.id.redeem_credit)

        URLLookUp().execute("http://get-data-share.com/payments/client_token")

        bGetBalance.setOnClickListener{
            URLLookUp().execute("http://get-data-share.com/payments/get_credit")
        }
        bAddCredit.setOnClickListener{

        }
        bRedeemCredit.setOnClickListener{

        }

    }

    var client = OkHttpClient()

    fun run(url : String): JSONObject{
        var request = Request.Builder()
                .header("access-token", PreferenceManager.getDefaultSharedPreferences(this).getString("AuthenticationToken", ""))
                .header("uid", PreferenceManager.getDefaultSharedPreferences(this).getString("uid", ""))
                .header("client", PreferenceManager.getDefaultSharedPreferences(this).getString("client", ""))
                .url(url)
                .build()
        val response = client.newCall(request).execute()
        var str = response.body()!!.string()
        return JSONObject(str)
    }

    fun creditHandler(result: JSONObject?){
        if (result!!.has("client_token")){
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("client_token", result.get("client_token").toString()).apply()
        }
        else{
            val strCredit = "You have a total of " + result.get("credit") + " on your account!"
            Toast.makeText(this, strCredit, Toast.LENGTH_LONG).show()
        }
    }

    private inner class URLLookUp : AsyncTask<String, Void, JSONObject>() {
        override fun doInBackground(vararg str: String): JSONObject? {
            return run(str[0])
        }

        override fun onPostExecute(result: JSONObject?) {
            creditHandler(result)
        }
    }
}