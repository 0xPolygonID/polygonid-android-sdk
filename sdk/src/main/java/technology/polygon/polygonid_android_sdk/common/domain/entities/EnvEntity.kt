package technology.polygon.polygonid_android_sdk.common.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class EnvEntity(
    val blockchain: String,
    val network: String,
    val web3Url: String,
    val web3RdpUrl: String,
    val web3ApiKey: String,
    val idStateContract: String,
    val pushUrl: String,
)
