package com.asp.imsgepickerplayground.ui.cropper

import android.content.Context
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

open class DragGestureDetector(private val context: Context, private val gestureListener: WFOnGestureListener): WFGestureDetector {
    private val configuration: ViewConfiguration = ViewConfiguration.get(context)
    private var touchSlop: Float = configuration.scaledTouchSlop.toFloat()
    private var minVelocity: Float = configuration.scaledMinimumFlingVelocity.toFloat()

    private var vTracker: VelocityTracker? = null

    protected var lastTouchX: Float = 0F
    protected var lastTouchY: Float = 0F
    private var isDragging: Boolean = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                vTracker = VelocityTracker.obtain()

                updateVTrackerMovement(event)

                updateLastTouch(event.x, event.y)
                isDragging = false
            }

            MotionEvent.ACTION_MOVE -> {
                val currentX = event.x
                val currentY = event.y
                val deltaX = currentX - lastTouchX
                val deltaY = currentY - lastTouchY

                if(isDragging.not()) {
                    isDragging = sqrt((deltaX * deltaX) + (deltaY *deltaY)) >= touchSlop
                }

                if(isDragging) {
                    gestureListener.onDrag(deltaX, deltaY)

                    // Keep on updating last touch indices
                    updateLastTouch(currentX, currentY)
                    updateVTrackerMovement(event)
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                recyclerVTracker()
            }

            MotionEvent.ACTION_UP -> {
                if(isDragging) {
                    vTracker?.let {
                        updateLastTouch(event.x, event.y)
                        updateVTrackerMovement(event)

                        it.computeCurrentVelocity(1000)

                        val xVelocity = it.xVelocity
                        val yVelocity = it.yVelocity

                        if(max(abs(xVelocity), abs(yVelocity)) >= minVelocity) {
                            // Velocity is more than min required, consider as fling
                            gestureListener.onFling(lastTouchX, lastTouchY, -xVelocity, -yVelocity)
                        }
                    }
                }

                updateVTrackerMovement(event)
                vTracker?.recycle()
                vTracker = null
            }
        }
        return true
    }

    private fun recyclerVTracker() {
        vTracker?.let {
            it.recycle()
        }
        vTracker = null
    }

    private fun updateLastTouch(x: Float, y: Float) {
        lastTouchX = x
        lastTouchY = y
    }

    private fun updateVTrackerMovement(event: MotionEvent) {
        vTracker?.let {
            it.addMovement(event)
        }
    }

    open fun getActiveX(event: MotionEvent) = event.x

    open fun getActiveY(event: MotionEvent) = event.y
}