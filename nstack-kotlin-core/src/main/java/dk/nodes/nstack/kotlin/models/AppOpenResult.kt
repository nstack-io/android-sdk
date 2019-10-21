package dk.nodes.nstack.kotlin.models

sealed class AppOpenResult {
    data class Success(val appUpdateResponse: AppUpdateResponse) : AppOpenResult()
    object NoInternet : AppOpenResult()
    object Failure : AppOpenResult()
}
