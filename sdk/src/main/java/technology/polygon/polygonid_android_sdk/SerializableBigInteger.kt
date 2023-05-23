package technology.polygon.polygonid_android_sdk

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigInteger

@Serializable(with = BigIntegerSerializer::class)
data class SerializableBigInteger(val value: BigInteger) {
    @Serializer(forClass = SerializableBigInteger::class)
    companion object BigIntegerSerializer : KSerializer<SerializableBigInteger> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("BigInteger", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: SerializableBigInteger) {
            encoder.encodeString(value.value.toString())
        }

        override fun deserialize(decoder: Decoder): SerializableBigInteger {
            return SerializableBigInteger(BigInteger(decoder.decodeString()))
        }
    }
}