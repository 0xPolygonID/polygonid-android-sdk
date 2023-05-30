package technology.polygon.polygonid_android_sdk.identity.domain.entities

import kotlinx.serialization.Serializable
import technology.polygon.polygonid_android_sdk.SerializableBigInteger

@Serializable
open class IdentityEntity {
    var did: String = ""
    var publicKey: List<String> = emptyList()
    var profiles: Map<SerializableBigInteger, String> = emptyMap()

    constructor(
        did: String,
        publicKey: List<String>,
        profiles: Map<SerializableBigInteger, String>
    ) {
        this.did = did
        this.publicKey = publicKey
        this.profiles = profiles
    }
}
