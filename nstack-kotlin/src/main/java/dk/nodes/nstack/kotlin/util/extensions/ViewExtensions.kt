package dk.nodes.nstack.kotlin.util.extensions

import android.view.View

fun View.show() {
    animation?.cancel()
    animate().alpha(1f).setDuration(200L).setStartDelay(200L).withStartAction { visibility = View.VISIBLE }.start()
}

fun View.hide() {
    animation?.cancel()
    animate().alpha(0f).setDuration(200L).setStartDelay(0L).withEndAction { visibility = View.INVISIBLE }.start()
}