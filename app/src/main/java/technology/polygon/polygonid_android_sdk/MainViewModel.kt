package technology.polygon.polygonid_android_sdk

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import technology.polygon.polygonid_protobuf.EnvEntityOuterClass.EnvEntity
import java.math.BigInteger

class MainViewModel : ViewModel() {
    val isInit = MutableLiveData(false)
    val hasIdentity = MutableLiveData(false)

    fun init(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.init(
                context = context,
                env = EnvEntity.newBuilder().setBlockchain("polygon").setNetwork("mumbai")
                    .setWeb3Url("https://polygon-mumbai.infura.io/v3/")
                    .setWeb3RdpUrl("wss://polygon-mumbai.infura.io/v3/")
                    .setWeb3ApiKey("theApiKey")
                    .setIdStateContract("0x134B1BE34911E39A8397ec6289782989729807a4")
                    .setPushUrl("https://push-staging.polygonid.com/api/v1").build().check()
            )
            isInit.postValue(true)
        }
    }

    fun getEnv(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getEnv(context = context)
                .thenApply { env ->
                    println("Blockchain: $env")
                }
        }
    }

    fun setEnv(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().callRaw(context = context, method = "setEnv")
                .thenAccept {
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
                context = context, secret = "some secret table yep fff so GJ"
            )
                .thenApply { identity ->
                    println("Identity: $identity")

                    PolygonIdSdk.getInstance().addProfile(
                        context = context,
                        genesisDid = identity.did,
                        privateKey = identity.privateKey,
                        profileNonce = BigInteger("1000")
                    )
                        .thenApply {
                            println("Profile added")
                        }
                }
        }
    }

    fun getIdentities(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getIdentities(
                context = context
            )
                .thenApply { identities ->
                    println("Identities: $identities")
                }.exceptionally { throwable ->
                    println("Error: $throwable")
                }
        }
    }

    fun getDidIdentifier(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(
                context = context, secret = "some secret table yep fff so GJ"
            ).thenApply { privateKey ->
                PolygonIdSdk.getInstance().getEnv(context = context)
                    .thenApply { env ->
                        PolygonIdSdk.getInstance().getDidIdentifier(
                            context = context,
                            privateKey = privateKey,
                            blockchain = env.blockchain,
                            network = env.network,
                        )
                            .thenApply { didIdentifier ->
                                println("DidIdentifier: $didIdentifier")
                            }.exceptionally { throwable ->
                                println("Error: $throwable")
                            }
                    }
            }
        }
    }
}
