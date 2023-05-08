package technology.polygon.polygonid_android_sdk

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class CustomIntDeserializer : JsonDeserializer<Int> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Int {
        return try {
            json.asInt
        } catch (e: NumberFormatException) {
            (json.asDouble).toInt()
        }
    }
}