package dk.nodes.nstack.kotlin.util

class LanguageFetchCallback constructor(private var numLanguages: Int) {

    var done: (() -> Unit)? = null

    fun fetchedLanguage() {
        numLanguages--
        if(numLanguages <= 0) {
            done?.invoke()
            done = null
        }
    }

}
