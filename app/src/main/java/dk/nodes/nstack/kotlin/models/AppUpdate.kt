package dk.nodes.nstack.kotlin.models

import org.json.JSONObject

data class AppUpdate(
        val newerVersion: UpdateNewerVersion? = null
) {

    constructor(jsonObject: JSONObject) : this(
        newerVersion = UpdateNewerVersion(jsonObject.optJSONObject("newer_version"))
    )
}
