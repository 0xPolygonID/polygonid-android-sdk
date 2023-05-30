package technology.polygon.polygonid_android_sdk.identity.domain.entities

import kotlinx.serialization.Serializable
import technology.polygon.polygonid_android_sdk.SerializableBigInteger

@Serializable
class PrivateIdentityEntity : IdentityEntity {
    var privateKey: String = ""

    constructor(
        did: String,
        publicKey: List<String>,
        profiles: Map<SerializableBigInteger, String>,
        privateKey: String
    )
            : super(did, publicKey, profiles) {
        this.privateKey = privateKey
    }
}