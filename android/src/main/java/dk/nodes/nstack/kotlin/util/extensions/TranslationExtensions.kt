package dk.nodes.nstack.kotlin.util.extensions

import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.TranslationData

private fun setTranslation(view: View, key: String) {
    if (!NStack.hasKey(key)) return
    NStack.addView(view, TranslationData(key = key))
}

fun Context.getNSString(@StringRes stringRes: Int): String? {
    return NStack.getTranslation(stringRes, this)
}

fun Fragment.getNSString(@StringRes stringRes: Int): String? {
    return NStack.getTranslation(stringRes, context ?: return null)
}
