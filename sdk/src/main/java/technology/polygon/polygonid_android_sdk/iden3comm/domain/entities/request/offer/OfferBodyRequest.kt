package technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.offer

import kotlinx.serialization.Serializable

@Serializable
data class OfferBodyRequest(
    val url: String,
    val credentials: List<CredentialOfferData>
)

@Serializable
data class CredentialOfferData(
    val id: String,
    val description: String?
)
