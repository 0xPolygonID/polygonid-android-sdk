package technology.polygon.polygonid_android_sdk.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import technology.polygon.polygonid_android_sdk.MainActivity
import technology.polygon.polygonid_android_sdk.R
import technology.polygon.polygonid_android_sdk.qr_code_scanner.QRCodeScannerActivity

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var viewModel: AuthenticationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        viewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]

        findViewById<Button>(R.id.button_authenticate).setOnClickListener {
            val intent = Intent(this, QRCodeScannerActivity::class.java)
            startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE)
        }

        findViewById<Button>(R.id.button_continue).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTHENTICATE_REQUEST_CODE || requestCode == FETCH_CREDENTIAL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val scanResult = data?.getStringExtra("SCAN_RESULT")
                when (requestCode) {
                    AUTHENTICATE_REQUEST_CODE -> {
                        viewModel.authenticate(applicationContext, scanResult ?: "")
                    }

                    /* FETCH_CREDENTIAL_REQUEST_CODE -> {
                         viewModel.fetch(applicationContext, scanResult ?: "")
                     }*/
                }
                viewModel.authenticate(applicationContext, scanResult ?: "")
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Handle cancel
            }
        }
    }

    companion object {
        const val AUTHENTICATE_REQUEST_CODE = 0
        const val FETCH_CREDENTIAL_REQUEST_CODE = 1
    }
}