package dk.nodes.nstack.kotlin.managers

import com.google.gson.Gson
import dk.nodes.nstack.kotlin.models.Result
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.Before
import org.junit.Test

class NetworkManagerTest {

    private lateinit var networkManager: NetworkManager
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setup() {
        okHttpClient = mockk(relaxed = true)
        networkManager = NetworkManager(
            okHttpClient,
            "https://nstack.io",
            true,
            Gson()
        )
    }

    @Test
    fun testGetLocalizeResource() = runBlocking {
        val responseMock = mockk<Response>()
        every { okHttpClient.newCall(any()).execute() } returns responseMock
        every { responseMock.isSuccessful } returns true
        every { responseMock.body()!!.string() } returns localizeResourceJson
        val response = networkManager.getLocalizeResource("")
        assert(response is Result.Success)
    }
}