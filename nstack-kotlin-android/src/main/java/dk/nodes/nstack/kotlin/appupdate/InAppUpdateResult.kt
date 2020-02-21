package dk.nodes.nstack.kotlin.appupdate

sealed class InAppUpdateResult {
    object Success: InAppUpdateResult()
    object Fail: InAppUpdateResult()
    object Cancelled: InAppUpdateResult()
    object Unknown: InAppUpdateResult()
    data class Unavailable(val code: Int): InAppUpdateResult()
}