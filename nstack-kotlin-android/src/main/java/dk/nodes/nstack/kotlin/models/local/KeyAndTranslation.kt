package dk.nodes.nstack.kotlin.models.local

internal data class KeyAndTranslation(val key: String, val translation: String, val styleable: StyleableEnum)

internal fun KeyAndTranslation.asPair() = key to translation

enum class StyleableEnum {
    Key,
    Text,
    Hint,
    Description,
    TextOn,
    TextOff,
    ContentDescription,
    Title,
    Subtitle
}