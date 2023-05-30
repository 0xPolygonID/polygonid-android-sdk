package technology.polygon.polygonid_android_sdk.iden3comm.domain.entities

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ProofRequestEntity(
    val context: HashMap<String, JsonElement>
)