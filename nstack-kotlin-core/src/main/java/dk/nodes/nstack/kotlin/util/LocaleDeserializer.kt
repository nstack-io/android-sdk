package dk.nodes.nstack.kotlin.util

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.util.Locale

internal class LocaleDeserializer : JsonDeserializer<Locale> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Locale {
        return Locale(json?.asString)
    }
}