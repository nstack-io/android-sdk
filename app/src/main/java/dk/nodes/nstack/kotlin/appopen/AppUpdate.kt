package dk.nodes.nstack.kotlin.appopen

data class AppUpdate(
        var state: String? = null,
        var link: String? = null,
        var versionCode: Int? = null,
        var versionName: String? = null,
        var isUpdate: Boolean = false,
        var force: Boolean = false,
        var title: String? = null,
        var message: String? = null,
        var positiveBtn: String? = null,
        var negativeBtn: String? = null
)