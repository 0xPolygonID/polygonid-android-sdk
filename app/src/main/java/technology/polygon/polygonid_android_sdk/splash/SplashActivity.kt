package technology.polygon.polygonid_android_sdk.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import technology.polygon.polygonid_android_sdk.MainActivity
import technology.polygon.polygonid_android_sdk.PolygonIdSdk
import technology.polygon.polygonid_android_sdk.R
import technology.polygon.polygonid_protobuf.DownloadInfoEntity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        viewModel = ViewModelProvider(this)[SplashViewModel::class.java]
        viewModel.init(applicationContext)

        lifecycleScope.launch {
            viewModel.uiState.collect { splashState ->
                if (splashState.isDownloaded == null) {
                    return@collect
                }

                if (splashState.isDownloaded) {
                    findViewById<TextView>(R.id.text_check_download_circuits).text = "Downloaded"
                }

                if (!splashState.isDownloaded) {
                    findViewById<TextView>(R.id.text_check_download_circuits).text =
                        "Not Downloaded Yet"
                }
            }

        }

        findViewById<Button>(R.id.button_start_download).setOnClickListener {
            lifecycleScope.launch {
                PolygonIdSdk.getInstance().getDownloadCircuitsFlow().collectLatest { info ->
                    var progress = ""

                    if (info is DownloadInfoEntity.DownloadInfoOnProgress) {
                        progress = "Downloaded ${info.downloaded} of ${info.contentLength} bytes"
                    }

                    if (info is DownloadInfoEntity.DownloadInfoOnDone) {
                        progress = "Download completed"
                    }

                    if (info is DownloadInfoEntity.DownloadInfoOnError) {
                        progress = "Download failed"
                    }
                    findViewById<TextView>(R.id.downloadText).text = progress
                }
            }

            viewModel.startDownload(applicationContext)
        }

        findViewById<Button>(R.id.button_check_download_circuits).setOnClickListener {
            viewModel.checkDownloadCircuits(applicationContext)
        }

        findViewById<Button>(R.id.button_cancel_download_circuits).setOnClickListener {
            viewModel.cancelDownloadCircuits(applicationContext)
        }

        //navigate to main activity from here
        findViewById<Button>(R.id.next_page).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }


}