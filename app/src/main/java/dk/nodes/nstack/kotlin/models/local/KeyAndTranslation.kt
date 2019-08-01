package dk.nodes.nstack.kotlin.models.local

data class KeyAndTranslation(val key: String, val translation: String, val styleable: Int)

fun KeyAndTranslation.asPair() = key to translation