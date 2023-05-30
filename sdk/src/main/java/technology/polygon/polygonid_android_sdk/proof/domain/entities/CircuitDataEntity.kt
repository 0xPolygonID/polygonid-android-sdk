package technology.polygon.polygonid_android_sdk.proof.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class CircuitDataEntity(
    val circuitId: String,
    val datFile: ByteArray,
    val circuitFile: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CircuitDataEntity

        if (circuitId != other.circuitId) return false
        if (!datFile.contentEquals(other.datFile)) return false
        if (!circuitFile.contentEquals(other.circuitFile)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = circuitId.hashCode()
        result = 31 * result + datFile.contentHashCode()
        result = 31 * result + circuitFile.contentHashCode()
        return result
    }
}
