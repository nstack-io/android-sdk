package dk.nodes.nstack.kotlin

import android.app.Application
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import com.google.gson.reflect.TypeToken
import dk.nodes.nstack.kotlin.managers.*
import dk.nodes.nstack.kotlin.models.*
import dk.nodes.nstack.kotlin.models.Result
import dk.nodes.nstack.kotlin.providers.NStackKoinComponent
import dk.nodes.nstack.kotlin.usecases.HandleLocalizeListUseCase
import dk.nodes.nstack.kotlin.util.GsonProvider
import dk.nodes.nstack.kotlin.util.extensions.ContextWrapper
import dk.nodes.nstack.kotlin.util.extensions.asJsonObject
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
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
        every { anyConstructed<NStackKoinComponent>().stateHolder } returns NStackStateHolder()
    }

    @Test
    fun `Test init`() {
        assert(appId == NStack.appIdKey)
        assert(apiKey == NStack.appApiKey)
        assert(env == NStack.env)

        assert(NStack.languages.containsKey(locale1))

        assert(languagesChanged)

        verify { classTranslationManagerMock.parseTranslations(translations1) }
    }

    @Test
    fun `Test ClassTranslationManager parse ran correct translation`() {
        val translationCacheMock = mapOf(Locale.getDefault() to translations3)
        val successResult = Result.Success(appUpdateResponse)

        every { connectionManagerMock.isConnected } returns true
        every { assetCacheManagerMock.loadTranslations() } returns translationCacheMock
        coEvery { networkManagerMock.postAppOpen(any(), any()) } returns successResult

        runBlocking { NStack.appOpen() }

        verify { classTranslationManagerMock.parseTranslations(translations1) }
    }

    @Test
    fun `Test languages set`() = runBlockingTest {
        val handleLocalizeListUseCase = HandleLocalizeListUseCase(
                networkManagerMock, prefManagerMock, appOpenSettingsManagerMock
        )
        handleLocalizeListUseCase(indexList)
        assert(NStack.defaultLanguage.toLanguageTag() == "en-GB")
        assert(NStack.language.toLanguageTag() == "da-DK")
    }

    @Test
    fun `Test languages set no best fit`() = runBlockingTest {
        val handleLocalizeListUseCase = HandleLocalizeListUseCase(
                networkManagerMock, prefManagerMock, appOpenSettingsManagerMock
        )
        handleLocalizeListUseCase(indexList.map {
            it.copy(language = it.language.copy(isBestFit = false))
        })
        assert(NStack.defaultLanguage.toLanguageTag() == NStack.language.toLanguageTag())
    }

    @Test
    fun `Test languages set random best fit`() = runBlockingTest {
        val handleLocalizeListUseCase = HandleLocalizeListUseCase(
                networkManagerMock, prefManagerMock, appOpenSettingsManagerMock
        )
        val random = indexList.random()

        handleLocalizeListUseCase(indexList.map {
            if (it == random) {
                it.copy(language = it.language.copy(isBestFit = true))
            } else it.copy(language = it.language.copy(isBestFit = false))
        })
        assert(NStack.language.toLanguageTag() == random.language.locale!!.toLanguageTag())
    }

    @Test
    fun `Test languages shouldn't update`() = runBlockingTest {
        val handleLocalizeListUseCase = HandleLocalizeListUseCase(
                networkManagerMock, prefManagerMock, appOpenSettingsManagerMock
        )

        handleLocalizeListUseCase(indexList.map {
            it.copy(shouldUpdate = false)
        })
        assert(NStack.defaultLanguage.toLanguageTag() == "en-GB")
        assert(NStack.language.toLanguageTag() == "da-DK")
    }

    private fun verifyLanguageIndicesAreHandled() {
        val translations1 = "translations1"
        val translations2 = "translations2"
        val loadTranslations1Slot = slot<(String) -> Unit>()
        val loadTranslations2Slot = slot<(String) -> Unit>()

        verify {
            networkManagerMock.loadTranslation(
                    languageIndex1.url,
                    capture(loadTranslations1Slot),
                    any()
            )
        }
        verify {
            networkManagerMock.loadTranslation(
                    languageIndex2.url,
                    capture(loadTranslations2Slot),
                    any()
            )
        }
        verify(exactly = 0) { networkManagerMock.loadTranslation(languageIndex3.url, any(), any()) }

        loadTranslations1Slot.captured(translations1)
        verify { prefManagerMock.setTranslations(any(), translations1) }

        loadTranslations2Slot.captured(translations2)
        verify { prefManagerMock.setTranslations(any(), translations2) }

        val uiActionSlot = slot<() -> Unit>()
        verify { contextWrapperMock.runUiAction(capture(uiActionSlot)) }
        uiActionSlot.captured()

        assert(NStack.defaultLanguage == language2.locale)
    }

    @Test
    fun `Test coroutine app open without internet connection`() {
        every { connectionManagerMock.isConnected } returns false

        val result = runBlocking { NStack.appOpen() }

        assert(result == Result.Error(Error.NetworkError))
    }

    @Test
    fun `Test coroutine app open with internet connection`() {
        val translations1 = "translations1"
        val translations2 = "translations2"
        val translations3 = "translations3"

        val successfulResult = Result.Success(appUpdateResponse)

        every { connectionManagerMock.isConnected } returns true
        coEvery { networkManagerMock.postAppOpen(appOpenSettings, any()) } returns successfulResult
        coEvery { networkManagerMock.loadTranslation(languageIndex1.url) } returns translations1
        coEvery { networkManagerMock.loadTranslation(languageIndex2.url) } returns translations2
        coEvery { networkManagerMock.loadTranslation(languageIndex3.url) } returns translations3

        val result = runBlocking { NStack.appOpen() }
        assert(result is Result.Success)
        verify(exactly = 1) { prefManagerMock.setTranslations(language1.locale!!, translations1) }
        verify(exactly = 1) { prefManagerMock.setTranslations(language2.locale!!, translations2) }
        verify(exactly = 1) { prefManagerMock.setTranslations(language3.locale!!, translations3) }
        assert(NStack.language == locale3)
        assert(NStack.defaultLanguage == locale1)
    }

    @Test
    fun `Test coroutine app open error`() {
        val errorResult = Result.Error(Error.UnknownError)

        every { connectionManagerMock.isConnected } returns true
        coEvery { networkManagerMock.postAppOpen(any(), any()) } returns errorResult

        val result = runBlocking(Dispatchers.Main) { NStack.appOpen() }

        assert(result is Result.Error)
    }

    @Test
    fun `Test message seen`() {
        val message = Message(
                id = 101,
                showSetting = Message.ShowSetting.ONCE,
                message = "",
                url = null,
                localization = Message.Localization(
                        okBtn = "",
                        urlBtn = ""
                )
        )

        NStack.messageSeen(message)

        verify { networkManagerMock.postMessageSeen(guid, message.id) }
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
        private val classTranslationManagerMock =
                mockk<ClassTranslationManager>(relaxUnitFun = true)
        private val connectionManagerMock = mockk<ConnectionManager>()
        private val networkManagerMock = mockk<NetworkManager>(relaxUnitFun = true)
        private val contextWrapperMock = mockk<ContextWrapper>(relaxUnitFun = true)

        private val contextMock = mockk<Context>(relaxUnitFun = true)
        private val intentMock = mockk<Intent>()

        private const val appId = "appId"
        private const val apiKey = "apiKey"
        private const val env = "env"

        private const val guid = "guid"
        private const val version = "version"
        private const val oldVersion = "oldVersion"
        private const val device = "device"
        private const val osVersion = "osVersion"
        private val lastUpdated = Date()
        private val appOpenSettings = AppOpenSettings(
                guid = guid,
                version = version,
                oldVersion = oldVersion,
                lastUpdated = lastUpdated,
                device = device,
                osVersion = osVersion
        )
        private val indexList: List<LocalizeIndex> by lazy {
            val data = localizeResourceJson.asJsonObject!!.getJSONArray("data").toString()
            val type = object : TypeToken<ArrayList<LocalizeIndex>>() {}.type
            GsonProvider.getGson().fromJson<ArrayList<LocalizeIndex>>(data, type)
        }

        private val locale1 = indexList[0].language.locale!!
        private val locale2 = indexList[1].language.locale!!
        private val locale3 = indexList[2].language.locale!!

        private val language1 = indexList[0].language
        private val language2 = indexList[1].language
        private val language3 = indexList[2].language
        private val languageIndex1 = indexList[0]
        private val languageIndex2 = indexList[1]
        private val languageIndex3 = indexList[2]
        private val appUpdateDate = AppOpenData(localize = indexList)

        private val appUpdateResponse =
                AppOpen(appUpdateDate, AppOpenMeta(language1.locale.toString()))

        private val translations1 = JSONObject(localizeUrlEnUk).getJSONObject("data")
        private val translations2 = JSONObject(localizeUrlEsEs).getJSONObject("data")
        private val translations3 = JSONObject(localizeUrlDaDk).getJSONObject("data")

        private val translations = mapOf(
                locale1 to translations1,
                locale2 to translations2,
                locale3 to translations3
        )

        private var languagesChanged = false
        private var currentLanguage: Locale? = null

        init {
            mockkConstructor(NStackKoinComponent::class)
            // mockkConstructor(ManagersModule::class)
            //
            every { anyConstructed<NStackKoinComponent>().assetCacheManager } returns assetCacheManagerMock
            every { anyConstructed<NStackKoinComponent>().appOpenSettingsManager } returns appOpenSettingsManagerMock
            every { anyConstructed<NStackKoinComponent>().prefManager } returns prefManagerMock
            every { anyConstructed<NStackKoinComponent>().nstackMeta } returns nstackMeta
            every { anyConstructed<NStackKoinComponent>().appInfo } returns clientAppInfoMock
            every { anyConstructed<NStackKoinComponent>().viewTranslationManager } returns viewTranslationManagerMock
            every { anyConstructed<NStackKoinComponent>().classTranslationManager } returns classTranslationManagerMock
            every { anyConstructed<NStackKoinComponent>().connectionManager } returns connectionManagerMock
            every { anyConstructed<NStackKoinComponent>().networkManager } returns networkManagerMock
            every { anyConstructed<NStackKoinComponent>().contextWrapper } returns contextWrapperMock
            every { anyConstructed<NStackKoinComponent>().handleLocalizeListUseCase } returns HandleLocalizeListUseCase(
                    networkManagerMock, prefManagerMock, appOpenSettingsManagerMock
            )

            coEvery { networkManagerMock.loadTranslation("https://nstack-staging.vapor.cloud/api/v2/content/localize/resources/16") } returns translations1.toString()
            coEvery { networkManagerMock.loadTranslation("https://nstack-staging.vapor.cloud/api/v2/content/localize/resources/249") } returns translations2.toString()
            coEvery { networkManagerMock.loadTranslation("https://nstack-staging.vapor.cloud/api/v2/content/localize/resources/307") } returns translations3.toString()


            every { nstackMeta.appIdKey } returns appId
            every { nstackMeta.apiKey } returns apiKey
            every { nstackMeta.env } returns env

            every { contextMock.registerReceiver(any(), any()) } returns intentMock

            every { prefManagerMock.getTranslations() } returns translations
            every { assetCacheManagerMock.loadTranslations() } returns translations

            every { appOpenSettingsManagerMock.getAppOpenSettings() } returns appOpenSettings
            mockkStatic(Log::class)
            mockkStatic(PreferenceManager::class)
            every { PreferenceManager.getDefaultSharedPreferences(any()) } returns mockk(relaxed = true)
            every { contextMock.applicationContext } returns mockk<Application>(relaxed = true)
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
