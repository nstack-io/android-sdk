package dk.nodes.nstack.kotlin.managers

import dk.nodes.nstack.kotlin.models.AppOpenResult
import dk.nodes.nstack.kotlin.models.AppOpenSettings
import dk.nodes.nstack.kotlin.models.AppUpdateData
import dk.nodes.nstack.kotlin.models.Proposal

interface NetworkManager {
    fun loadTranslation(url: String, onSuccess: (String) -> Unit, onError: (Exception) -> Unit)
    suspend fun loadTranslation(url: String): String?
    fun postAppOpen(
        settings: AppOpenSettings,
        acceptLanguage: String,
        onSuccess: (AppUpdateData) -> Unit,
        onError: (Exception) -> Unit
    )

    suspend fun postAppOpen(settings: AppOpenSettings, acceptLanguage: String): AppOpenResult
    fun postMessageSeen(guid: String, messageId: Int)
    fun postRateReminderSeen(appOpenSettings: AppOpenSettings, rated: Boolean)
    fun getResponse(slug: String, onSuccess: (String) -> Unit, onError: (Exception) -> Unit)
    suspend fun getResponseSync(slug: String): String?
    fun postProposal(
        settings: AppOpenSettings,
        locale: String,
        key: String,
        section: String,
        newValue: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    )

    fun fetchProposals(onSuccess: (List<Proposal>) -> Unit, onError: (Exception) -> Unit)
}