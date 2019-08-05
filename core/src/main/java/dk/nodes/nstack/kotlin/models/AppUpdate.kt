package dk.nodes.nstack.kotlin.models

data class AppUpdate(
    var state: AppUpdateState = AppUpdateState.NONE,
    var update: Update? = null
)