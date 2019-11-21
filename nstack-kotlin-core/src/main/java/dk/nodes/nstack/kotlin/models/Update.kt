package dk.nodes.nstack.kotlin.models

data class Update(
    val state: String,
    val lastId: Int,
    val version: String,
    val link: String,
    val translate: UpdateTranslate,
    val fileUrl: String?
)
