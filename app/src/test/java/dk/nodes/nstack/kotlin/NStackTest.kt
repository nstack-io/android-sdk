package dk.nodes.nstack.kotlin

import android.content.Context
import android.content.Intent
import dk.nodes.nstack.kotlin.managers.*
import dk.nodes.nstack.kotlin.models.*
import dk.nodes.nstack.kotlin.providers.NStackModule
import dk.nodes.nstack.kotlin.util.ContextWrapper
import io.mockk.*
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

    @Test
    fun `Test app open without internet connection`() {
        every { connectionManagerMock.isConnected } returns false

        var updated = false
        NStack.onAppUpdateListener = { updated = true }

        NStack.appOpen { }

        assert(updated)

        NStack.onAppUpdateListener = null
    }

    @Test
    fun `Test app open with internet connection`() {
        val language1 = Language(0, "", Locale.ENGLISH, "", isDefault = false, isBestFit = false)
        val language2 = Language(0, "", Locale.GERMAN, "", isDefault = true, isBestFit = false)
        val language3 = Language(0, "", Locale.FRENCH, "", isDefault = false, isBestFit = false)
        val index1 = LocalizeIndex(0, "url1", Date(), shouldUpdate = true, language = language1)
        val index2 = LocalizeIndex(0, "url2", Date(), shouldUpdate = true, language = language2)
        val index3 = LocalizeIndex(0, "url3", Date(), shouldUpdate = false, language = language3)
        val translations1 = "translations1"
        val translations2 = "translations2"
        val appUpdate = AppUpdateData(localize = listOf(index1, index2, index3))
        var updated = false

        val successCallbackSlot = slot<(AppUpdateData) -> Unit>()

        every { connectionManagerMock.isConnected } returns true

        NStack.onAppUpdateListener = { updated = true }
        NStack.appOpen { }

        verify { networkManagerMock.postAppOpen(appOpenSettings, any(), capture(successCallbackSlot), any()) }

        successCallbackSlot.captured(appUpdate)
        verify { appOpenSettingsManagerMock.setUpdateDate() }

        val loadTranslations1Slot = slot<(String) -> Unit>()
        val loadTranslations2Slot = slot<(String) -> Unit>()

        verify { networkManagerMock.loadTranslation(index1.url, capture(loadTranslations1Slot), any()) }
        verify { networkManagerMock.loadTranslation(index2.url, capture(loadTranslations2Slot), any()) }
        verify(exactly = 0) { networkManagerMock.loadTranslation(index3.url, any(), any()) }

        loadTranslations1Slot.captured(translations1)
        verify { prefManagerMock.setTranslations(any(), translations1) }

        loadTranslations2Slot.captured(translations2)
        verify { prefManagerMock.setTranslations(any(), translations2) }

        val uiActionSlot = slot<() -> Unit>()
        verify { contextWrapperMock.runUiAction(capture(uiActionSlot)) }
        uiActionSlot.captured()

        assert(NStack.defaultLanguage == language2.locale)
        assert(updated)

        NStack.onAppUpdateListener = null
    }

    @Test
    fun `Test app open error`() {
        var updated = false

        val errorCallbackSlot = slot<(Exception) -> Unit>()

        every { connectionManagerMock.isConnected } returns true

        NStack.onAppUpdateListener = { updated = true }
        NStack.appOpen { }

        verify { networkManagerMock.postAppOpen(appOpenSettings, any(), any(), capture(errorCallbackSlot)) }
        errorCallbackSlot.captured(RuntimeException())

        assert(updated)

        NStack.onAppUpdateListener = null
    }

    @Test
    fun `Test coroutine app open without internet connection`() {

    }

    @Test
    fun `Test coroutine app open with internet connection`() {

    }

    @Test
    fun `Test coroutine app open error`() {

    }

    @Test
    fun `Test message seen`() {

    }

    @Test
    fun `Test rate reminder action`() {

    }

    companion object {

        private val assetCacheManagerMock = mockk<AssetCacheManager>(relaxUnitFun = true)
        private val appOpenSettingsManagerMock = mockk<AppOpenSettingsManager>(relaxUnitFun = true)
        private val prefManagerMock = mockk<PrefManager>(relaxUnitFun = true)
        private val nstackMeta = mockk<NStackMeta>()
        private val clientAppInfoMock = mockk<ClientAppInfo>()
        private val viewTranslationManagerMock = mockk<ViewTranslationManager>(relaxUnitFun = true)
        private val classTranslationManagerMock = mockk<ClassTranslationManager>(relaxUnitFun = true)
        private val connectionManagerMock = mockk<ConnectionManager>()
        private val networkManagerMock = mockk<NetworkManager>(relaxUnitFun = true)
        private val contextWrapperMock = mockk<ContextWrapper>(relaxUnitFun = true)

        private val contextMock = mockk<Context>(relaxUnitFun = true)
        private val intentMock = mockk<Intent>()

        private val appId = "appId"
        private val apiKey = "apiKey"
        private val env = "env"

        private val guid = "guid"
        private val version = "version"
        private val oldVersion = "oldVersion"
        private val lastUpdated = Date()
        private val appOpenSettings = AppOpenSettings(
            guid = guid,
            version = version,
            oldVersion = oldVersion,
            lastUpdated = lastUpdated
        )

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
            every { anyConstructed<NStackModule>().provideConnectionManager() } returns connectionManagerMock
            every { anyConstructed<NStackModule>().provideNetworkManager() } returns networkManagerMock
            every { anyConstructed<NStackModule>().provideContextWrapper() } returns contextWrapperMock

            every { nstackMeta.appIdKey } returns appId
            every { nstackMeta.apiKey } returns apiKey
            every { nstackMeta.env } returns env

            every { contextMock.registerReceiver(any(), any()) } returns intentMock

            every { prefManagerMock.getTranslations() } returns translations
            every { assetCacheManagerMock.loadTranslations() } returns translations

            every { appOpenSettingsManagerMock.getAppOpenSettings() } returns appOpenSettings

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
