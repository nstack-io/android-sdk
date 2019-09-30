package dk.nodes.nstack.kotlin

import android.content.Context
import android.content.Intent
import android.util.Log
import dk.nodes.nstack.kotlin.managers.*
import dk.nodes.nstack.kotlin.models.*
import dk.nodes.nstack.kotlin.providers.ManagersModule
import dk.nodes.nstack.kotlin.providers.NStackModule
import dk.nodes.nstack.kotlin.util.ContextWrapper
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.setMain
import org.json.JSONObject
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.*

internal class NStackTest {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Test
    fun `Test init`() {
        assert(appId == NStack.appIdKey)
        assert(apiKey == NStack.appApiKey)
        assert(env == NStack.env)

        verify { contextMock.registerReceiver(any(), any()) }

        assert(NStack.languages.containsKey(locale1))

        assert(languagesChanged)

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
        var updated = false
        val translations1 = "translations1"
        val translations2 = "translations2"

        val successCallbackSlot = slot<(AppUpdateData) -> Unit>()

        every { connectionManagerMock.isConnected } returns true
        coEvery { networkManagerMock.loadTranslation(languageIndex1.url) } returns translations1
        coEvery { networkManagerMock.loadTranslation(languageIndex2.url) } returns translations2

        NStack.onAppUpdateListener = { updated = true }
        NStack.appOpen { }

        verify { networkManagerMock.postAppOpen(appOpenSettings, any(), capture(successCallbackSlot), any()) }

        successCallbackSlot.captured(appUpdateDate)
        verify { appOpenSettingsManagerMock.setUpdateDate() }

        verifyLanguageIndicesAreHandled()

        assert(updated)
    }

    @Test
    fun `Test ClassTranslationManager parse ran correct translation`() {
        val translationCacheMock = mapOf(Locale.getDefault() to translations3)
        val successResult = AppOpenResult.Success(appUpdateResponse)

        every { connectionManagerMock.isConnected } returns true
        every { assetCacheManagerMock.loadTranslations() } returns translationCacheMock
        coEvery { networkManagerMock.postAppOpen(any(), any()) } returns successResult

        runBlocking { NStack.appOpen() }

        verify { classTranslationManagerMock.parseTranslations(translations3) }
        verify(exactly = 0) { classTranslationManagerMock.parseTranslations(translations1) }
        verify(exactly = 0) { classTranslationManagerMock.parseTranslations(translations2) }
    }

    private fun verifyLanguageIndicesAreHandled() {
        val translations1 = "translations1"
        val translations2 = "translations2"
        val loadTranslations1Slot = slot<(String) -> Unit>()
        val loadTranslations2Slot = slot<(String) -> Unit>()

        verify { networkManagerMock.loadTranslation(languageIndex1.url, capture(loadTranslations1Slot), any()) }
        verify { networkManagerMock.loadTranslation(languageIndex2.url, capture(loadTranslations2Slot), any()) }
        verify(exactly = 0) { networkManagerMock.loadTranslation(languageIndex3.url, any(), any()) }

        loadTranslations1Slot.captured(translations1)
        verify { prefManagerMock.setTranslations(any(), translations1) }

        loadTranslations2Slot.captured(translations2)
        verify { prefManagerMock.setTranslations(any(), translations2) }

        val uiActionSlot = slot<() -> Unit>()
        verify { contextWrapperMock.runUiAction(capture(uiActionSlot)) }
        uiActionSlot.captured()

        assert(NStack.defaultLanguage == language2.locale)

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
        every { connectionManagerMock.isConnected } returns false

        val result = runBlocking { NStack.appOpen() }

        assert(result is AppOpenResult.NoInternet)
    }

    @Test
    fun `Test coroutine app open with internet connection`() {
        val translations1 = "translations1"
        val translations2 = "translations2"

        val successfulResult = AppOpenResult.Success(appUpdateResponse)

        every { connectionManagerMock.isConnected } returns true
        coEvery { networkManagerMock.postAppOpen(appOpenSettings, any()) } returns successfulResult
        coEvery { networkManagerMock.loadTranslation(languageIndex1.url) } returns translations1
        coEvery { networkManagerMock.loadTranslation(languageIndex2.url) } returns translations2

        val result = runBlocking { NStack.appOpen() }
        assert(result is AppOpenResult.Success)
        verify { prefManagerMock.setTranslations(language1.locale, translations1) }
        verify { prefManagerMock.setTranslations(language2.locale, translations2) }
        verify(exactly = 0) { prefManagerMock.setTranslations(language3.locale, any()) }
        assert(NStack.defaultLanguage == language2.locale)
    }

    @Test
    fun `Test coroutine app open error`() {
        val errorResult = AppOpenResult.Failure

        every { connectionManagerMock.isConnected } returns true
        coEvery { networkManagerMock.postAppOpen(any(), any()) } returns errorResult

        val result = runBlocking(Dispatchers.Main) { NStack.appOpen() }

        assert(result is AppOpenResult.Failure)
    }

    @Test
    fun `Test message seen`() {
        val messageId = 101
        val message = Message(messageId, "", "")

        NStack.messageSeen(message)

        verify { networkManagerMock.postMessageSeen(guid, messageId) }
    }

    @Test
    fun `Test rate reminder action`() {
        val rated = true

        NStack.onRateReminderAction(rated)

        verify { networkManagerMock.postRateReminderSeen(appOpenSettings, rated) }
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

        private val language1 = Language(0, "", Locale.ENGLISH, "", isDefault = false, isBestFit = false)
        private val language2 = Language(0, "", Locale.GERMAN, "", isDefault = true, isBestFit = false)
        private val language3 = Language(0, "", Locale.FRENCH, "", isDefault = false, isBestFit = false)
        private val languageIndex1 = LocalizeIndex(0, "url1", Date(), shouldUpdate = true, language = language1)
        private val languageIndex2 = LocalizeIndex(0, "url2", Date(), shouldUpdate = true, language = language2)
        private val languageIndex3 = LocalizeIndex(0, "url3", Date(), shouldUpdate = false, language = language3)
        private val appUpdateDate = AppUpdateData(localize = listOf(languageIndex1, languageIndex2, languageIndex3))
        private val appUpdateResponse = AppUpdateResponse(appUpdateDate, AppUpdateMeta(language1.locale.toString()))

        private val locale1 = Locale("it-IT")
        private val translations1 = mockk<JSONObject>()

        private val locale2 = Locale("fr-FR")
        private val translations2 = mockk<JSONObject>()

        private val locale3 = Locale.getDefault()
        // private val translations3 = mockk<JSONObject>()
        private val translations3 = JSONObject("{\"default\":{\"hi\":\"Hi\",\"cancel\":\"Canceler\",\"no\":\"No\",\"yes\":\"Yes\",\"edit\":\"Edit\",\"next\":\"Next\",\"on\":\"On\",\"off\":\"Off\",\"ok\":\"Ok\"},\"errorBody\":{\"errorRandom\":\"Totally random errorBody\",\"errorTitle\":\"Error\",\"authenticationError\":\"Login expired, please login again.\",\"connectionError\":\"No or bad connection, please try again.\",\"unknownError\":\"Unknown errorBody, please try again.\"},\"test\":{\"title\":\"NStack Demo\",\"message\":\"Bacon ipsum dolor amet magna meatball jerky in, shank sunt do burgdoggen spare ribs. Lorem boudin eiusmod short ribs pastrami. Sausage bresaola do turkey, dolor qui tail ground round culpa boudin nulla minim sunt beef ribs ham. Cillum in pastrami adipisicing swine lorem, velit sunt meatloaf bresaola short loin fugiat tri-tip boudin.\",\"subTitle\":\"Subtitle demo\",\"on\":\"on\",\"off\":\"off\"}}")

        private val translations = mapOf(
                locale1 to translations1,
                locale2 to translations2,
                locale3 to translations3
        )

        private var languagesChanged = false
        private var currentLanguage: Locale? = null

        init {
            mockkConstructor(NStackModule::class)
            mockkConstructor(ManagersModule::class)

            every { anyConstructed<ManagersModule>().provideAssetCacheManager() } returns assetCacheManagerMock
            every { anyConstructed<ManagersModule>().provideAppOpenSettingsManager() } returns appOpenSettingsManagerMock
            every { anyConstructed<ManagersModule>().providePrefManager() } returns prefManagerMock
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
            mockkStatic(Log::class)
            every { Log.e(any(), any()) } returns 0
            every { Log.e(any(), any(), any()) } returns 0

            NStack.addLanguagesChangeListener { languagesChanged = true }
            NStack.addLanguageChangeListener { currentLanguage = it }
        }

        @BeforeClass
        @JvmStatic
        fun singleSetup() {
            NStack.init(contextMock, true)
        }
    }
}
