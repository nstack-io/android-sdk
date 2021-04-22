package dk.nodes.nstack.kotlin

import dk.nodes.nstack.kotlin.models.AppOpen
import dk.nodes.nstack.kotlin.models.Result
import dk.nodes.nstack.kotlin.util.extensions.consumable

/**
 * Holder of shared state of NStack singleton.
 */
internal class NStackStateHolder {
    var appOpenConsumable by consumable<Result<AppOpen>>()
}
