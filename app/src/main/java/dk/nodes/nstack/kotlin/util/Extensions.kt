package dk.nodes.nstack.kotlin.util

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dk.nodes.nstack.kotlin.models.UpdateType
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

// Extension

fun Date.parseFromISO8601(str: String) {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
    try {
        this.time = format.parse(str).time
    } catch (e: Exception) {
    }
}

fun Date.toFormattedString(): String {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
    return format.format(this)
}


// log function defintion, yay typedef is back :D
typealias LogFunction = (tag: String, msg: String) -> Unit

typealias AppOpenCallback = (success: Boolean) -> Unit
typealias VersionControlCallback = (updateType: UpdateType) -> Unit
typealias SaveCallback = (success: Boolean) -> Unit
typealias LoadCallback = (data: JSONObject?) -> Unit

internal var nLog: LogFunction = fun(tag, msg) {
    println("$tag : $msg")
}

fun String.toLocale(): Locale {
    val splitLocale = when {
        contains("_") -> split("_")
        contains("-") -> split("-")
        else          -> arrayListOf("en", "gb")
    }

    val language = splitLocale[0]
    val country = splitLocale[1]

    return Locale(language, country)
}

fun String.toLanguageMap(): HashMap<Locale, JsonObject> {
    val languageMap = hashMapOf<Locale, JsonObject>()

    val jsonElement = JsonParser().parse(this) as? JsonObject ?: return languageMap

    var jsonObject = jsonElement.asJsonObject

    if (jsonObject.has("data")) {
        jsonObject = jsonObject.getAsJsonObject("data")
    }

    jsonObject.keySet()
            .forEach { key ->
                val language = jsonObject[key]
                if (language is JsonObject) {
                    val localeKey = key.toLocale()
                    languageMap[localeKey] = language
                }
            }

    return languageMap
}