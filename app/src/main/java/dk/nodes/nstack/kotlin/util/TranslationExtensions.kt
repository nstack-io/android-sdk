package dk.nodes.nstack.kotlin.util

import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.ToggleButton
import dk.nodes.nstack.kotlin.NStack
import dk.nodes.nstack.kotlin.models.TranslationData

operator fun TextView.plusAssign(key: String) {
    setTranslation(this, key)
}

operator fun ToggleButton.plusAssign(key: String) {
    setTranslation(this, key)
}

operator fun CompoundButton.plusAssign(key: String) {
    setTranslation(this, key)
}

private fun setTranslation(view: View, key: String) {
    if (!NStack.hasKey(key)) return
    NStack.addView(view, TranslationData(key = key))
}

