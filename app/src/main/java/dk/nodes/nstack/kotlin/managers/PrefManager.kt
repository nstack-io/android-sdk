package dk.nodes.nstack.kotlin.managers

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import dk.nodes.nstack.kotlin.models.Constants
import dk.nodes.nstack.kotlin.util.asJsonObject
import dk.nodes.nstack.kotlin.util.formatted
import dk.nodes.nstack.kotlin.util.iso8601Date
import org.json.JSONObject
import java.util.*


class PrefManager(context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private fun setLastUpdateDate() {
        val updateString = Date().formatted
        setString(Constants.spk_nstack_network_update_time, updateString)
    }

    fun getLastUpdateDate(): Date? {
        val updateString = getString(Constants.spk_nstack_network_update_time) ?: return null

        return updateString.iso8601Date
    }

    fun setTranslations(locale: Locale, translations: String) {
        setLastUpdateDate()
        setString("${Constants.spk_nstack_language_cache}_$locale", translations)
    }

    fun getTranslations(): Map<Locale, JSONObject> {
        return prefs.all
            .filterKeys { it.startsWith(Constants.spk_nstack_language_cache) }
            .mapKeys { it.key.localeFromKey }
            .mapValues { (it.value as String).asJsonObject!! }
    }

    private fun setString(key: String, value: String) {
        prefs.edit()
            .putString(key, value)
            .apply()
    }

    private fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    private val String.localeFromKey: Locale
        get() {
            val pattern = "${Constants.spk_nstack_language_cache}_(\\w\\w[\\-_]\\w\\w)".toRegex()
            val result = pattern.find(this)!!
            return Locale(result.groupValues[1])
        }
}