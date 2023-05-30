package technology.polygon.polygonid_android_sdk.iden3comm.domain.entities

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ProofScopeQueryRequestEntity(
    val allowedIssuers: List<String>? = null,
    val context: String? = null,
    val type: String? = null,
    val challenge: Int? = null,
    val skipClaimRevocationCheck: Boolean?,
    val credentialSubject: HashMap<String, JsonElement>?,
)
