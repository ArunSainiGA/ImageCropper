package com.asp.imsgepickerplayground.ui.cropper

interface WFOnGestureListener {
    fun onScale(scaleFactor: Float, focusX: Float, focusY: Float)

    fun onDrag(deltaX: Float, deltaY: Float)

    fun onFling(lastX: Float, lastY: Float, velocityX: Float, velocityY: Float)
}