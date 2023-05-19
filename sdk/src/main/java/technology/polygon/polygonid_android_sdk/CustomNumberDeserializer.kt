package technology.polygon.polygonid_android_sdk

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.math.BigDecimal


class CustomNumberDeserializer : JsonDeserializer<Number?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Number? {
        if (json.isJsonPrimitive) {
            val jsonPrimitive = json.asJsonPrimitive
            if (jsonPrimitive.isNumber) {
                return BigDecimal(jsonPrimitive.asString)
            }
        }
        return null
    }
}