package dk.nodes.nstack.kotlin.managers

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.JsonObject
import dk.nodes.nstack.kotlin.models.Constants
import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.util.parseFromISO8601
import dk.nodes.nstack.kotlin.util.toFormattedString
import dk.nodes.nstack.kotlin.util.toLanguageMap
import java.util.*


class PrefManager(context: Context) {
    private val TAG = "NStack"
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private fun setLastUpdateDate() {
        val updateString = Date().toFormattedString()
        setString(Constants.spk_update_time, updateString)
    }

    fun getLastUpdateDate(): Date? {
        val updateString = getString(Constants.spk_update_time) ?: return null
        val updateDate = Date()

        updateDate.parseFromISO8601(updateString)

        return updateDate
    }

    fun setTranslations(translationString: String) {
        setLastUpdateDate()
        setString(Constants.spk_languages_cache, translationString)
    }

    fun getTranslations(): HashMap<Locale, JsonObject> {
        val translationString = getString(Constants.spk_languages_cache)
        val languageMap = translationString?.toLanguageMap()

        NLog.d(TAG,"Getting Translations: ${languageMap?.size}")

        return languageMap ?: hashMapOf()
    }

    private fun setString(key: String, value: String) {
        prefs.edit()
                .putString(key, value)
                .apply()
    }

    private fun getString(key: String): String? {
        return prefs.getString(key, null)
    }
}