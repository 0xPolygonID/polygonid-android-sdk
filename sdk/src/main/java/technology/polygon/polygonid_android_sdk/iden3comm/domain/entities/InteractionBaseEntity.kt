package technology.polygon.polygonid_android_sdk.iden3comm.domain.entities

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
enum class InteractionType {
    offer, revocation, update, authRequest
}

@Serializable
enum class InteractionState {
    received, opened, accepted, declined
}

@Serializable
open class InteractionBaseEntity {
    var id: String
    var from: String
    @Serializable(with = InteractionTypeSerializer::class)
    @SerialName("type")
    var interactionType: InteractionType
    @Serializable(with = InteractionStateSerializer::class)
    @SerialName("state")
    var interactionState: InteractionState
    var timestamp: Long
    var message: String

    constructor(
        id: String,
        from: String,
        interactionType: InteractionType,
        interactionState: InteractionState,
        timestamp: Long,
        message: String
    ) {
        this.id = id
        this.from = from
        this.interactionType = interactionType
        this.interactionState = interactionState
        this.timestamp = timestamp
        this.message = message
    }
}

object InteractionTypeSerializer : KSerializer<InteractionType> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("InteractionType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: InteractionType) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): InteractionType {
        val fullName = decoder.decodeString()
        val name = fullName.split(".").last() // remove "InteractionType."
        return InteractionType.valueOf(name)
    }
}

object InteractionStateSerializer : KSerializer<InteractionState> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("InteractionState", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: InteractionState) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): InteractionState {
        val fullName = decoder.decodeString()
        val name = fullName.split(".").last() // remove "InteractionState."
        return InteractionState.valueOf(name)
    }
}