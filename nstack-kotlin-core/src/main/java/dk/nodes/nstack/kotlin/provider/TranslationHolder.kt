package dk.nodes.nstack.kotlin.provider

interface TranslationHolder {
    fun hasKey(key: String?): Boolean
    fun getTranslationByKey(key: String?): String?
}
