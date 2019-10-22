package dk.nodes.nstack.demo.home

data class HomeViewState(
    val isLoading: Boolean,
    val errorMessage: String?,
    val isFeedbackSent: Boolean?
)
