package dk.nodes.nstack.kotlin.util

import java.util.*

typealias OnLanguageChangedFunction = ((Locale) -> Unit)

interface OnLanguageChangedListener {
    fun onLanguageChanged(locale: Locale)
}

data class LanguageListener(
    val onLanguageChangedListener: OnLanguageChangedListener? = null,
    val onLanguageChangedFunction: OnLanguageChangedFunction? = null
)
