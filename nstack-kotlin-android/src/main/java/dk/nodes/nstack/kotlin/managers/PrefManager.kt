package dk.nodes.nstack.kotlin.managers

import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.util.Constants
import dk.nodes.nstack.kotlin.util.Preferences
import dk.nodes.nstack.kotlin.util.extensions.asJsonObject
import dk.nodes.nstack.kotlin.util.extensions.formatted
import dk.nodes.nstack.kotlin.util.extensions.iso8601Date
import java.util.Date
import java.util.Locale
import org.json.JSONObject
import java.lang.Exception

internal class PrefManager(private val preferences: Preferences) {

    private fun setLastUpdateDate() {
        val updateString = Date().formatted
        preferences.saveString(Constants.spk_nstack_network_update_time, updateString)
    }

    fun getLastUpdateDate(): Date? {
        val updateString = preferences.loadString(Constants.spk_nstack_network_update_time)

        return if (updateString.isEmpty()) {
            null
        } else {
            updateString.iso8601Date
        }
    }

    fun setTranslations(locale: Locale, translations: String) {
        setLastUpdateDate()
        preferences.saveString("${Constants.spk_nstack_language_cache}_$locale", translations)
    }

    fun getTranslations(): Map<Locale, JSONObject> {
        return preferences
            .loadStringsWhereKey { it.startsWith(Constants.spk_nstack_language_cache) }
            .mapKeys { it.key.localeFromKey }
            .mapValues { it.value.asJsonObject ?: JSONObject() }
    }

    private val String.localeFromKey: Locale
        get() {
            return try {
                // Support both "en-GB", "en_GB" and "en"
                val pattern = "${Constants.spk_nstack_language_cache}_(\\w\\w[\\-_]\\w\\w|\\w\\w)".toRegex()
                val result = pattern.find(this) ?: return fallbackLocale
                Locale(result.groupValues[1])
            } catch (e: Exception) {
                fallbackLocale
            }
        }

    private val fallbackLocale: Locale
            get() {
                return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Locale(NStack.defaultLanguage.toLanguageTag())
                } else {
                    Locale(NStack.defaultLanguage.toString())
                }
            }
}
