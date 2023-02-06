package com.asp.imsgepickerplayground.ui.cropper

import android.content.Context
import android.view.MotionEvent

class DragPointerDetector(
    private val context: Context, private val gestureListener: WFOnGestureListener
): DragGestureDetector(context, gestureListener) {
    private val invalidPointerId = -1
    private var activePointerId = invalidPointerId
    private var activePointerIndex = 0

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                // Get the pointer index when user taps down on screen
                activePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                // As now we are gonna work with different pointer id, if there is any
                activePointerId = invalidPointerId
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = getPointerIndex(event.action)
                val pointerId = event.getPointerId(pointerIndex)

                if (pointerId == activePointerId) {
                    // Active pointer was the one going up, so we have to check for new pointer
                    val newPointerIndex = if(pointerId == 0) 1 else 0
                    activePointerId = event.getPointerId(newPointerIndex)

                    // We fetch the new x and y from new pointer and update it for base gesture listener
                    lastTouchX = event.getX(newPointerIndex)
                    lastTouchY = event.getY(newPointerIndex)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getPointerIndex(action: Int): Int {
        return (action and MotionEvent.ACTION_POINTER_INDEX_MASK) shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
    }

    override fun getActiveX(event: MotionEvent): Float {
        return try {
            event.getX(activePointerIndex)
        } catch (exception: Exception) {
            event.x
        }
    }

    override fun getActiveY(event: MotionEvent): Float {
        return try {
            event.getY(activePointerIndex)
        } catch (exception: Exception) {
            event.y
        }
    }
}