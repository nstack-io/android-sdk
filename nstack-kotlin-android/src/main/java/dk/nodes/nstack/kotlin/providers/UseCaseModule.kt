package dk.nodes.nstack.kotlin.providers

import dk.nodes.nstack.kotlin.usecases.HandleLocalizeListUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { HandleLocalizeListUseCase(get(), get(), get()) }
}