package dk.nodes.nstack.kotlin.util

typealias OnLanguagesChangedFunction = (() -> Unit)

interface OnLanguagesChangedListener {
    fun onLanguagesChanged()
}

data class LanguagesListener(val onLanguagesChangedListener: OnLanguagesChangedListener? = null,
                             val onLanguagesChangedFunction: OnLanguagesChangedFunction? = null)