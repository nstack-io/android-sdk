package dk.nodes.nstack.kotlin.util.extensions

import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import dk.nodes.nstack.R

fun View.addLiveEditOverlay() {
    val drawable = ContextCompat.getDrawable(context, R.drawable.overlay_live_edit_view)!!
    drawable.setBounds(0, 0, width, height)
    overlay.add(drawable)
}

fun View.clearLiveEditOverlay() {
    overlay.clear()
}

fun View.setOnVeryLongClickListener(listener: () -> Unit) {
    setOnTouchListener(object : View.OnTouchListener {
        private val longClickDuration = 1_200L
        private var timerFinished = false
        private var cancelOrderCountdownTimer: CountDownTimer? = null
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.actionMasked) {
                MotionEvent.ACTION_DOWN -> startCountdownTimer()
                MotionEvent.ACTION_CANCEL -> cancelCountdownTimer()
                MotionEvent.ACTION_UP -> {
                    if (!timerFinished) {
                        performClick()
                    }
                    cancelCountdownTimer()
                }
            }
            return true
        }

        private fun startCountdownTimer() {
            cancelCountdownTimer()
            cancelOrderCountdownTimer = object : CountDownTimer(longClickDuration, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    timerFinished = true
                    listener.invoke()
                }
            }.start()
        }

        private fun cancelCountdownTimer() {
            cancelOrderCountdownTimer?.cancel()
            timerFinished = false
        }
    })
}
