package com.asp.imsgepickerplayground.ui.cropper

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector

class ScaleDetector(private val context: Context, private val gestureListener: WFOnGestureListener) {

    private var scaleDetector: ScaleGestureDetector? = null

    init {
        scaleDetector = ScaleGestureDetector(context, WFScaleGestureDetector())
    }

    inner class WFScaleGestureDetector: ScaleGestureDetector.OnScaleGestureListener {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor

            if(scaleFactor.isNaN() || scaleFactor.isInfinite()) {
                return false
            }

            gestureListener.onScale(scaleFactor, detector.focusX, detector.focusY)

            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean = true

        override fun onScaleEnd(detector: ScaleGestureDetector) {}
    }

    fun isScaling() = scaleDetector?.isInProgress ?: false

    fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector?.onTouchEvent(event)
        return true
    }
}