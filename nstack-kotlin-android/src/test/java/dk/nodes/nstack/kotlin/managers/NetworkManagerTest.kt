package dk.nodes.nstack.kotlin.managers

import dk.nodes.nstack.kotlin.models.AppOpenSettings
import dk.nodes.nstack.kotlin.models.FeedbackType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.security.cert.CertPathValidatorException
import java.security.cert.CertificateExpiredException
import java.util.*

class NetworkManagerTest {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    private val guid = "guid"
    private val version = "version"
    private val oldVersion = "oldVersion"
    private val device = "device"
    private val osVersion = "osVersion"
    private val lastUpdated = Date()
    private val appOpenSettings = AppOpenSettings(
            guid = guid,
            version = version,
            oldVersion = oldVersion,
            lastUpdated = lastUpdated,
            device = device,
            osVersion = osVersion
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Test
    fun `Handle certificate expired`() {
        val client = mockk<OkHttpClient>()
        val manager = NetworkManager(client, "https://127.0.0.1", true)
        every { client.newCall(any()).execute() } throws CertificateExpiredException()
        every { client.newCall(any()).enqueue(any()) } throws CertificateExpiredException()

        runManagerMethods(manager)
    }

    @Test
    fun `Handle invalid certificate`() {
        val client = mockk<OkHttpClient>()
        val manager = NetworkManager(client, "https://127.0.0.1", true)
        every { client.newCall(any()).execute() } throws CertPathValidatorException()
        every { client.newCall(any()).enqueue(any()) } throws CertPathValidatorException()

        runManagerMethods(manager)
    }

    @Test
    fun `Handle IO Exception`() {
        val client = mockk<OkHttpClient>()
        val manager = NetworkManager(client, "https://127.0.0.1", true)
        every { client.newCall(any()).execute() } throws IOException()
        every { client.newCall(any()).enqueue(any()) } throws IOException()

        runManagerMethods(manager)
    }

    private fun runManagerMethods(manager: NetworkManager) {
        runBlockingTest { manager.loadTranslation("") }
        runBlockingTest { manager.loadTranslation("", {}, {}) }

        runBlockingTest { manager.fetchProposals({}, {}) }

        runBlockingTest { manager.getCollection(1) }
        runBlockingTest { manager.getCollectionItem(1, 1) }
        runBlockingTest { manager.getLatestTerms(1, "da-DK", appOpenSettings) }
        runBlockingTest { manager.getResponse("") }
        runBlockingTest { manager.getRateReminder2(appOpenSettings) }

        runBlockingTest { manager.postAppOpen(appOpenSettings, "da-DK") }
        runBlockingTest { manager.postAppOpen(appOpenSettings, "da-DK", {}, {}) }
        runBlockingTest { manager.postFeedback(appOpenSettings, "", "", "", FeedbackType.FEEDBACK) }
        runBlockingTest { manager.postMessageSeen(appOpenSettings, 1) }
        runBlockingTest { manager.postProposal(appOpenSettings, "", "", "", "", {}, {}) }
        runBlockingTest { manager.postRateReminderAction(appOpenSettings, "") }
        runBlockingTest { manager.postRateReminderAction(appOpenSettings, 1, "") }
        runBlockingTest { manager.postRateReminderSeen(appOpenSettings, true) }
    }

}