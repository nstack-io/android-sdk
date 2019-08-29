package dk.nodes.nstack.kotlin.models

import com.google.gson.annotations.SerializedName
import java.io.IOException

class NStackException(val errorBody: NStackErrorBody) : IOException() {
    override fun toString(): String {
        return errorBody.toString()
    }
}

data class NStackErrorBody(
    @SerializedName("class")
    val errorClass: String = "",
    val code: String = "",
    val errors: List<String> = listOf(),
    val localizedMessage: String? = null,
    val message: String = "",
    val service: String? = null
)