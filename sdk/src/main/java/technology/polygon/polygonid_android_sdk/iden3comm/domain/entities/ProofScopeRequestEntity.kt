package technology.polygon.polygonid_android_sdk.iden3comm.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class ProofScopeRequestEntity(
    val id: Int,
    val circuitId: String,
    val optional: Boolean? = null,
    val query: ProofScopeQueryRequestEntity,
)
