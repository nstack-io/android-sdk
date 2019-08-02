package dk.nodes.nstack.models

import dk.nodes.nstack.kotlin.util.extensions.iso8601Date
import dk.nodes.nstack.kotlin.util.extensions.localizeIndices
import org.json.JSONObject
import java.util.Date

data class AppUpdateData(
    val count: Int = 0,
    val update: AppUpdate = AppUpdate(),
    val localize: List<LocalizeIndex> = listOf(),
    val platform: String = "",
    val createdAt: Date = Date(),
    val lastUpdated: Date = Date(),
    val message: Message? = null,
    val rateReminder: RateReminder? = null
)

val JSONObject.appUpdateData: AppUpdateData
    get() {
        val count = optInt("count")
        val localize = getJSONArray("localize").localizeIndices
        val platform = getString("platform")
        val createdAt = getString("created_at").iso8601Date
        val lastUpdated = getString("last_updated").iso8601Date

        val update = if (has("update")) {
            AppUpdate(getJSONObject("update"))
        } else {
            AppUpdate()
        }

        val message = if (has("message")) {
            Message(getJSONObject("message"))
        } else {
            null
        }

        val rateReminder = if (has("rate_reminder")) {
            RateReminder(getJSONObject("rate_reminder"))
        } else {
            null
        }

        return AppUpdateData(
            count = count,
            localize = localize,
            platform = platform,
            createdAt = createdAt,
            lastUpdated = lastUpdated,
            update = update,
            message = message,
            rateReminder = rateReminder
        )
    }
