package dk.nodes.nstack.kotlin.usecases

import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.managers.AppOpenSettingsManager
import dk.nodes.nstack.kotlin.managers.NetworkManager
import dk.nodes.nstack.kotlin.managers.PrefManager
import dk.nodes.nstack.kotlin.models.LocalizeIndex
import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.util.extensions.asJsonObject

internal class HandleLocalizeIndexUseCase(
    private val networkManager: NetworkManager,
    private val prefManager: PrefManager,
    private val appOpenSettingsManager: AppOpenSettingsManager
) {

    suspend operator fun invoke(indexes: List<LocalizeIndex>) {
        var wasUpdated = false
        indexes.forEach { index ->
            if (index.shouldUpdate) {
                networkManager.loadTranslation(index.url)?.let { translation ->

                    prefManager.setTranslations(
                        index.language.locale ?: NStack.defaultLanguage,
                        translation
                    )

                    try {
                        NStack.networkLanguages = NStack.networkLanguages?.toMutableMap()?.apply {
                            put(
                                index.language.locale ?: NStack.defaultLanguage,
                                translation.asJsonObject ?: return@apply
                            )
                        }
                        wasUpdated = true
                    } catch (e: Exception) {
                        NLog.e(this, e.toString())
                    }
                }
            }
        }

        if (wasUpdated) appOpenSettingsManager.setUpdateDate()

        indexes.find { it.language.isDefault }
            ?.let { it.language.locale }
            ?.let { NStack.defaultLanguage = it }

        indexes.find { it.language.isBestFit }
            ?.let { it.language.locale }
            ?.let { NStack.language = it }
    }
}