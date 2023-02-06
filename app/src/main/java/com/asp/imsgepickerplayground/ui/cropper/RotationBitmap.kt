package com.asp.imsgepickerplayground.ui.cropper

import android.graphics.Bitmap
import android.graphics.Matrix

class RotationBitmap(val bitmap: Bitmap, val orientation: Int) {
    private val updatedOrientation = orientation % 360 // 360 -> 0

    fun getRotationMatrix() = Matrix().apply {
            if(bitmap != null && updatedOrientation != 0) {
                val cx = bitmap.width.toFloat() / 2
                val cy = bitmap.height.toFloat() / 2

                preTranslate(-cx, -cy)
                postRotate(updatedOrientation.toFloat())
                postTranslate(getWidth().toFloat() / 2, getHeight().toFloat() / 2)
            }
        }

    fun orientationChanged(): Boolean {
        return (updatedOrientation / 90) % 2 != 0
    }

    fun getWidth() =
        if(orientationChanged())
            bitmap.height
        else
            bitmap.width

    fun getHeight() =
        if(orientationChanged())
            bitmap.width
        else
            bitmap.height
}