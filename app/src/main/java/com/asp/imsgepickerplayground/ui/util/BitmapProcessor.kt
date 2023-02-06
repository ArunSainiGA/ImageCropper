package com.asp.imsgepickerplayground.ui.util

import android.graphics.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

class BitmapProcessor {
    private val pngBackgroundColor = Color.WHITE
    private val tag = "BitmapProcessor"
    private val fileUtils = FileUtil()

    fun hardScaleBitmap(path: String, height: Int, width: Int): Bitmap?{
        val bitmap = decodeBitmap(path)
        return bitmap?.let { bitmap ->

            var resultantBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

            if(bitmap.hasAlpha())
                resultantBitmap = addBackgroundLayer(bitmap)

            writeBitmap(path, resultantBitmap)

            resultantBitmap
        }
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

    private fun isPNG(file: File) = file.absolutePath.endsWith(PNG_EXTENSION)

    private fun isJPEG(file: File) = file.absolutePath.endsWith(JPG_EXTENSION) || file.absolutePath.endsWith(JPEG_EXTENSION)

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

    private fun writeBitmap(path: String, bitmap: Bitmap) {
        try {
            fileUtils.getFile(path)?.let {  file ->
                val fos = file.outputStream()
                bitmap.compress(DEFAULT_COMPRESSION_FORMAT, COMPRESSION_QUALITY, fos)
                fos.flush()
                fos.close()
            }
        } catch (e: Exception) {

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