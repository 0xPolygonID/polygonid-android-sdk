package technology.polygon.polygonid_android_sdk.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import technology.polygon.polygonid_android_sdk.PolygonIdSdk
import technology.polygon.polygonid_android_sdk.check
import technology.polygon.polygonid_protobuf.EnvEntityOuterClass

const val apiKey = "b936512326ea4e22a2a8552b6e9db7b7"

data class SplashState(
    val isDownloaded: Boolean? = null,
)

class SplashViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SplashState())
    val uiState: StateFlow<SplashState> = _uiState.asStateFlow()

    fun init(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.init(
                context = context,
                env = EnvEntityOuterClass.EnvEntity.newBuilder().setBlockchain("polygon")
                    .setNetwork("mumbai")
                    .setWeb3Url("https://polygon-mumbai.infura.io/v3/")
                    .setWeb3RdpUrl("wss://polygon-mumbai.infura.io/v3/").setWeb3ApiKey(apiKey)
                    .setIdStateContract("0x134B1BE34911E39A8397ec6289782989729807a4")
                    .setPushUrl("https://push-staging.polygonid.com/api/v1").build().check()
            )

            PolygonIdSdk.getInstance().
        }
    }

    fun startDownload(context: Context) {
        PolygonIdSdk.getInstance().startDownloadCircuits(context = context).thenAccept {
            println("Stream started")
        }
    }

    fun checkDownloadCircuits(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().isAlreadyDownloadedCircuitsFromServer(context)
                .thenApply { isDownloaded ->
                    _uiState.update { currentState ->
                        currentState.copy(isDownloaded = isDownloaded)
                    }
                }
        }
    }

    fun cancelDownloadCircuits(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().cancelDownloadCircuits(context).thenApply {
                println("cancelDownloadCircuits: $it")
            }
        }
    }
}