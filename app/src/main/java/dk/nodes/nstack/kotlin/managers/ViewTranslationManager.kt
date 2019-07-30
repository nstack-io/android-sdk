package dk.nodes.nstack.kotlin.managers

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import dk.nodes.nstack.R
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.TranslationData
import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.util.extensions.setOnVeryLongClickListener
import dk.nodes.nstack.kotlin.view.ProposalsAdapter
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

class ViewTranslationManager(
    private val networkManager: NetworkManager,
    private val appOpenSettingsManager: AppOpenSettingsManager
) {
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
                view.background = view.tag as? Drawable
                view.setOnTouchListener(null)
                closestView = view
            }
        }
        closestView?.let(::showProposalsDialog)
    }

    private fun showProposalsDialog(view: View) {
        networkManager.fetchProposals({
            if (it.isNotEmpty()) {
                runUiAction {
                    val dialogBuilder =
                        AlertDialog.Builder(view.context, R.style.Theme_AppCompat_Light_Dialog)
                    val dialogView = LayoutInflater.from(view.context)
                        .inflate(R.layout.bottomsheet_translation_proposals, null)
                    dialogBuilder.setView(dialogView)
                    val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)
                    dialogBuilder.create().show()
                    recyclerView.adapter = ProposalsAdapter().apply { update(it) }
                }
            }
        }, {

        })
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

        if (NStack.liveEditEnabled) {
            // Storing background drawable to view's tag
            view.tag = view.background
            view.background = ColorDrawable(Color.RED)
            view.setOnVeryLongClickListener {
                showEditDialog(view, translationData, translatedKey, translatedText, translatedHint)
            }
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
                NLog.d(this, "Is ToggleButton")
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
                NLog.d(this, "Is CompoundButton")
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
        }
    }

    private fun showEditDialog(
        view: View,
        translationData: TranslationData,
        translatedKey: String?,
        translatedText: String?,
        translatedHint: String?
    ) {
        NLog.d(this, "key: $translatedKey - $translatedText")

        val dialogBuilder = AlertDialog.Builder(view.context, R.style.Theme_AppCompat_Light_Dialog)
        val dialogView =
            LayoutInflater.from(view.context).inflate(R.layout.bottomsheet_translation_change, null)
        val editText = dialogView.findViewById<EditText>(R.id.zzz_nstack_translation_et)
        val btn = dialogView.findViewById<Button>(R.id.zzz_nstack_translation_change_btn)

        editText.setText(translatedText ?: translatedHint ?: translatedKey ?: "")
        dialogBuilder.setView(dialogView)

        val dialog = dialogBuilder.create()
        btn.setOnClickListener {
            val pair = getSectionAndKeyPair(translationData.key)
            networkManager.postProposal(
                appOpenSettingsManager.getAppOpenSettings(),

                NStack.language.toString().replace("_", "-"),
                pair?.second ?: "",
                pair?.first ?: "",
                editText.text.toString(),
                onSuccess = {
                    runUiAction {
                        when (view) {
                            is EditText -> {
                                view.hint = editText.text.toString()
                            }
                            is TextView -> {
                                view.text = editText.text.toString()
                            }
                            is CompoundButton -> {
                                view.text = editText.text.toString()
                            }
                            is androidx.appcompat.widget.Toolbar -> {
                                view.title = editText.text.toString()
                            }
                        }
                    }
                },
                onError = {
                    runUiAction {
                        Toast.makeText(view.context, "Unknown Error", Toast.LENGTH_SHORT).show();
                    }
                }
            )
            dialog.dismiss()
        }
        dialog.show()
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

    private fun getSectionAndKeyPair(key: String?): Pair<String, String>? {
        val key = cleanKeyName(key) ?: return null
        val divider = key.indexOfFirst { it == '_' }
        return key.substring(0, divider) to key.substring(divider + 1, key.length)
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
}
