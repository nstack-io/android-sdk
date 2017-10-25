package dk.nodes.nstack.kotlin.appopen

import org.json.JSONObject

/**
 * Created by bison on 25-05-2017.
 */
class AppUpdate(json : JSONObject)
{
    var state : String? = null
    var link : String? = null
    var version : String? = null
    var isUpdate : Boolean = false
    var force : Boolean = false

    var title : String? = null
    var message : String? = null
    var positiveBtn : String? = null
    var negativeBtn : String? = null

    init {
        val nv : JSONObject? = json.optJSONObject("newer_version")
        state = nv?.optString("state", null)
        if(state?.contentEquals("force") ?: false) {
            force = true
            isUpdate = true
        }
        if(state?.contentEquals("yes") ?: false) {
            isUpdate = true
        }
        link = nv?.optString("link", null)
        version = nv?.optString("version", null)
        val translate : JSONObject? = nv?.optJSONObject("translate")
        if(translate != null)
        {
            title = translate.optString("title", null)
            message = translate.optString("message", null)
            positiveBtn = translate.optString("positiveBtn", null)
            negativeBtn = translate.optString("negativeBtn", null)
        }
    }
}