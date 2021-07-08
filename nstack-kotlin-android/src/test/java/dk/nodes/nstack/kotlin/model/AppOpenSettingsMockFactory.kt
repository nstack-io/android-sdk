package dk.nodes.nstack.kotlin.model

import dk.nodes.nstack.kotlin.models.*
import java.util.*

object AppOpenSettingsMockFactory {

    fun getAppSettings(): AppOpenSettings {
        return AppOpenSettings(
                guid = "guid",
                version = "version",
                oldVersion = "oldVersion",
                lastUpdated = Date(),
                device = "device",
                osVersion = "osVersion"
        )
    }

    fun getAppOpen(): AppOpen {
        val language1 =
                Language(0, "", Locale.ENGLISH, "", isDefault = false, isBestFit = false)
        val language2 =
                Language(0, "", Locale.GERMAN, "", isDefault = true, isBestFit = false)
        val language3 =
                Language(0, "", Locale.FRENCH, "", isDefault = false, isBestFit = false)
        val languageIndex1 =
                LocalizeIndex(0, "url1", Date(), shouldUpdate = true, language = language1)
        val languageIndex2 =
                LocalizeIndex(0, "url2", Date(), shouldUpdate = true, language = language2)
        val languageIndex3 =
                LocalizeIndex(0, "url3", Date(), shouldUpdate = false, language = language3)
        val appUpdateDate =
                AppOpenData(localize = listOf(languageIndex1, languageIndex2, languageIndex3))
        return AppOpen(appUpdateDate, AppOpenMeta(language1.locale.toString()))
    }
}