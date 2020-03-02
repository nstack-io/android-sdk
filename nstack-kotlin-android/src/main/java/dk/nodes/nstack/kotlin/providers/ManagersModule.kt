package dk.nodes.nstack.kotlin.providers

import dk.nodes.nstack.kotlin.managers.AppOpenSettingsManager
import dk.nodes.nstack.kotlin.managers.AssetCacheManager
import dk.nodes.nstack.kotlin.managers.PrefManager
import org.koin.dsl.module

val managersModule = module {
    single { AppOpenSettingsManager(get(), get()) }
    single { AssetCacheManager(get()) }
    single { PrefManager(get()) }
}