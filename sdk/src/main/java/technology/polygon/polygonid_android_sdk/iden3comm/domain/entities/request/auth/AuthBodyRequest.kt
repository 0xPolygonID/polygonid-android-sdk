package technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.auth

import kotlinx.serialization.Serializable
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.ProofScopeRequestEntity

@Serializable
data class AuthBodyRequest(
    val callbackUrl: String? = null,
    val reason: String? = null,
    val message: String? = null,
    val scope: List<ProofScopeRequestEntity>? = null,
    val url: String? = null,
    val credentials: List<AuthBodyCredentialsRequest>? = null,
)
