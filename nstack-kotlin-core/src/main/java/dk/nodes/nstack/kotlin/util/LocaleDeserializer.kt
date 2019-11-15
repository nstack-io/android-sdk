package dk.nodes.nstack.kotlin.util

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.util.Locale

class LocaleDeserializer : JsonDeserializer<Locale> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Locale? {

        return json?.asString?.let {
            val locale = Locale.forLanguageTag(it)
            if (locale.toLanguageTag() != "und") {
                locale
            } else {
                val split = it.split("_", "-")
                if (split.size == 3) {
                    Locale(
                        split[0],
                        split[1],
                        split[2]
                    )
                } else
                    Locale(
                        split.firstOrNull() ?: "",
                        split.getOrNull(1) ?: ""
                    )
            }

        }
    }
}
