package technology.polygon.polygonid_android_sdk.common.domain.entities

import kotlinx.serialization.Serializable

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

//TODO handle Any?
data class FilterEntity(
    val operator: FilterOperator,
    val name: String,
    val value: Any,
)
