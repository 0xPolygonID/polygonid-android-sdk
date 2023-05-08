package technology.polygon.polygonid_android_sdk

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

class CustomIntTypeAdapter : TypeAdapter<Number>() {
    @Throws(IOException::class)
    override fun read(reader: JsonReader): Number {
        return if (reader.peek() == JsonToken.NUMBER) {
            val value = reader.nextString()
            try {
                value.toInt()
            } catch (e: NumberFormatException) {
                value.toDouble()
            }
        } else {
            reader.skipValue()
            0
        }
    }

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Number) {
        out.value(value.toString())
    }
}