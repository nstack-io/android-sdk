package dk.nodes.nstack.kotlin.managers

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.View
import android.widget.TextView
import android.widget.ToggleButton
import dk.nodes.nstack.R
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.TranslationData
import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.util.extensions.setOnVeryLongClickListener
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

class ViewTranslationManager {

    var liveEditDialogListener: LiveEditDialogListener? = null
    var liveEditProposalsDialogListener: LiveEditProposalsDialogListener? = null

    /**
     * Contains a weak reference to our view along with a string value of our NStack Key
     *
     * We shouldn't need a lock when using the ConcurrentHashMap
     */
    private var viewMap: ConcurrentHashMap<WeakReference<View>, TranslationData> =
        ConcurrentHashMap()

    /**
     * Contains a flat map of the current selected language (Format -> sectionName_stringKey)
     */
    private var language = JSONObject()

    fun translate() {
        updateViews()
    }

    fun enableLiveEdit() {
        updateViews()
    }

    private var handler: Handler = Handler()

    /**
     * Removes background and long click listener
     */
    fun disableLiveEdit() {
        val it: MutableIterator<Map.Entry<WeakReference<View>, TranslationData>> =
            viewMap.iterator()

        var closestView: View? = null
        while (it.hasNext()) {
            val entry = it.next()
            val view = entry.key.get()
            // If our view is null we should remove it from the map and return
            if (view == null) {
                it.remove()
            } else {
                view.background = view.getTag(NStackViewBackgroundTag)as? Drawable
                view.setOnTouchListener(null)
                closestView = view
            }
        }
        closestView?.let(::showProposalsDialog)
    }

    private fun showProposalsDialog(view: View) {
        liveEditProposalsDialogListener?.invoke(view)
    }

    /**
     * Iterates through each view in the language map and tries to apply translation for the matching key
     * and removes garbage collected views
     */
    private fun updateViews() {
        val it: MutableIterator<Map.Entry<WeakReference<View>, TranslationData>> =
            viewMap.iterator()

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
     * Should check if the view is of a stylable and try to add the translation
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

        translatedContentDescription?.let(view::setContentDescription)
        view.setTag(NStackViewTag, translationData)

        if (NStack.liveEditEnabled) {
            // Storing background drawable to view's tag
            view.setTag(NStackViewBackgroundTag, view.background)
            val data = view.getTag(NStackViewTag) as? TranslationData
            if (data.isValid()) {
                view.background = ColorDrawable(Color.parseColor("#E2FF0266"))
                view.setOnVeryLongClickListener {
                    liveEditDialogListener?.invoke(
                        view, translationData to TranslationData(
                            translatedKey,
                            translatedText,
                            translatedHint,
                            translatedDescription,
                            translatedTextOn,
                            translatedTextOff,
                            translatedContentDescription,
                            translatedTitle,
                            translatedSubtitle
                        )
                    )
                }
            }
        }

        when (view) {
            is androidx.appcompat.widget.Toolbar -> {
                translatedTitle?.let(view::setTitle)
                translatedSubtitle?.let(view::setSubtitle)
            }

            is ToggleButton -> {
                NLog.d(this, "Is ToggleButton")
                (translatedKey ?: translatedText)?.let(view::setText)
                translatedHint?.let(view::setHint)
                translatedDescription?.let(view::setContentDescription)

                translatedTextOn?.let(view::setTextOn)
                translatedTextOff?.let(view::setTextOff)
            }
            is TextView -> {
                (translatedKey ?: translatedText)?.let(view::setText)
                translatedHint?.let(view::setHint)
                translatedDescription?.let(view::setContentDescription)
            }
        }
    }

    /**
     * Returns the translation based on the key
     *
     * Can return a null so we need to cast it to a nullable string
     * (This is because of JSONObject)
     */
    fun getTranslationByKey(key: String?): String? {
        if (key == null) {
            return null
        }
        return language.optString(cleanKeyName(key), null)
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
            val newSectionKey = "${sectionName}_$sectionKey"
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

    /**
     * Check if the key exists
     */
    fun hasKey(nstackKey: String): Boolean {
        return language.has(cleanKeyName(nstackKey))
    }

    private fun cleanKeyName(keyName: String?): String? {
        val key = keyName ?: return null
        return if (key.startsWith("{") && key.endsWith("}")) {
            key.substring(1, key.length - 1)
        } else key
    }

    private fun runUiAction(action: () -> Unit) {
        handler.post {
            action()
        }
    }

    companion object {
        private val NStackViewTag = R.id.nstack_tag
        private val NStackViewBackgroundTag = R.id.nstack_background_tag
    }

    private fun TranslationData?.isValid(): Boolean {
        return if (this == null) false
        else {
            when {
                getTranslationByKey(key) != null -> true
                getTranslationByKey(text) != null -> true
                getTranslationByKey(hint) != null -> true
                getTranslationByKey(description) != null -> true
                getTranslationByKey(textOn) != null -> true
                getTranslationByKey(textOff) != null -> true
                getTranslationByKey(contentDescription) != null -> true
                getTranslationByKey(title) != null -> true
                getTranslationByKey(subtitle) != null -> true
                else -> false
            }
        }
    }
}
