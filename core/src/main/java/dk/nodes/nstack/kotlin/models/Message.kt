package dk.nodes.nstack.kotlin.models

data class Message(
    val id: Int,
    val projectId: Int,
    val platform: String,
    val showSetting: String,
    val viewCount: Int,
    val message: String
)
