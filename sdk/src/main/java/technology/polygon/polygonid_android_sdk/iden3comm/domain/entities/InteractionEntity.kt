package technology.polygon.polygonid_android_sdk.iden3comm.domain.entities

import kotlinx.serialization.Serializable
import technology.polygon.polygonid_android_sdk.SerializableBigInteger
import java.math.BigInteger

@Serializable
class InteractionEntity : InteractionBaseEntity {
    var genesisDid: String = ""
    var profileNonce: SerializableBigInteger = SerializableBigInteger(BigInteger("0"))

    constructor(
        id: String,
        from: String,
        interactionType: InteractionType,
        interactionState: InteractionState,
        timestamp: Long,
        message: String,
        genesisDid: String,
        profileNonce: SerializableBigInteger
    ) : super(id, from, interactionType, interactionState, timestamp, message) {
        this.genesisDid = genesisDid
        this.profileNonce = profileNonce
    }
}