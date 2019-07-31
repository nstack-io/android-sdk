package dk.nodes.nstack.kotlin.managers

import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
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

        val dialogBuilder = AlertDialog.Builder(view.context, R.style.Theme_AppCompat_Light_Dialog)
        val dialogView =
                LayoutInflater.from(view.context).inflate(R.layout.bottomsheet_translation_change, null)
        val editText = dialogView.findViewById<EditText>(R.id.zzz_nstack_translation_et)
        val btn = dialogView.findViewById<Button>(R.id.zzz_nstack_translation_change_btn)

        editText.setText(translatedText ?: translatedHint ?: translatedKey ?: "")
        dialogBuilder.setView(dialogView)

        val dialog = dialogBuilder.create()
        btn.setOnClickListener {
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
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showProposalsDialog(view: View) {
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


