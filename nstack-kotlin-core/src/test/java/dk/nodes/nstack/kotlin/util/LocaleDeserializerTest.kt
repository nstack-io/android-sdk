package dk.nodes.nstack.kotlin.util

import com.google.gson.JsonPrimitive
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import org.junit.Test

class LocaleDeserializerTest {

    private val undefined = "und"

    @Test
    fun `Test valid`() {
        validLocations
            .map(::JsonPrimitive)
            .forEach {
                val locale = LocaleDeserializer().deserialize(it, null, null)
                assertNotNull(locale); locale!!
                assert(locale.toLanguageTag() != undefined)
            }
    }

    @Test
    fun `Test null`() {
        assertNull(LocaleDeserializer().deserialize(null, null, null))
    }

    @Test
    fun `Test empty`() {
        val emptyString = ""
        val locale = LocaleDeserializer().deserialize(emptyString.let(::JsonPrimitive), null, null)
        assertNotNull(locale); locale!!
        assert(locale.toLanguageTag() == undefined)
    }

    @Test
    fun `Test invalid`() {
        // Add more
        val invalids = listOf("asdaszdasd").map(::JsonPrimitive)
            .forEach {
                val locale = LocaleDeserializer().deserialize(it, null, null)
                assertNotNull(locale); locale!!
                assert(locale.toLanguageTag() == undefined)
            }
    }
}
