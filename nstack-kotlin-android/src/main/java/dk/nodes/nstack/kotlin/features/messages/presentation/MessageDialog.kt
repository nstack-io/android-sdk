package dk.nodes.nstack.kotlin.features.messages.presentation

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.Message
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.reflect.KFunction2

internal class MessageDialog(private val context: Context) {

    private lateinit var dialog: AlertDialog

    private lateinit var message: Message

    fun show(message: Message) {
        this.message = message
        this.dialog = MaterialAlertDialogBuilder(context)
            .setCancelable(false)
            .setMessage(message.message)
            .setPositiveButton(
                message.localization.okBtn ?: "Ok",
                null
            )
            .setConditionalNegativeButton(
                message.url != null,
                message.localization.urlBtn ?: "Open URL",
                null
            )
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener(::onOkButtonClicked)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setOnClickListener(::onUrlButtonClicked)
        }

        dialog.show()
    }

    private fun onOkButtonClicked(view: View) {
        GlobalScope.launch {
            NStack.Messages.setMessageViewed(message)
        }
        dialog.dismiss()
    }

    private fun onUrlButtonClicked(view: View) = try {
        val uri = Uri.parse(message.url)
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open ${message.url}", Toast.LENGTH_SHORT).show()
    }

    private fun MaterialAlertDialogBuilder.setConditionalNegativeButton(
        condition: Boolean,
        text: CharSequence?,
        listener: KFunction2<@ParameterName(
            name = "dialogInterface"
        ) DialogInterface, @ParameterName(
            name = "which"
        ) Int, Unit>?
    ): MaterialAlertDialogBuilder = if (condition) {
        setNegativeButton(text, listener)
    } else {
        this
    }
}