package dk.nodes.nstack.kotlin.util.extensions

import android.view.View
import android.view.ViewGroup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import org.json.JSONObject
import kotlin.reflect.KProperty

// Extension

private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"

val Date.formatted: String
    get() {
        return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(this)
    }

val String.iso8601Date: Date
    get() {
        val format = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

        return try {
            format.parse(this)
        } catch (e: Exception) {
            // We need to return some time in the past to get new translations on first run
            Calendar.getInstance().apply {
                set(Calendar.YEAR, 1971)
            }.time
        }
    }

/**
 * Gets all children (And their children too) views if the view is a view group
 */
val View.children: List<View>
    get() {
        val children = arrayListOf<View>()

        if (this is ViewGroup) {
            // Go through our views
            for (i in 0 until childCount) {
                val childView = getChildAt(i)
                // Add Self
                children.add(childView)
                // Omg recursion
                children.addAll(childView.children)
            }
        }

        return children
    }

// log function defintion, yay typedef is back :D

typealias LogFunction = (tag: String, msg: String) -> Unit

internal var nLog: LogFunction = fun(tag, msg) {
    println("$tag : $msg")
}

val String.locale: Locale
    get() {
        val splitLocale = when {
            contains("_") -> split("_")
            contains("-") -> split("-")
            else -> arrayListOf("en", "gb")
        }

        val language = splitLocale[0]
        val country = splitLocale[1]

        return Locale(language, country)
    }

val String.asJsonObject: JSONObject?
    get() = try {
        JSONObject(this)
    } catch (e: Exception) {
        null
    }

val Locale.languageCode: String
    get() {
        return try {
            val regex = "([a-z]+)([_\\-][A-Za-z]+)?".toRegex()
            val result = regex.find(language)
            result?.groupValues?.get(1) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

internal inline fun <T : Any> MutableList<T?>.removeFirst(predicate: (T) -> Boolean) {
    asSequence().filterNotNull().indexOfFirst(predicate).takeUnless { it == -1 }?.let { removeAt(it) }
}

fun <T> consumable() = ConsumableDelegate<T>()
fun <T> consumable(t: T) = ConsumableDelegate<T>(t)

class Consumable<out T>(private val content: T) {
    private var consumed = false
    fun consume(): T? {
        return if (consumed) {
            null
        } else {
            consumed = true
            content
        }
    }
}

class ConsumableDelegate<T> {

    constructor()
    constructor(value: T) {
        consumable = Consumable(value)
    }

    private var consumable: Consumable<T?>? = null
    operator fun getValue(thisRef: Any, property: KProperty<*>): T? = consumable?.consume()
    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        consumable = Consumable(value)
    }
}
