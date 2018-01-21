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
import com.braintreepayments.api.dropin.DropInRequest
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.braintreepayments.api.dropin.DropInActivity
import com.braintreepayments.api.dropin.DropInResult
import android.content.Intent
import android.support.annotation.IntegerRes
import android.util.Log
import android.widget.EditText
import okhttp3.MultipartBody
import okhttp3.Response


class CreditActivity: Activity() {
    var amount = 0
    lateinit var nonce : DropInResult
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
            if (!PreferenceManager.getDefaultSharedPreferences(this).getString("client_token", "").equals("")) {
                amount = Integer.parseInt((findViewById<EditText>(R.id.amount) as EditText).text.toString())
                val dropInRequest = DropInRequest()
                        .clientToken(PreferenceManager.getDefaultSharedPreferences(this).getString("client_token", ""))
                startActivityForResult(dropInRequest.getIntent(this), 711)
            }
        }
        bRedeemCredit.setOnClickListener{

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == 711) {
            if (resultCode == Activity.RESULT_OK) {
                nonce = data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                // use the result to update your UI and send the payment method nonce to your server
                URLLookUp().execute("http://get-data-share.com/payments/checkout")
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // the user canceled
            } else {
                // handle errors here, an exception may be available in
                val error = data.getSerializableExtra(DropInActivity.EXTRA_ERROR) as Exception
                Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    var client = OkHttpClient()

    fun run(url : String): JSONObject{
        lateinit var request : Request
        if(!url.equals("http://get-data-share.com/payments/checkout")) {
            request = Request.Builder()
                    .header("access-token", PreferenceManager.getDefaultSharedPreferences(this).getString("AuthenticationToken", ""))
                    .header("uid", PreferenceManager.getDefaultSharedPreferences(this).getString("uid", ""))
                    .header("client", PreferenceManager.getDefaultSharedPreferences(this).getString("client", ""))
                    .url(url)
                    .build()
        }
        else{
            val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("amount", amount.toString())
                    .addFormDataPart("payment_method_nonce", nonce.paymentMethodNonce!!.nonce)
                    .build()

            request = Request.Builder()
                    .header("access-token", PreferenceManager.getDefaultSharedPreferences(this).getString("AuthenticationToken", ""))
                    .header("uid", PreferenceManager.getDefaultSharedPreferences(this).getString("uid", ""))
                    .header("client", PreferenceManager.getDefaultSharedPreferences(this).getString("client", ""))
                    .url(url)
                    .post(requestBody)
                    .build()


        }
        val response = client.newCall(request).execute()
        var str = response.body()!!.string()
        return JSONObject(str)
    }

    fun creditHandler(result: JSONObject?){
        if (result!!.has("client_token")){
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("client_token", result.get("client_token").toString()).apply()
        }
        else if (result.has("credit")){
            val strCredit = "You have a total of " + result.get("credit") + " on your account!"
            Toast.makeText(this, strCredit, Toast.LENGTH_LONG).show()
        }
        else{
            URLLookUp().execute("http://get-data-share.com/payments/get_credit")
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