package technology.polygon.polygonid_android_sdk.common.domain.entities

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
enum class FilterOperator {
    equal,
    equalsAnyInList,
    greater,
    lesser,
    greaterEqual,
    lesserEqual,
    inList,
    or,
    nonEqual
}
@Serializable
data class FilterEntity(
    val operator: FilterOperator,
    val name: String,
    val value: JsonElement,
)
