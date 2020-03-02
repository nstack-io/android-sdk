package dk.nodes.nstack.kotlin.providers

import dk.nodes.nstack.kotlin.features.terms.data.TermsRepository
import org.koin.dsl.module

val repositoryModule = module {
    single {
        TermsRepository(get(), get())
    }
}
