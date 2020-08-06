package com.pdftron.barcode

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import me.dm7.barcodescanner.zxing.ZXingScannerView

class SimpleScannerActivity : Activity(), ZXingScannerView.ResultHandler {

    val TAG = SimpleScannerActivity::class.java.name
    private var mScannerView: ZXingScannerView? = null

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        mScannerView = ZXingScannerView(this)   // Programmatically initialize the scanner view
        setContentView(mScannerView)                // Set the scanner view as the content view
    }

    public override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this) // Register ourselves as a handler for scan results.
        mScannerView!!.startCamera()          // Start camera on resume
    }

    public override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera()           // Stop camera on pause
    }

    override fun handleResult(rawResult: com.google.zxing.Result) {
        // Do something with the result here
        Log.v(TAG, rawResult.getText()) // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()) // Prints the scan format (qrcode, pdf417 etc.)

        // return intent
        val resultIntent = Intent()
        resultIntent.putExtra("scan_result", rawResult.text)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}