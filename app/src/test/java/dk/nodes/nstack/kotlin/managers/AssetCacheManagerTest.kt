package dk.nodes.nstack.kotlin.managers

import dk.nodes.nstack.kotlin.util.ContextWrapper
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class AssetCacheManagerTest {

    private val contextWrapperMock = mockk<ContextWrapper>()

    private val assetCacheManager = AssetCacheManager(contextWrapperMock)

    private val firstTranslationsFile = "translations_0_en-US.json"
    private val secondTranslationsFile = "translations/translations_1_en_GB.json"
    private val firstTranslations = "{\"section1\":{\"key1\":\"value1\",\"key2\":\"value2\"}, \"section2\":{\"key1\":\"value1\",\"key2\":\"value2\"}}"
    private val secondTranslations = "{\"section1\":{\"key1\":\"_value1\",\"key2\":\"_value2\"}, \"section2\":{\"key1\":\"_value1\",\"key2\":\"_value2\"}}"

    private val assets = listOf(
        secondTranslationsFile,
        firstTranslationsFile,
        "another_asset.txt"
    )

    init {
        every { contextWrapperMock.assets } returns assets
        every { contextWrapperMock.readAsset(firstTranslationsFile) } returns firstTranslations
        every { contextWrapperMock.readAsset(secondTranslationsFile) } returns secondTranslations
    }

    @Test
    fun `Test translation assets loading`() {
        val result = assetCacheManager.loadTranslations()
        val list = result.toList()
        val firstTranslations = list[0]
        val secondTranslations = list[1]

        assert(firstTranslations.first.toString() == "en-us")
        assert(secondTranslations.first.toString() == "en_gb")
    }
}
