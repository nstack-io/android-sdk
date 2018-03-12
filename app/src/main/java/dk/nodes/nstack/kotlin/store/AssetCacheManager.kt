package dk.nodes.nstack.kotlin.store

import android.content.Context
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dk.nodes.nstack.kotlin.NStack


class AssetCacheManager(private val context: Context) {

    fun loadFromAssetCache() {
        val languageMap = hashMapOf<String, JsonObject>()
        val cacheString = getFileAsStringFromCache()
        val jsonElement = JsonParser().parse(cacheString) as? JsonObject ?: return

        val jsonObject = jsonElement.asJsonObject

        jsonObject.keySet()
                .forEach { key ->
                    val language = jsonObject[key]
                    if (language is JsonObject) {
                        languageMap[key] = language
                    }
                }

        NStack.assetLanguageCache = languageMap
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