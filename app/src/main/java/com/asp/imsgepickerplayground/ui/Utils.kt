package com.asp.imsgepickerplayground.ui

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import com.oginotihiro.cropview.RotateBitmap
import java.io.InputStream
import kotlin.math.pow
import kotlin.math.sqrt

object Utils {
    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    private fun getMaxImageSize(context: Context): Int {
        var width = 0
        var height = 0

        // TODO: Remove deprecated calls
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            (context.getSystemService(WINDOW_SERVICE) as WindowManager).currentWindowMetrics.let {
                width = it.bounds.width()
                height = it.bounds.height()
            }
        } else {
            val display = (context.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
            val point = Point()
            display.getSize(point)
            width = point.x
            height = point.y
        }

        return sqrt(width.toFloat().pow(2) + height.toFloat().pow(2)).toInt()
    }

    fun getBitmapSampleSize(context: Context, uri: Uri): Int {
        val maxAllowedSize = getMaxImageSize(context)
        var sampleSize = 1

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        var inputStream: InputStream? = null

        try {
            inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options) // Just get image size
        } catch (ex: Exception) {
            // TODO: Log Exception
        } finally {
            inputStream?.close()
        }

        while (options.outHeight / sampleSize > maxAllowedSize || options.outWidth / sampleSize > maxAllowedSize) {
            sampleSize = sampleSize shl 1
        }

        return sampleSize
    }

//    fun getMatrix(bitmap: Bitmap/*, rotation: Int*/): Matrix {
//        return Matrix().apply {
//            val cx = (bitmap.width / 2).toFloat()
//            val cy = (bitmap.height / 2).toFloat()
//
//            preTranslate(-cx, -cy)
//            // TODO: Handle rotation if needed
////            postRotate(rotation.toFloat())
//            postTranslate(cx, cy) // Todo: Handle orientation change
//        }
//    }
//
//    fun getExifRotation(uri: String): Int {
//        return try {
//            uri.let {
//                val exifInterface = ExifInterface(it)
//                when(exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
//                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
//                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
//                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
//                    else -> ExifInterface.ORIENTATION_UNDEFINED
//                }
//            } ?: 0
//        } catch (ex: Exception) {
//            0
//        }
//    }

    fun decodeBitmapRegionCrop(context: Context, sourceUri: Uri, rect: Rect, requiredWidth: Int, requiredHeight: Int, rotation: Int): Bitmap? {
        var updatedRect = rect
        var croppedBitmap: Bitmap? = null
        try {
            val bitmapRegionDecoder = context.contentResolver.openInputStream(sourceUri)?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    BitmapRegionDecoder.newInstance(it)
                } else {
                    BitmapRegionDecoder.newInstance(it, false)
                }
            }

            val width = bitmapRegionDecoder?.width ?: 0
            val height = bitmapRegionDecoder?.height ?: 0

            if(rotation != 0) {
                Matrix().apply {
                    setRotate(-(rotation).toFloat())

                    val adjustedRect = RectF()
                    mapRect(adjustedRect, RectF(rect))

                    val adjustedDx = if(adjustedRect.left < 0) width.toFloat() else 0F
                    val adjustedDy = if(adjustedRect.top < 0) height.toFloat() else 0F

                    adjustedRect.offset(adjustedDx, adjustedDy)
                    updatedRect = Rect(
                        adjustedRect.left.toInt(),
                        adjustedRect.top.toInt(),
                        adjustedRect.right.toInt(),
                        adjustedRect.bottom.toInt()
                    )
                }
            }

            val maxSize = getMaxImageSize(context)
            var sampleSize = 1

            while (updatedRect.height() / sampleSize > maxSize || updatedRect.width() / sampleSize > maxSize) {
                sampleSize = sampleSize shl 1
            }

            val options = BitmapFactory.Options()
            options.inSampleSize = sampleSize

            croppedBitmap = bitmapRegionDecoder?.decodeRegion(updatedRect, options)

            val matrix = Matrix()
            var isRequired = false
            if(rotation != 0) {
                matrix.postRotate(rotation.toFloat())
                isRequired = true
            }

            croppedBitmap?.let {
                if(requiredWidth > 0 && requiredHeight > 0) {
                    matrix.postScale((requiredWidth / it.width.toFloat()), (requiredHeight / it.height.toFloat()))
                    isRequired = true
                }

                if(isRequired) {
                    croppedBitmap = Bitmap.createBitmap(it, 0, 0, it.width, it.height, matrix, true)
                }
            }
        } catch (ex: Exception) {
            croppedBitmap = null
            // TODO: Log exception
        }
        return croppedBitmap
    }

    fun createRotateBitmap(context: Context, uri: Uri, sampleSize: Int, rotation: Int): RotateBitmap? {
        return try {
            var inputStream: InputStream? = null

            inputStream = context.contentResolver.openInputStream(uri)

            val option = BitmapFactory.Options()
            option.inSampleSize = sampleSize

            RotateBitmap(BitmapFactory.decodeStream(inputStream, null, option), rotation)
        } catch (ex: Exception) {
            // TODO: Log exception
            null
        }
    }
}