package dk.nodes.nstack.kotlin.models

/**
 * Holds all NStack features related data relevant for app start.
 *
 * @see [AppOpenData]
 * @see [AppOpenMeta]
 */
data class AppOpen(
    val data: AppOpenData,
    val meta: AppOpenMeta
)
