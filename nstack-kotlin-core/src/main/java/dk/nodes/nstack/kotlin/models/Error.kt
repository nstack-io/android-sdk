package dk.nodes.nstack.kotlin.models

sealed class Error {
    object UnknownError : Error()
    object NetworkError : Error()
    data class ApiError(val errorCode: Int) : Error()
}