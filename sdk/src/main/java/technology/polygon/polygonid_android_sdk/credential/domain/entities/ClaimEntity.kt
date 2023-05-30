package technology.polygon.polygonid_android_sdk.credential.domain.entities

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ClaimEntity(
    val id: String,
    val issuer: String,
    val did: String,
    val state: ClaimState,
    val expiration: String? = null,
    val schema: Map<String, JsonElement>? = null,
    val vocab: Map<String, JsonElement>? = null,
    val type: String,
    val info: Map<String, JsonElement>
)

@Serializable
enum class ClaimState {
    active, expired, pending, revoked
}
