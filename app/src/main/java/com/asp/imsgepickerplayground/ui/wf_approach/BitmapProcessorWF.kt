package com.asp.imsgepickerplayground.ui.wf_approach

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.Log
import android.util.Size
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

class BitmapProcessorWF {
    /**
     * Background layer to replace the alpha in bitmap
     */
    private val pngBackgroundColor = Color.WHITE

    /**
     * Scales the decoded bitmap to fixed height and width
     * @param uriMeta is the [URIMeta]
     * @param size is the expected height and width for the resultant bitmap
     * @return [Bitmap] It can be null in case if there is any issue processing the bitmap]
     */
    fun hardScaleBitmap(context: Context, uriMeta: URIMetaWF, size: Size): Bitmap?{
        val bitmap = decodeBitmap(context, uriMeta.path)
        Log.i("NAF", bitmapToByteArray(bitmap!!).size.toString())
        return bitmap?.let { b ->

            var resultantBitmap = Bitmap.createScaledBitmap(b, size.width, size.height, false)

            if(resultantBitmap.hasAlpha())
                resultantBitmap = addBackgroundLayer(resultantBitmap)

            if(!isJPEG(uriMeta.extension))
                resultantBitmap = compressImage(resultantBitmap, COMPRESSION_QUALITY)

            Log.i("NAF Final:", bitmapToByteArray(resultantBitmap!!).size.toString())

            resultantBitmap
        }
    }

    /**
     * Converts the bitmap to a byte array,
     * this can be used to attach the byte array in a Multipart request
     */
    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val byteArray = outputStream.let {
            bitmap.compress(DEFAULT_COMPRESSION_FORMAT, COMPRESSION_QUALITY, it)
            it.toByteArray()
        }
        return byteArray
    }

    /**
     * Adds the background layer to an image that has transparency level
     */
    private fun addBackgroundLayer(bitmap: Bitmap): Bitmap {
        return Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config).apply {
            eraseColor(pngBackgroundColor)
            Canvas(this).drawBitmap(bitmap, Matrix(), Paint(Paint.FILTER_BITMAP_FLAG))
        }
    }

    /**
     * Compresses the image to JPEG format with the defined quality level
     * @param bitmap is the bitmap to be compressed
     * @param quality is the quality for the compressed bitmap
     */
    private fun compressImage(bitmap: Bitmap, quality: Int): Bitmap {
        val outputStream = ByteArrayOutputStream()
        val byteArray = outputStream.let {
            bitmap.compress(DEFAULT_COMPRESSION_FORMAT, quality, it)
            it.toByteArray()
        }
        val inputStream = ByteArrayInputStream(byteArray)
        val resultantBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        outputStream.close()

        return resultantBitmap
    }

    /**
     * Checks if the image has JPEG or JPG format
     */
    private fun isJPEG(extension: String?) = extension != null && (extension.endsWith(JPG_EXTENSION) || extension.endsWith(JPEG_EXTENSION))

    /**
     * Decodes the bitmap from given path
     */
    private fun decodeBitmap(context: Context, path: Uri) =
        when {
            path.toString().isBlank() -> null
            else -> {
                try {
                    var bitmap: Bitmap? = null
                    context.contentResolver.openFileDescriptor(path, "r").use { pfd ->
                        if( pfd != null ){
                            bitmap = BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
                        }
                    }
                    bitmap
                } catch (e: Exception) {
//                    Timber.e(e, "Could not decode bitmap for $path")
                    null
                }
            }
        }



    companion object {
        const val JPG_EXTENSION = ".jpg"
        const val JPEG_EXTENSION = ".jpeg"
        const val COMPRESSION_QUALITY = 100
        const val CONVERTED_BITMAP_EXT = JPEG_EXTENSION
        const val CONVERTED_BITMAP_MIME = "image/jpeg"
        val DEFAULT_COMPRESSION_FORMAT = Bitmap.CompressFormat.JPEG
    }
}