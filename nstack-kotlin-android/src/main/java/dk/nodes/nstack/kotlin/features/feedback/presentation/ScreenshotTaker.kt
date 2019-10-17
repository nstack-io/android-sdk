package dk.nodes.nstack.kotlin.features.feedback.presentation

import android.app.Activity
import android.graphics.Bitmap
import android.view.View

internal object ScreenshotTaker {

    fun take(view: View): Bitmap {
        // TODO: find a non-deprecated solution for this

        view.isDrawingCacheEnabled = true
        view.buildDrawingCache(true)

        val screenshotBitmap = Bitmap.createBitmap(view.drawingCache)

        view.isDrawingCacheEnabled = false

        return screenshotBitmap
    }

    fun take(activity: Activity): Bitmap = activity.window.decorView.rootView.let(::take)
}