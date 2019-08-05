package dk.nodes.nstack.kotlin.managers

import android.content.res.Resources
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
import dk.nodes.nstack.R
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.NStack.runUiAction
import dk.nodes.nstack.models.TranslationData
import dk.nodes.nstack.models.local.KeyAndTranslation
import dk.nodes.nstack.kotlin.view.KeyAndTranslationAdapter
import dk.nodes.nstack.kotlin.view.ProposalsAdapter

typealias LiveEditDialogListener = (View, Pair<TranslationData, TranslationData>) -> Unit
typealias LiveEditProposalsDialogListener = (View) -> Unit

class LiveEditManager(
    private val networkManager: NetworkManager,
    private val appOpenSettingsManager: AppOpenSettingsManagerImpl
) {
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
                NStack.language.toString().replace("_", "-"),
                pair?.second ?: "",
                pair?.first ?: "",
                editedTranslation,
                onSuccess = {
                    runUiAction {
                        when (view) {
                            is ToggleButton -> {
                                when (keyAndTranslation.styleable) {
                                    R.styleable.nstack_key, R.styleable.nstack_text -> view.text =
                                        editedTranslation
                                    R.styleable.nstack_hint -> view.hint = editedTranslation
                                    R.styleable.nstack_description -> view.contentDescription =
                                        editedTranslation
                                    R.styleable.nstack_textOn -> view.textOn = editedTranslation
                                    R.styleable.nstack_textOff -> view.textOff = editedTranslation
                                }
                            }
                            is TextView -> {
                                when (keyAndTranslation.styleable) {
                                    R.styleable.nstack_key, R.styleable.nstack_text -> view.text =
                                        editedTranslation
                                    R.styleable.nstack_hint -> view.hint = editedTranslation
                                    R.styleable.nstack_description -> view.contentDescription =
                                        editedTranslation
                                }
                            }
                            is androidx.appcompat.widget.Toolbar -> {
                                when (keyAndTranslation.styleable) {
                                    R.styleable.nstack_title -> view.title = editedTranslation
                                    R.styleable.nstack_subtitle -> view.subtitle = editedTranslation
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
        keyAndTranslationList: List<KeyAndTranslation> = emptyList(),
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
                                keyAndTranslationList.map { getSectionAndKeyPair(it.key) }
                            if (sectionAndKeyPairList.isNotEmpty()) {
                                update(proposals.filter { sectionAndKeyPairList.contains(it.section to it.key) })
                            } else {
                                update(proposals)
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

    fun showChooseOptionDialog(
        view: View,
        translationPair: Pair<TranslationData, TranslationData>
    ) {
        val builder = AlertDialog.Builder(view.context)
            .setTitle("NStack proposal")
            .setPositiveButton("View translation proposals") { _, _ ->
                showProposalsDialog(view, translationPair.toKeyAndTranslationList(), true)
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
            mutableList += KeyAndTranslation(first.key!!, second.key!!, R.styleable.nstack_key)
        }
        if (first.text != null && second.text != null) {
            mutableList += KeyAndTranslation(first.text!!, second.text!!, R.styleable.nstack_text)
        }
        if (first.hint != null && second.hint != null) {
            mutableList += KeyAndTranslation(first.hint!!, second.hint!!, R.styleable.nstack_hint)
        }
        if (first.description != null && second.description != null) {
            mutableList += KeyAndTranslation(
                first.description!!,
                second.description!!,
                R.styleable.nstack_description
            )
        }
        if (first.textOn != null && second.textOn != null) {
            mutableList += KeyAndTranslation(
                first.textOn!!,
                second.textOn!!,
                R.styleable.nstack_textOn
            )
        }
        if (first.textOff != null && second.textOff != null) {
            mutableList += KeyAndTranslation(
                first.textOff!!,
                second.textOff!!,
                R.styleable.nstack_textOff
            )
        }
        if (first.contentDescription != null && second.contentDescription != null) {
            mutableList += KeyAndTranslation(
                first.contentDescription!!,
                second.contentDescription!!,
                R.styleable.nstack_contentDescription
            )
        }
        if (first.title != null && second.title != null) {
            mutableList += KeyAndTranslation(
                first.title!!,
                second.title!!,
                R.styleable.nstack_title
            )
        }
        if (first.subtitle != null && second.subtitle != null) {
            mutableList += KeyAndTranslation(
                first.subtitle!!,
                second.subtitle!!,
                R.styleable.nstack_subtitle
            )
        }
        return mutableList
    }
}
