package dk.nodes.nstack.kotlin.models.local

internal data class Environment(private val name: String) {

    val shouldEnableTestMode: Boolean
        get() = listOf("staging", "debug", "development", "develop").contains(name)
}
