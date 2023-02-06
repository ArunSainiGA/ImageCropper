package com.asp.imsgepickerplayground.ui.cropper

import android.view.MotionEvent

interface WFGestureDetector {
    fun onTouchEvent(event: MotionEvent): Boolean
}