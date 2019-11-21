package dk.nodes.nstack.kotlin.managers

import android.content.DialogInterface
import android.content.res.Resources
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import dk.nodes.nstack.R
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.NStackException
import dk.nodes.nstack.kotlin.models.TranslationData
import dk.nodes.nstack.kotlin.models.local.KeyAndTranslation
import dk.nodes.nstack.kotlin.models.local.StyleableEnum
import dk.nodes.nstack.kotlin.provider.TranslationHolder
import dk.nodes.nstack.kotlin.util.extensions.attachLiveEditListener
import dk.nodes.nstack.kotlin.util.extensions.detachLiveEditListener
import dk.nodes.nstack.kotlin.util.extensions.dp
import dk.nodes.nstack.kotlin.util.extensions.hide
import dk.nodes.nstack.kotlin.util.extensions.setNavigationBarColor
import dk.nodes.nstack.kotlin.util.extensions.show
import dk.nodes.nstack.kotlin.view.KeyAndTranslationAdapter
import dk.nodes.nstack.kotlin.view.ProposalsAdapter
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue

internal class LiveEditManager(
    private val translationHolder: TranslationHolder,
    private val viewTranslationManager: ViewTranslationManager,
    private val networkManager: NetworkManager,
    private val appOpenSettingsManager: AppOpenSettingsManager
) {

    private val handler: Handler = Handler()
    private val viewQueue: ConcurrentLinkedQueue<WeakReference<View>> = ConcurrentLinkedQueue()
    private val openDialogs: MutableMap<String, WeakReference<BottomSheetDialog>> = mutableMapOf()

    init {
        viewTranslationManager.addOnUpdateViewTranslationListener { view, translationData ->
            if (liveEditEnabled) {
                val data = view.getTag(NStackViewTag) as? TranslationData
                if (data.isValid) {
                    viewQueue += WeakReference(view)
                    view.attachLiveEditListener {
                        showChooseOptionDialog(view, translationData)
                    }
                }
            }
        }
    }

    /**
     * Removes nulls and the calling dialog from the open dialogs map
     */
    private val onDialogCancelListener = { dialog: DialogInterface ->
        openDialogs
            .filterValues { it.get() === dialog || it.get() == null }
            .keys
            .forEach { key -> openDialogs.remove(key) }
    }

    /**
     * Enable/Disable live editing
     */
    private var liveEditEnabled: Boolean = false
        set(value) {
            field = value
            if (value) {
                viewTranslationManager.translate()
            } else {
                disableLiveEdit()
            }
        }

    /**
     * Turns live edit on
     */
    fun turnLiveEditOn() {
        liveEditEnabled = true
    }

    /**
     * Cancels all dialogs and disables live edit
     */
    fun reset() {
        liveEditEnabled = false

        openDialogs
            .values
            .mapNotNull { weakReference -> weakReference.get() }
            .forEach { dialog -> dialog.cancel() }
    }

    /**
     * Removes background and long click listener
     */
    private fun disableLiveEdit() {
        while (viewQueue.isNotEmpty()) {
            val view = viewQueue.poll()?.get()
            view?.detachLiveEditListener()
        }
        viewQueue.clear()
    }

    private fun showLiveEditDialog(
        view: View,
        keyAndTranslation: KeyAndTranslation
    ) {
        val bottomSheetDialog = BottomSheetDialog(view.context, R.style.NstackBottomSheetTheme)

        bottomSheetDialog.setNavigationBarColor()
        bottomSheetDialog.setContentView(R.layout.bottomsheet_translation_edit)
        bottomSheetDialog.setOnShowListener {
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        val contentView = bottomSheetDialog.findViewById<View>(R.id.contentView)
        val editText = bottomSheetDialog.findViewById<EditText>(R.id.zzz_nstack_translation_et)
        val btn = bottomSheetDialog.findViewById<Button>(R.id.zzz_nstack_translation_change_btn)
        val loadingView = bottomSheetDialog.findViewById<ProgressBar>(R.id.loadingView)

        editText?.setText(keyAndTranslation.translation)
        btn?.setOnClickListener {
            val pair = getSectionAndKeyPair(keyAndTranslation.key)
            val editedTranslation = editText?.text.toString()

            editText?.isEnabled = false
            btn.isEnabled = false
            contentView?.hide()
            loadingView?.show()
            networkManager.postProposal(
                appOpenSettingsManager.getAppOpenSettings(),
                NStack.language.toString().replace("_", "-"),
                pair?.second ?: "",
                pair?.first ?: "",
                editedTranslation,
                onSuccess = {
                    bottomSheetDialog.dismiss()
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
                            is TextInputLayout -> {
                                when (keyAndTranslation.styleable) {
                                    StyleableEnum.Hint -> view.hint = editedTranslation
                                    else -> {
                                    }
                                }
                            }
                        }
                    }
                },
                onError = { exception ->
                    runUiAction {
                        editText?.isEnabled = true
                        btn.isEnabled = true
                        loadingView?.hide()
                        contentView?.show()

                        when (exception) {
                            is NStackException -> {
                                Toast.makeText(
                                    view.context,
                                    exception.errorBody.localizedMessage
                                        ?: exception.errorBody.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                Toast.makeText(view.context, "Unknown Error", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            )
        }

        bottomSheetDialog.makeCancellableOnReset(dialogKey = "live_edit")
        bottomSheetDialog.show()
    }

    private fun showChooseSectionKeyDialog(
        view: View,
        keyAndTranslationList: List<KeyAndTranslation>
    ) {
        val bottomSheetDialog = BottomSheetDialog(view.context, R.style.NstackBottomSheetTheme)
        bottomSheetDialog.setNavigationBarColor()
        bottomSheetDialog.setContentView(R.layout.bottomsheet_choose_key_section)
        val recyclerView = bottomSheetDialog.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView?.adapter =
            KeyAndTranslationAdapter(keyAndTranslationList) {
                showLiveEditDialog(view, it)
                bottomSheetDialog.dismiss()
            }
        bottomSheetDialog.behavior.apply {
            peekHeight = 168.dp
            isFitToContents = true
        }
        recyclerView!!.layoutParams = recyclerView.layoutParams.apply {
            height = getWindowHeight() * 2 / 3
        }

        bottomSheetDialog.makeCancellableOnReset(dialogKey = "section")
        bottomSheetDialog.show()
    }

    private fun showProposalsDialog(
        view: View,
        translationPair: Pair<TranslationData, TranslationData>? = null
    ) {
        val bottomSheetDialog = BottomSheetDialog(view.context, R.style.NstackBottomSheetTheme)
        bottomSheetDialog.setNavigationBarColor()
        bottomSheetDialog.setContentView(R.layout.bottomsheet_translation_proposals)

        val recyclerView = bottomSheetDialog.findViewById<RecyclerView>(R.id.recyclerView)
        val loadingView = bottomSheetDialog.findViewById<ProgressBar>(R.id.loadingView)
        val errorTextView = bottomSheetDialog.findViewById<TextView>(R.id.errorTextView)

        bottomSheetDialog.behavior.apply {
            peekHeight = 300.dp
        }

        loadingView?.show()
        recyclerView?.hide()
        networkManager.fetchProposals(
            { proposals ->
                runUiAction {
                    if (proposals.isNotEmpty()) {
                        errorTextView?.visibility = View.GONE
                        recyclerView?.adapter = ProposalsAdapter().apply {
                            val sectionAndKeyPairList =
                                translationPair?.toKeyAndTranslationList()
                                    ?.map { getSectionAndKeyPair(it.key) }
                            if (sectionAndKeyPairList.isNullOrEmpty()) {
                                update(proposals)
                            } else {
                                update(proposals.filter { sectionAndKeyPairList.contains(it.section to it.key) })
                            }
                        }

                        loadingView?.hide()
                        recyclerView?.show()
                    } else {
                        errorTextView?.text = "No proposals found"
                        errorTextView?.visibility = View.VISIBLE
                    }
                }
            },
            {
                runUiAction {
                    errorTextView?.text = "Could not load proposals"
                    errorTextView?.visibility = View.VISIBLE
                    loadingView?.hide()
                }
            }
        )

        bottomSheetDialog.makeCancellableOnReset(dialogKey = "proposals")
        bottomSheetDialog.show()
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
        val bottomSheetDialog = BottomSheetDialog(view.context, R.style.NstackBottomSheetTheme)
        bottomSheetDialog.setNavigationBarColor()
        bottomSheetDialog.setContentView(R.layout.bottomsheet_translation_options)
        bottomSheetDialog.setOnShowListener {
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        val optionViewProposalTextView =
            bottomSheetDialog.findViewById<TextView>(R.id.optionViewProposalTextView)
        val optionEditProposalTextView =
            bottomSheetDialog.findViewById<TextView>(R.id.optionEditProposalTextView)

        optionViewProposalTextView?.setOnClickListener {
            bottomSheetDialog.dismiss()
            showProposalsDialog(view, translationPair)
        }

        optionEditProposalTextView?.setOnClickListener {
            bottomSheetDialog.dismiss()
            val list = translationPair.toKeyAndTranslationList()
            if (list.size == 1) {
                showLiveEditDialog(view, list.first())
            } else {
                showChooseSectionKeyDialog(view, list)
            }
        }

        bottomSheetDialog.makeCancellableOnReset(dialogKey = "option")
        bottomSheetDialog.show()
    }

    private fun Pair<TranslationData, TranslationData>.toKeyAndTranslationList(): List<KeyAndTranslation> {
        fun StyleableEnum.toKeyAndTranslation(getter: (TranslationData) -> String?): KeyAndTranslation? {
            val key = getter(first) ?: return null
            val translation = getter(second) ?: return null
            return KeyAndTranslation(key, translation, this)
        }
        return listOfNotNull(
            StyleableEnum.Key.toKeyAndTranslation { it.key },
            StyleableEnum.Text.toKeyAndTranslation { it.text },
            StyleableEnum.Hint.toKeyAndTranslation { it.hint },
            StyleableEnum.Description.toKeyAndTranslation { it.description },
            StyleableEnum.TextOn.toKeyAndTranslation { it.textOn },
            StyleableEnum.TextOff.toKeyAndTranslation { it.textOff },
            StyleableEnum.ContentDescription.toKeyAndTranslation { it.contentDescription },
            StyleableEnum.Title.toKeyAndTranslation { it.title },
            StyleableEnum.Subtitle.toKeyAndTranslation { it.subtitle }
        )
    }

    private fun runUiAction(action: () -> Unit) {
        handler.post {
            action()
        }
    }

    private val TranslationData?.isValid: Boolean
        get() {
            return this != null &&
                listOf(
                    key,
                    text,
                    hint,
                    description,
                    textOn,
                    textOff,
                    contentDescription,
                    title,
                    subtitle
                ).any { translationHolder.hasKey(it) }
        }

    companion object {
        private val NStackViewTag = R.id.nstack_tag
    }

    /**
     * Cancels this dialog if reset() is called. The method also adds an `OnCancelListener` to your
     * dialog, so please make sure you haven't set any on your own.
     */
    private fun BottomSheetDialog.makeCancellableOnReset(dialogKey: String) {
        this.setOnCancelListener(onDialogCancelListener)
        openDialogs[dialogKey] = WeakReference(this)
    }
}
