package technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthBodyCredentialsRequest(
    val id: String?,
    val description: String?
)
