package technology.polygon.polygonid_android_sdk

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import technology.polygon.polygonid_protobuf.CircuitDataEntityOuterClass.CircuitDataEntity
import technology.polygon.polygonid_protobuf.ClaimEntityOuterClass.*
import technology.polygon.polygonid_protobuf.DidEntityOuterClass.DidEntity
import technology.polygon.polygonid_protobuf.EnvEntityOuterClass.EnvEntity
import technology.polygon.polygonid_protobuf.FilterEntityOuterClass.FilterEntity
import technology.polygon.polygonid_protobuf.IdentityEntityOuterClass.*
import technology.polygon.polygonid_protobuf.InteractionEntityOuterClass.*
import technology.polygon.polygonid_protobuf.ProofScopeRequestOuterClass.ProofScopeRequest
import technology.polygon.polygonid_protobuf.iden3_message.Iden3MessageEntityOuterClass.*
import java.math.BigInteger
import java.util.concurrent.CompletableFuture

const val CHANNEL = "technology.polygon.polygonid_flutter_sdk"
const val ENGINE = "PolygonIdEngine"

@Suppress("UNCHECKED_CAST")
class PolygonIdSdk(private val flows: MutableMap<String, MutableSharedFlow<Any?>> = mutableMapOf()) {
    companion object {
        private var ref: PolygonIdSdk? = null

        fun getInstance(): PolygonIdSdk {
            return ref
                ?: throw IllegalStateException("PolygonIdSdk not initialized, please call init() first")
        }

        fun init(context: Context, env: EnvEntity? = null) {
            ref = PolygonIdSdk()

            ref!!.getChannel(context = context).setMethodCallHandler { call, _ ->
                when (call.method) {
                    "onStreamData" -> {
                        val key: String? =
                            call.hasArgument("key").let { call.argument<String>("key") }

                        if (key != null) {
                            GlobalScope.launch {
                                ref!!.getFlow(key).emit(call.argument<Any>("data"))
                            }
                        }
                    }
                }
            }

            Handler(Looper.getMainLooper()).post {
                ref!!.getEngine(context).dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint(
                    "main.dart", "init"
                ), env?.let { listOf(JsonFormat.printer().print(env)) })
            }
        }
    }

    fun getFlowKeys(): List<String> {
        return flows.keys.toList()
    }

    fun getFlow(key: String): MutableSharedFlow<Any?> {
        if (flows[key] == null) {
            flows[key] = MutableSharedFlow()
        }

        return flows[key]!!
    }

    fun closeFlow(context: Context, key: String) {
        call<Void>(
            context = context,
            method = "closeStream",
            arguments = mapOf("key" to key)
        ).thenAccept {
            flows.remove(key)
        }
    }

    private fun getEngine(context: Context): FlutterEngine {
        return if (FlutterEngineCache.getInstance().contains(ENGINE)) {
            FlutterEngineCache.getInstance().get(ENGINE)!!
        } else {
            val flutterEngine = FlutterEngine(context)
            FlutterEngineCache.getInstance().put(ENGINE, flutterEngine)

            flutterEngine
        }
    }

    private fun getChannel(context: Context): MethodChannel {
        val engine: FlutterEngine = getEngine(context)
        return MethodChannel(
            engine.dartExecutor.binaryMessenger, CHANNEL
        )
    }

    fun callRaw(
        context: Context,
        method: String,
        arguments: Map<String, Any?>? = null,
        isListResult: Boolean = false
    ): CompletableFuture<Any> {
        return callFlutterMethod<String>(context, method, arguments, isListResult)
    }

    private inline fun <reified T> callFlutterMethod(
        context: Context,
        method: String,
        arguments: Map<String, Any?>? = null,
        isListResult: Boolean = false
    ): CompletableFuture<Any> {
        val completable = CompletableFuture<Any>()
        val channel = getChannel(context)

        try {
            val args = arguments?.mapValues { prepareArg(it.value) }

            Handler(Looper.getMainLooper()).post {
                channel.invokeMethod(
                    method,
                    args,
                    MainThreadResultHandler(result = object : MethodChannel.Result {
                        override fun success(result: Any?) {
                            when {
                                Message::class.java.isAssignableFrom(T::class.java) -> {
                                    val clazz = T::class.java
                                    val builderMethod = clazz.getMethod("newBuilder")

                                    if (isListResult) {
                                        val resultList = mutableListOf<T>()

                                        for (element: String in (result as List<String>)) {
                                            val builder =
                                                builderMethod.invoke(null) as Message.Builder
                                            JsonFormat.parser().merge(element, builder)
                                            resultList.add(builder.build() as T)
                                        }

                                        completable.complete(resultList)
                                    } else {
                                        val builder = builderMethod.invoke(null) as Message.Builder
                                        JsonFormat.parser().merge(result as String, builder)
                                        completable.complete(builder.build() as T)
                                    }
                                }
                                else -> {
                                    if (isListResult) {
                                        completable.complete(result as? List<T>)
                                    } else {
                                        completable.complete(result as? T)
                                    }
                                }
                            }
                        }

                        override fun error(
                            errorCode: String,
                            errorMessage: String?,
                            errorDetails: Any?
                        ) {
                            completable.completeExceptionally(Throwable(errorMessage))
                        }

                        override fun notImplemented() {
                            completable.completeExceptionally(Throwable("notImplemented"))
                        }
                    })
                )
            }
        } catch (e: Exception) {
            completable.completeExceptionally(e)
        }



        return completable
    }

    private inline fun <reified T> call(
        context: Context,
        method: String,
        arguments: Map<String, Any?>? = null
    ): CompletableFuture<T> {
        return callFlutterMethod<T>(
            context = context, method = method, arguments = arguments
        ) as CompletableFuture<T>
    }

    private inline fun <reified T> callAsList(
        context: Context,
        method: String,
        arguments: Map<String, Any?>? = null
    ): CompletableFuture<List<T>> {
        return callFlutterMethod<T>(
            context = context, method = method, arguments = arguments, isListResult = true
        ) as CompletableFuture<List<T>>
    }

    private fun prepareArg(arg: Any?): Any? {
        return when (arg) {
            is Message -> arg.validate().let { JsonFormat.printer().print(it) }
            is List<*> -> arg.map { prepareArg(it) }
            else -> arg
        }
    }

    // SDK
    fun init(context: Context): CompletableFuture<Void> {
        return call(
            context = context, method = "init"
        )
    }

    fun setEnv(context: Context, env: EnvEntity): CompletableFuture<Void> {
        return call(
            context = context, method = "setEnv", arguments = mapOf("env" to env)
        )
    }

    fun getEnv(
        context: Context
    ): CompletableFuture<EnvEntity> {
        return call(
            context = context, method = "getEnv"
        )
    }

    // Iden3comm
    fun addInteraction(
        context: Context, genesisDid: String, interaction: Message
    ): CompletableFuture<Message> {
        interaction.isOf(listOf(InteractionEntity::class, InteractionBaseEntity::class))

        return call<String>(
            context = context, method = "addInteraction", arguments = mapOf(
                "genesisDid" to genesisDid, "interaction" to interaction
            )
        ).thenApply {
            val builder: Message.Builder =
                when (Gson().fromJson(it, Map::class.java)["genesisDid"] != null) {
                    true -> InteractionEntity.newBuilder()
                    else -> InteractionBaseEntity.newBuilder()
                }

            JsonFormat.parser().merge(it, builder)
            builder.build()
        }
    }

    fun authenticate(
        context: Context,
        message: AuthIden3MessageEntity,
        genesisDid: String,
        profileNonce: BigInteger? = null,
        privateKey: String,
        pushToken: String? = null
    ): CompletableFuture<Void> {
        return call(
            context = context, method = "authenticate", arguments = mapOf(
                "message" to message,
                "genesisDid" to genesisDid,
                "profileNonce" to profileNonce?.toString(),
                "privateKey" to privateKey,
                "pushToken" to pushToken
            )
        )
    }

    fun fetchAndSaveClaims(
        context: Context,
        message: Message,
        genesisDid: String,
        profileNonce: BigInteger? = null,
        privateKey: String
    ): CompletableFuture<List<ClaimEntity>> {
        message.isOf(listOf(OfferIden3MessageEntity::class))

        return callAsList(
            context = context, method = "fetchAndSaveClaims", arguments = mapOf(
                "message" to message,
                "genesisDid" to genesisDid,
                "profileNonce" to profileNonce?.toString(),
                "privateKey" to privateKey
            )
        )
    }

    fun getClaimsFromIden3Message(
        context: Context,
        message: Message,
        genesisDid: String,
        profileNonce: BigInteger? = null,
        privateKey: String
    ): CompletableFuture<List<ClaimEntity>> {
        message.isOf(
            listOf(
                AuthIden3MessageEntity::class, OnchainIden3MessageEntity::class
            )
        )

        return callAsList(
            context = context, method = "getClaimsFromIden3Message", arguments = mapOf(
                "message" to message,
                "genesisDid" to genesisDid,
                "profileNonce" to profileNonce?.toString(),
                "privateKey" to privateKey
            )
        )
    }

    fun getFilters(
        context: Context, message: Message
    ): CompletableFuture<List<FilterEntity>> {
        message.isOf(
            listOf(
                AuthIden3MessageEntity::class, OnchainIden3MessageEntity::class
            )
        )

        return callAsList(
            context = context,
            method = "getFilters",
            arguments = mapOf("message" to message)
        )
    }

    fun getIden3Message(
        context: Context, message: String
    ): CompletableFuture<Message> {
        return call<String>(
            context = context, method = "getIden3Message", arguments = mapOf("message" to message)
        ).thenApply { result ->
            Gson().fromJson(result, Map::class.java).let {
                val builder: Message.Builder = when (it["messageType"]) {
                    Iden3MessageType.auth.name -> {
                        AuthIden3MessageEntity.newBuilder()
                    }
                    Iden3MessageType.issuance.name -> {
                        FetchIden3MessageEntity.newBuilder()
                    }
                    Iden3MessageType.offer.name -> {
                        OfferIden3MessageEntity.newBuilder()
                    }
                    Iden3MessageType.contractFunctionCall.name -> {
                        OnchainIden3MessageEntity.newBuilder()
                    }
                    else -> {
                        throw IllegalStateException("Unsupported type")
                    }
                }

                JsonFormat.parser().merge(result, builder)
                builder.build()
            }
        }
    }

    fun getSchemas(
        context: Context, message: Message
    ): CompletableFuture<List<Map<String, Any?>>> {
        message.isOf(
            listOf(
                AuthIden3MessageEntity::class, OnchainIden3MessageEntity::class
            )
        )

        return callAsList(
            context = context,
            method = "getSchemas",
            arguments = mapOf("message" to message),
        )
    }

    fun getVocabs(
        context: Context, message: Message
    ): CompletableFuture<List<Map<String, Any?>>> {
        message.isOf(
            listOf(
                AuthIden3MessageEntity::class, OnchainIden3MessageEntity::class
            )
        )

        return callAsList(
            context = context,
            method = "getVocabs",
            arguments = mapOf("message" to message),
        )
    }

    fun getInteractions(
        context: Context,
        genesisDid: String? = null,
        profileNonce: BigInteger? = null,
        privateKey: String? = null,
        type: List<InteractionType>? = null,
        states: List<InteractionState>? = null,
        filters: List<FilterEntity>? = null
    ): CompletableFuture<List<Any>> {
        return callAsList<String>(
            context = context,
            method = "getInteractions",
            arguments = mapOf(
                "genesisDid" to genesisDid,
                "profileNonce" to profileNonce?.toString(),
                "privateKey" to privateKey,
                "type" to type?.map { it.name },
                "states" to states?.map { it.name },
                "filters" to filters
            )
        ).thenApply { result ->
            result.map { interaction ->
                val builder: Message.Builder =
                    when (Gson().fromJson(interaction, Map::class.java)["genesisDid"] != null) {
                        true -> InteractionEntity.newBuilder()
                        else -> InteractionBaseEntity.newBuilder()
                    }

                JsonFormat.parser().merge(interaction, builder)
                builder.build()
            }
        }
    }

    // We are returning the json representation of the proof because the
    // protobuf doesn't support nested lists
    fun getProofs(
        context: Context,
        message: Message,
        genesisDid: String,
        profileNonce: BigInteger? = null,
        privateKey: String,
        challenge: String? = null
    ): CompletableFuture<List<String>> {
        return callAsList(
            context = context,
            method = "getProofs",
            arguments = mapOf(
                "message" to message,
                "genesisDid" to genesisDid,
                "profileNonce" to profileNonce?.toString(),
                "privateKey" to privateKey,
                "challenge" to challenge
            )
        )
//            .thenApply { result ->
//            result.map { proof ->
//                var builder: Message.Builder = JWZSDProofEntity.newBuilder()
//                JsonFormat.parser().merge(proof, builder)
//                val entity = builder.build() as JWZSDProofEntity
//
//                if (!entity.hasVp()) {
//                    builder = JWZProofEntity.newBuilder()
//                    JsonFormat.parser().merge(proof, builder)
//                    builder.build() as JWZProofEntity
//                } else {
//                    entity
//                }
//            }
//        }
    }

    fun removeInteractions(
        context: Context, genesisDid: String? = null, privateKey: String? = null, ids: List<String>
    ): CompletableFuture<Void> {
        return call(
            context = context,
            method = "removeInteractions",
            arguments = mapOf(
                "genesisDid" to genesisDid,
                "privateKey" to privateKey,
                "ids" to ids
            )
        )
    }

    fun updateInteraction(
        context: Context,
        genesisDid: String? = null,
        privateKey: String? = null,
        state: InteractionState? = null
    ): CompletableFuture<Any> {
        return call<String>(
            context = context,
            method = "updateInteraction",
            arguments = mapOf(
                "genesisDid" to genesisDid,
                "privateKey" to privateKey,
                "state" to state?.name
            )
        ).thenApply {
            val builder: Message.Builder =
                when (Gson().fromJson(it, Map::class.java)["genesisDid"] != null) {
                    true -> InteractionEntity.newBuilder()
                    else -> InteractionBaseEntity.newBuilder()
                }

            JsonFormat.parser().merge(it, builder)
            builder.build()
        }
    }

    // Identity
    fun addIdentity(
        context: Context, secret: String
    ): CompletableFuture<PrivateIdentityEntity> {
        return call(
            context = context, method = "addIdentity", arguments = mapOf("secret" to secret)
        )
    }

    fun addProfile(
        context: Context, genesisDid: String, privateKey: String, profileNonce: BigInteger
    ): CompletableFuture<Void> {
        return call(
            context = context, method = "addProfile", arguments = mapOf(
                "genesisDid" to genesisDid,
                "privateKey" to privateKey,
                "profileNonce" to profileNonce.toString()
            )
        )
    }

    fun backupIdentity(
        context: Context, genesisDid: String, privateKey: String
    ): CompletableFuture<String> {
        return call(
            context = context, method = "backupIdentity", arguments = mapOf(
                "genesisDid" to genesisDid, "privateKey" to privateKey
            )
        )
    }

    fun checkIdentityValidity(
        context: Context, secret: String
    ): CompletableFuture<Void> {
        return call(
            context = context,
            method = "checkIdentityValidity",
            arguments = mapOf("secret" to secret)
        )
    }

    fun getDidEntity(
        context: Context, did: String
    ): CompletableFuture<DidEntity> {
        return call(
            context = context, method = "getDidEntity", arguments = mapOf("did" to did)
        )
    }

    fun getDidIdentifier(
        context: Context, privateKey: String, blockchain: String, network: String
    ): CompletableFuture<String> {
        return call(
            context = context, method = "getDidIdentifier", arguments = mapOf(
                "privateKey" to privateKey, "blockchain" to blockchain, "network" to network
            )
        )
    }

    fun getIdentities(
        context: Context
    ): CompletableFuture<List<IdentityEntity>> {
        return callAsList(
            context = context, method = "getIdentities"
        )
    }

    fun getIdentity(
        context: Context, privateKey: String? = null
    ): CompletableFuture<Message> {
        return call<String>(
            context = context, method = "getIdentity", arguments = mapOf("privateKey" to privateKey)
        ).thenApply {
            when {
                privateKey != null -> {
                    val builder: Message.Builder = PrivateIdentityEntity.newBuilder()
                    JsonFormat.parser().merge(it, builder)
                    builder.build()
                }
                else -> {
                    val builder: Message.Builder =
                        IdentityEntity.newBuilder()
                    JsonFormat.parser().merge(it, builder)
                    builder.build()
                }
            }
        }
    }

    fun getPrivateKey(
        context: Context, secret: String
    ): CompletableFuture<String> {
        return call(
            context = context,
            method = "getPrivateKey",
            arguments = mapOf("secret" to secret)
        )
    }

    fun getProfiles(
        context: Context, genesisDid: String, privateKey: String
    ): CompletableFuture<Map<BigInteger, String>> {
        return call<Map<String, String>>(
            context = context, method = "getProfiles", arguments = mapOf(
                "genesisDid" to genesisDid, "privateKey" to privateKey
            )
        ).thenApply { result ->
            result.mapKeys { BigInteger(it.key) }
        }
    }

    fun getState(
        context: Context, did: String
    ): CompletableFuture<String> {
        return call(
            context = context, method = "getState", arguments = mapOf("did" to did)
        )
    }

    fun removeIdentity(
        context: Context, genesisDid: String, privateKey: String
    ): CompletableFuture<Void> {
        return call(
            context = context, method = "removeIdentity", arguments = mapOf(
                "genesisDid" to genesisDid, "privateKey" to privateKey
            )
        )
    }

    fun removeProfile(
        context: Context, privateKey: String, profileNonce: BigInteger
    ): CompletableFuture<Void> {
        return call(
            context = context, method = "removeProfile", arguments = mapOf(
                "privateKey" to privateKey, "profileNonce" to profileNonce.toString()
            )
        )
    }

    fun restoreIdentity(
        context: Context, genesisDid: String, privateKey: String, encryptedDb: String? = null
    ): CompletableFuture<PrivateIdentityEntity> {
        return call(
            context = context, method = "restoreIdentity", arguments = mapOf(
                "genesisDid" to genesisDid,
                "privateKey" to privateKey,
                "encryptedDb" to encryptedDb
            )
        )
    }

    fun sign(
        context: Context, privateKey: String, message: String
    ): CompletableFuture<String> {
        return call(
            context = context, method = "sign", arguments = mapOf(
                "privateKey" to privateKey, "message" to message
            )
        )
    }

    fun getClaims(
        context: Context,
        filters: List<FilterEntity>? = null,
        genesisDid: String,
        privateKey: String
    ): CompletableFuture<List<ClaimEntity>> {
        return callAsList(
            context = context, method = "getClaims", arguments = mapOf(
                "filters" to filters,
                "genesisDid" to genesisDid,
                "privateKey" to privateKey
            )
        )
    }

    fun getClaimsByIds(
        context: Context, claimIds: List<String>, genesisDid: String, privateKey: String
    ): CompletableFuture<List<ClaimEntity>> {
        return callAsList(
            context = context, method = "getClaimsByIds", arguments = mapOf(
                "claimIds" to claimIds,
                "genesisDid" to genesisDid,
                "privateKey" to privateKey
            )
        )
    }

    fun removeClaim(
        context: Context, claimId: String, genesisDid: String, privateKey: String
    ): CompletableFuture<Void> {
        return call(
            context = context, method = "removeClaim", arguments = mapOf(
                "claimId" to claimId,
                "genesisDid" to genesisDid,
                "privateKey" to privateKey
            )
        )
    }

    fun removeClaims(
        context: Context, claimIds: List<String>, genesisDid: String, privateKey: String
    ): CompletableFuture<Void> {
        return call(
            context = context, method = "removeClaims", arguments = mapOf(
                "claimIds" to claimIds,
                "genesisDid" to genesisDid,
                "privateKey" to privateKey
            )
        )
    }

    fun saveClaims(
        context: Context, claims: List<ClaimEntity>, genesisDid: String, privateKey: String
    ): CompletableFuture<List<ClaimEntity>> {
        return callAsList(
            context = context, method = "saveClaims", arguments = mapOf(
                "claims" to claims,
                "genesisDid" to genesisDid,
                "privateKey" to privateKey
            )
        )
    }

    fun updateClaim(
        context: Context,
        claimId: String,
        issuer: String? = null,
        genesisDid: String,
        state: ClaimState? = null,
        expiration: String? = null,
        type: String? = null,
        data: Map<String, Any?>? = null,
        privateKey: String
    ): CompletableFuture<ClaimEntity> {
        return call(
            context = context, method = "updateClaim", arguments = mapOf(
                "claimId" to claimId,
                "issuer" to issuer,
                "genesisDid" to genesisDid,
                "state" to state?.name,
                "expiration" to expiration,
                "type" to type,
                "data" to data,
                "privateKey" to privateKey
            )
        )
    }

    fun startDownloadCircuits(
        context: Context
    ): CompletableFuture<String> {
        return call(
            context = context, method = "startDownloadCircuits"
        )
    }

    fun isAlreadyDownloadedCircuitsFromServer(
        context: Context
    ): CompletableFuture<Boolean> {
        return call(
            context = context, method = "isAlreadyDownloadedCircuitsFromServer"
        )
    }

    fun cancelDownloadCircuits(
        context: Context
    ): CompletableFuture<Void> {
        return call(
            context = context, method = "cancelDownloadCircuits"
        )
    }

    fun proofGenerationStepsStream(
        context: Context
    ): CompletableFuture<String> {
        return call(
            context = context, method = "proofGenerationStepsStream"
        )
    }

    // We are returning the json representation of the proof because the
    // protobuf doesn't support nested lists
    fun prove(
        context: Context,
        genesisDid: String,
        profileNonce: BigInteger,
        claimSubjectProfileNonce: BigInteger,
        claim: ClaimEntity,
        circuitData: CircuitDataEntity,
        request: ProofScopeRequest,
        privateKey: String? = null,
        challenge: String? = null
    ): CompletableFuture<String> {
        return call(
            context = context, method = "prove", arguments = mapOf(
                "genesisDid" to genesisDid,
                "profileNonce" to profileNonce.toString(),
                "claimSubjectProfileNonce" to claimSubjectProfileNonce.toString(),
                "claim" to claim,
                "circuitData" to circuitData,
                "request" to request,
                "privateKey" to privateKey,
                "challenge" to challenge
            )
        )

//            .thenApply {
//            var builder: Message.Builder = JWZSDProofEntity.newBuilder()
//            JsonFormat.parser().merge(it, builder)
//            val entity = builder.build() as JWZSDProofEntity
//
//            if (!entity.hasVp()) {
//                builder = JWZProofEntity.newBuilder()
//                JsonFormat.parser().merge(it, builder)
//                builder.build() as JWZProofEntity
//            } else {
//                entity
//            }
//        }
    }

    fun startOne(
        context: Context
    ): CompletableFuture<Void> {
        return call(context = context, method = "getOne")
    }

    fun stopOne(
        context: Context
    ): CompletableFuture<Void> {
        return call(context = context, method = "closeStream", arguments = mapOf("key" to "one"))
    }
}