package dk.nodes.nstack.kotlin.models

data class AppUpdate(
        var state: AppUpdateState = AppUpdateState.NONE,
        var link: String? = null,
        var versionCode: Int? = null,
        var versionName: String? = null,
        var title: String? = null,
        var message: String? = null,
        var positiveBtn: String? = null,
        var negativeBtn: String? = null
)