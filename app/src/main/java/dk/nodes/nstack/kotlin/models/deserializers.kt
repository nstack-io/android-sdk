package dk.nodes.nstack.kotlin.models

import dk.nodes.nstack.kotlin.util.extensions.iso8601Date
import dk.nodes.nstack.kotlin.util.extensions.localizeIndices
import dk.nodes.nstack.models.AppUpdate
import dk.nodes.nstack.models.AppUpdateData
import dk.nodes.nstack.models.AppUpdateMeta
import dk.nodes.nstack.models.AppUpdateState
import dk.nodes.nstack.models.Message
import dk.nodes.nstack.models.RateReminder
import dk.nodes.nstack.models.Update
import org.json.JSONObject

fun AppUpdate(jsonObject: JSONObject): AppUpdate {
    val update = Update(
        jsonObject.optJSONObject("newer_version") ?: jsonObject.optJSONObject("new_in_version")
    )
    return AppUpdate(

        update = update,
        state = when {
            update.state == "yes" -> AppUpdateState.UPDATE
            update.state == "force" -> AppUpdateState.FORCE
            jsonObject.has("new_in_version") -> AppUpdateState.CHANGELOG
            else -> AppUpdateState.NONE
        }
    )
}

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

fun AppUpdateMeta(jsonObject: JSONObject): AppUpdateMeta {
    return AppUpdateMeta(acceptLanguage = jsonObject.optString("accept_Language"))
}
