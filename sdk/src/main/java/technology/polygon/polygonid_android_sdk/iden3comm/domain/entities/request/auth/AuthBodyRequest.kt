package technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.auth

import kotlinx.serialization.Serializable
import technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.ProofScopeRequestEntity

@Serializable
data class AuthBodyRequest(
    val callbackUrl: String?,
    val reason: String?,
    val message: String?,
    val scope: List<ProofScopeRequestEntity>?,
    val url: String?,
    val credentials: List<AuthBodyCredentialsRequest>?,
)
