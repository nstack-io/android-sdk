package dk.nodes.nstack.models

import java.util.Date

data class LocalizeIndex(
    val id: Int,
    val url: String,
    val lastUpdatedAt: Date,
    val shouldUpdate: Boolean,
    val language: Language
)
