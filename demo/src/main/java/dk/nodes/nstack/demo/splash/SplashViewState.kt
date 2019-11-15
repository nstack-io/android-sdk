package dk.nodes.nstack.demo.splash

import dk.nodes.nstack.kotlin.models.Message

data class SplashViewState(
    val isLoading: Boolean,
    val isFinished: Boolean,
    val message: Message?
)
