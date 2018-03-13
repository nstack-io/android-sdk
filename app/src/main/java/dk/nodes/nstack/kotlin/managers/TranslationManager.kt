package dk.nodes.nstack.kotlin.managers

import com.google.gson.JsonObject
import dk.nodes.nstack.kotlin.util.NLog

class TranslationManager {
    fun parseTranslations(jsonObject: JsonObject) {
        val keys = jsonObject.keySet()

        keys.forEach {
            val sectionObject = jsonObject.getAsJsonObject(it)
            updateSection(it, sectionObject)
        }
    }

    private fun updateSection(sectionKey: String, sectionObject: JsonObject) {
        try {
            var sectionFixedKey = sectionKey

            if (sectionFixedKey.equals("default", ignoreCase = true)) {
                sectionFixedKey = "defaultSection"
            }

            val sectionClass = Class.forName(translationClass!!.name + "$" + sectionFixedKey)

            val sectionKeys = sectionObject.keySet()

            sectionKeys.forEach {
                val value = sectionObject.getAsJsonPrimitive(it)

                if (value.isString) {
                    updateField(sectionClass, it, value.asString)
                }
            }
        } catch (e: Exception) {
            NLog.d("", "Parsing failed for section -> " + sectionKey + " | " + e.toString())
        }

    }

    private fun updateField(classType: Class<*>, key: String, value: String) {
        try {
            val field = classType.getField(key)
            field.isAccessible = true
            field.set(null, value)
        } catch (e: Exception) {
            NLog.d("", e.message ?: "")
            NLog.d("TranslationManager", "Error updating field: $key : $value")
        }

    }

    companion object {
        var translationClass: Class<*>? = null
    }
}