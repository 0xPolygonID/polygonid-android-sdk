package technology.polygon.polygonid_android_sdk.iden3comm.domain.entities

import kotlinx.serialization.Serializable

@Serializable
enum class InteractionType {
    offer, revocation, update, authRequest
}

@Serializable
enum class InteractionState {
    received, opened, accepted, declined
}

@Serializable
open class InteractionBaseEntity {
    var id: String = ""
    var from: String = ""
    var interactionType: InteractionType = InteractionType.offer
    var interactionState: InteractionState = InteractionState.received
    var timestamp: Long = 0
    var message: String = ""

    constructor(
        id: String,
        from: String,
        interactionType: InteractionType,
        interactionState: InteractionState,
        timestamp: Long,
        message: String
    ) {
        this.id = id
        this.from = from
        this.interactionType = interactionType
        this.interactionState = interactionState
        this.timestamp = timestamp
        this.message = message
    }
}