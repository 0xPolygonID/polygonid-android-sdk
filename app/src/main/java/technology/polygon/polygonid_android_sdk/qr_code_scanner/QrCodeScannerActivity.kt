package technology.polygon.polygonid_android_sdk.qr_code_scanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator

class QRCodeScannerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scanner = IntentIntegrator(this)
        scanner.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if(result != null) {
            if(result.contents == null) {
                setResult(Activity.RESULT_CANCELED)
            } else {
                val intent = Intent().apply { putExtra("SCAN_RESULT", result.contents) }
                setResult(Activity.RESULT_OK, intent)
            }
            finish()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}