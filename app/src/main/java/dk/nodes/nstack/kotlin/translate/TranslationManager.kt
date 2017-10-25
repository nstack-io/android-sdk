package dk.nodes.nstack.translate

import dk.nodes.nstack.kotlin.nLog
import dk.nodes.nstack.kotlin.translate.Translate
import org.json.JSONObject

/**
 * Created by joso on 25/02/15.
 * Edited by Mario on 30/12/16
 */
class TranslationManager() {
    fun setTranslationClass(translationClass: Class<*>) {
        classType = translationClass
    }

    fun translate(view: Any) {
        val fields = view.javaClass.declaredFields
        for (f in fields) {
            val annotation = f.getAnnotation(Translate::class.java)
            if (annotation != null) {

                try {
                    val translation = findValue(annotation.value)
                    f.isAccessible = true
                    val viewClass = f.type.simpleName
                    if (viewClass.contentEquals("Toolbar")) {
                        f.type.getMethod("setTitle", CharSequence::class.java).invoke(f.get(view), translation)
                    } else if (viewClass.contentEquals("EditText") ||
                            viewClass.contentEquals("AppCompatEditText")
                            || viewClass.contentEquals("TextInputEditText")
                            || viewClass.contentEquals("TextInputLayout")) {
                        f.type.getMethod("setHint", CharSequence::class.java).invoke(f.get(view), translation)
                    } else {
                        f.type.getMethod("setText", CharSequence::class.java).invoke(f.get(view), translation)
                    }
                    f.type.getMethod("setContentDescription", CharSequence::class.java).invoke(f.get(view), translation)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun findValue(key: String): String {

        // Sections
        try {
            val innerClassName = key.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            val sectionKey = key.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            val sectionClass = Class.forName(classType!!.name + "$" + innerClassName)
            val field = sectionClass.getField(sectionKey)
            return field.get(null).toString()
        } catch (e: Exception) {
            nLog("", "sections findValue failed on key: " + key + ". Exception -> " + e.toString())
            throw IllegalArgumentException()
        }

    }

    fun parseTranslations(jsonObject: JSONObject) {
        val iterator = jsonObject.keys()
        while (iterator.hasNext()) {
            var sectionKey = iterator.next()
            try {
                val sectionObject = jsonObject.getJSONObject(sectionKey)
                val translationKeys = sectionObject.keys()
                if (sectionKey.equals("default", ignoreCase = true)) {
                    sectionKey = "defaultSection"
                }
                val sectionClass = Class.forName(classType!!.name + "$" + sectionKey)
                while (translationKeys.hasNext()) {
                    val translationKey = translationKeys.next()
                    // Reached actual translation string
                    if (sectionObject.get(translationKey) is String) {
                        updateField(sectionClass, translationKey, sectionObject.getString(translationKey))
                    }
                }
            } catch (e: Exception) {
                nLog("", "Parsing failed for section -> " + sectionKey + " | " + e.toString())
            }

        }
    }

    private fun updateField(classType: Class<*>, key: String, value: String) {
        try {
            val field = classType.getField(key)
            field.isAccessible = true
            field.set(null, value)
        } catch (e: Exception) {
            nLog("", e.message ?: "")
            nLog("TranslationManager", "Error updating field: $key : $value")
        }

    }

    companion object {

        private var classType: Class<*>? = null
    }
}