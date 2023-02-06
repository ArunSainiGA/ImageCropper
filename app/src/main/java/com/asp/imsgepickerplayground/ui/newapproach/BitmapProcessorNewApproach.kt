package com.asp.imsgepickerplayground.ui.newapproach

import android.graphics.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

class BitmapProcessorNewApproach {
    private val pngBackgroundColor = Color.WHITE
    private val tag = "BitmapProcessor"

    fun hardScaleBitmap(path: String, height: Int, width: Int): Bitmap?{
        val bitmap = decodeBitmap(path)
        return bitmap?.let { bitmap ->

            var resultantBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

            if(bitmap.hasAlpha())
                resultantBitmap = addBackgroundLayer(bitmap)

            if(!isJPEG(path))
                resultantBitmap = compressImage(resultantBitmap, COMPRESSION_QUALITY)

            resultantBitmap
        }
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val byteArray = outputStream.let {
            bitmap.compress(DEFAULT_COMPRESSION_FORMAT, COMPRESSION_QUALITY, it)
            it.toByteArray()
        }
        return byteArray
    }

    private fun addBackgroundLayer(bitmap: Bitmap): Bitmap {
        return Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config).apply {
            eraseColor(pngBackgroundColor)
            Canvas(this).drawBitmap(bitmap, Matrix(), Paint(Paint.FILTER_BITMAP_FLAG))
        }
    }

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

    private fun isJPEG(path: String) = path.endsWith(JPG_EXTENSION) || path.endsWith(JPEG_EXTENSION)

    private fun decodeBitmap(path: String) =
        when {
            path.isBlank() -> null
            else -> {
                try {
                    BitmapFactory.decodeFile(path)
                } catch (e: Exception) {
                    // TODO: Add proper logging exception
                    null
                }
            }
        }

    companion object {
        const val PNG_EXTENSION = ".png"
        const val JPG_EXTENSION = ".jpg"
        const val JPEG_EXTENSION = ".jpeg"
        const val COMPRESSION_QUALITY = 100
        val DEFAULT_COMPRESSION_FORMAT = Bitmap.CompressFormat.JPEG
    }
}