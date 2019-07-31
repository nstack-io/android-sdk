package dk.nodes.nstack.kotlin.managers

import android.content.res.Resources
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dk.nodes.nstack.R
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.NStack.runUiAction
import dk.nodes.nstack.kotlin.models.TranslationData
import dk.nodes.nstack.kotlin.util.NLog
import dk.nodes.nstack.kotlin.view.ProposalsAdapter

typealias LiveEditDialogListener = (View, TranslationData, String?, String?, String?) -> Unit
typealias LiveEditProposalsDialogListener = (View) -> Unit

class LiveEditManager(
        private val networkManager: NetworkManager,
        private val appOpenSettingsManager: AppOpenSettingsManager
) {
    fun showLiveEditDialog(
            view: View,
            translationData: TranslationData,
            translatedKey: String?,
            translatedText: String?,
            translatedHint: String?
    ) {
        NLog.d(this, "key: $translatedKey - $translatedText")
        val bottomSheetDialog = BottomSheetDialog(view.context, R.style.NstackBottomSheetTheme)
        bottomSheetDialog.setTitle("Test")
        bottomSheetDialog.setContentView(R.layout.bottomsheet_translation_change)
        val editText = bottomSheetDialog.findViewById<EditText>(R.id.zzz_nstack_translation_et)
        val btn = bottomSheetDialog.findViewById<Button>(R.id.zzz_nstack_translation_change_btn)
        editText!!.setText(translatedText ?: translatedHint ?: translatedKey ?: "")
        btn!!.setOnClickListener {
            val pair = getSectionAndKeyPair(translationData.text ?: translationData.key)
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
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    fun showProposalsDialog(view: View) {
        val bottomSheetDialog = BottomSheetDialog(view.context, R.style.NstackBottomSheetTheme)
        bottomSheetDialog.setContentView(R.layout.bottomsheet_translation_proposals)
        val bottomSheetInternal = bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        val recyclerView = bottomSheetDialog.findViewById<RecyclerView>(R.id.recyclerView)
        BottomSheetBehavior.from(bottomSheetInternal).apply {
            peekHeight = 400
            isFitToContents = true
        }
        recyclerView!!.layoutParams = recyclerView.layoutParams.apply {
            height = getWindowHeight() * 2 / 3
        }
        networkManager.fetchProposals(
                { proposals ->
                    if (proposals.isNotEmpty()) {
                        runUiAction {
                            recyclerView.adapter = ProposalsAdapter().apply { update(proposals) }
                        }
                        Snackbar.make(view, "Open Proposals", Snackbar.LENGTH_LONG)
                                .setAction("OPEN") {
                                    bottomSheetDialog.show()
                                }
                                .show()
                    } else {
                        Toast.makeText(view.context, "There isn't any proposals", Toast.LENGTH_SHORT).show();
                    }
                },
                {
                    Toast.makeText(view.context, "Unknown Error", Toast.LENGTH_SHORT).show();
                }
        )


    }

    private fun getWindowHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    private fun getSectionAndKeyPair(key: String?): Pair<String, String>? {
        val cleanedKey = cleanKeyName(key) ?: return null
        val divider = cleanedKey.indexOfFirst { it == '_' }
        return cleanedKey.substring(0, divider) to cleanedKey.substring(divider + 1, cleanedKey.length)
    }


    private fun cleanKeyName(keyName: String?): String? {
        val key = keyName ?: return null
        return if (key.startsWith("{") && key.endsWith("}")) {
            key.substring(1, key.length - 1)
        } else key
    }
}


