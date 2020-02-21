package dk.nodes.nstack.demo.update

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import dk.nodes.nstack.kotlin.models.AppUpdate
import dk.nodes.nstack.kotlin.models.update

fun Fragment.showChangelogDialog(appUpdate: AppUpdate) {
    AlertDialog.Builder(requireContext())
        .setTitle(appUpdate.update?.translate?.title ?: return)
        .setMessage(appUpdate.update?.translate?.message ?: return)
        .setNegativeButton(appUpdate.update?.translate?.negativeButton ?: return) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}