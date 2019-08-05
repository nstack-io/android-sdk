package dk.nodes.nstack.kotlin.managers

import dk.nodes.nstack.kotlin.util.ContextWrapper
import dk.nodes.nstack.kotlin.util.asJsonObject
import org.json.JSONObject
import java.util.*


internal class AssetCacheManager(private val contextWrapper: ContextWrapper) {

    fun loadTranslations(): Map<Locale, JSONObject> {
        return contextWrapper.assets
            .asSequence()
            .mapNotNull { loadTranslation(it) }
            .sortedBy { it.index }
            .map { it.locale to it.translations }
            .toList()
            .toMap()
    }

    private fun loadTranslation(translationFile: String): Translation? {
        val pattern = "translations_(\\d+)_(\\w{2}[-_]\\w{2})\\.json$".toRegex()
        val result = pattern.find(translationFile) ?: return null
        val groups = result.groupValues
        val index = groups[1]
        val locale = Locale(groups[2])
        val translations = try {
            contextWrapper.readAsset(translationFile).asJsonObject ?: return null
        } catch (e: Exception) {
            return null
        }
        return Translation(index = index, locale = locale, translations = translations)
    }

    private data class Translation(
        val index: String,
        val locale: Locale,
        val translations: JSONObject
    )
}
