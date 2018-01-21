package com.soylentispeople.datashare.datashare

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.app.AlertDialog;
import android.os.Handler;
import android.net.TrafficStats;
import android.util.Log

/**
 * Created by sidpu on 1/20/2018.
 */
class BTDataTransmission : BTActivity(), BTClientCallbacks {

    var self: BTDataTransmission = this

    private var mHandler : Handler = Handler()
    private var mStartRx : Long = 0;
    private var mStartTx : Long = 0;
    private var callbacks : BTClientCallbacks = BTClientCallbacks
    //private var myIntent : Intent = Intent(this, BTDataTransmission::class.java)

    override fun onBluetoothDiscover(device: BluetoothDevice) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun BTDataTranssmion() {

    }

    override fun onConnected() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionFail() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDisconnect() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onMessageReceived(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, BTServerActivity::class.java)
        startActivity(intent)
        setContentView(R.layout.activity_main)

        mStartRx = TrafficStats.getTotalRxBytes()
        mStartTx = TrafficStats.getTotalTxBytes()

        if (mStartRx.equals(TrafficStats.UNSUPPORTED) || mStartTx.equals(TrafficStats.UNSUPPORTED)) {
            var alert : AlertDialog.Builder = AlertDialog.Builder(this)
            alert.setTitle("Uh Oh!")
            alert.setMessage("Your device does not support traffic stat monitoring.")
            alert.show()
        } else {
           // mHandler.postDelayed(mRunnable, 15000)
        }

        val r : Runnable = thread(mStartRx, mStartTx, this, callbacks)
        var t1 = Thread(r)
        t1.start()

    }


    /*private val mRunnable : Runnable = Runnable() {
        @Override
        fun run() {

            //val RX : TextView = findViewById<TextView>(R.id.RX)
            //val TX: TextView = findViewById<TextView>(R.id.TX)
            var rxBytes = TrafficStats.getTotalRxBytes() - mStartRx
            var txBytes = TrafficStats.getTotalTxBytes() - mStartTx
            //long rxBytes = TrafficStats . getTotalRxBytes () - mStartRx;
            //RX.setText(Long.toString(rxBytes));
            //long txBytes = TrafficStats . getTotalTxBytes () - mStartTx;
            //TX.setText(Long.toString(txBytes));
            mHandler.postDelayed(mRunnable, 15000);
            //TextView RX =(TextView) findViewById (R.id.RX);
            //TextView TX =(TextView) findViewById (R.id.TX);

        }
    }*/


    class thread(mStartRx: Long, mStartTx: Long, myActivity : Activity, myCallbacks : BTClientCallbacks) : Runnable {

        var mmStartRx : Long = mStartRx
        var mmStartTx : Long = mStartTx
        var activity : Activity = myActivity
        var callbacks : BTClientCallbacks = myCallbacks

        fun thread(mStartRx : Long, mStartTx : Long, myActivity: Activity) {

        }
        override fun run() {
            val btClient = BTClient(activity, callbacks as BTClientActivity)
            //btClient.connect(Data-Share Provider, UUID.fromString(getString(R.string.GLOBAL_UUID)))
            var stringRx = mmStartRx.toString()
            var stringTx = mmStartTx.toString()
            //btClient.sendMessage("Hello World")
            btClient.sendMessage("Packets sent: " + stringRx + " , packets received: " + stringTx)
            Log.d("TAG", "Packets sent: " + stringRx + " , packets received: " + stringTx)
        }

    }

        //when your math tutor is the school shooter (watch video)
    //use thread (figure out how to loop), pass in btclient, and keep sending data as
    //messages
    //runonuithread (schedules runnable inside main thread)
    //UI methods have to be run on main thread (onCreate)
}