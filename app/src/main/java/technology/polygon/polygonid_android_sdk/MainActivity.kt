package technology.polygon.polygonid_android_sdk

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private val sharedFlow = MutableSharedFlow<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        findViewById<Button>(R.id.button_init).setOnClickListener {
            viewModel.init(applicationContext)
        }

        findViewById<Button>(R.id.button_start_download).setOnClickListener {
            lifecycleScope.launch {
                PolygonIdSdk.getInstance().getFlow("downloadCircuits").collect { info ->
                    findViewById<TextView>(R.id.text_result).text = info.toString()
                }
            }

            viewModel.startDownload(applicationContext)
        }

        findViewById<Button>(R.id.button_get_env).setOnClickListener {
            viewModel.getEnv(applicationContext)
        }

        findViewById<Button>(R.id.button_set_env).setOnClickListener {
            viewModel.setEnv(applicationContext)
        }

        findViewById<Button>(R.id.button_add_identity).setOnClickListener {
            viewModel.addIdentity(applicationContext)
        }

        findViewById<Button>(R.id.button_get_identities).setOnClickListener {
            viewModel.getIdentities(applicationContext)
        }

        findViewById<Button>(R.id.button_get_did_identifier).setOnClickListener {
            viewModel.getDidIdentifier(applicationContext)
        }

        findViewById<Button>(R.id.button_authenticate).setOnClickListener {
            viewModel.authenticate(applicationContext)
        }

        findViewById<Button>(R.id.button_stop_stream).setOnClickListener {
            viewModel.stopStream(applicationContext)
        }
    }
}