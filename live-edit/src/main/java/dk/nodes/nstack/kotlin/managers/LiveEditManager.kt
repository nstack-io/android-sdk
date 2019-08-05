package dk.nodes.nstack.kotlin.managers

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.hardware.SensorManager
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dk.nodes.nstack.kotlin.liveedit.R
import dk.nodes.nstack.kotlin.models.TranslationData
import dk.nodes.nstack.kotlin.models.local.KeyAndTranslation
import dk.nodes.nstack.kotlin.models.local.StyleableEnum
import dk.nodes.nstack.kotlin.util.OnLanguageChangedListener
import dk.nodes.nstack.kotlin.util.ShakeDetector
import dk.nodes.nstack.kotlin.util.UpdateViewTranslationListener
import dk.nodes.nstack.kotlin.util.extensions.setOnVeryLongClickListener
import dk.nodes.nstack.kotlin.view.KeyAndTranslationAdapter
import dk.nodes.nstack.kotlin.view.ProposalsAdapter
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.util.Locale

typealias LiveEditDialogListener = (View, Pair<TranslationData, TranslationData>) -> Unit
typealias LiveEditProposalsDialogListener = (View) -> Unit

class LiveEditManager(
    private val networkManager: NetworkManager,
    private val appOpenSettingsManager: AppOpenSettingsManager,
    private val viewMap: Map<WeakReference<View>, TranslationData>
) : OnLanguageChangedListener {
    private var locale: Locale = Locale.getDefault()
    private var language = JSONObject()
    private val handler: Handler = Handler()


    val updateViewTranslationListener: UpdateViewTranslationListener = { view, translationData ->
        if (liveEditEnabled) {
            // Storing background drawable to view's tag
            view.setTag(NStackViewBackgroundTag, view.background)
            val data = view.getTag(NStackViewTag) as? TranslationData
            if (data.isValid()) {
                view.background = ColorDrawable(Color.parseColor("#E2FF0266"))
                view.setOnVeryLongClickListener {
                    showChooseOptionDialog(view, translationData)
                }
            }
        }
    }

    /**
     * Enable/Disable live editing
     */
    var liveEditEnabled: Boolean = false
        set(value) {
            field = value
            if (value) {
                enableLiveEdit()
            } else {
                disableLiveEdit()
            }
        }

    private fun enableLiveEdit() {
    }

    /**
     * Removes background and long click listener
     */
    fun disableLiveEdit() {
        val it: MutableIterator<Map.Entry<WeakReference<View>, TranslationData>> =
            viewMap.toMutableMap().iterator()

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

    fun init(context: Context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val shakeDetector = ShakeDetector(object : ShakeDetector.Listener {
            override fun hearShake() {
                liveEditEnabled = !liveEditEnabled
            }
        })
        shakeDetector.start(sensorManager)
    }

    private fun showLiveEditDialog(
        view: View,
        keyAndTranslation: KeyAndTranslation
    ) {
        val bottomSheetDialog = BottomSheetDialog(view.context, R.style.NstackBottomSheetTheme)
        bottomSheetDialog.setContentView(R.layout.bottomsheet_translation_change)
        bottomSheetDialog.setOnShowListener {
            val bottomSheetInternal =
                bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheetInternal).state = BottomSheetBehavior.STATE_EXPANDED
        }
        val editText = bottomSheetDialog.findViewById<EditText>(R.id.zzz_nstack_translation_et)
        val btn = bottomSheetDialog.findViewById<Button>(R.id.zzz_nstack_translation_change_btn)

        editText!!.setText(keyAndTranslation.translation)
        btn!!.setOnClickListener {
            val pair = getSectionAndKeyPair(keyAndTranslation.key)
            val editedTranslation = editText.text.toString()
            networkManager.postProposal(
                appOpenSettingsManager.getAppOpenSettings(),
                locale.toString().replace("_", "-"),
                pair?.second ?: "",
                pair?.first ?: "",
                editedTranslation,
                onSuccess = {
                    runUiAction {
                        when (view) {
                            is ToggleButton -> {
                                when (keyAndTranslation.styleable) {
                                    StyleableEnum.Key, StyleableEnum.Text -> view.text =
                                        editedTranslation
                                    StyleableEnum.Hint -> view.hint = editedTranslation
                                    StyleableEnum.Description, StyleableEnum.ContentDescription -> view.contentDescription =
                                        editedTranslation
                                    StyleableEnum.TextOn -> view.textOn = editedTranslation
                                    StyleableEnum.TextOff -> view.textOff = editedTranslation
                                    else -> {

                                    }
                                }
                            }
                            is TextView -> {
                                when (keyAndTranslation.styleable) {
                                    StyleableEnum.Key, StyleableEnum.Text -> view.text =
                                        editedTranslation
                                    StyleableEnum.Hint -> view.hint = editedTranslation
                                    StyleableEnum.Description, StyleableEnum.ContentDescription -> view.contentDescription =
                                        editedTranslation
                                    else -> {
                                    }
                                }
                            }
                            is androidx.appcompat.widget.Toolbar -> {
                                when (keyAndTranslation.styleable) {
                                    StyleableEnum.Title -> view.title = editedTranslation
                                    StyleableEnum.Subtitle -> view.subtitle = editedTranslation
                                    else -> {
                                    }
                                }
                            }
                        }
                    }
                },
                onError = {
                    runUiAction {
                        Toast.makeText(view.context, "Unknown Error", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun showChooseSectionKeyDialog(
        view: View,
        keyAndTranslationList: List<KeyAndTranslation>
    ) {
        val bottomSheetDialog = BottomSheetDialog(view.context, R.style.NstackBottomSheetTheme)
        bottomSheetDialog.setContentView(R.layout.bottomsheet_choose_key_section)
        val recyclerView = bottomSheetDialog.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView?.adapter =
            KeyAndTranslationAdapter(keyAndTranslationList) {
                showLiveEditDialog(view, it)
                bottomSheetDialog.dismiss()
            }
        val bottomSheetInternal =
            bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        BottomSheetBehavior.from(bottomSheetInternal).apply {
            peekHeight = 400
            isFitToContents = true
        }
        recyclerView!!.layoutParams = recyclerView.layoutParams.apply {
            height = getWindowHeight() * 2 / 3
        }
        bottomSheetDialog.show()
    }

    fun showProposalsDialog(
        view: View,
        translationPair: Pair<TranslationData, TranslationData>? = null,
        showDialogOnLoad: Boolean = false
    ) {
        networkManager.fetchProposals(
            { proposals ->
                runUiAction {
                    if (proposals.isNotEmpty()) {
                        val bottomSheetDialog =
                            BottomSheetDialog(view.context, R.style.NstackBottomSheetTheme)
                        bottomSheetDialog.setContentView(R.layout.bottomsheet_translation_proposals)
                        val bottomSheetInternal =
                            bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
                        val recyclerView =
                            bottomSheetDialog.findViewById<RecyclerView>(R.id.recyclerView)
                        BottomSheetBehavior.from(bottomSheetInternal).apply {
                            peekHeight = 400
                            isFitToContents = true
                        }
                        recyclerView!!.layoutParams = recyclerView.layoutParams.apply {
                            height = getWindowHeight() * 2 / 3
                        }

                        recyclerView.adapter = ProposalsAdapter().apply {
                            val sectionAndKeyPairList =
                                translationPair?.toKeyAndTranslationList()
                                    ?.map { getSectionAndKeyPair(it.key) }
                            if (sectionAndKeyPairList.isNullOrEmpty()) {
                                update(proposals)
                            } else {
                                update(proposals.filter { sectionAndKeyPairList.contains(it.section to it.key) })
                            }
                        }
                        if (showDialogOnLoad) {
                            bottomSheetDialog.show()
                        } else {
                            Snackbar.make(view, "Open Proposals", Snackbar.LENGTH_LONG)
                                .setAction("OPEN") {
                                    bottomSheetDialog.show()
                                }
                                .show()
                        }
                    } else {
                        Toast.makeText(
                            view.context,
                            "There isn't any proposals",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            },
            {
                Toast.makeText(view.context, "Unknown Error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onLanguageChanged(locale: Locale) {
        this.locale = locale
    }

    private fun getWindowHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    private fun getSectionAndKeyPair(key: String?): Pair<String, String>? {
        val cleanedKey = cleanKeyName(key) ?: return null
        val divider = cleanedKey.indexOfFirst { it == '_' }
        return cleanedKey.substring(0, divider) to cleanedKey.substring(
            divider + 1,
            cleanedKey.length
        )
    }

    private fun cleanKeyName(keyName: String?): String? {
        val key = keyName ?: return null
        return if (key.startsWith("{") && key.endsWith("}")) {
            key.substring(1, key.length - 1)
        } else key
    }

    fun showChooseOptionDialog(
        view: View,
        translationPair: Pair<TranslationData, TranslationData>
    ) {
        val builder = AlertDialog.Builder(view.context)
            .setTitle("NStack proposal")
            .setPositiveButton("View translation proposals") { _, _ ->
                showProposalsDialog(view, translationPair, true)
            }
            .setNegativeButton("Propose new translation") { _, _ ->
                val list = translationPair.toKeyAndTranslationList()
                if (list.size == 1) {
                    showLiveEditDialog(view, list.first())
                } else {
                    showChooseSectionKeyDialog(view, list)
                }
            }
        builder.create().show()
    }

    private fun Pair<TranslationData, TranslationData>.toKeyAndTranslationList(): List<KeyAndTranslation> {
        val mutableList = mutableListOf<KeyAndTranslation>()
        if (first.key != null && second.key != null) {
            mutableList += KeyAndTranslation(
                first.key!!,
                second.key!!,
                StyleableEnum.Key
            )
        }
        if (first.text != null && second.text != null) {
            mutableList += KeyAndTranslation(
                first.text!!,
                second.text!!,
                StyleableEnum.Text
            )
        }
        if (first.hint != null && second.hint != null) {
            mutableList += KeyAndTranslation(
                first.hint!!,
                second.hint!!,
                StyleableEnum.Hint
            )
        }
        if (first.description != null && second.description != null) {
            mutableList += KeyAndTranslation(
                first.description!!,
                second.description!!,
                StyleableEnum.Description
            )
        }
        if (first.textOn != null && second.textOn != null) {
            mutableList += KeyAndTranslation(
                first.textOn!!,
                second.textOn!!,
                StyleableEnum.TextOn
            )
        }
        if (first.textOff != null && second.textOff != null) {
            mutableList += KeyAndTranslation(
                first.textOff!!,
                second.textOff!!,
                StyleableEnum.TextOff
            )
        }
        if (first.contentDescription != null && second.contentDescription != null) {
            mutableList += KeyAndTranslation(
                first.contentDescription!!,
                second.contentDescription!!,
                StyleableEnum.ContentDescription
            )
        }
        if (first.title != null && second.title != null) {
            mutableList += KeyAndTranslation(
                first.title!!,
                second.title!!,
                StyleableEnum.Title
            )
        }
        if (first.subtitle != null && second.subtitle != null) {
            mutableList += KeyAndTranslation(
                first.subtitle!!,
                second.subtitle!!,
                StyleableEnum.Subtitle
            )
        }
        return mutableList
    }

    /**
     * Run Ui Action
     */

    private fun runUiAction(action: () -> Unit) {
        handler.post {
            action()
        }
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
        return language.optString(cleanKeyName(key), null)
    }

    /**
     * In order to match the format that we use in our XML file we need to flatten the structure and prepend the key to the nstack key
     */

    fun parseTranslations(jsonParent: JSONObject) {
        // Clear our locale map
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

    companion object {
        private val NStackViewBackgroundTag = R.id.nstack_background_tag
        private val NStackViewTag = dk.nodes.nstack.kotlin.core.R.id.nstack_tag
    }
}
