package dk.nodes.nstack.kotlin

import android.content.Context
import dk.nodes.nstack.kotlin.managers.AppOpenSettingsManager
import dk.nodes.nstack.kotlin.managers.AssetCacheManager
import dk.nodes.nstack.kotlin.managers.PrefManager
import dk.nodes.nstack.kotlin.models.ClientAppInfo
import dk.nodes.nstack.kotlin.models.NStackMeta
import dk.nodes.nstack.kotlin.providers.NStackModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.junit.Test

class NStackTest {

    private val assetCacheManagerMock = mockk<AssetCacheManager>(relaxed = true)
    private val appOpenSettingsManagerMock = mockk<AppOpenSettingsManager>()
    private val prefManagerMock = mockk<PrefManager>(relaxed = true)
    private val nstackMeta = mockk<NStackMeta>()
    private val clientAppInfoMock = mockk<ClientAppInfo>()

    private val contextMock = mockk<Context>(relaxed = true)

    private val appId = "appId"
    private val apiKey = "apiKey"
    private val env = "env"

    init {
        mockkConstructor(NStackModule::class)

        every { anyConstructed<NStackModule>().provideAssetCacheManager() } returns assetCacheManagerMock
        every { anyConstructed<NStackModule>().provideAppOpenSettingsManager() } returns appOpenSettingsManagerMock
        every { anyConstructed<NStackModule>().providePrefManager() } returns prefManagerMock
        every { anyConstructed<NStackModule>().provideNStackMeta() } returns nstackMeta
        every { anyConstructed<NStackModule>().provideClientAppInfo() } returns clientAppInfoMock

        every { nstackMeta.appIdKey } returns appId
        every { nstackMeta.apiKey } returns apiKey
        every { nstackMeta.env } returns env
    }

    @Test
    fun `Test init sets meta fields`() {
        NStack.init(contextMock)

        assert(appId == NStack.appIdKey)
        assert(apiKey == NStack.appApiKey)
        assert(env == NStack.env)
    }
}
