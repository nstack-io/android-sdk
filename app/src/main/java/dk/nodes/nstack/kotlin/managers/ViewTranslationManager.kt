package dk.nodes.nstack.kotlin.managers

import android.view.View
import android.widget.Button
import android.widget.TextView
import org.json.JSONObject
import java.lang.ref.WeakReference

class ViewTranslationManager {
    /**
     * Contains a weak reference to our view along with a string value of our NStack Key
     */
    var viewMap: HashMap<WeakReference<View>, String> = hashMapOf()

    /**
     * Contains a flat map of the current selected language (Format -> sectionName_stringKey)
     */
    private var language = JSONObject()

    fun translate() {
        updateViews()
    }

    /**
     * Iterates through each view in the language map and tries to apply translation for the matching key
     */
    private fun updateViews() {
        viewMap.forEach {
            val view = it.key.get()

            // If our view is null we should remove it from the map and return
            if (view == null) {
                viewMap.remove(it.key)
                return@forEach
            }

            val translationString = getTranslationByKey(it.value)

            updateViewTranslation(view, translationString)
        }
    }

    /**
     * Apply the translation to the view
     *
     * Should check if the view is of a type and try to add the translation
     */
    private fun updateViewTranslation(view: View?, translation: String?) {
        // TODO add more types
        when (view) {
            is TextView -> view.text = translation
            is Button   -> view.text = translation
        }
    }

    /**
     * Returns the translation based on the key
     *
     * Can return a null so we need to cast it to a nullable string
     * (This is because of JSONObject)
     */
    private fun getTranslationByKey(key: String): String? {
        val value: String? = language.getString(key)
        // Don't inline me
        return value
    }

    /**
     * In order to match the format that we use in our XML file we need to flatten the structure and prepend the key to the nstack key
     */

    fun parseTranslations(jsonParent: JSONObject) {
        // Clear our langauge map
        language = JSONObject()

        // Pull our keys
        val keys = jsonParent.keys()

        // Iterate through each key and add the sub section
        keys.forEach { sectionName ->
            val subSection: JSONObject? = jsonParent.optJSONObject(sectionName)

            if (subSection != null) {
                parseSubsection(sectionName, subSection)
            }
        }

        println("New Language")

        updateViews()
    }

    /**
     * Goes through each sub section and adds the value under the new key
     */

    private fun parseSubsection(sectionName: String, jsonSection: JSONObject) {
        val sectionKeys = jsonSection.keys()

        sectionKeys.forEach { sectionKey ->
            val newSectionKey = "${sectionName}_${sectionKey}"
            val sectionValue = jsonSection.getString(sectionKey)
            language.put(newSectionKey, sectionValue)
        }

    }

    /**
     * Clears the view map of any references
     */
    fun clear() {
        viewMap.clear()
    }
}