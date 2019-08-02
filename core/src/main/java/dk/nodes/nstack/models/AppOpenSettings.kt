package dk.nodes.nstack.models

import java.util.Date

data class AppOpenSettings(
        val platform: String = "android",
        var guid: String,
        var version: String,
        var oldVersion: String,
        var lastUpdated: Date
)
