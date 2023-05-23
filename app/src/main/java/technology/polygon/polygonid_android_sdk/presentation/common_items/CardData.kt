package technology.polygon.polygonid_android_sdk.presentation.common_items

data class CardData(
    val title: String,
    val methodName: String,
    val description: String,
    val onClick: () -> Unit
)