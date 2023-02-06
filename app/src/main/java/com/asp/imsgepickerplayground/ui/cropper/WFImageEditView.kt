package com.asp.imsgepickerplayground.ui.cropper

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatImageView
import com.asp.imsgepickerplayground.ui.Utils
import com.oginotihiro.cropview.RotateBitmap
import com.oginotihiro.cropview.scrollerproxy.ScrollerProxy
import java.io.InputStream
import java.lang.ref.WeakReference
import kotlin.math.*

class WFImageEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): AppCompatImageView(
    context, attrs, defStyleAttr
), ViewTreeObserver.OnGlobalLayoutListener, WFOnGestureListener {
    private val boxPaint = Paint()
    private val shadowPaint = Paint()
    private val boxBorderColor = Color.WHITE
    private val boxBorderWidth = 2F

    private var minScale = 1F
    private val midScale = 3F
    private val maxScale = 6F

    private val path = Path()
    private var boxRect = RectF()
    private var displayRect = RectF()
    private var viewDrawingRect = Rect()
    private var rotateBitmap: RotateBitmap? = null
    private var uri: Uri? = null
    private var filePath: String? = null

    private var baseMatrix = Matrix()
    private var suppMatrix = Matrix()
    private var drawMatrix = Matrix()

    private var aspectX = 1
    private var aspectY = 1
    private var outputX: Int? = null
    private var outputY: Int? = null
    private var orientation: Int = 0

    private var viewTop = -1
    private var viewBottom = -1
    private var viewLeft = -1
    private var viewRight = -1

    private val matrixValues = FloatArray(9)

//    private var gestureDetector: GestureDetector? = null
    private var dragDetector: DragPointerDetector? = null
    private var scaleDetector: ScaleDetector? = null
    private var flingRunner: FlingRunnable? = null

    var sampleSize: Int = 0

    init {
        scaleType = ScaleType.MATRIX

        initializeDragDetector()
        initializeScaleDetector()

//        initializeDoubleTapGesture()

        initializeBoxPaint()

        initializeShadowPaint()

        viewTreeObserver?.let {
            it.addOnGlobalLayoutListener(this)
        }
    }

//    private fun initializeDoubleTapGesture() {
//        gestureDetector = GestureDetector(context, GestureDetector.SimpleOnGestureListener())
//        gestureDetector?.setOnDoubleTapListener(DoubleTapGestureListener())
//    }

    private fun initializeDragDetector() {
        dragDetector = DragPointerDetector(context, this)
    }

    private fun initializeScaleDetector() {
        scaleDetector = ScaleDetector(context, this)
    }

    fun setCropMeta(meta: ImageEditMeta) {
        // Initializing variables
        this.aspectX = meta.aspectX
        this.aspectY = meta.aspectY
        this.outputX = meta.outputX
        this.outputY = meta.outputY
        this.uri = meta.uri
//        this.filePath = meta.filePath
        this.orientation = meta.orientation
    }

    fun initialize() {

        // Flush previous data
        viewTop = -1
        viewBottom = -1
        viewLeft = -1
        viewRight = -1

        uri?.let {
            val sampleSize = Utils.getBitmapSampleSize(context, it)

            this.sampleSize = sampleSize

            var inputStream: InputStream? = null

            inputStream = context.contentResolver.openInputStream(it)

            val option = BitmapFactory.Options()
            option.inSampleSize = sampleSize

            val bitmap = RotateBitmap(BitmapFactory.decodeStream(inputStream, null, option), orientation)

            // Updating orientation
            bitmap?.let {
                // Setting bitmap
                setBitmap(it)
            }

            this.rotateBitmap = bitmap

        } ?: Log.i("WFIEV-Init", "URI is null")
    }

    private fun setBitmap(rotateBitmap: RotateBitmap) {
        val oldBitmap = this.rotateBitmap?.bitmap

        setImageBitmap(rotateBitmap.bitmap)

        oldBitmap?.recycle()

        modifyBaseMatrix()
    }

    private fun initializeBoxPaint() {
        boxPaint.isAntiAlias = true
        boxPaint.color = boxBorderColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = Utils.dpToPx(context, boxBorderWidth) // TODO: convert to pixel from dp
    }

    private fun initializeShadowPaint() {
        shadowPaint.setARGB(125, 10, 10, 10) // TODO: Fluctuate
    }

//    private fun initializeBoxRect() {
//        boxRect = RectF((left + 50).toFloat(), (top +50).toFloat(), (right - 50).toFloat(), (bottom - 50).toFloat() )
//    }

    private fun modifyBaseMatrix() {
        rotateBitmap?.let {
            // Get area
            val width = width - paddingLeft - paddingRight
            val height = height - paddingTop - paddingBottom
            val bitmapWidth = it.width
            val bitmapHeight = it.height

            baseMatrix.reset()

            // Keeping max scale as 3x
            val widthScale = min((width / bitmapWidth.toFloat()), 3.toFloat())
            val heightScale = min((height / bitmapHeight.toFloat()), 3.toFloat())
            var scale = min(widthScale, heightScale)

            Log.i("WFIEV-ModifyMatrix", "scale: $scale")

            // Initially keeping the default scale to 1:1 and at least 4/5th of the minimum size of any side
            var cropBoxWidth = min(bitmapWidth, bitmapHeight) * 4 / 5 * scale
            var cropBoxHeight = cropBoxWidth

            // Updating the actual aspect ratio
            // At this point both cropBoxWidth and cropBoxHeight are equal
            if(aspectX != 0 && aspectY != 0) {
                if(aspectX > aspectY) {
                    cropBoxHeight = cropBoxWidth * aspectY / aspectX
                } else {
                    cropBoxWidth = cropBoxHeight * aspectX / aspectY
                }
            }

            // To handle panorama like images
            val zx = width  / cropBoxWidth * 0.6F
            val zy = height  / cropBoxHeight * 0.6F
            val zoom = min(zx, zy)

            if (zoom > 1F) {
                // Zoom in required, update scale to reflect the required zoom factor
                // We would update the scale which will help zoom in on the image
                scale *= zoom
                cropBoxWidth *= zoom
                cropBoxHeight *= zoom
            }

            val boxCenterX = (width - cropBoxWidth) / 2F
            val boxCenterY = (height - cropBoxHeight) / 2F

            // Update box Rect
            boxRect = RectF(boxCenterX, boxCenterY, boxCenterX + cropBoxWidth, boxCenterY + cropBoxHeight)

            // TODO: Rotate if required here, using the matrix
            rotateBitmap?.rotateMatrix?.let { rotation -> baseMatrix.postConcat(rotation) }

            baseMatrix.postScale(scale, scale)

            // Posting translation for the axis of final bitmap
            baseMatrix.postTranslate((width - bitmapWidth * scale)/2F, (height - bitmapHeight * scale)/2F)

            suppMatrix.reset()

            imageMatrix = getDrawMatrix()

            // Initialize image display Rect
            val displayRect = getDisplayRect(baseMatrix)

            displayRect?.let {
                // Calculating the minimum scale that is possible so that the the image does not go below the box's size
                val wScale = boxRect.width() / displayRect.width()
                val hScale = boxRect.height() / displayRect.height()

                minScale = max(wScale, hScale)
            }
        }
    }

    private fun getDrawMatrix(): Matrix {
        drawMatrix.set(baseMatrix)
        drawMatrix.postConcat(suppMatrix)
        return drawMatrix
    }

    private fun getDisplayRect(matrix: Matrix): RectF?{
        return drawable?.let {
            displayRect.set(0F, 0F, it.intrinsicWidth.toFloat(), it.intrinsicHeight.toFloat())
            Log.i("WFIEV-DisplayRect","${it.intrinsicWidth.toFloat()}, ${it.intrinsicHeight.toFloat()}")
            matrix.mapRect(displayRect)
            displayRect
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Prepare path
        path.reset()
        path.addRect(boxRect.left, boxRect.top, boxRect.right, boxRect.bottom, Path.Direction.CW)

//        if(canvas.isHardwareAccelerated) {
            getDrawingRect(viewDrawingRect)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                canvas.clipOutPath(path)
            } else {
                canvas.clipPath(path, android.graphics.Region.Op.DIFFERENCE)
            }
            canvas.drawRect(viewDrawingRect, shadowPaint)
//        }
    /* else {
            drawOutsideFallback(canvas)
        }*/

        canvas.drawPath(path, boxPaint)

    }

//    private fun drawOutsideFallback(canvas: Canvas) {
//        canvas.drawRect(0f, 0f, canvas.width.toFloat(), boxRect.top, shadowPaint)
//        canvas.drawRect(
//            0f,
//            boxRect.bottom,
//            canvas.width.toFloat(),
//            canvas.height.toFloat(),
//            shadowPaint
//        )
//        canvas.drawRect(0f, boxRect.top, boxRect.left, boxRect.bottom, shadowPaint)
//        canvas.drawRect(
//            boxRect.right,
//            boxRect.top,
//            canvas.width.toFloat(),
//            boxRect.bottom,
//            shadowPaint
//        )
//    }

    protected fun getScale(matrix: Matrix): Float {
        return sqrt(
            getValueFromMatrix(matrix, Matrix.MSCALE_X).pow(2) + getValueFromMatrix(matrix, Matrix.MSKEW_Y).pow(2)
        )
    }

    private fun getValueFromMatrix(matrix: Matrix, type: Int): Float {
        matrix.getValues(matrixValues)
        return matrixValues[type]
    }

    override fun onGlobalLayout() {
        if(left != viewLeft || top != viewTop || right != viewRight || bottom != viewBottom) {
            modifyBaseMatrix()

            viewLeft = left
            viewTop = top
            viewRight = right
            viewBottom = bottom
        }
    }

    private fun cleanup() {
        viewTreeObserver?.let {
            if(it.isAlive) {
                it.removeOnGlobalLayoutListener(this)
            }
        }

        setImageBitmap(null)

        rotateBitmap?.recycle()
    }

    override fun onDetachedFromWindow() {
        cleanup()
        super.onDetachedFromWindow()
    }

//    protected fun setScale(scale: Float, scaleX: Float, scaleY: Float, animate: Boolean) {
//        if(scale < minScale || scale > maxScale) return
//        if (animate) {
//            // TODO: Handle animation for double tap
//        } else {
//            suppMatrix.setScale(scale, scale, scaleX, scaleY)
//            updateMatrix()
//        }
//    }

    private fun updateMatrix() {
        if(checkMatrixBounds())
            imageMatrix = getDrawMatrix()
    }

    private fun checkMatrixBounds(): Boolean {
        return getDisplayRect(getDrawMatrix())?.let { rect ->

            val deltaY = when {
                rect.top >= boxRect.top -> boxRect.top - rect.top
                rect.bottom <= boxRect.bottom -> boxRect.bottom - rect.bottom
                else -> 0F
            }

            val deltaX = when {
                rect.left >= boxRect.left -> boxRect.left - rect.left
                rect.right <= boxRect.right -> boxRect.right - rect.right
                else -> 0F
            }

            suppMatrix.postTranslate(deltaX, deltaY)

            true
        } ?: false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var intercepted = false
        if(drawable != null && boxRect != null) {
            // We have the content and also the box is drawn
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    cancelFling()
                    // TODO: Cancel fling to stop the motion
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Do nothing for these
                }
            }

            // TODO: Push update to drag detector
            dragDetector?.let {
                intercepted = it.onTouchEvent(event)
            }

            scaleDetector?.let {
                intercepted = it.onTouchEvent(event)
            }

//            gestureDetector?.let {
//                it.onTouchEvent(event)
//                    intercepted = true
//            }
        }
        return intercepted
    }

    private fun cancelFling() {
        flingRunner?.cancelFling()
    }

//    inner class DoubleTapGestureListener: GestureDetector.OnDoubleTapListener {
//        override fun onSingleTapConfirmed(e: MotionEvent): Boolean  = false
//
//        override fun onDoubleTap(e: MotionEvent): Boolean {
//            try {
//                val scale = getScale(suppMatrix)
//                val x = e.x
//                val y = e.y
//
//                if(scale < midScale) {
//                    setScale(midScale, x, y, false)
//                } else if(scale >= midScale && scale < maxScale) {
//                    setScale(maxScale, x, y, false)
//                } else {
//                    setScale(minScale, x, y, false)
//                }
//
//            } catch (ex: ArrayIndexOutOfBoundsException) {
//                Log.i("WFIEV-DoubleTapGesture", ex.message.toString())
//            }
//            return true
//        }
//
//        override fun onDoubleTapEvent(e: MotionEvent): Boolean  = true
//    }

    override fun onScale(scaleFactor: Float, focusX: Float, focusY: Float) {
        val scale = getScale(suppMatrix)

        var updatedScaleFactor = scaleFactor

        if(updatedScaleFactor > 1) {
            val maxScaleFactor = maxScale/scale
            if(updatedScaleFactor >= maxScaleFactor) {
                updatedScaleFactor = maxScaleFactor
            }
        } else if (updatedScaleFactor < 1){
            val minScaleFactor = minScale/scale
            if(updatedScaleFactor <= minScaleFactor) {
                updatedScaleFactor = minScaleFactor
            }
            Log.i("WFIEV-onScale", "minScale: $minScale, scale: $scale, minScaleFactor: $minScaleFactor, updatedScaleFactor: $updatedScaleFactor")
        }

        suppMatrix.postScale(updatedScaleFactor, updatedScaleFactor, focusX, focusY)
        updateMatrix()
    }

    override fun onDrag(deltaX: Float, deltaY: Float) {
        // We do not want to consider scaling as dragging
        if(scaleDetector?.isScaling() == true) return

        suppMatrix.postTranslate(deltaX, deltaY)
        updateMatrix()
    }

    override fun onFling(lastX: Float, lastY: Float, velocityX: Float, velocityY: Float) {
        flingRunner = FlingRunnable(WeakReference(context), object: FlingChangeListener {
            override fun handleFling(dx: Int, dy: Int) {
                val rect = getDisplayRect(getDrawMatrix())
                rect?.let {
                    val startX = round(boxRect.left - rect.left).toInt()
                    val startY = round(boxRect.top - rect.top).toInt()

                    val endX = round(rect.width() - boxRect.width()).toInt()
                    val endY = round(rect.height() - boxRect.height()).toInt()

                    flingRunner?.fling(
                        velocityX.toInt(),
                        velocityY.toInt(),
                        startX,
                        startY,
                        endX,
                        endY
                    )

                    post(flingRunner)
                }
            }
        })
    }

    inner class FlingRunnable(private val context: WeakReference<Context>, private var listener: FlingChangeListener? = null): Runnable {
        private var scrollerProxy: ScrollerProxy? = null
        private var currentX = 0
        private var currentY = 0

        init {
            context.safeRun {
                scrollerProxy = ScrollerProxy.getScroller(it)
            }
        }

        fun fling(velocityX: Int, velocityY: Int, startX: Int, startY: Int, endX: Int, endY: Int) {
            val minX = 0
            val minY = 0

            currentX = startX
            currentY = startY

            scrollerProxy?.fling(startX, startY, velocityX, velocityY, minX, endX, minY, endY, 0, 0)
        }

        override fun run() {
            if(scrollerProxy?.isFinished == true ) return

            scrollerProxy?.let {
                if(it.computeScrollOffset()) {
                    val newY = it.currY
                    val newX = it.currX

                    listener?.handleFling(currentX - newX, currentY - newY)

                    currentX = newX
                    currentY = newY
                }
            }

            this@WFImageEditView.postOnAnimation(this)
        }

        fun cancelFling() {
            scrollerProxy?.forceFinished(true)
        }

        fun cleanup() {
            scrollerProxy = null
            listener = null
        }

        private fun WeakReference<Context>.safeRun(op: (context: Context) -> Unit) {
            get()?.let {
                op(it)
            }
        }
    }

    fun getOutput(): Bitmap? {
        if(boxRect == null || drawable == null) return null

        // Current display
        val drawMatrix = getDrawMatrix()
        val displayRect = getDisplayRect(drawMatrix)

        return displayRect?.let { displayRect ->
            val leftOffset = boxRect.left - displayRect.left
            val topOffset = boxRect.top - displayRect.top

            val scale = getScale(drawMatrix)

            val rect = Rect(
                (leftOffset / scale * sampleSize).toInt(),
                (topOffset / scale * sampleSize).toInt(),
                ((leftOffset + boxRect.width()) / scale * sampleSize).toInt(),
                ((topOffset + boxRect.height()) / scale * sampleSize).toInt(),
            )

            uri?.let { nUri ->
                    Utils.decodeBitmapRegionCrop(
                        context,
                        nUri,
                        rect,
                        outputX ?: 0,
                        outputY ?: 0,
                        orientation
                    )
            }
        }
    }
}