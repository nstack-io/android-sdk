package dk.nodes.nstack.kotlin.util.extensions

import android.app.Dialog
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import dk.nodes.nstack.R

/**
 * Changes the system navigationBarColor to [color] while dialog is shown.
 */
fun Dialog.setNavigationBarColor(@ColorRes color: Int = R.color.nstackbackgroundDark) {
    window?.navigationBarColor = ContextCompat.getColor(context, color)
}
