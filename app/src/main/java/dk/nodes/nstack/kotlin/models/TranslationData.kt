package dk.nodes.nstack.kotlin.models

data class TranslationData(
        var key: String? = null,
        var text: String? = null,
        var hint: String? = null,
        var description: String? = null,
        var textOn: String? = null,
        var textOff: String? = null,
        var contentDescription: String? = null,
        var title: String? = null,
        var subtitle : String? = null
)