package dk.nodes.nstack.kotlin.models

import java.util.Date

data class AppOpenSettings(
    val platform: String = "android",
    var device : String,
    var osVersion : String,
    var guid: String,
    var version: String,
    var oldVersion: String,
    var lastUpdated: Date,
    var versionUpdateTestMode: Boolean = false
)
