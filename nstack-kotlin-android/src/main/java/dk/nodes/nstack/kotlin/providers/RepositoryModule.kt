package dk.nodes.nstack.kotlin.providers

import dk.nodes.nstack.kotlin.data.terms.TermsRepository
import dk.nodes.nstack.kotlin.provider.GsonProvider

internal class RepositoryModule(
        private val nStackModule: NStackModule
) {

    fun provideTermsRepository(): TermsRepository = TermsRepository(
            preferences = nStackModule.providePreferences(),
            gson = GsonProvider.provideGson()
    )
}
