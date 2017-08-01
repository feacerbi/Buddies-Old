package br.com.felipeacerbi.buddies.activities

import android.app.Activity
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import br.com.felipeacerbi.buddies.R
import com.dlazaro66.qrcodereaderview.QRCodeReaderView
import kotlinx.android.synthetic.main.activity_qrcode.*
import kotlinx.android.synthetic.main.qrcode_reader_view.*

class QRCodeActivity :
        AppCompatActivity(),
        QRCodeReaderView.OnQRCodeReadListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        val TAG = "QRCodeActivity"
        val QR_CODE_TEXT = "qr_result"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)

        startCamera()
    }

    fun startCamera() {
        layoutInflater.inflate(R.layout.qrcode_reader_view, constraint)

        qrdecoderview.setOnQRCodeReadListener(this)

        // Use this function to enable/disable decoding
        qrdecoderview.setQRDecodingEnabled(true)

        // Use this function to change the autofocus interval (default is 5 secs)
        qrdecoderview.setAutofocusInterval(2000L)

        // Use this function to enable/disable Torch
        qrdecoderview.setTorchEnabled(false)

        // Use this function to set front camera preview
        //qrdecoderview.setFrontCamera()

        // Use this function to set back camera preview
        qrdecoderview.setBackCamera()

        qrdecoderview.startCamera()
    }

    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed in View
    override fun onQRCodeRead(text: String, points: Array<PointF>) {
        val resultIntent = Intent(this, MainActivity::class.java)

        resultIntent.putExtra(QR_CODE_TEXT, text)
        setResult(RESULT_OK, resultIntent)

        finish()
    }

    override fun onPause() {
        super.onPause()
        qrdecoderview.stopCamera()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
