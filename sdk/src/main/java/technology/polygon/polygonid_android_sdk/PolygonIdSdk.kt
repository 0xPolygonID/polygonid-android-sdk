package technology.polygon.polygonid_android_sdk

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.reflect.TypeToken
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.json.JSONArray
import org.json.JSONObject
import technology.polygon.polygonid_android_sdk.common.domain.entities.EnvEntity
import technology.polygon.polygonid_android_sdk.common.domain.entities.FilterEntity
import technology.polygon.polygonid_android_sdk.credential.domain.entities.ClaimEntity
import technology.polygon.polygonid_android_sdk.credential.domain.entities.ClaimState
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.Iden3MessageEntity
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.InteractionBaseEntity
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.InteractionEntity
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.InteractionState
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.InteractionType
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.ProofScopeRequestEntity
import technology.polygon.polygonid_android_sdk.identity.domain.entities.DidEntity
import technology.polygon.polygonid_android_sdk.identity.domain.entities.IdentityEntity
import technology.polygon.polygonid_android_sdk.identity.domain.entities.PrivateIdentityEntity
import technology.polygon.polygonid_android_sdk.proof.domain.entities.CircuitDataEntity
import technology.polygon.polygonid_android_sdk.proof.domain.entities.DownloadInfoEntity
import java.math.BigInteger
import java.util.concurrent.CompletableFuture

const val CHANNEL = "technology.polygon.polygonid_flutter_sdk"
const val ENGINE = "PolygonIdEngine"

/** The PolygonIdSdk class which is used to interact with the PolygonId Flutter SDK through [MethodChannel].
 * The [PolygonIdSdk] is a singleton and needs to be initialized through [init] before being used.
 *
 * Each method needs a [Context] to be called because it uses the [FlutterEngine] to communicate with the Flutter SDK.
 * Each method call is asynchronous and returns a [CompletableFuture].
 *
 * @property flows A map of [MutableSharedFlow]. In some circumstances, the Flutter SDK uses Streams
 * which are emitting data (like Kotlin Flow). As there is no way of directly using a Stream though [MethodChannel],
 * we emit the data through [MutableSharedFlow] which are stored in this map.
 * The key of the map is the name returned by the method starting the Stream, you can get the list of all the keys
 * with [getFlowKeys].
 */
@Suppress("UNCHECKED_CAST")
class PolygonIdSdk(private val flows: MutableMap<String, MutableSharedFlow<Any?>> = mutableMapOf()) {
    companion object {
        private var ref: PolygonIdSdk? = null

        /** Get the singleton instance of the PolygonIdSdk.
         * @throws IllegalStateException if the SDK has not been initialized through [init].
         */
        fun getInstance(): PolygonIdSdk {
            return ref
                ?: throw IllegalStateException("PolygonIdSdk not initialized, please call init() first")
        }

        /** Initialize the PolygonIdSdk.
         * @param context A context.
         * @param env An optional [EnvEntity] to initialize the SDK with.
         */
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
                ), env?.let { listOf(Json.encodeToString(EnvEntity.serializer(), it)) })
            }
        }
    }

    /** Get the list of all the keys of the [MutableSharedFlow] stored in [flows].
     *
     */
    private fun getFlowKeys(): List<String> {
        return flows.keys.toList()
    }

    /** Get a [MutableSharedFlow] from [flows] with the given key.
     *
     * @param key The key of the [MutableSharedFlow] to get.
     *
     * @return The [MutableSharedFlow].
     */
    fun getFlow(key: String): MutableSharedFlow<Any?> {
        if (flows[key] == null) {
            flows[key] = MutableSharedFlow()
        }

        return flows[key]!!
    }

    /** Get a [Flow] from [flows] of the downloadCircuits key.
     *
     * @return The [Flow].
     */
    fun getDownloadCircuitsFlow(): Flow<Any> {
        return getFlow("downloadCircuits").map { data ->
            processDownloadInfo(data as String)
        }
    }

    private fun processDownloadInfo(data: String): Any {
        val jsonFormat = Json {
            serializersModule = SerializersModule {
                polymorphic(DownloadInfoEntity::class) {
                    subclass(
                        DownloadInfoEntity.DownloadInfoOnDone::class,
                        DownloadInfoEntity.DownloadInfoOnDone.serializer()
                    )
                    subclass(
                        DownloadInfoEntity.DownloadInfoOnProgress::class,
                        DownloadInfoEntity.DownloadInfoOnProgress.serializer()
                    )
                    subclass(
                        DownloadInfoEntity.DownloadInfoOnError::class,
                        DownloadInfoEntity.DownloadInfoOnError.serializer()
                    )
                }
            }
            encodeDefaults = true
        }

        return jsonFormat.decodeFromString<DownloadInfoEntity>(data)
    }

    /** Close a flow and cleanup the Flutter SDK counterpart.
     *
     */
    fun closeFlow(context: Context, key: String) {
        call<Void>(
            context = context, method = "closeStream", arguments = mapOf("key" to key)
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

    /** Call a method from the Flutter SDK.
     * Shortcut to [callFlutterMethod] with an [Any] return type.
     *
     * @param context is an Android context.
     * @param method is the name of the method to call.
     * @param arguments is an optional map of arguments to pass to the method.
     * @param isListResult is a boolean indicating if the result of the method is a list.
     *
     * @return a [CompletableFuture] containing the result of the method call.
     */
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
                    method, args, MainThreadResultHandler(result = object : MethodChannel.Result {
                        override fun success(result: Any?) {
                            val resultString = result as? String

                            if (isListResult) {
                                val resultList = mutableListOf<T>()

                                for (element: String in (result as List<String>)) {
                                    val item = Json.decodeFromString<T>(element)
                                    resultList.add(item)
                                }

                                completable.complete(resultList)
                            } else {
                                val obj = if (T::class == String::class) {
                                    resultString as T
                                } else {
                                    resultString?.let { Json.decodeFromString<T>(it) }
                                        ?: resultString
                                }
                                completable.complete(obj)
                            }
                        }

                        override fun error(
                            errorCode: String, errorMessage: String?, errorDetails: Any?
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
        context: Context, method: String, arguments: Map<String, Any?>? = null
    ): CompletableFuture<T> {
        return callFlutterMethod<T>(
            context = context, method = method, arguments = arguments
        ) as CompletableFuture<T>
    }

    private inline fun <reified T> callAsList(
        context: Context, method: String, arguments: Map<String, Any?>? = null
    ): CompletableFuture<List<T>> {
        return callFlutterMethod<T>(
            context = context, method = method, arguments = arguments, isListResult = true
        ) as CompletableFuture<List<T>>
    }

    private fun prepareArg(arg: Any?): Any? {
        return when (arg) {
            is List<*> -> arg.map { prepareArg(it) }
            else -> arg
        }
    }

    // SDK
    /**
     * Initializes the SDK.
     * @param context is an Android context.
     * @return A [CompletableFuture] that completes when the SDK is initialized.
     */
    fun init(context: Context): CompletableFuture<Void> {
        return call(
            context = context, method = "init"
        )
    }

    /**
     * Switch SDK logging on or off.
     *
     * @param context is an Android context.
     * @param enabled is a boolean indicating if logging should be enabled or not.
     */
    fun switchLog(context: Context, enabled: Boolean): CompletableFuture<Void> {
        return call(
            context = context, method = "switchLog", arguments = mapOf("enabled" to enabled)
        )
    }

    /**
     * Sets the environment.
     * @param context is an Android context.
     * @param env The environment to set.
     */
    fun setEnv(context: Context, env: EnvEntity): CompletableFuture<Void> {
        return call(
            context = context,
            method = "setEnv",
            arguments = mapOf("env" to Json.encodeToString(EnvEntity.serializer(), env))
        )
    }

    /**
     * Gets the current environment.
     * @param context is an Android context.
     *
     * @return A [CompletableFuture] that completes with the current [EnvEntity].
     */
    fun getEnv(
        context: Context
    ): CompletableFuture<EnvEntity> {
        return call(
            context = context, method = "getEnv"
        )
    }

    // Iden3comm
    /** Saves an [InteractionEntity] or [InteractionBaseEntity] in the Polygon ID Sdk
     *
     * @param interaction is the interaction to be saved
     * @param privateKey  is the key used to access all the sensitive info from the identity
     * @param genesisDid is the unique id of the identity
     * @param profileNonce is the nonce of the profile used from identity to obtain the did identifier
     *
     * @return A [CompletableFuture] that completes with the saved [InteractionEntity] or [InteractionBaseEntity]
     */
    fun addInteraction(
        context: Context,
        genesisDid: String,
        interaction: InteractionEntity,
        privateKey: String? = null
    ): CompletableFuture<Any> {
        return call<String>(
            context = context, method = "addInteraction", arguments = mapOf(
                "genesisDid" to genesisDid,
                "interaction" to Json.encodeToString(
                    InteractionEntity.serializer(),
                    interaction
                ),
                "privateKey" to privateKey,
            )
        ).thenApply {
            try {
                Json.decodeFromString<InteractionEntity>(it)
            } catch (e: Exception) {
                Json.decodeFromString<InteractionBaseEntity>(it)
            }
        }
    }

    /** Authenticate response from [Iden3MessageEntity.AuthIden3MessageEntity] sharing the needed
     * (if any) proofs requested by it
     *
     * @param message is the iden3comm message entity
     * @param genesisDid is the unique id of the identity
     * @param profileNonce is the nonce of the profile used from identity
     * to obtain the did identifier
     * @param privateKey is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs
     * @param pushToken is the push notification registration token so the issuer/verifer
     * can send notifications to the identity.
     **/
    fun authenticate(
        context: Context,
        message: Iden3MessageEntity.AuthIden3MessageEntity,
        genesisDid: String,
        profileNonce: BigInteger? = null,
        privateKey: String,
        pushToken: String? = null
    ): CompletableFuture<Void> {
        return call(
            context = context, method = "authenticate", arguments = mapOf(
                "message" to Json.encodeToString(
                    Iden3MessageEntity.AuthIden3MessageEntity.serializer(),
                    message
                ),
                "genesisDid" to genesisDid,
                "profileNonce" to profileNonce?.toString(),
                "privateKey" to privateKey,
                "pushToken" to pushToken
            )
        )
    }

    /** Fetch a list of [ClaimEntity] from issuer using iden3comm message and stores them in Polygon Id Sdk.
     *
     * @param message is the iden3comm message entity of type [Iden3MessageEntity.OfferIden3MessageEntity]
     * @param genesisDid is the unique id of the identity
     * @param profileNonce is the nonce of the profile used from identity to obtain the did identifier
     * @param privateKey is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs
     *
     * @return A [CompletableFuture] that completes with the list of [ClaimEntity] fetched.
     **/
    fun fetchAndSaveClaims(
        context: Context,
        message: Iden3MessageEntity.OfferIden3MessageEntity,
        genesisDid: String,
        profileNonce: BigInteger? = null,
        privateKey: String
    ): CompletableFuture<List<ClaimEntity>> {
        return callAsList(
            context = context, method = "fetchAndSaveClaims", arguments = mapOf(
                "message" to Json.encodeToString(
                    Iden3MessageEntity.OfferIden3MessageEntity.serializer(),
                    message
                ),
                "genesisDid" to genesisDid,
                "profileNonce" to profileNonce?.toString(),
                "privateKey" to privateKey
            )
        )
    }

    /** Get a list of [ClaimEntity] from iden3comm message stored in Polygon Id Sdk.
     *
     * @param message is the iden3comm message entity
     * @param genesisDid is the unique id of the identity
     * @param profileNonce is the nonce of the profile used from identity to obtain the did identifier
     * @param privateKey is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs
     *
     * @return A [CompletableFuture] that completes with the list of [ClaimEntity].
     **/
    fun getClaimsFromIden3Message(
        context: Context,
        message: Any,
        genesisDid: String,
        profileNonce: BigInteger? = null,
        privateKey: String
    ): CompletableFuture<List<ClaimEntity>> {
        if (message !is Iden3MessageEntity.OfferIden3MessageEntity) {
            throw IllegalArgumentException("Message must be of type OfferIden3MessageEntity")
        }

        return callAsList(
            context = context, method = "getClaimsFromIden3Message", arguments = mapOf(
                "message" to Json.encodeToString(
                    Iden3MessageEntity.OfferIden3MessageEntity.serializer(),
                    message
                ),
                "genesisDid" to genesisDid,
                "profileNonce" to profileNonce?.toString(),
                "privateKey" to privateKey
            )
        )
    }

    /** Get a list of [FilterEntity] from an iden3comm message to apply to [getClaims]
     *
     * @param message is the iden3comm message entity of type [Iden3MessageEntity.AuthIden3MessageEntity]
     * or [Iden3MessageEntity.ContractFunctionCallIden3MessageEntity]
     *
     * @return A [CompletableFuture] that completes with the list of [FilterEntity].
     **/
    fun getFilters(
        context: Context, message: Any
    ): CompletableFuture<List<FilterEntity>> {
        val encodedIden3Message = encodeIden3MessageToJson(message)

        return callAsList(
            context = context,
            method = "getFilters",
            arguments = mapOf("message" to encodedIden3Message)
        )
    }

    /** Get an iden3Message object from an iden3comm message string.
     *
     * @param message is the iden3comm message in string format
     *
     * When communicating through iden3comm with an Issuer or Verifier,
     * iden3comm message string needs to be parsed to a supported
     * iden3Message by the PolygonId Sdk using this method.
     *
     * @return A [CompletableFuture] that completes with the iden3Message that could be
     * [Iden3MessageEntity.AuthIden3MessageEntity], [Iden3MessageEntity.FetchIden3MessageEntity],
     * [Iden3MessageEntity.OfferIden3MessageEntity]
     * or [Iden3MessageEntity.ContractFunctionCallIden3MessageEntity]
     **/
    fun getIden3Message(
        context: Context, message: String
    ): CompletableFuture<Any> {
        return call<String>(
            context = context, method = "getIden3Message", arguments = mapOf("message" to message)
        ).thenApply { result ->
            try {
                val jsonFormat = Json {
                    classDiscriminator = "customTypeDiscriminator"
                    serializersModule = SerializersModule {
                        polymorphic(Iden3MessageEntity::class) {
                            subclass(
                                Iden3MessageEntity.AuthIden3MessageEntity::class,
                                Iden3MessageEntity.AuthIden3MessageEntity.serializer(),
                            )
                            subclass(
                                Iden3MessageEntity.FetchIden3MessageEntity::class,
                                Iden3MessageEntity.FetchIden3MessageEntity.serializer(),
                            )
                            subclass(
                                Iden3MessageEntity.OfferIden3MessageEntity::class,
                                Iden3MessageEntity.OfferIden3MessageEntity.serializer(),
                            )
                            subclass(
                                Iden3MessageEntity.ContractFunctionCallIden3MessageEntity::class,
                                Iden3MessageEntity.ContractFunctionCallIden3MessageEntity.serializer(),
                            )
                        }
                    }
                    encodeDefaults = true
                }
                val respo = jsonFormat.decodeFromString<Iden3MessageEntity>(result)
                respo
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    /**
     * Get the schemas from an iden3comm message.
     * @param message is the iden3comm message
     *
     * @return A [CompletableFuture] that completes with the schemas.
     */
    fun getSchemas(
        context: Context, message: Any
    ): CompletableFuture<List<Map<String, Any?>>> {
        if (message !is Iden3MessageEntity.AuthIden3MessageEntity
            && message !is Iden3MessageEntity.ContractFunctionCallIden3MessageEntity
        ) {
            throw IllegalArgumentException("Message must be of type AuthIden3MessageEntity or ContractFunctionCallIden3MessageEntity")
        }

        val encodedMessage = encodeIden3MessageToJson(message)

        return call<String>(
            context = context,
            method = "getSchemas",
            arguments = mapOf("message" to encodedMessage),
        ).thenApply { result ->
            try {
                Gson().fromJson(
                    result,
                    object : TypeToken<List<Map<String, Any?>>>() {}.type
                )
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /** Gets a list of interaction associated to the identity previously stored
     * in the the Polygon ID Sdk
     *
     * @param genesisDid is the unique id of the identity
     * @param profileNonce is the nonce of the profile used from identity
     * to obtain the did identifier
     * @param privateKey  is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs
     *
     * @return A [CompletableFuture] that completes with the list of interactions fetched which can be
     * [InteractionEntity] or [InteractionBaseEntity]
     **/
    fun getInteractions(
        context: Context,
        genesisDid: String? = null,
        profileNonce: BigInteger? = null,
        privateKey: String? = null,
        type: List<InteractionType>? = null,
        states: List<InteractionState>? = null,
        filters: List<FilterEntity>? = null
    ): CompletableFuture<List<InteractionEntity>> {
        return callAsList<InteractionEntity>(
            context = context, method = "getInteractions", arguments = mapOf(
                "genesisDid" to genesisDid,
                "profileNonce" to profileNonce?.toString(),
                "privateKey" to privateKey,
                "type" to type?.map { it.name },
                "states" to states?.map { it.name },
                "filters" to filters
            )
        ).thenApply { result ->
            result
        }
    }

    /** Get a list of proofs from iden3comm message
     *
     * @param message is the iden3comm message entity
     * @param genesisDid is the unique id of the identity
     * @param profileNonce is the nonce of the profile used from identity
     * to obtain the did identifier
     * @param privateKey is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs
     *
     * FIXME: return type should be List<JWZProofEntity>
     * @return We are returning the json representation of the proof because the
     * protobuf doesn't support nested lists
     **/
    fun getProofs(
        context: Context,
        message: Any,
        genesisDid: String,
        profileNonce: BigInteger? = null,
        privateKey: String,
        challenge: String? = null
    ): CompletableFuture<List<String>> {
        return callAsList(
            context = context, method = "getProofs", arguments = mapOf(
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

    /** Removes a list of interactions from the Polygon ID Sdk by their ids
     *
     * @param genesisDid is the unique id of the identity
     * @param privateKey  is the key used to access all the sensitive info from the identity
     * @param ids is the list of ids of the interactions to be removed
     **/
    fun removeInteractions(
        context: Context, genesisDid: String? = null, privateKey: String? = null, ids: List<String>
    ): CompletableFuture<Void> {
        return call(
            context = context, method = "removeInteractions", arguments = mapOf(
                "genesisDid" to genesisDid, "privateKey" to privateKey, "ids" to ids
            )
        )
    }

    /** Updated the states of a interaction in the Polygon ID Sdk
     *
     * @param id is the id of the notification to be updated
     * @param genesisDid is the unique id of the identity
     * @param privateKey  is the key used to access all the sensitive info from the identity
     * @param state is the new state of the interaction
     *
     * @return A [CompletableFuture] that completes with the interaction updated which can be
     * [InteractionEntity] or [InteractionBaseEntity]
     **/
    fun updateInteraction(
        context: Context,
        id: String,
        genesisDid: String? = null,
        privateKey: String? = null,
        state: InteractionState
    ): CompletableFuture<Any> {
        return call<String>(
            context = context, method = "updateInteraction", arguments = mapOf(
                "genesisDid" to genesisDid,
                "privateKey" to privateKey,
                "state" to state.name,
                "id" to id,
            )
        ).thenApply {
            try {
                Json.decodeFromString<InteractionEntity>(it)
            } catch (e: Exception) {
                Json.decodeFromString<InteractionBaseEntity>(it)
            }
        }
    }

    // Identity
    /** Creates and stores an [PrivateIdentityEntity] from a secret
     * if it doesn't exist already in the Polygon ID Sdk.
     *
     * If [secret] is omitted or null, a random one will be used to create a new identity.
     *
     * Be aware [secret] is internally converted to a 32 length bytes array
     * in order to be compatible with the SDK. The following rules will be applied:
     * - If the byte array is not 32 length, it will be padded with 0s.
     * - If the byte array is longer than 32, an exception will be thrown.
     *
     * The identity will be created using the current env set with [setEnv]
     *
     * @param context is an Android context.
     * @param secret is the secret used to create the identity.
     *
     * @return A [CompletableFuture] that completes with the stored identity.
     * **/
    fun addIdentity(
        context: Context, secret: String
    ): CompletableFuture<PrivateIdentityEntity> {
        return call(
            context = context, method = "addIdentity", arguments = mapOf("secret" to secret)
        )
    }

    /** Adds a profile if it doesn't already exist to the identity derived from private key and stored
     * in the Polygon ID Sdk.
     *
     * @param genesisDid is the unique id of the identity which profileNonce is 0.
     * @param privateKey  is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs.
     * @param profileNonce is the nonce of the profile used from identity
     * to obtain the did identifier. Value must be greater than 0 and less than 2^248
     *
     * The profile will be added using the current env set with [setEnv]
     **/
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

    /*** Backup a previously stored [IdentityEntity] from a privateKey
     * associated to the identity
     *
     * @param context is an Android context.
     * @param genesisDid is the unique id of the identity which profileNonce is 0
     * @param privateKey is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs
     * using the claims associated to the identity
     *
     * @return An encrypted Identity's Database.
     **/
    fun backupIdentity(
        context: Context, genesisDid: String, privateKey: String
    ): CompletableFuture<String> {
        return call(
            context = context, method = "backupIdentity", arguments = mapOf(
                "genesisDid" to genesisDid, "privateKey" to privateKey
            )
        )
    }

    /** Checks the identity validity from a secret
     *
     * If [secret] is omitted or null, a random one will be used to create a new identity.
     *
     * Be aware [secret] is internally converted to a 32 length bytes array
     * in order to be compatible with the SDK. The following rules will be applied:
     * - If the byte array is not 32 length, it will be padded with 0s.
     * - If the byte array is longer than 32, an exception will be thrown.
     *
     * @param context is an Android context.
     * @param secret is the secret used to create the identity
     **/
    fun checkIdentityValidity(
        context: Context, secret: String
    ): CompletableFuture<Void> {
        return call(
            context = context,
            method = "checkIdentityValidity",
            arguments = mapOf("secret" to secret)
        )
    }

    /** Get a [DidEntity] from a did
     *
     * @param context is an Android context.
     * @param did is the did of the identity
     *
     * @return A [CompletableFuture] that completes with the did entity.
     */
    fun getDidEntity(
        context: Context, did: String
    ): CompletableFuture<DidEntity> {
        return call(
            context = context, method = "getDidEntity", arguments = mapOf("did" to did)
        )
    }

    /** Returns the did identifier derived from a privateKey
     *
     * @param context is an Android context.
     * @param privateKey is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs
     * using the claims associated to the identity
     * @param blockchain is the blockchain name where the identity
     * is associated, e.g. Polygon
     * @param network is the network name of the blockchain where the identity
     * is associated, e.g. Main
     *
     * @return A [CompletableFuture] that completes with the did identifier.
     **/
    fun getDidIdentifier(
        context: Context, privateKey: String, blockchain: String, network: String
    ): CompletableFuture<String> {
        return call(
            context = context, method = "getDidIdentifier", arguments = mapOf(
                "privateKey" to privateKey, "blockchain" to blockchain, "network" to network
            )
        )
    }

    /** Get a list of public info of [IdentityEntity] associated
     * to the identities stored in the Polygon ID Sdk.
     * The identities returned will come from the current env set with [setEnv]
     *
     * @param context is an Android context.
     *
     * @return A [CompletableFuture] that completes with the list of identities.
     **/
    fun getIdentities(
        context: Context
    ): CompletableFuture<List<IdentityEntity>> {
        return callAsList(
            context = context, method = "getIdentities"
        )
    }

    /** Gets an identity from an identifier.
     *
     * @param context is an Android context.
     * @param genesisDid is the unique id of the identity which profileNonce is 0
     * @param privateKey is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs
     * using the claims associated to the identity
     *
     * Be aware the secret is internally converted to a 32 length bytes array
     * in order to be compatible with the SDK. The following rules will be applied:
     * - If the byte array is not 32 length, it will be padded with 0s.
     * - If the byte array is longer than 32, an exception will be thrown.
     *
     * @return An identity as a [PrivateIdentityEntity] or [IdentityEntity]
     **/
    fun getIdentity(
        context: Context, privateKey: String? = null, genesisDid: String? = null
    ): CompletableFuture<IdentityEntity> {
        return call<String>(
            context = context,
            method = "getIdentity",
            arguments = mapOf("privateKey" to privateKey, "genesisDid" to genesisDid)
        ).thenApply {
            when {
                privateKey != null -> {
                    Json.decodeFromString<PrivateIdentityEntity>(
                        it
                    )
                }

                else -> {
                    Json.decodeFromString<IdentityEntity>(
                        it
                    )
                }
            }
        }
    }

    /** Gets the identity private key from a secret
     *
     * Be aware [secret] is internally converted to a 32 length bytes array
     * in order to be compatible with the SDK. The following rules will be applied:
     * - If the byte array is not 32 length, it will be padded with 0s.
     * - If the byte array is longer than 32, an exception will be thrown.
     *
     * @param context is an Android context.
     * @param secret is the secret to get the private key from.
     *
     *  @return A [CompletableFuture] that completes with the private key.
     **/
    fun getPrivateKey(
        context: Context, secret: String
    ): CompletableFuture<String> {
        return call(
            context = context, method = "getPrivateKey", arguments = mapOf("secret" to secret)
        )
    }

    /** Gets a map of profile nonce as key and profile did as value associated
     * to the identity derived from private key and stored in the Polygon ID Sdk.
     *
     * @param context is an Android context.
     * @param genesisDid is the unique id of the identity which profileNonce is 0.
     * @param privateKey is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs.
     *
     * The returned profiles will come from the current env set with [setEnv]
     * @return A [CompletableFuture] that completes with the map of profiles.
     **/
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

    /** Get the state from a did
     *
     *  @param context is an Android context.
     *  @param did is the did to get the state from.
     *
     *  @return A [CompletableFuture] that completes with the state.
     */
    fun getState(
        context: Context, did: String
    ): CompletableFuture<String> {
        return call(
            context = context, method = "getState", arguments = mapOf("did" to did)
        )
    }

    /** Remove the previously stored identity associated with the identifier
     *
     * @param context is an Android context.
     * @param genesisDid is the unique id of the identity which profileNonce is 0.
     * @param privateKey is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs
     * using the claims associated to the identity.
     *
     **/
    fun removeIdentity(
        context: Context, genesisDid: String, privateKey: String
    ): CompletableFuture<Void> {
        return call(
            context = context, method = "removeIdentity", arguments = mapOf(
                "genesisDid" to genesisDid, "privateKey" to privateKey
            )
        )
    }

    /** Removes a profile from the identity derived from private key and stored
     * in the Polygon ID Sdk.
     *
     * @param context is an Android context.
     * @param genesisDid is the unique id of the identity which profileNonce is 0.
     * @param privateKey  is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs.
     * @param profileNonce is the nonce of the profile used from identity
     * to obtain the did identifier. Value must be greater than 0 and less than 2^248.
     *
     * The profile will be removed using the current env set with [setEnv]
     **/
    fun removeProfile(
        context: Context, privateKey: String, profileNonce: BigInteger, genesisDid: String? = null
    ): CompletableFuture<Void> {
        return call(
            context = context, method = "removeProfile", arguments = mapOf(
                "privateKey" to privateKey,
                "profileNonce" to profileNonce.toString(),
                "genesisDid" to genesisDid
            )
        )
    }

    /** Restores an [IdentityEntity] from a privateKey and encrypted backup databases
     * associated to the identity
     *
     * @param context is an Android context.
     * @param genesisDid is the unique id of the identity which profileNonce is 0.
     * @param privateKey is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs
     * using the credentials associated to the identity.
     * @param encryptedDb is an encrypted Identity's Database.
     *
     * @return A [CompletableFuture] that completes with the restored identity.
     **/
    fun restoreIdentity(
        context: Context, genesisDid: String, privateKey: String, encryptedDb: String? = null
    ): CompletableFuture<PrivateIdentityEntity> {
        return call(
            context = context, method = "restoreIdentity", arguments = mapOf(
                "genesisDid" to genesisDid, "privateKey" to privateKey, "encryptedDb" to encryptedDb
            )
        )
    }

    /** Sign a message through a identity's private key.
     *
     * @param context is an Android context.
     * @param privateKey is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs.
     * @param message is the message to sign.
     *
     * @return A [CompletableFuture] that completes with the signature.
     **/
    fun sign(
        context: Context, privateKey: String, message: String
    ): CompletableFuture<String> {
        return call(
            context = context, method = "sign", arguments = mapOf(
                "privateKey" to privateKey, "message" to message
            )
        )
    }

    // Credential
    /** Get a list of [ClaimEntity] associated to the identity previously stored
     * in the the Polygon ID Sdk.
     *
     * @param context is an Android context.
     * @param filters is a list to filter the claims.
     * @param genesisDid is the unique id of the identity.
     * @param privateKey is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs.
     *
     * @return A [CompletableFuture] that completes with the list of claims.
     **/
    fun getClaims(
        context: Context,
        filters: List<FilterEntity>? = null,
        genesisDid: String,
        privateKey: String
    ): CompletableFuture<List<ClaimEntity>> {
        return callAsList(
            context = context, method = "getClaims", arguments = mapOf(
                "filters" to filters, "genesisDid" to genesisDid, "privateKey" to privateKey
            )
        )
    }

    /** Get a list of [ClaimEntity] filtered by ids associated to the identity previously stored
     * in the the Polygon ID Sdk.
     *
     * @param context is an Android context.
     * @param claimIds is a list of claim ids to filter by.
     * @param genesisDid is the unique id of the identity.
     * @param privateKey is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs.
     *
     *  @return A [CompletableFuture] that completes with the list of claims.
     **/
    fun getClaimsByIds(
        context: Context, claimIds: List<String>, genesisDid: String, privateKey: String
    ): CompletableFuture<List<ClaimEntity>> {
        return callAsList(
            context = context, method = "getClaimsByIds", arguments = mapOf(
                "claimIds" to claimIds, "genesisDid" to genesisDid, "privateKey" to privateKey
            )
        )
    }

    /** Remove a [ClaimEntity] filtered by id associated to the identity previously stored
     * in the the Polygon ID Sdk.
     *
     * @param context is an Android context.
     * @param claimId is the unique id of the claim to remove.
     * @param genesisDid is the unique id of the identity.
     * @param privateKey  is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs.
     *
     **/
    fun removeClaim(
        context: Context, claimId: String, genesisDid: String, privateKey: String
    ): CompletableFuture<Void> {
        return call(
            context = context, method = "removeClaim", arguments = mapOf(
                "claimId" to claimId, "genesisDid" to genesisDid, "privateKey" to privateKey
            )
        )
    }

    /** Remove a list of [ClaimEntity] filtered by ids associated to the identity previously stored
     * in the the Polygon ID Sdk.
     *
     * @param context is an Android context.
     * @param claimIds is a list of claim ids to filter by.
     * @param genesisDid is the unique id of the identity.
     * @param privateKey  is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs.
     *
     **/
    fun removeClaims(
        context: Context, claimIds: List<String>, genesisDid: String, privateKey: String
    ): CompletableFuture<Void> {
        return call(
            context = context, method = "removeClaims", arguments = mapOf(
                "claimIds" to claimIds, "genesisDid" to genesisDid, "privateKey" to privateKey
            )
        )
    }

    fun saveClaims(
        context: Context, claims: List<ClaimEntity>, genesisDid: String, privateKey: String
    ): CompletableFuture<List<ClaimEntity>> {
        return callAsList(
            context = context, method = "saveClaims", arguments = mapOf(
                "claims" to claims, "genesisDid" to genesisDid, "privateKey" to privateKey
            )
        )
    }

    /** Update a [ClaimEntity] filtered by id associated to the identity previously stored
     * in the the Polygon ID Sdk.
     *
     * @param context is an Android context.
     * @param claimId is a claim id to filter by.
     * @param genesisDid is the unique id of the identity.
     * @param privateKey  is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs.
     * @param issuer is the issuer of the claim.
     * @param state is the state of the claim.
     * @param expiration is the expiration of the claim.
     * @param type is the type of the claim.
     * @param data is the data of the claim and could be subject to validation by the data layer
     *
     * @return A [CompletableFuture] that completes with the updated claim.
     **/
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

    // Proof
    /** Start the download of the circuits from the server.
     *
     * @param context is an Android context.
     *
     * @return A [CompletableFuture] that completes with the key of the [flows] where the download
     * will be emitted to.
     */
    fun startDownloadCircuits(
        context: Context
    ): CompletableFuture<String> {
        return call(
            context = context, method = "startDownloadCircuits"
        )
    }

    /** Check if the circuits are already downloaded from the server.
     *
     * @param context is an Android context.
     */
    fun isAlreadyDownloadedCircuitsFromServer(
        context: Context
    ): CompletableFuture<Boolean> {
        return call(
            context = context, method = "isAlreadyDownloadedCircuitsFromServer"
        )
    }

    /** Cancel the download of the circuits from the server.
     *
     * @param context is an Android context.
     */
    fun cancelDownloadCircuits(
        context: Context
    ): CompletableFuture<Void> {
        return call(
            context = context, method = "cancelDownloadCircuits"
        )
    }

    /** Get the proof generation steps.
     *
     * @param context is an Android context.
     *
     * @return A [CompletableFuture] that completes with the key of the [flows] where the download
     * will be emitted to.
     */
    fun proofGenerationStepsStream(
        context: Context
    ): CompletableFuture<String> {
        return call(
            context = context, method = "proofGenerationStepsStream"
        )
    }

    /** Get a proof from a [ClaimEntity].
     *
     * @param context is an Android context.
     * @param genesisDid is the unique id of the identity which profileNonce is 0.
     * @param privateKey  is the key used to access all the sensitive info from the identity
     * and also to realize operations like generating proofs.
     * @param profileNonce is the nonce of the profile used from identity
     * @param claimSubjectProfileNonce is the nonce of the profile used from claim subject
     * @param claim is the claim to generate the proof from.
     * @param circuitData is the circuit data to generate the proof from.
     * @param request is the proof request.
     * @param challenge is the challenge to generate the proof from.
     *
     * FIXME: return type should be List<JWZProofEntity>
     * @return A [CompletableFuture] that completes with the proof.
     * We are returning the json representation of the proof because the
     * protobuf doesn't support nested lists.
     */
    fun prove(
        context: Context,
        genesisDid: String,
        profileNonce: BigInteger,
        claimSubjectProfileNonce: BigInteger,
        claim: ClaimEntity,
        circuitData: CircuitDataEntity,
        request: ProofScopeRequestEntity,
        privateKey: String? = null,
        challenge: String? = null
    ): CompletableFuture<String> {
        return call(
            context = context, method = "prove", arguments = mapOf(
                "genesisDid" to genesisDid,
                "profileNonce" to profileNonce.toString(),
                "claimSubjectProfileNonce" to claimSubjectProfileNonce.toString(),
                "claim" to claim,
                "circuitData" to Json.encodeToString(CircuitDataEntity.serializer(), circuitData),
                "request" to Json.encodeToString(ProofScopeRequestEntity.serializer(), request),
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

    private fun encodeIden3MessageToJson(iden3message: Any): String {
        return when (iden3message) {
            is Iden3MessageEntity.AuthIden3MessageEntity ->
                Json.encodeToString(
                    Iden3MessageEntity.AuthIden3MessageEntity.serializer(),
                    iden3message
                )

            is Iden3MessageEntity.ContractFunctionCallIden3MessageEntity ->
                Json.encodeToString(
                    Iden3MessageEntity.ContractFunctionCallIden3MessageEntity.serializer(),
                    iden3message
                )

            is Iden3MessageEntity.OfferIden3MessageEntity ->
                Json.encodeToString(
                    Iden3MessageEntity.OfferIden3MessageEntity.serializer(),
                    iden3message
                )

            is Iden3MessageEntity.FetchIden3MessageEntity ->
                Json.encodeToString(
                    Iden3MessageEntity.FetchIden3MessageEntity.serializer(),
                    iden3message
                )

            else -> ""
        }
    }
}