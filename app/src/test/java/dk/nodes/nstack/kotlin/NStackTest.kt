package dk.nodes.nstack.kotlin

import android.content.Context
import android.content.Intent
import dk.nodes.nstack.kotlin.managers.*
import dk.nodes.nstack.kotlin.models.ClientAppInfo
import dk.nodes.nstack.kotlin.models.NStackMeta
import dk.nodes.nstack.kotlin.providers.NStackModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.json.JSONObject
import org.junit.BeforeClass
import org.junit.Test
import java.util.*

class NStackTest {

    @Test
    fun `Test init`() {
        assert(appId == NStack.appIdKey)
        assert(apiKey == NStack.appApiKey)
        assert(env == NStack.env)

        verify { contextMock.registerReceiver(any(), any()) }

        assert(NStack.languages.containsKey(locale1))

        assert(languagesChanged)

        verify { viewTranslationManagerMock.parseTranslations(translations3) }
        verify { classTranslationManagerMock.parseTranslations(translations3) }
        assert(currentLanguage == locale3)
    }

    companion object {

        private val assetCacheManagerMock = mockk<AssetCacheManager>(relaxUnitFun = true)
        private val appOpenSettingsManagerMock = mockk<AppOpenSettingsManager>()
        private val prefManagerMock = mockk<PrefManager>()
        private val nstackMeta = mockk<NStackMeta>()
        private val clientAppInfoMock = mockk<ClientAppInfo>()
        private val viewTranslationManagerMock = mockk<ViewTranslationManager>(relaxUnitFun = true)
        private val classTranslationManagerMock = mockk<ClassTranslationManager>(relaxUnitFun = true)

        private val contextMock = mockk<Context>(relaxUnitFun = true)
        private val intentMock = mockk<Intent>()

        private val appId = "appId"
        private val apiKey = "apiKey"
        private val env = "env"

        private val locale1 = Locale("it-IT")
        private val translations1 = mockk<JSONObject>()

        private val locale2 = Locale("fr-FR")
        private val translations2 = mockk<JSONObject>()

        private val locale3 = Locale.getDefault()
        private val translations3 = mockk<JSONObject>()

        private val translations = mapOf(
            locale1 to translations1,
            locale2 to translations2,
            locale3 to translations3
        )

        private var languagesChanged = false
        private var currentLanguage: Locale? = null

        init {
            mockkConstructor(NStackModule::class)

            every { anyConstructed<NStackModule>().provideAssetCacheManager() } returns assetCacheManagerMock
            every { anyConstructed<NStackModule>().provideAppOpenSettingsManager() } returns appOpenSettingsManagerMock
            every { anyConstructed<NStackModule>().providePrefManager() } returns prefManagerMock
            every { anyConstructed<NStackModule>().provideNStackMeta() } returns nstackMeta
            every { anyConstructed<NStackModule>().provideClientAppInfo() } returns clientAppInfoMock
            every { anyConstructed<NStackModule>().provideViewTranslationManager() } returns viewTranslationManagerMock
            every { anyConstructed<NStackModule>().provideClassTranslationManager() } returns classTranslationManagerMock

            every { nstackMeta.appIdKey } returns appId
            every { nstackMeta.apiKey } returns apiKey
            every { nstackMeta.env } returns env

            every { contextMock.registerReceiver(any(), any()) } returns intentMock

            every { prefManagerMock.getTranslations() } returns translations
            every { assetCacheManagerMock.loadTranslations() } returns translations

            NStack.addLanguagesChangeListener { languagesChanged = true }
            NStack.addLanguageChangeListener { currentLanguage = it }
        }


        @BeforeClass
        @JvmStatic
        fun singleSetup() {
            NStack.init(contextMock)
        }
    }
}
