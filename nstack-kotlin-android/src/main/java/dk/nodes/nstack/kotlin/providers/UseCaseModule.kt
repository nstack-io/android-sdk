package dk.nodes.nstack.kotlin.providers

import dk.nodes.nstack.kotlin.usecases.HandleLocalizeIndexUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { HandleLocalizeIndexUseCase(get(), get(), get()) }
}