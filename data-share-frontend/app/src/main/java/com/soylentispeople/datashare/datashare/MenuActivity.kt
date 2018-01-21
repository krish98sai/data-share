package com.soylentispeople.datashare.datashare

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

/**
 * Created by adity on 1/20/2018.
 */
class MenuActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu_activity)
        findViewById<Button>(R.id.ProvideMenu).setOnClickListener {
            var intent = Intent(this, ProviderSettings::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.ReciveMenu).setOnClickListener {
            var intent = Intent(this, BTClientActivity::class.java)
            startActivity(intent)

            /*changes bluetooth name*/


        }
        findViewById<Button>(R.id.addCredit).setOnClickListener {
            //var intent = Intent(this, this)
        }
    }
}