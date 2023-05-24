package technology.polygon.polygonid_android_sdk.proof.domain.entities

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

@Serializable
enum class DownloadInfoType { onDone, onError, onProgress }

@Serializable(with = DownloadInfoEntitySerializer::class)
sealed class DownloadInfoEntity {
    abstract val downloadInfoType: DownloadInfoType

    @Serializable
    data class DownloadInfoOnDone(
        val contentLength: Int,
        val downloaded: Int,
        override val downloadInfoType: DownloadInfoType = DownloadInfoType.onDone,
    ) : DownloadInfoEntity()

    @Serializable
    data class DownloadInfoOnProgress(
        val contentLength: Int,
        val downloaded: Int,
        override val downloadInfoType: DownloadInfoType = DownloadInfoType.onProgress,
    ) : DownloadInfoEntity()

    @Serializable
    data class DownloadInfoOnError(
        val errorMessage: String,
        override val downloadInfoType: DownloadInfoType = DownloadInfoType.onError,
    ) : DownloadInfoEntity()
}

object DownloadInfoEntitySerializer : KSerializer<DownloadInfoEntity> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DownloadInfoEntity", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: DownloadInfoEntity) {
        when (value) {
            is DownloadInfoEntity.DownloadInfoOnDone -> DownloadInfoEntity.DownloadInfoOnDone.serializer()
                .serialize(encoder, value)

            is DownloadInfoEntity.DownloadInfoOnProgress -> DownloadInfoEntity.DownloadInfoOnProgress.serializer()
                .serialize(encoder, value)

            is DownloadInfoEntity.DownloadInfoOnError -> DownloadInfoEntity.DownloadInfoOnError.serializer()
                .serialize(encoder, value)
        }
    }

    override fun deserialize(decoder: Decoder): DownloadInfoEntity {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be loaded only by Json")
        val jsonObject = jsonDecoder.decodeJsonElement() as? JsonObject
            ?: throw SerializationException("Expected JsonObject")

        return when (val type = jsonObject["downloadInfoType"]?.jsonPrimitive?.content) {
            DownloadInfoType.onDone.name -> jsonDecoder.json.decodeFromJsonElement(
                DownloadInfoEntity.DownloadInfoOnDone.serializer(),
                jsonObject
            )

            DownloadInfoType.onError.name -> jsonDecoder.json.decodeFromJsonElement(
                DownloadInfoEntity.DownloadInfoOnError.serializer(),
                jsonObject
            )

            DownloadInfoType.onProgress.name -> jsonDecoder.json.decodeFromJsonElement(
                DownloadInfoEntity.DownloadInfoOnProgress.serializer(),
                jsonObject
            )

            else -> throw SerializationException("Unknown type: $type")
        }
    }
}
