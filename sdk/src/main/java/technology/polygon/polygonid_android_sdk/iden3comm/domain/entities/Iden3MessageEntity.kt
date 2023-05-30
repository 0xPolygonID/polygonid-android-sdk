package technology.polygon.polygonid_android_sdk.iden3comm.domain.entities

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.auth.AuthBodyRequest
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.fetch.FetchBodyRequest
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.offer.OfferBodyRequest
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.onchain.ContractFunctionCallBodyRequest

@Serializable
enum class Iden3MessageType { unknown, auth, offer, issuance, contractFunctionCall }

@Serializable(with = Iden3MessageEntitySerializer::class)
sealed class Iden3MessageEntity {
    abstract val messageType: Iden3MessageType
    abstract val body: Any

    @Serializable
    data class AuthIden3MessageEntity(
        val id: String,
        val typ: String,
        val type: String,
        override val messageType: Iden3MessageType = Iden3MessageType.auth,
        val thid: String,
        override val body: AuthBodyRequest,
        val from: String,
        val to: String? = null
    ) : Iden3MessageEntity()

    @Serializable
    data class FetchIden3MessageEntity(
        val id: String,
        val typ: String,
        val type: String,
        override val messageType: Iden3MessageType = Iden3MessageType.issuance,
        val thid: String,
        override val body: FetchBodyRequest,
        val from: String,
        val to: String? = null
    ) : Iden3MessageEntity()

    @Serializable
    data class OfferIden3MessageEntity(
        val id: String,
        val typ: String,
        val type: String,
        override val messageType: Iden3MessageType = Iden3MessageType.offer,
        val thid: String,
        override val body: OfferBodyRequest,
        val from: String,
        val to: String? = null
    ) : Iden3MessageEntity()

    @Serializable
    data class ContractFunctionCallIden3MessageEntity(
        val id: String,
        val typ: String,
        val type: String,
        override val messageType: Iden3MessageType = Iden3MessageType.contractFunctionCall,
        val thid: String,
        override val body: ContractFunctionCallBodyRequest,
        val from: String,
        val to: String? = null
    ) : Iden3MessageEntity()
}

object Iden3MessageEntitySerializer : KSerializer<Iden3MessageEntity> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Iden3MessageEntity", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Iden3MessageEntity) {
        when (value) {
            is Iden3MessageEntity.AuthIden3MessageEntity -> Iden3MessageEntity.AuthIden3MessageEntity.serializer()
                .serialize(encoder, value)

            is Iden3MessageEntity.FetchIden3MessageEntity -> Iden3MessageEntity.FetchIden3MessageEntity.serializer()
                .serialize(encoder, value)

            is Iden3MessageEntity.OfferIden3MessageEntity -> Iden3MessageEntity.OfferIden3MessageEntity.serializer()
                .serialize(encoder, value)

            is Iden3MessageEntity.ContractFunctionCallIden3MessageEntity -> Iden3MessageEntity.ContractFunctionCallIden3MessageEntity.serializer()
                .serialize(encoder, value)
        }
    }

    override fun deserialize(decoder: Decoder): Iden3MessageEntity {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be loaded only by Json")
        val jsonObject = jsonDecoder.decodeJsonElement() as? JsonObject
            ?: throw SerializationException("Expected JsonObject")

        return when (val type = jsonObject["messageType"]?.jsonPrimitive?.content) {
            Iden3MessageType.auth.name -> jsonDecoder.json.decodeFromJsonElement(
                Iden3MessageEntity.AuthIden3MessageEntity.serializer(),
                jsonObject
            )

            Iden3MessageType.issuance.name -> jsonDecoder.json.decodeFromJsonElement(
                Iden3MessageEntity.FetchIden3MessageEntity.serializer(),
                jsonObject
            )

            Iden3MessageType.offer.name -> jsonDecoder.json.decodeFromJsonElement(
                Iden3MessageEntity.OfferIden3MessageEntity.serializer(),
                jsonObject
            )

            Iden3MessageType.contractFunctionCall.name -> jsonDecoder.json.decodeFromJsonElement(
                Iden3MessageEntity.ContractFunctionCallIden3MessageEntity.serializer(),
                jsonObject
            )

            else -> throw SerializationException("Unknown type: $type")
        }
    }
}