package dk.nodes.nstack.kotlin.features.feedback.domain.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

/**
 * A container for image data created by the NStack SDK. Can output different types for the
 * contained data using conversion functions
 */
@Suppress("MemberVisibilityCanBePrivate")
data class ImageData internal constructor(
    // A bitmap is only used internally for practicality
    private val bitmap: Bitmap
) {

    /**
     * Constructs a new ImageData object from a valid byte array of image data
     *
     * @throws IllegalArgumentException if the byte data is not a valid image
     */
    constructor(data: ByteArray) : this(
        BitmapFactory
            .decodeByteArray(data, 0, data.size)
            ?: throw IllegalArgumentException("The passed data is not in a valid image format")
    )

    /**
     * Creates a byte array copy of the contained data
     */
    fun asJpegBytes(): ByteArray {
        return ByteArrayOutputStream()
            .also { stream -> bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream) }
            .toByteArray()
    }
}

/**
 * A convenience extension function to turn a bitmap into [ImageData]
 */
fun Bitmap.toImageData() = ImageData(this)