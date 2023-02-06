package com.asp.imsgepickerplayground.ui.main

import android.content.Context
import android.graphics.*
import android.net.Uri
import com.asp.imsgepickerplayground.ui.util.FileUtil
import com.asp.imsgepickerplayground.ui.util.URIResolver
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

class BitmapProcessor(private val context: Context) {
//    private val fileUtils = FileUtil()
//    private val uriResolver = URIResolver(fileUtils)
//    private val sizeResolver = SizeResolver()
//
//    fun getHardScaledImage(uri: Uri, width: Int, height: Int, backgroundColor: Int = Color.WHITE) =
//        uriToBitmap(uri)?.let { bitmap: Bitmap ->
//            resize(bitmap, width, height)
//        }
//
//    fun getHardScaledImage(file: File, width: Int, height: Int, backgroundColor: Int = Color.WHITE) =
//        fileToBitmap(file).let { bitmap: Bitmap ->
//            resize(bitmap, width, height)
//        }
//
//    fun getSoftScaledImage(uri: Uri, width: Int, height: Int, backgroundColor: Int = Color.WHITE) =
//        resize(getScaledBitmap(uriToBitmap(uri), width, height), width, height)
//
//    fun getSoftScaledImage(file: File, width: Int, height: Int, backgroundColor: Int = Color.WHITE) =
//        resize(getScaledBitmap(fileToBitmap(file), width, height), width, height)
//
//    private fun resize(bitmap: Bitmap, width: Int, height: Int) =
//        Bitmap.createScaledBitmap(bitmap, width, height, false)
//
//    private fun getScaledBitmap(source: Bitmap, eWidth: Int, eHeight: Int): Bitmap {
//        val allowedSize = sizeResolver.resizeForMultipleSizeScale(eWidth, eHeight, source.width, source.height)
//
//        val croppedBitmap = Bitmap.createBitmap(
//            source,
//            ((source.width / 2) - (allowedSize.first / 2)),
//            ((source.height / 2) - (allowedSize.second / 2)),
//            allowedSize.first,
//            allowedSize.second
//        )
//        if (source != croppedBitmap) source.recycle()
//
//        val bitmap = Bitmap.createBitmap(allowedSize.first, allowedSize.second, Bitmap.Config.ARGB_8888)
//
//        val canvas = Canvas(bitmap)
//        val bitmapShader = BitmapShader(croppedBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
//        val paint = Paint().apply {
//            shader = bitmapShader
//            isAntiAlias = true
//        }
//        canvas.drawRect(
//            0F, 0F, allowedSize.first.toFloat(), allowedSize.second.toFloat(), paint
//        )
//
//        if (source != bitmap) source.recycle()
//
//        return bitmap
//    }
//
//    fun addBackgroundLayer(bitmap: Bitmap, color: Int): Bitmap {
//        return Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config).apply {
//            eraseColor(color)
//            Canvas(this).drawBitmap(bitmap, Matrix(), Paint(Paint.FILTER_BITMAP_FLAG))
//        }
//    }
//
//    fun compressImage(bitmap: Bitmap, quality: Int): Bitmap {
//        val outputStream = ByteArrayOutputStream()
//        val byteArray = outputStream.let {
//            bitmap.compress(DEFAULT_COMPRESSION_FORMAT, quality, it)
//            it.toByteArray()
//        }
//        val inputStream = ByteArrayInputStream(byteArray)
//        val resultantBitmap = BitmapFactory.decodeStream(inputStream)
//        inputStream.close()
//        outputStream.close()
//
//        return resultantBitmap
//    }
//
//    fun uriToBitmap(uri: Uri) =
//        uriResolver.resolve(context, uri).let { path ->
//            BitmapFactory.decodeFile(path)
//        }
//
//    fun fileToBitmap(file: File) =
//        BitmapFactory.decodeFile(file.absolutePath)
//
//    fun isPNG(uri: Uri) = uri.path?.endsWith(PNG_EXTENSION)
//
//    fun isPNG(file: File) = file.absolutePath.endsWith(PNG_EXTENSION)
//
//    companion object {
//        const val PNG_EXTENSION = ".png"
//        val DEFAULT_COMPRESSION_FORMAT = Bitmap.CompressFormat.JPEG
//    }
}