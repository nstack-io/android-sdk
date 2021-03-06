package dk.nodes.nstack.kotlin.util

import dk.nodes.nstack.kotlin.util.extensions.languageCode
import dk.nodes.nstack.kotlin.util.extensions.locale
import java.util.Locale
import org.junit.Test

internal class ExtensionsTest {

    @Test
    fun `Test extracting language code from locale`() {
        val locales = listOf(
            Locale("en-GB"),
            Locale("fr-fr"),
            Locale("de_DE"),
            Locale("it_it")
        )

        val languageCodes = listOf("en", "fr", "de", "it")

        locales.forEachIndexed { index, locale ->
            assert(locale.languageCode == languageCodes[index])
        }
    }

    @Test
    fun `Test locale parsing`() {
        val strings = listOf(
            "en-GB", "fr-fr", "de_DE", "it_it"
        )
        val locales = listOf(
            Locale("en", "gb"),
            Locale("fr", "fr"),
            Locale("de", "de"),
            Locale("it", "it")
        )

        strings.forEachIndexed { index, s ->
            assert(s.locale == locales[index])
        }
    }
}
