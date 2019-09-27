package dk.nodes.nstack.kotlin.managers

import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.ToggleButton
import dk.nodes.nstack.kotlin.models.TranslationData
import dk.nodes.nstack.kotlin.util.NLog
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

class ViewTranslationManager {

    /**
     * Contains a weak reference to our view along with a string value of our NStack Key
     *
     * We shouldn't need a lock when using the ConcurrentHashMap
     */
    private var viewMap: ConcurrentHashMap<WeakReference<View>, TranslationData> = ConcurrentHashMap()

    /**
     * Contains a flat map of the current selected language (Format -> sectionName_stringKey)
     */
    private var language = JSONObject()

    fun translate() {
        updateViews()
    }

    /**
     * Iterates through each view in the language map and tries to apply translation for the matching key
     * and removes garbage collected views
     */
    private fun updateViews() {
        val it: MutableIterator<Map.Entry<WeakReference<View>, TranslationData>> = viewMap.iterator()

        while (it.hasNext()) {
            val entry = it.next()
            val view = entry.key.get()
            // If our view is null we should remove it from the map and return
            if (view == null) {
                it.remove()
            } else
                updateView(view, entry.value)
        }
    }

    private fun updateView(view: View?, translationData: TranslationData) {
        updateViewTranslation(view, translationData)
    }

    /**
     * Apply the translation to the view
     *
     * Should check if the view is of a type and try to add the translation
     */
    private fun updateViewTranslation(view: View?, translationData: TranslationData) {
        if (view == null) {
            return
        }

        val translatedKey = getTranslationByKey(translationData.key)
        val translatedText = getTranslationByKey(translationData.text)
        val translatedHint = getTranslationByKey(translationData.hint)
        val translatedDescription = getTranslationByKey(translationData.description)
        val translatedTextOn = getTranslationByKey(translationData.textOn)
        val translatedTextOff = getTranslationByKey(translationData.textOff)
        val translatedContentDescription = getTranslationByKey(translationData.contentDescription)
        val translatedTitle = getTranslationByKey(translationData.title)
        val translatedSubtitle = getTranslationByKey(translationData.subtitle)
        // All views should have this
        translatedContentDescription?.let {
            view.contentDescription = it
        }

        when (view) {
            is androidx.appcompat.widget.Toolbar -> {
                translatedTitle?.let {
                    view.title = it
                }
                translatedSubtitle?.let {
                    view.subtitle = it
                }
            }

            is ToggleButton -> {
                (translatedKey ?: translatedText)?.let {
                    view.text = it
                }
                translatedHint?.let {
                    view.hint = it
                }
                translatedDescription?.let {
                    view.contentDescription = it
                }
                translatedTextOn?.let {
                    view.textOn = it
                }
                translatedTextOff?.let {
                    view.textOff = it
                }
            }
            is CompoundButton -> {
                (translatedKey ?: translatedText)?.let {
                    view.text = it
                }
                translatedHint?.let {
                    view.hint = it
                }
                translatedDescription?.let {
                    view.contentDescription = it
                }
            }
            is TextView -> {
                (translatedKey ?: translatedText)?.let {
                    view.text = it
                }
                translatedHint?.let {
                    view.hint = it
                }
                translatedDescription?.let {
                    view.contentDescription = it
                }
            }
            else -> {
                NLog.d(this, "$view was a recognized type, not translating...")
            }
        }
    }

    /**
     * Returns the translation based on the key
     *
     * Can return a null so we need to cast it to a nullable string
     * (This is because of JSONObject)
     */
    private fun getTranslationByKey(key: String?): String? {
        if (key == null) {
            return null
        }
        return language.optString(key, null)
    }

    /**
     * In order to match the format that we use in our XML file we need to flatten the structure and prepend the key to the nstack key
     */

    fun parseTranslations(jsonParent: JSONObject) {
        // Clear our language map
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
     * Adds our view to our viewMap while adding the translation string as well
     */
    fun addView(weakView: WeakReference<View>, translationData: TranslationData) {
        val view = weakView.get() ?: return

        viewMap[weakView] = translationData

        updateView(view, translationData)
    }

    /**
     * Clears the view map of any references
     */
    fun clear() {
        viewMap.clear()
    }
}
