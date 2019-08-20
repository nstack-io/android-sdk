package dk.nodes.nstack.kotlin.models

import java.util.Locale

data class Language(
    val id: Int,
    val name: String,
    val locale: Locale,
    val direction: String,
    val isDefault: Boolean,
    val isBestFit: Boolean
)
