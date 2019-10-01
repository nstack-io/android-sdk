package dk.nodes.nstack.kotlin.models

import okhttp3.FormBody

data class Feedback(
    val appVersion: String? = null,
    val deviceName: String? = null,
    val name: String? = null,
    val email: String? = null,
    val message: String? = null
) {

    val form: FormBody
        get() {
            val builder = FormBody.Builder()
            appVersion?.let { builder.add("app_version", it) }
            deviceName?.let { builder.add("device", it) }
            name?.let { builder.add("name", it) }
            email?.let { builder.add("email", it) }
            message?.let { builder.add("message", it) }
            return builder.build()
        }
}
