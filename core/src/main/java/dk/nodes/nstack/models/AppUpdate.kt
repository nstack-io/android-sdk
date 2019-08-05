package dk.nodes.nstack.models

data class AppUpdate(
    var state: AppUpdateState = AppUpdateState.NONE,
    var update: Update? = null
)