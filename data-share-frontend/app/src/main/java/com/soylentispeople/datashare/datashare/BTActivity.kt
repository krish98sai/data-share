package com.soylentispeople.datashare.datashare

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast

/**
 * This class is identical to an AppCompatActivity, except it contains
 * the method scan(), and the abstract method onBluetoothDiscover().
 *
 * @author Yuval Shabtai
 */
abstract class BTActivity : AppCompatActivity() {

    private val REQUEST_LOCATION = 1
    private val REQUEST_ENABLE_BT = 2

    /**
     * setupStatus determines whether or not bluetooth is setup
     * and ready to use.
     * It is false if it is not ready to use, and true otherwise.
     */
    open var setupStatus : Boolean = false

    protected var mBTAdapter : BluetoothAdapter? = null
    protected val mBroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothDevice.ACTION_FOUND) {
                //Call onBluetoothDiscover once a bluetooth device has been discovered
                onBluetoothDiscover(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE))
            } else if(intent.action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
                //Unregister receiver when discovery is finished
                unregisterBTReceiver()
            } else if(intent.action == BluetoothAdapter.ACTION_DISCOVERY_STARTED) {
                Log.e("Bluetooth", "Discovery started")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupStatus = setupAdapter()
    }

    /**
     * Sets up the bluetooth adapter, so that it
     * can be used further. Requests required permissions
     *
     * @return True if the setup was successful, false otherwise
     */
    protected fun setupAdapter() : Boolean {
        mBTAdapter = BluetoothAdapter.getDefaultAdapter()

        //Check if device supports bluetooth
        if(mBTAdapter == null) {
            Toast.makeText(this, "Device does not support bluetooth", Toast.LENGTH_SHORT).show()
            return false
        }

        //Request location permission (required for bluetooth access)
        if(Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        REQUEST_LOCATION)

                //If permission was not granted, don't proceed
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.location_access_error), Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        }

        //If bluetooth is turned off, ask to turn it on
        if(!mBTAdapter!!.isEnabled) {
            val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT)

            if(!mBTAdapter!!.isEnabled) {
                Toast.makeText(this, "Bluetooth must be enabled to access the app's services", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        //Setup successful
        return true
    }

    /**
     * Called whenever a new bluetooth device was discovered
     * by scan().
     *
     * @param device The device that was discovered.
     */
    protected abstract fun onBluetoothDiscover(device: BluetoothDevice)

    /**
     * Scans for nearby bluetooth devices. Calls onBluetoothDiscover
     * for every discovered device.
     */
    protected fun scan() {
        if(!setupStatus) {
            Toast.makeText(this, "Cannot scan. Bluetooth is not set up", Toast.LENGTH_SHORT).show()
            return
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)

        registerReceiver(mBroadcastReceiver, filter)

        if(mBTAdapter!!.isDiscovering) mBTAdapter!!.cancelDiscovery()

        mBTAdapter!!.startDiscovery()
    }

    /**
     * Unregisters the bluetooth receiver of this class.
     */
    private fun unregisterBTReceiver() {
        unregisterReceiver(mBroadcastReceiver)
    }

    override fun onDestroy() {
        unregisterBTReceiver()
        super.onDestroy()
    }
}