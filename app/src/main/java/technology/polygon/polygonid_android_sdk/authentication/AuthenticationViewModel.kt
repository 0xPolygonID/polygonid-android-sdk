package technology.polygon.polygonid_android_sdk.authentication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import technology.polygon.polygonid_android_sdk.PolygonIdSdk
import technology.polygon.polygonid_android_sdk.secret
import technology.polygon.polygonid_protobuf.iden3_message.Iden3MessageEntityOuterClass

data class AuthenticationState(
    val did: String? = null,
)

class AuthenticationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthenticationState())
    val uiState: StateFlow<AuthenticationState> = _uiState.asStateFlow()

    fun authenticate(context: Context, authMessage: String) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getIden3Message(
                context, authMessage
            ).thenApply { message ->
                PolygonIdSdk.getInstance().getPrivateKey(
                    context = context, secret = secret
                ).thenApply { privateKey ->
                    PolygonIdSdk.getInstance().getDidIdentifier(
                        context = context,
                        privateKey = privateKey,
                        blockchain = "polygon",
                        network = "mumbai",
                    ).thenApply { did ->
                        PolygonIdSdk.getInstance().authenticate(
                            context = context,
                            message = message as Iden3MessageEntityOuterClass.AuthIden3MessageEntity,
                            genesisDid = did,
                            privateKey = privateKey
                        ).thenAccept {
                            println("Authenticated")
                        }.exceptionally {
                            println("Authentication Error: $it")
                            null
                        }
                    }
                }
            }
        }
    }
}