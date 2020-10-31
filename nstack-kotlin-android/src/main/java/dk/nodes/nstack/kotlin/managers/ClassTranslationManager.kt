package dk.nodes.nstack.kotlin.managers

import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.util.extensions.cleanKeyName
import org.json.JSONObject

class ClassTranslationManager {

    fun parseTranslations(jsonObject: JSONObject) {
        val keys = jsonObject.keys()

        keys.forEach {
            val sectionObject = jsonObject.get(it)

            if (sectionObject is JSONObject) {
                updateSection(it, sectionObject)
            }
        }
    }

    private fun updateSection(sectionKey: String, sectionObject: JSONObject) {
        try {
            var sectionFixedKey = sectionKey

            if (sectionFixedKey.equals("default", ignoreCase = true)) {
                sectionFixedKey = "defaultSection"
            }

            val sectionClass = Class.forName(translationClass!!.name + "$" + sectionFixedKey)

            val sectionKeys = sectionObject.keys()

            sectionKeys.forEach {
                val value = sectionObject.getString(it)

                if (value != null) {
                    updateField(sectionClass, it, value)
                }
            }
        } catch (e: Exception) {
            NLog.d("", "Parsing failed for section -> $sectionKey | $e")
        }
    }

    private fun updateField(classType: Class<*>, key: String, value: String) {
        try {
            classType.getField(key).apply {
                isAccessible = true
                set(null, value)
            }
        } catch (e: Exception) {
            NLog.d("", e.message ?: "")
            NLog.d("ClassTranslationManager", "Error updating field: $key : $value")
        }
    }

    fun getFieldValue(xmlKey: String): String? {
        try {
            var sectionName = xmlKey.cleanKeyName.split("_").firstOrNull() ?: return null
            val keyName = xmlKey.cleanKeyName.split("_").lastOrNull() ?: return null

            if (sectionName.equals("default", ignoreCase = true)) {
                sectionName = "defaultSection"
            }

            val sectionClass = Class.forName(translationClass!!.name + "$" + sectionName)
            val actualValue = sectionClass.getField(keyName).get(null) as? String

            return actualValue
        } catch (e: Exception) {
            NLog.d("", e.message ?: "")
        }

        return null
    }

    companion object {
        var translationClass: Class<*>? = null
    }
}
