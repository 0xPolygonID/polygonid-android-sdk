package technology.polygon.polygonid_android_sdk.iden3comm.domain.entities

import kotlinx.serialization.Serializable
import technology.polygon.polygonid_android_sdk.SerializableBigInteger

@Serializable
class InteractionEntity : InteractionBaseEntity {
    var genesisDid: String
    var profileNonce: SerializableBigInteger

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