package dk.nodes.nstack.kotlin.managers

import android.content.Context
import com.google.gson.JsonObject
import dk.nodes.nstack.kotlin.util.toLanguageMap
import java.util.*


class AssetCacheManager(private val context: Context) {

    fun loadTranslations(): HashMap<Locale, JsonObject> {
        var languageMap = hashMapOf<Locale, JsonObject>()
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