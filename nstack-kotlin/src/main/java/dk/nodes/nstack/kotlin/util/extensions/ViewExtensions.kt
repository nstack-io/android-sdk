package dk.nodes.nstack.kotlin.util.extensions

import android.view.View

/**
 * Changes visibility of a View to [View.VISIBLE] along with a alpha-fade-in animation.
 */
fun View.show(duration : Long = 200L, startDelay : Long = 200) {
    animation?.cancel()
    animate().alpha(1f).setDuration(duration).setStartDelay(startDelay).withStartAction { visibility = View.VISIBLE }.start()
}

/**
 * Changes visibility of a View to [View.INVISIBLE] along with a alpha-fade-out animation.
 */
fun View.hide(duration : Long = 200L, startDelay : Long = 0) {
    animation?.cancel()
    animate().alpha(0f).setDuration(duration).setStartDelay(startDelay).withEndAction { visibility = View.INVISIBLE }.start()
}