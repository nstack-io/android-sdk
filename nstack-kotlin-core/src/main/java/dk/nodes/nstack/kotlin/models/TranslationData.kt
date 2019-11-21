package dk.nodes.nstack.kotlin.models

data class TranslationData(
    var key: String? = null,
    val text: String? = null,
    val hint: String? = null,
    val description: String? = null,
    val textOn: String? = null,
    val textOff: String? = null,
    val contentDescription: String? = null,
    val title: String? = null,
    val subtitle: String? = null
) {

    val isValid: Boolean
        get() = !(
            key == null &&
                text == null &&
                hint == null &&
                description == null &&
                textOn == null &&
                textOff == null &&
                contentDescription == null &&
                title == null &&
                subtitle == null
            )
}
