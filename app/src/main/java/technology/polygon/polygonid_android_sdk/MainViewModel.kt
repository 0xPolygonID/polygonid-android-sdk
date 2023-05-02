package technology.polygon.polygonid_android_sdk

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import technology.polygon.polygonid_protobuf.EnvEntityOuterClass.EnvEntity
import technology.polygon.polygonid_protobuf.iden3_message.Iden3MessageEntityOuterClass
import java.math.BigInteger

const val TAG = "PolygonIdSdk"
const val secret = "some secret table yep fff so GJ"
const val apiKey = "b936512326ea4e22a2a8552b6e9db7b7"
const val authMessage = "{\"id\":\"0e9f213b-b9b2-4c7c-95e9-9e442672d739\",\"typ\":\"application/iden3comm-plain-json\",\"type\":\"https://iden3-communication.io/authorization/1.0/request\",\"thid\":\"0e9f213b-b9b2-4c7c-95e9-9e442672d739\",\"body\":{\"callbackUrl\":\"https://self-hosted-testing-backend-platform.polygonid.me/api/callback?sessionId=534374\",\"reason\":\"test flow\",\"scope\":[]},\"from\":\"did:polygonid:polygon:mumbai:2qFXmNqGWPrLqDowKz37Gq2FETk4yQwVUVUqeBLmf9\"}"

class MainViewModel : ViewModel() {
    fun init(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.init(
                context = context,
                env = EnvEntity.newBuilder().setBlockchain("polygon").setNetwork("mumbai")
                    .setWeb3Url("https://polygon-mumbai.infura.io/v3/")
                    .setWeb3RdpUrl("wss://polygon-mumbai.infura.io/v3/")
                    .setWeb3ApiKey(apiKey)
                    .setIdStateContract("0x134B1BE34911E39A8397ec6289782989729807a4")
                    .setPushUrl("https://push-staging.polygonid.com/api/v1").build().check()
            )
        }
    }

    fun getEnv(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getEnv(context = context).thenApply { env ->
                println("Blockchain: $env")
            }
        }
    }

    fun setEnv(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().callRaw(context = context, method = "setEnv").thenAccept {
                println("SetEnv Done")
            }
//            PolygonIdSdk.getInstance()
//                .setEnv(
//                    context = context, env = EnvEntity.newBuilder().setBlockchain("polygon")
//                        .setNetwork("mumbai")
//                        .setWeb3Url("www").setWeb3ApiKey("3RB23F").setWeb3RdpUrl("rdp")
//                        .setIdStateContract("98YIFJB").setPushUrl("www").build()
//                )
//                .thenAccept {
//                    println("SetEnv Done")
//                }
        }
    }

    fun addIdentity(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().addIdentity(
                context = context, secret = secret
            ).thenApply { identity ->
                println("Identity: $identity")

                PolygonIdSdk.getInstance().addProfile(
                    context = context,
                    genesisDid = identity.did,
                    privateKey = identity.privateKey,
                    profileNonce = BigInteger("1000")
                ).thenApply {
                    println("Profile added")
                }
            }
        }
    }

    fun getIdentities(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getIdentities(
                context = context
            ).thenApply { identities ->
                println("Identities: $identities")
            }.exceptionally { throwable ->
                println("Error: $throwable")
            }
        }
    }

    fun getDidIdentifier(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(
                context = context, secret = secret
            ).thenApply { privateKey ->
                PolygonIdSdk.getInstance().getEnv(context = context).thenApply { env ->
                    PolygonIdSdk.getInstance().getDidIdentifier(
                        context = context,
                        privateKey = privateKey,
                        blockchain = env.blockchain,
                        network = env.network,
                    ).thenApply { didIdentifier ->
                        println("DidIdentifier: $didIdentifier")
                    }.exceptionally { throwable ->
                        println("Error: $throwable")
                    }
                }
            }
        }
    }

    fun startDownload(context: Context) {
        PolygonIdSdk.getInstance().startDownloadCircuits(context = context).thenAccept {
            println("Stream started")
        }
    }

    fun stopStream(context: Context) {
        PolygonIdSdk.getInstance().cancelDownloadCircuits(context = context).thenAccept {
            println("Stream stopped")
        }
    }

    fun authenticate(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getIden3Message(
                context,
                authMessage
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
                        }
                    }
                }
            }.thenAccept {
                println("Authenticated outer")
            }
        }
    }
}
