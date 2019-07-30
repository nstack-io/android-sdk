package dk.nodes.nstack.kotlin.managers

import android.content.Context
import dk.nodes.nstack.kotlin.util.extensions.toLanguageMap
import org.json.JSONObject
import java.util.*


class AssetCacheManager(private val context: Context) {

    fun loadTranslations(): HashMap<Locale, JSONObject> {
        val languageMap: HashMap<Locale, JSONObject>
        val cacheString = getFileAsStringFromCache()

        languageMap = cacheString.toLanguageMap()

        return languageMap
    }

    private fun getFileAsStringFromCache(): String {
        var string = ""

        try {
            val inputStream = context.resources.assets.open("all_translations.json")
            string = inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.message
        }

        return string
    }
}