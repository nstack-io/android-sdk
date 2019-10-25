package dk.nodes.nstack.kotlin.features.feedback.presentation

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlin.math.max

internal class FeedbackImageDecoder(private val contentResolver: ContentResolver) {

    fun decode(
        uri: Uri,
        requiredSize: Int = 1200
    ): Bitmap? = try {
        val bitmapSize = obtainBitmapSize(uri)
        val sampleSize = bitmapSize.maximum / requiredSize

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = sampleSize
        }

        val stream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(stream, null, options)
        stream?.close()

        bitmap
    } catch (e: Exception) {
        null
    }

    private fun obtainBitmapSize(uri: Uri): BitmapSize {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        val stream = contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(stream, null, options)
        stream?.close()

        return BitmapSize(
            width = options.outWidth,
            height = options.outHeight
        )
    }

    data class BitmapSize(
        val width: Int,
        val height: Int
    ) {
        val maximum = max(width, height)
    }
}