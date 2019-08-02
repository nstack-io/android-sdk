package dk.nodes.nstack.kotlin.util.extensions

import dk.nodes.nstack.models.Proposal
import org.json.JSONArray
import org.json.JSONObject


fun MutableList<Proposal>.parseFromString(json: String?) {
    val jsonRoot: JSONObject = try {
        JSONObject(json)
    } catch (e: Exception) {
        JSONObject()
    }
    val array: JSONArray = jsonRoot.optJSONArray("data") ?: return

    for (i in 0 until array.length()) {
        val jsonObject = array.getJSONObject(i)
        val id = jsonObject.getLong("id")
        val applicationId = jsonObject.getLong("application_id")
        val key = jsonObject.getString("key")
        val section = jsonObject.getString("section")
        val locale = jsonObject.getString("locale")
        val value = jsonObject.getString("value")
        val proposal = Proposal(
                id, applicationId, section, key, locale, value
        )
        add(proposal)
    }
}