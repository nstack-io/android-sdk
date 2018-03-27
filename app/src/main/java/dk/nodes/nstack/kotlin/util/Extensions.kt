package dk.nodes.nstack.kotlin.util

import android.view.View
import android.view.ViewGroup
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

/**
 * Gets all children (And their children too) views if the view is a view group
 */
fun View.getChildrenViews(): ArrayList<View> {
    val children = arrayListOf<View>()

    if (this is ViewGroup) {
        // Go through our views
        for (i in 0..(childCount - 1)) {
            val childView = getChildAt(i)
            // Add Self
            children.add(childView)
            // Omg recursion
            children.addAll(childView.getChildrenViews())
        }
    }

    return children
}

// log function defintion, yay typedef is back :D
typealias LogFunction = (tag: String, msg: String) -> Unit

typealias AppOpenCallback = (success: Boolean) -> Unit

internal var nLog: LogFunction = fun(tag, msg) {
    println("$tag : $msg")
}

fun String.toLocale(): Locale {
    val splitLocale = when {
        contains("_") -> split("_")
        contains("-") -> split("-")
        else -> arrayListOf("en", "gb")
    }

    val language = splitLocale[0]
    val country = splitLocale[1]

    return Locale(language, country)
}

fun String.toLanguageMap(): HashMap<Locale, JSONObject> {
    val languageMap = hashMapOf<Locale, JSONObject>()

    var jsonRoot: JSONObject?

    jsonRoot = try {
        JSONObject(this)
    } catch (e: Exception) {
        null
    }

    if (jsonRoot?.has("data") == true) {
        jsonRoot = jsonRoot.optJSONObject("data")
    }

    jsonRoot?.keys()
            ?.forEach { key ->
                val language = jsonRoot[key]
                if (language is JSONObject) {
                    val localeKey = key.toLocale()
                    languageMap[localeKey] = language
                }
            }

    return languageMap
}