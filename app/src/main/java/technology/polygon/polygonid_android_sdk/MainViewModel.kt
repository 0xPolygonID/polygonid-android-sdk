package technology.polygon.polygonid_android_sdk

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.protobuf.Int64Value
import com.google.protobuf.ListValue
import com.google.protobuf.StringValue
import com.google.protobuf.Value
import kotlinx.coroutines.launch
import technology.polygon.polygonid_protobuf.EnvEntityOuterClass.EnvEntity
import technology.polygon.polygonid_protobuf.FilterEntityOuterClass.FilterEntity
import technology.polygon.polygonid_protobuf.InteractionEntityOuterClass.InteractionEntity
import technology.polygon.polygonid_protobuf.InteractionEntityOuterClass.InteractionState
import technology.polygon.polygonid_protobuf.InteractionEntityOuterClass.InteractionType
import technology.polygon.polygonid_protobuf.iden3_message.Iden3MessageEntityOuterClass
import java.math.BigInteger

const val TAG = "PolygonIdSdk"
const val secret = "some secret table yep fff so GJ"
const val apiKey = "theApiKey"
const val authMessage =
    "{\"id\":\"f6a69960-763f-48f5-a7e5-b3ea066cfbc7\",\"typ\":\"application/iden3comm-plain-json\",\"type\":\"https://iden3-communication.io/authorization/1.0/request\",\"thid\":\"f6a69960-763f-48f5-a7e5-b3ea066cfbc7\",\"body\":{\"callbackUrl\":\"https://self-hosted-demo-backend-platform.polygonid.me/api/callback?sessionId=98378\",\"reason\":\"test flow\",\"scope\":[]},\"from\":\"did:polygonid:polygon:mumbai:2qLhNLVmoQS7pQtpMeKHDqkTcENBZUj1nkZiRNPGgV\"}"
const val fetchMessage =
    "{\"id\":\"bae3a15c-3570-4e33-9cdd-739b6105fc15\",\"typ\":\"application/iden3comm-plain-json\",\"type\":\"https://iden3-communication.io/credentials/1.0/offer\",\"thid\":\"bae3a15c-3570-4e33-9cdd-739b6105fc15\",\"body\":{\"url\":\"https://issuer-testing.polygonid.me/v1/agent\",\"credentials\":[{\"id\":\"2bcb98bc-e8db-11ed-938b-0242ac180006\",\"description\":\"KYCAgeCredential\"}]},\"from\":\"did:polygonid:polygon:mumbai:2qFXmNqGWPrLqDowKz37Gq2FETk4yQwVUVUqeBLmf9\",\"to\":\"did:polygonid:polygon:mumbai:2qLmyLKBkCXDSHku8mgjU9XM7n6aH8Lwvp4XESPyJt\"}"
const val credentialRequestMessage =
    "{\"id\":\"b11bdbb1-5a6c-49ca-a180-6e5040a50f41\",\"typ\":\"application/iden3comm-plain-json\",\"type\":\"https://iden3-communication.io/authorization/1.0/request\",\"thid\":\"b11bdbb1-5a6c-49ca-a180-6e5040a50f41\",\"body\":{\"callbackUrl\":\"https://self-hosted-testing-backend-platform.polygonid.me/api/callback?sessionId=174262\",\"reason\":\"test flow\",\"scope\":[{\"id\":1,\"circuitId\":\"credentialAtomicQuerySigV2\",\"query\":{\"allowedIssuers\":[\"*\"],\"context\":\"https://raw.githubusercontent.com/iden3/claim-schema-vocab/main/schemas/json-ld/kyc-v3.json-ld\",\"credentialSubject\":{\"birthday\":{\"\$lt\":20000101}},\"skipClaimRevocationCheck\":true,\"type\":\"KYCAgeCredential\"}}]},\"from\":\"did:polygonid:polygon:mumbai:2qFXmNqGWPrLqDowKz37Gq2FETk4yQwVUVUqeBLmf9\"}"

class MainViewModel : ViewModel() {
    fun init(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.init(
                context = context,
                env = EnvEntity.newBuilder().setBlockchain("polygon").setNetwork("mumbai")
                    .setWeb3Url("https://polygon-mumbai.infura.io/v3/")
                    .setWeb3RdpUrl("wss://polygon-mumbai.infura.io/v3/").setWeb3ApiKey(apiKey)
                    .setIdStateContract("0x134B1BE34911E39A8397ec6289782989729807a4")
                    .setPushUrl("https://push-staging.polygonid.com/api/v1").build().check()
            )
        }
    }

    fun switchOnLog(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().switchLog(context = context, true).thenAccept {
                println("SwitchOnLog Done")
            }
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

    /// IDENTITY
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
                    profileNonce = BigInteger("3000")
                ).thenApply {
                    println("Profile added")
                }
            }
        }
    }

    fun backupIdentity(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(context = context, secret = secret)
                .thenApply { privateKey ->
                    PolygonIdSdk.getInstance().getEnv(context = context).thenApply { env ->
                        PolygonIdSdk.getInstance().getDidIdentifier(
                            context = context,
                            privateKey = privateKey,
                            blockchain = env.blockchain,
                            network = env.network,
                        ).thenApply { didIdentifier ->
                            PolygonIdSdk.getInstance().backupIdentity(
                                context = context,
                                privateKey = privateKey,
                                genesisDid = didIdentifier
                            ).thenApply { backup ->
                                println("Backup: $backup")
                            }
                        }
                    }
                }
        }
    }

    fun restoreIdentity(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(context = context, secret = secret)
                .thenApply { privateKey ->
                    PolygonIdSdk.getInstance().getEnv(context = context).thenApply { env ->
                        PolygonIdSdk.getInstance().getDidIdentifier(
                            context = context,
                            privateKey = privateKey,
                            blockchain = env.blockchain,
                            network = env.network,
                        ).thenApply { didIdentifier ->
                            PolygonIdSdk.getInstance().backupIdentity(
                                context = context,
                                privateKey = privateKey,
                                genesisDid = didIdentifier
                            ).thenApply { backup ->
                                PolygonIdSdk.getInstance().restoreIdentity(
                                    context = context,
                                    privateKey = privateKey,
                                    genesisDid = didIdentifier,
                                    encryptedDb = backup
                                ).thenApply { restored ->
                                    println("RestoredIdentity: $restored")
                                }
                            }
                        }
                    }
                }
        }
    }

    fun checkIdentityValidity(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().checkIdentityValidity(context = context, secret = secret)
                .thenApply { isValid ->
                    println("isValid: $isValid")
                }.exceptionally { throwable ->
                    println("ErrorCheckIdentityValidity: $throwable")
                }
        }
    }

    fun getState(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(context = context, secret = secret)
                .thenApply { privateKey ->
                    PolygonIdSdk.getInstance().getEnv(context = context).thenApply { env ->
                        PolygonIdSdk.getInstance().getDidIdentifier(
                            context = context,
                            privateKey = privateKey,
                            blockchain = env.blockchain,
                            network = env.network,
                        ).thenApply { didIdentifier ->
                            PolygonIdSdk.getInstance()
                                .getState(context = context, did = didIdentifier)
                                .thenApply { state ->
                                    println("State: $state")
                                }
                        }
                    }
                }
        }
    }

    fun getDidEntity(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(context = context, secret = secret)
                .thenApply { privateKey ->
                    PolygonIdSdk.getInstance().getEnv(context = context).thenApply { env ->
                        PolygonIdSdk.getInstance().getDidIdentifier(
                            context = context,
                            privateKey = privateKey,
                            blockchain = env.blockchain,
                            network = env.network,
                        ).thenApply { didIdentifier ->
                            PolygonIdSdk.getInstance()
                                .getDidEntity(context = context, did = didIdentifier)
                                .thenApply {
                                    println("DidEntity: ${it.did}")
                                }.exceptionally {
                                    println("Error: $it")
                                }
                        }
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

    /// GET IDENTITY
    fun getIdentity(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(context = context, secret = secret)
                .thenApply { privateKey ->
                    PolygonIdSdk.getInstance().getEnv(context = context).thenApply { env ->
                        PolygonIdSdk.getInstance().getDidIdentifier(
                            context = context,
                            privateKey = privateKey,
                            blockchain = env.blockchain,
                            network = env.network,
                        ).thenApply { didIdentifier ->
                            println("DidIdentifier: $didIdentifier")
                            PolygonIdSdk.getInstance().getIdentity(
                                context = context,
                                privateKey = privateKey,
                                genesisDid = didIdentifier
                            ).thenApply { identity ->
                                println("Identity: ${identity.did}")
                            }
                        }
                    }
                }
        }
    }

    fun removeIdentity(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(context = context, secret = secret)
                .thenApply { privateKey ->
                    PolygonIdSdk.getInstance().getEnv(context = context).thenApply { env ->
                        PolygonIdSdk.getInstance().getDidIdentifier(
                            context = context,
                            privateKey = privateKey,
                            blockchain = env.blockchain,
                            network = env.network,
                        ).thenApply { didIdentifier ->
                            println("DidIdentifier: $didIdentifier")
                            PolygonIdSdk.getInstance().removeIdentity(
                                context = context,
                                privateKey = privateKey,
                                genesisDid = didIdentifier
                            ).thenApply { identity ->
                                println("removeIdentity: $identity")
                            }
                        }
                    }
                }
        }
    }

    fun removeProfile(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(context = context, secret = secret)
                .thenApply { privateKey ->
                    PolygonIdSdk.getInstance().getEnv(context = context).thenApply { env ->
                        PolygonIdSdk.getInstance().getDidIdentifier(
                            context = context,
                            privateKey = privateKey,
                            blockchain = env.blockchain,
                            network = env.network,
                        ).thenApply { didIdentifier ->
                            println("DidIdentifier: $didIdentifier")
                            PolygonIdSdk.getInstance().removeProfile(
                                context = context,
                                privateKey = privateKey,
                                genesisDid = didIdentifier,
                                profileNonce = BigInteger("1000"),
                            ).thenApply { identity ->
                                println("removeIdentity: $identity")
                            }
                        }
                    }
                }
        }
    }

    fun sign(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(context = context, secret = secret)
                .thenApply { privateKey ->
                    PolygonIdSdk.getInstance().sign(
                        context = context,
                        privateKey = privateKey,
                        message = "0xff123456",
                    ).thenApply { signature ->
                        println("Signature: $signature")
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

    fun fetch(context: Context, fetchMessage: String) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getIden3Message(
                context, fetchMessage
            ).thenApply { message ->
                println("Message: $message")
                PolygonIdSdk.getInstance().getPrivateKey(
                    context = context, secret = secret
                ).thenApply { privateKey ->
                    PolygonIdSdk.getInstance().getDidIdentifier(
                        context = context,
                        privateKey = privateKey,
                        blockchain = "polygon",
                        network = "mumbai",
                    ).thenApply { did ->
                        PolygonIdSdk.getInstance().fetchAndSaveClaims(
                            context = context,
                            message = message as Iden3MessageEntityOuterClass.OfferIden3MessageEntity,
                            genesisDid = did,
                            privateKey = privateKey
                        ).thenAccept { claims ->
                            println("Fetched: ${claims.first().id}")
                        }.exceptionally {
                            println("Error: $it")
                            null
                        }
                    }
                }
            }
        }
    }

    fun getClaims(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(
                context = context, secret = secret
            ).thenApply { privateKey ->
                PolygonIdSdk.getInstance().getDidIdentifier(
                    context = context,
                    privateKey = privateKey,
                    blockchain = "polygon",
                    network = "mumbai",
                ).thenApply { did ->
                    /*val id =
                        "https://issuer-testing.polygonid.me/v1/did:polygonid:polygon:mumbai:2qFXmNqGWPrLqDowKz37Gq2FETk4yQwVUVUqeBLmf9/claims/2bcb98bc-e8db-11ed-938b-0242ac180006"
                    val listValueBuilder = ListValue.newBuilder()
                    listValueBuilder.addValues(
                        Value.newBuilder().setStringValue(id).build()
                    )
                    val value = Value.newBuilder().setListValue(listValueBuilder).build()
                    val filter =
                        FilterEntity.newBuilder().setOperator("nonEqual").setName("id")
                            .setValue(value).build()*/
                    PolygonIdSdk.getInstance().getClaims(
                        context = context,
                        genesisDid = did,
                        privateKey = privateKey,
                        //filters = listOf(filter)
                    ).thenApply { claims ->
                        println("ClaimsFiltered: $claims")
                    }

                }
            }
        }
    }

    fun getClaimsByIds(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(
                context = context, secret = secret
            ).thenApply { privateKey ->
                PolygonIdSdk.getInstance().getDidIdentifier(
                    context = context,
                    privateKey = privateKey,
                    blockchain = "polygon",
                    network = "mumbai",
                ).thenApply { did ->
                    val id =
                        "https://issuer-testing.polygonid.me/v1/did:polygonid:polygon:mumbai:2qFXmNqGWPrLqDowKz37Gq2FETk4yQwVUVUqeBLmf9/claims/2bcb98bc-e8db-11ed-938b-0242ac180006"
                    PolygonIdSdk.getInstance().getClaimsByIds(
                        context = context,
                        genesisDid = did,
                        privateKey = privateKey,
                        claimIds = listOf(id)
                    ).thenApply { claims ->
                        println("Claims: $claims")
                    }


                }
            }
        }
    }

    fun removeClaim(context: Context) {
        val id =
            "https://issuer-testing.polygonid.me/v1/did:polygonid:polygon:mumbai:2qFXmNqGWPrLqDowKz37Gq2FETk4yQwVUVUqeBLmf9/claims/2bcb98bc-e8db-11ed-938b-0242ac180006"
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(
                context = context, secret = secret
            ).thenApply { privateKey ->
                PolygonIdSdk.getInstance().getDidIdentifier(
                    context = context,
                    privateKey = privateKey,
                    blockchain = "polygon",
                    network = "mumbai",
                ).thenApply { did ->
                    PolygonIdSdk.getInstance().removeClaim(
                        context = context, genesisDid = did, privateKey = privateKey, claimId = id
                    ).thenApply { claim ->
                        println("RemoveClaim: $claim")
                    }
                }
            }
        }
    }

    fun removeClaims(context: Context) {
        val id =
            "https://issuer-testing.polygonid.me/v1/did:polygonid:polygon:mumbai:2qFXmNqGWPrLqDowKz37Gq2FETk4yQwVUVUqeBLmf9/claims/2bcb98bc-e8db-11ed-938b-0242ac180006"
        val ids = listOf(id)
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(
                context = context, secret = secret
            ).thenApply { privateKey ->
                PolygonIdSdk.getInstance().getDidIdentifier(
                    context = context,
                    privateKey = privateKey,
                    blockchain = "polygon",
                    network = "mumbai",
                ).thenApply { did ->
                    PolygonIdSdk.getInstance().removeClaims(
                        context = context, genesisDid = did, privateKey = privateKey, claimIds = ids
                    ).thenApply { claims ->
                        println("RemoveClaimsByIds: $claims")
                    }
                }
            }
        }
    }

    fun saveClaims(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(
                context = context, secret = secret
            ).thenApply { privateKey ->
                PolygonIdSdk.getInstance().getDidIdentifier(
                    context = context,
                    privateKey = privateKey,
                    blockchain = "polygon",
                    network = "mumbai",
                ).thenApply { did ->
                    PolygonIdSdk.getInstance().getClaims(
                        context = context,
                        genesisDid = did,
                        privateKey = privateKey,
                    ).thenApply { claims ->
                        val expirationValue =
                            StringValue.newBuilder().setValue("2030-01-01T00:00:00Z").build()
                        println("ClaimsFiltered: $claims")
                        var claimEntity = claims.first()
                        //we set customId with timestamp
                        //val customId = "customId" + System.currentTimeMillis().toString()
                        //val claimBuilder = claimEntity.newBuilderForType().setId(customId).build()
                        /*ClaimEntity.newBuilder()
                            .setId(customId)
                            .setIssuer(claimEntity.issuer)
                            .setDid(claimEntity.did)
                            .setState(claimEntity.state)
                            .setType(claimEntity.type)
                            .setExpiration(expirationValue)
                            .build()*/
                        PolygonIdSdk.getInstance().saveClaims(
                            context = context,
                            genesisDid = did,
                            privateKey = privateKey,
                            claims = listOf(claimEntity)
                        ).thenApply { claim ->
                            println("SaveClaims: $claim")
                        }.exceptionally {
                            println("SaveClaims: $it")
                        }
                    }

                }
            }
        }
    }

    fun checkDownloadCircuits(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().isAlreadyDownloadedCircuitsFromServer(context).thenApply {
                println("isAlreadyDownloadedCircuitsFromServer: $it")
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

    fun addInteraction(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getIden3Message(context, fetchMessage).thenApply { message ->
                PolygonIdSdk.getInstance().getPrivateKey(
                    context = context, secret = secret
                ).thenApply { privateKey ->
                    PolygonIdSdk.getInstance().getDidIdentifier(
                        context = context,
                        privateKey = privateKey,
                        blockchain = "polygon",
                        network = "mumbai",
                    ).thenApply { did ->
                        val iden3message =
                            message as Iden3MessageEntityOuterClass.OfferIden3MessageEntity
                        val interaction = InteractionEntity.newBuilder()
                            .setId(iden3message.id)
                            .setGenesisDid(did)
                            .setMessage(fetchMessage)
                            .setFrom(iden3message.from)
                            .setState("InteractionState.${InteractionState.received.name}")
                            .setType("InteractionType.${InteractionType.offer.name}")
                            .setTimestamp(1683286535)
                            .setProfileNonce(Int64Value.of(0L))
                            .build()


                        PolygonIdSdk.getInstance().addInteraction(
                            context = context,
                            genesisDid = did,
                            interaction = interaction,
                            privateKey = privateKey,
                        ).thenApply {
                            println("AddInteractionSuccess: $it")
                        }.exceptionally {
                            println("AddInteractionError: $it")
                        }
                    }
                }
            }
        }
    }

    fun getInteractions(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(
                context = context, secret = secret
            ).thenApply { privateKey ->
                PolygonIdSdk.getInstance().getDidIdentifier(
                    context = context,
                    privateKey = privateKey,
                    blockchain = "polygon",
                    network = "mumbai",
                ).thenApply { did ->
                    PolygonIdSdk.getInstance().getInteractions(
                        context = context,
                        genesisDid = did,
                        privateKey = privateKey,
                    ).thenApply { interactions ->
                        println("GetInteractions: $interactions")
                    }
                }
            }
        }
    }

    fun removeInteractions(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(
                context = context, secret = secret
            ).thenApply { privateKey ->
                PolygonIdSdk.getInstance().getDidIdentifier(
                    context = context,
                    privateKey = privateKey,
                    blockchain = "polygon",
                    network = "mumbai",
                ).thenApply { did ->
                    PolygonIdSdk.getInstance().removeInteractions(
                        context = context,
                        genesisDid = did,
                        privateKey = privateKey,
                        ids = listOf("0x1")
                    ).thenApply { interactions ->
                        println("RemoveInteractions: $interactions")
                    }
                }
            }
        }
    }

    fun updateInteraction(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getIden3Message(context, fetchMessage).thenApply { message ->
                PolygonIdSdk.getInstance().getPrivateKey(
                    context = context, secret = secret
                ).thenApply { privateKey ->
                    PolygonIdSdk.getInstance().getDidIdentifier(
                        context = context,
                        privateKey = privateKey,
                        blockchain = "polygon",
                        network = "mumbai",
                    ).thenApply { did ->
                        val iden3message =
                            message as Iden3MessageEntityOuterClass.OfferIden3MessageEntity

                        PolygonIdSdk.getInstance().updateInteraction(
                            context = context,
                            id = iden3message.id,
                            genesisDid = did,
                            privateKey = privateKey,
                            state = InteractionState.opened,
                        ).thenApply { interaction ->
                            println("UpdateInteraction: $interaction")
                        }.exceptionally {
                            println("UpdateInteractionError: $it")
                        }
                    }
                }
            }
        }
    }

    fun getClaimsFromIden3Message(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getIden3Message(context, credentialRequestMessage)
                .thenApply { message ->
                    println("getClaimsFromIden3Message - MESSAGE: $message")
                    PolygonIdSdk.getInstance().getPrivateKey(
                        context = context, secret = secret
                    ).thenApply { privateKey ->
                        PolygonIdSdk.getInstance().getDidIdentifier(
                            context = context,
                            privateKey = privateKey,
                            blockchain = "polygon",
                            network = "mumbai",
                        ).thenApply { did ->
                            println("getClaimsFromIden3Message - DID: $did")
                            PolygonIdSdk.getInstance().getClaimsFromIden3Message(
                                context = context,
                                privateKey = privateKey,
                                genesisDid = did,
                                profileNonce = BigInteger("0"),
                                message = message,
                            ).thenApply { claims ->
                                println("getClaimsFromIden3Message - CLAIMS: ${claims.size}")
                            }.exceptionally {
                                println("GetClaimsFromIden3MessageError: $it")
                            }
                        }
                    }
                }
        }
    }

    fun getFiltersFromIden3Message(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getIden3Message(context, credentialRequestMessage)
                .thenApply { message ->
                    println("getFiltersFromIden3Message - MESSAGE: $message")

                    PolygonIdSdk.getInstance().getFilters(
                        context = context,
                        message = message,
                    ).thenApply { filters ->
                        println("getFiltersFromIden3Message - FILTERS: ${filters.size}")
                    }.exceptionally {
                        println("getFiltersFromIden3MessageError: $it")
                    }
                }
        }
    }

    fun getSchemasFromIden3Message(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getIden3Message(context, credentialRequestMessage)
                .thenApply { message ->
                    println("getSchemasFromIden3Message - MESSAGE: $message")
                    PolygonIdSdk.getInstance().getSchemas(
                        context = context,
                        message = message,
                    ).thenApply { schemas ->
                        println("getSchemasFromIden3Message - SCHEMAS: $schemas")
                    }.exceptionally {
                        println("getSchemasFromIden3MessageError: $it")
                    }
                }
        }
    }

    fun getVocabsFromIden3Message(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getIden3Message(context, credentialRequestMessage)
                .thenApply { message ->
                    println("getVocabsFromIden3Message - MESSAGE: $message")
                    PolygonIdSdk.getInstance().getVocabs(
                        context = context,
                        message = message,
                    ).thenApply { vocabs ->
                        println("getVocabsFromIden3Message - VOCABS: $vocabs")
                    }.exceptionally {
                        println("getVocabsFromIden3MessageError: $it")
                    }
                }
        }
    }

    fun getProofsFromIden3Message(context: Context) {
        viewModelScope.launch {
            PolygonIdSdk.getInstance().getPrivateKey(context = context, secret = secret)
                .thenApply { privateKey ->
                    PolygonIdSdk.getInstance().getEnv(context = context).thenApply { env ->
                        PolygonIdSdk.getInstance().getDidIdentifier(
                            context = context,
                            privateKey = privateKey,
                            blockchain = env.blockchain,
                            network = env.network,
                        ).thenApply { didIdentifier ->
                            PolygonIdSdk.getInstance()
                                .getIden3Message(context, credentialRequestMessage)
                                .thenApply { message ->
                                    println("getProofsFromIden3Message - MESSAGE: $message")
                                    PolygonIdSdk.getInstance().getProofs(
                                        context = context,
                                        message = message,
                                        profileNonce = BigInteger("0"),
                                        privateKey = privateKey,
                                        genesisDid = didIdentifier,
                                    ).thenApply { proofs ->
                                        println("getProofsFromIden3Message - PROOFS: $proofs")
                                    }.exceptionally {
                                        println("getProofsFromIden3MessageError: $it")
                                    }
                                }
                        }
                    }
                }
        }
    }
}

