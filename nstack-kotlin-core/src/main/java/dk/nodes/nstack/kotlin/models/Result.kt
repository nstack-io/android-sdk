package dk.nodes.nstack.kotlin.models

sealed class Result<out T> {

    data class Success<T>(val value: T) : Result<T>()

    data class Error(val error: dk.nodes.nstack.kotlin.models.Error) : Result<Nothing>()
}