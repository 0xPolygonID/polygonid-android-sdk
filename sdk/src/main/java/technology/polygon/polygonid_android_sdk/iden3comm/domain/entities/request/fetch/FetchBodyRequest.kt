package technology.polygon.polygonid_android_sdk.iden3comm.domain.entities.request.fetch

import kotlinx.serialization.Serializable

@Serializable
data class FetchBodyRequest(
    val id: String,
)
