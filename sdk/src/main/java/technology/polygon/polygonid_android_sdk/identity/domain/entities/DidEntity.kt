package technology.polygon.polygonid_android_sdk.identity.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class DidEntity(
    val did: String,
    val identifier: String,
    val blockchain: String,
    val network: String,
)
