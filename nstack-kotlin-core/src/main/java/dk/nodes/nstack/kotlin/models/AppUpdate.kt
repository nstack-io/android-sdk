package dk.nodes.nstack.kotlin.models

data class AppUpdate(
    internal val newerVersion: Update? = null,
    internal val newInVersion: Update? = null
)

val AppUpdate.update: Update?
    get() {
        return newerVersion ?: newInVersion
    }

val AppUpdate.state: AppUpdateState
    get() {
        return when {
            update?.state == "yes" -> AppUpdateState.UPDATE
            update?.state == "force" -> AppUpdateState.FORCE
            newInVersion != null -> AppUpdateState.CHANGELOG
            else -> AppUpdateState.NONE
        }
    }
