package dk.nodes.nstack.kotlin.managers

import android.os.Handler
import android.view.View
import android.widget.TextView
import android.widget.ToggleButton
import dk.nodes.nstack.kotlin.models.TranslationData
import dk.nodes.nstack.kotlin.plugin.NStackViewPlugin
import dk.nodes.nstack.kotlin.provider.TranslationHolder
import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.util.UpdateViewTranslationListener
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

class ViewTranslationManager(private val translationHolder: TranslationHolder) :
    NStackViewPlugin {

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

    private val updateViewListeners = mutableListOf<UpdateViewTranslationListener>()

    override fun translate() {
        updateViews()
    }

    fun addOnUpdateViewTranslationListener(listener: UpdateViewTranslationListener) {
        updateViewListeners += listener
    }

    fun removeOnUpdateViewTranslationListener(listener: UpdateViewTranslationListener) {
        updateViewListeners -= listener
    }

    private var handler: Handler = Handler()

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

        val translatedKey = translationHolder.getTranslationByKey(translationData.key)
        val translatedText = translationHolder.getTranslationByKey(translationData.text)
        val translatedHint = translationHolder.getTranslationByKey(translationData.hint)
        val translatedDescription =
            translationHolder.getTranslationByKey(translationData.description)
        val translatedTextOn = translationHolder.getTranslationByKey(translationData.textOn)
        val translatedTextOff = translationHolder.getTranslationByKey(translationData.textOff)
        val translatedContentDescription =
            translationHolder.getTranslationByKey(translationData.contentDescription)
        val translatedTitle = translationHolder.getTranslationByKey(translationData.title)
        val translatedSubtitle = translationHolder.getTranslationByKey(translationData.subtitle)
        // All views should have this

        translatedContentDescription?.let(view::setContentDescription)
        view.setTag(NStackViewTag, translationData)

        updateViewListeners.forEach {
            it(
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
     * Adds our view to our viewMap while adding the translation string as well
     */
    override fun addView(weakView: WeakReference<View>, translationData: TranslationData) {
        val view = weakView.get() ?: return

        viewMap[weakView] = translationData

        updateView(view, translationData)
    }

    /**
     * Clears the view map of any references
     */
    override fun clear() {
        viewMap.clear()
    }

    /**
     * Check if the key exists
     */

    private fun runUiAction(action: () -> Unit) {
        handler.post {
            action()
        }
    }

    companion object {
        private val NStackViewTag = dk.nodes.nstack.kotlin.core.R.id.nstack_tag
    }
}
