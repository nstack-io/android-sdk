package dk.nodes.nstack.kotlin.util.extensions

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import dk.nodes.nstack.R

/**
 * Clears the live-edit view overlay and the associated touch-listener.
 */
fun View.detachLiveEditListener() {
    overlay.clear()
    setOnTouchListener(null)
}

/**
 * Applies the live-edit view overlay and the corresponding touch-listener.
 * To disable call [detachLiveEditListener].
 */
fun View.attachLiveEditListener(listener: () -> Unit) {
    val drawable = LiveEditOverlayDrawable(context)
    drawable.setBounds(0, 0, width, height)
    overlay.add(drawable)

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
                    // Do nothing
                }

                override fun onFinish() {
                    drawable.isPressed = false
                    timerFinished = true
                    listener.invoke()
                }
            }.start()

            drawable.isPressed = true
        }

        private fun cancelCountdownTimer() {
            drawable.isPressed = false

            cancelOrderCountdownTimer?.cancel()
            timerFinished = false
        }
    })
}

private class LiveEditOverlayDrawable(context: Context) : Drawable() {

    private val background = ContextCompat.getDrawable(context, R.drawable.overlay_live_edit_view)

    private val backgroundPressed = ContextCompat.getDrawable(context, R.drawable.overlay_live_edit_view_pressed)

    var isPressed: Boolean = false
        set(value) {
            field = value
            invalidateSelf()
        }

    override fun draw(canvas: Canvas) {
        if (isPressed) {
            backgroundPressed?.bounds = bounds
            backgroundPressed?.draw(canvas)
        } else {
            background?.bounds = bounds
            background?.draw(canvas)
        }
    }

    override fun setAlpha(alpha: Int) {
        background?.alpha = alpha
        backgroundPressed?.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        background?.colorFilter = colorFilter
        backgroundPressed?.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity() = PixelFormat.TRANSPARENT
}