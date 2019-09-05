package dk.nodes.nstack.kotlin.managers

import android.content.Context
import android.content.res.Resources
import android.hardware.SensorManager
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dk.nodes.nstack.R
import dk.nodes.nstack.kotlin.models.NStackException
import dk.nodes.nstack.kotlin.models.TranslationData
import dk.nodes.nstack.kotlin.models.local.KeyAndTranslation
import dk.nodes.nstack.kotlin.models.local.StyleableEnum
import dk.nodes.nstack.kotlin.provider.TranslationHolder
import dk.nodes.nstack.kotlin.util.ShakeDetector
import dk.nodes.nstack.kotlin.util.extensions.detachLiveEditListener
import dk.nodes.nstack.kotlin.util.extensions.attachLiveEditListener
import dk.nodes.nstack.kotlin.view.KeyAndTranslationAdapter
import dk.nodes.nstack.kotlin.view.ProposalsAdapter
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue

internal class LiveEditManager(
    context: Context,
    private val language: String,
    private val translationHolder: TranslationHolder,
    private val viewTranslationManager: ViewTranslationManager,
    private val networkManager: NetworkManager,
    private val appOpenSettingsManager: AppOpenSettingsManager
) {

    private val handler: Handler = Handler()
    private val viewQueue: ConcurrentLinkedQueue<WeakReference<View>> = ConcurrentLinkedQueue()

    init {
        viewTranslationManager.addOnUpdateViewTranslationListener { view, translationData ->
            if (liveEditEnabled) {
                val data = view.getTag(NStackViewTag) as? TranslationData
                if (data.isValid()) {
                    viewQueue += WeakReference(view)
                    view.attachLiveEditListener {
                        showChooseOptionDialog(view, translationData)
                    }
                }
            }
        }
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val shakeDetector = ShakeDetector(object :
            ShakeDetector.Listener {
            override fun hearShake() {
                liveEditEnabled = !liveEditEnabled
            }
        })
        shakeDetector.start(sensorManager)
    }

    /**
     * Enable/Disable live editing
     */
    var liveEditEnabled: Boolean = false
        set(value) {
            field = value
            if (value) {
                viewTranslationManager.translate()
            } else {
                disableLiveEdit()
            }
        }

    /**
     * Removes background and long click listener
     */
    private fun disableLiveEdit() {
        var closestView: View? = null
        while (viewQueue.isNotEmpty()) {
            val view = viewQueue.poll()?.get()
            if (view != null) {
                view.detachLiveEditListener()
                closestView = view
            }
        }
        closestView?.let { showProposalsDialog(it) }
        viewQueue.clear()
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
                language,
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
                onError = { exception ->

                    when (exception) {
                        is NStackException -> {
                            runUiAction {
                                Toast.makeText(
                                    view.context,
                                    exception.errorBody.localizedMessage ?: exception.errorBody.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        else -> {
                            runUiAction {
                                Toast.makeText(view.context, "Unknown Error", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
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

    private fun showProposalsDialog(
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

    private fun showChooseOptionDialog(
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
                translationHolder.hasKey(key) -> true
                translationHolder.hasKey(text) -> true
                translationHolder.hasKey(hint) -> true
                translationHolder.hasKey(description) -> true
                translationHolder.hasKey(textOn) -> true
                translationHolder.hasKey(textOff) -> true
                translationHolder.hasKey(contentDescription) -> true
                translationHolder.hasKey(title) -> true
                translationHolder.hasKey(subtitle) -> true
                else -> false
            }
        }
    }

    companion object {
        private val NStackViewTag = R.id.nstack_tag
    }
}
