package technology.polygon.polygonid_android_sdk

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import technology.polygon.polygonid_protobuf.DownloadInfoEntity.DownloadInfoOnDone
import technology.polygon.polygonid_protobuf.DownloadInfoEntity.DownloadInfoOnError
import technology.polygon.polygonid_protobuf.DownloadInfoEntity.DownloadInfoOnProgress

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
                PolygonIdSdk.getInstance().getFlow("downloadCircuits").collectLatest { info ->


                    var progress = ""

                    if (info is DownloadInfoOnProgress) {
                        progress = "Downloaded ${info.downloaded} of ${info.contentLength} bytes"
                    }

                    if (info is DownloadInfoOnDone) {
                        progress = "Download completed"
                    }

                    if (info is DownloadInfoOnError) {
                        progress = "Download failed"
                    }
                    findViewById<TextView>(R.id.text_result).text = progress
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

        findViewById<Button>(R.id.button_get_claims).setOnClickListener {
            // viewModel.getDidIdentifier(applicationContext)
            viewModel.getClaims(applicationContext)
        }

        findViewById<Button>(R.id.button_authenticate).setOnClickListener {
            viewModel.authenticate(applicationContext)
        }

        findViewById<Button>(R.id.button_fetch).setOnClickListener {
            // viewModel.stopStream(applicationContext)
            viewModel.fetch(applicationContext)
        }

        findViewById<Button>(R.id.button_get_identity).setOnClickListener {
            viewModel.getIdentity(applicationContext)
        }

        findViewById<Button>(R.id.button_backup_identity).setOnClickListener {
            viewModel.backupIdentity(applicationContext)
        }

        findViewById<Button>(R.id.button_restore_identity).setOnClickListener {
            viewModel.restoreIdentity(applicationContext)
        }

        findViewById<Button>(R.id.button_get_did_entity).setOnClickListener {
            viewModel.getDidEntity(applicationContext)
        }

        findViewById<Button>(R.id.button_check_identity_validity).setOnClickListener {
            viewModel.checkIdentityValidity(applicationContext)
        }

        findViewById<Button>(R.id.button_get_state).setOnClickListener {
            viewModel.getState(applicationContext)
        }

        findViewById<Button>(R.id.button_remove_identity).setOnClickListener {
            viewModel.removeIdentity(applicationContext)
        }

        findViewById<Button>(R.id.button_remove_profile).setOnClickListener {
            viewModel.removeProfile(applicationContext)
        }

        findViewById<Button>(R.id.button_sign_message).setOnClickListener {
            viewModel.sign(applicationContext)
        }

        findViewById<Button>(R.id.button_claims_by_ids).setOnClickListener {
            viewModel.getClaimsByIds(applicationContext)
        }

        findViewById<Button>(R.id.button_remove_claim).setOnClickListener {
            viewModel.removeClaim(applicationContext)
        }

        findViewById<Button>(R.id.button_remove_claims_by_ids).setOnClickListener {
            viewModel.removeClaims(applicationContext)
        }

        findViewById<Button>(R.id.button_save_claims).setOnClickListener {
            viewModel.saveClaims(applicationContext)
        }

        findViewById<Button>(R.id.button_check_download_circuits).setOnClickListener {
            viewModel.checkDownloadCircuits(applicationContext)
        }

        findViewById<Button>(R.id.button_cancel_download_circuits).setOnClickListener {
            viewModel.cancelDownloadCircuits(applicationContext)
        }

        findViewById<Button>(R.id.button_add_interaction).setOnClickListener {
            viewModel.addInteraction(applicationContext)
        }

        findViewById<Button>(R.id.button_get_interactions).setOnClickListener {
            viewModel.getInteractions(applicationContext)
        }

        findViewById<Button>(R.id.button_get_claims_from_iden3message).setOnClickListener {
            viewModel.getClaimsFromIden3Message(applicationContext)
        }

        findViewById<Button>(R.id.button_get_filters_from_iden3message).setOnClickListener {
            viewModel.getFiltersFromIden3Message(applicationContext)
        }

        findViewById<Button>(R.id.button_get_schemas_from_iden3message).setOnClickListener {
            viewModel.getSchemasFromIden3Message(applicationContext)
        }

        findViewById<Button>(R.id.button_get_vocabs_from_iden3message).setOnClickListener {
            viewModel.getVocabsFromIden3Message(applicationContext)
        }

        findViewById<Button>(R.id.button_get_proofs_from_iden3message).setOnClickListener {
            viewModel.getProofsFromIden3Message(applicationContext)
        }

        findViewById<Button>(R.id.button_remove_interactions).setOnClickListener {
            viewModel.removeInteractions(applicationContext)
        }

        findViewById<Button>(R.id.button_update_interaction).setOnClickListener {
            viewModel.updateInteraction(applicationContext)
        }
    }
}