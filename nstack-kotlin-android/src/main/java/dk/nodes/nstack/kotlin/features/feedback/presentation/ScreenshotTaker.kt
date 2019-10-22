package dk.nodes.nstack.kotlin.features.feedback.presentation

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import androidx.core.view.drawToBitmap

internal object ScreenshotTaker {

    fun take(view: View): Bitmap = view.drawToBitmap()

    fun take(activity: Activity): Bitmap = activity.window.decorView.rootView.let(::take)
}