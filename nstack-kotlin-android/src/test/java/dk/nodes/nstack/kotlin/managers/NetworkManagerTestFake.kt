package dk.nodes.nstack.kotlin.managers

import dk.nodes.nstack.kotlin.managers.Helper.setResponse
import dk.nodes.nstack.kotlin.model.AppOpenSettingsMockFactory
import dk.nodes.nstack.kotlin.models.AppOpen
import dk.nodes.nstack.kotlin.models.Error
import dk.nodes.nstack.kotlin.models.Result
import dk.nodes.nstack.kotlin.provider.GsonProvider
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before


class NetworkManagerTestFake : TestCase() {

    private val mockWebServer = MockWebServer()
    private val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor())
            .build()
    private val manager = NetworkManager(client, mockWebServer.url("").toString(), true)
    private val gson = GsonProvider.provideGson()

    @Before
    fun setup() {
        mockWebServer.start(8080)
    }

    @After
    fun shutdown() {
        mockWebServer.shutdown()
    }

    fun testLoadTranslation() {
        mockWebServer.setResponse("response.json", 200)
        val response = runBlocking { manager.loadTranslation(mockWebServer.url("").toString()) }
        assert(response == Helper.getFileAsString("response_check.json"))
    }

    fun testLoadTranslationBadCode() {
        mockWebServer.setResponse("response.json", 500)
        val response = runBlocking { manager.loadTranslation(mockWebServer.url("").toString()) }
        assert(response == null)
    }

    fun testLoadTranslationBadResponse() {
        mockWebServer.setResponse("response_empty.json", 200)
        val response = runBlocking { manager.loadTranslation(mockWebServer.url("").toString()) }
        assert(response == "null")
    }

    fun testPostAppOpenSuccess() {
        mockWebServer.setResponse("response2.json", 200)
        val response = runBlocking {
            manager.postAppOpen(AppOpenSettingsMockFactory.getAppSettings(),
                    "da-DK")
        }
        val successResult = Result.Success(gson.fromJson(Helper.getFileAsString("response2_check.json"), AppOpen::class.java))
        assert(response == successResult)
    }

    fun testPostAppOpenNetworkError() {
        val response = runBlocking {
            manager.postAppOpen(AppOpenSettingsMockFactory.getAppSettings(),
                    "da-DK")
        }
        val networkError = Result.Error(Error.NetworkError)
        assert(response == networkError)
    }

    fun testPostAppOpenUnknownError() {
        mockWebServer.setResponse("response.json", 200)
        val response = runBlocking {
            manager.postAppOpen(AppOpenSettingsMockFactory.getAppSettings(),
                    "da-DK")
        }
        val unknowError = Result.Error(Error.UnknownError)
        assert(response == unknowError)
    }


    fun testTestPostAppOpen() {}

    fun testPostMessageSeen() {}

    fun testTestPostMessageSeen() {}

    fun testPostRateReminderSeen() {}

    fun testGetResponse() {}

    fun testGetCollection() {}

    fun testGetCollectionItem() {}

    fun testPostProposal() {}

    fun testFetchProposals() {}

    fun testGetLatestTerms() {}

    fun testSetTermsViewed() {}

    fun testGetAsJsonObject() {}

    fun testGetRateReminder2() {}

    fun testPostRateReminderAction() {}

    fun testTestPostRateReminderAction() {}

    fun testPostFeedback() {}
}