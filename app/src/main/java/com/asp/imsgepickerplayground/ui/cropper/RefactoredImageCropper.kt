package com.asp.imsgepickerplayground.ui.cropper

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatImageView
import com.asp.imsgepickerplayground.ui.Utils
import com.oginotihiro.cropview.RotateBitmap
import com.oginotihiro.cropview.scrollerproxy.ScrollerProxy
import java.lang.ref.WeakReference
import kotlin.math.*

class RefactoredImageCropper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): AppCompatImageView(
    context, attrs, defStyleAttr
), ViewTreeObserver.OnGlobalLayoutListener, WFOnGestureListener {
    // Cropper Box Paint
    private val boxPaint = Paint()
    // Box border color
    private val boxBorderColor = Color.WHITE
    // Box border width
    private val boxBorderWidth = 2F
    // Box path
    private val path = Path()

    // Rect representing box bounds
    private var boxRect = RectF()
    // Display rect
    private var displayRect = RectF()
    // View Drawing rect
    private var viewDrawingRect = Rect()

    // Base Matrix
    private var baseMatrix = Matrix()
    // Support matrix
    private var suppMatrix = Matrix()
    // Final draw matrix - Uses base matrix & supp matrix
    private var drawMatrix = Matrix()

    // Shadow Paint for around the box
    private val shadowPaint = Paint()
    // Shadow color
    private val shadowColor = Color.argb(125, 10, 10, 10)

    // Allowed possible scale range
    private var minScale = 1F
    private val midScale = 3F
    private val maxScale = 6F

    // Bitmap with rotation information
    private var rotateBitmap: RotateBitmap? = null
    // URI of the selected image
    private var uri: Uri? = null

    // Crop information
    private var aspectX = 1
    private var aspectY = 1
    private var outputX = 0 // Default - will be considered as no output x required
    private var outputY = 0 // Default - will be considered as no output y required
    private var rotation = 0

    // Matrix values to have the scale information
    private val matrixValues = FloatArray(9)

    // View bounds - Default to -1
    private var viewTop = -1
    private var viewBottom = -1
    private var viewLeft = -1
    private var viewRight = -1

    // Detects the drag movement
    private var dragDetector: DragPointerDetector? = null
    // Detects the scale movement
    private var scaleDetector: ScaleDetector? = null
    // Detects the fling movement
    private var flingRunner: FlingRunnable? = null

    // Calculated image sample size
    var sampleSize = 0

    init {
        scaleType = ScaleType.MATRIX

        initializeDragDetector()

        initializeScaleDetector()

        initializeBoxPaint()

        initializeShadowPaint()

        viewTreeObserver?.let {
            it.addOnGlobalLayoutListener(this)
        }
    }

    private fun initializeDragDetector() {
        dragDetector = DragPointerDetector(context, this)
    }

    private fun initializeScaleDetector() {
        scaleDetector = ScaleDetector(context, this)
    }

    private fun initializeBoxPaint() {
        boxPaint.isAntiAlias = true
        boxPaint.color = boxBorderColor
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = Utils.dpToPx(context, boxBorderWidth)
    }

    private fun initializeShadowPaint() {
        shadowPaint.color = shadowColor
    }

    fun setCropMeta(meta: ImageEditMeta) {
        this.aspectX = meta.aspectX
        this.aspectY = meta.aspectY
        this.outputX = meta.outputX
        this.outputY = meta.outputY
        this.uri = meta.uri
        this.rotation = meta.orientation
    }

    fun initialize() {
        resetViewBounds()

        uri?.let {
            val sampleSize = Utils.getBitmapSampleSize(context, it)

            this.sampleSize = sampleSize

            val rotateBitmap = Utils.createRotateBitmap(context, it, sampleSize, rotation)

            rotateBitmap?.let { b ->
                setBitmap(b)

                this.rotateBitmap = b
            }
        }
    }

    private fun resetViewBounds() {
        viewTop = -1
        viewBottom = -1
        viewLeft = -1
        viewRight = -1
    }

    private fun setBitmap(rotateBitmap: RotateBitmap) {
        val oldBitmap = this.rotateBitmap?.bitmap

        setImageBitmap(rotateBitmap.bitmap)

        oldBitmap?.recycle()

        modifyBaseMatrix()
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

    private fun modifyBaseMatrix() {
        rotateBitmap?.let {
            // View max allowed Width and Height
            val width = width - paddingLeft - paddingRight
            val height = height - paddingTop - paddingBottom

            // Rotated bitmap height and width after consideration of rotation
            val bitmapWidth = it.width
            val bitmapHeight = it.height

            baseMatrix.reset()

            // At any given instance, Bitmap's width and height can vary based on the scaling
            // We calculate the scaling difference between the device and bitmap dimensions
            val widthScale = min((width / bitmapWidth.toFloat()), 3.toFloat())
            val heightScale = min((height / bitmapHeight.toFloat()), 3.toFloat())

            var scale = min(widthScale, heightScale)

            // Initially keeping the default scale to 1:1 and at least 4/5th of the minimum size of any side
            // Also, considering the scale so crop box is in proper image bounds
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

            // Calculating box's left and top coordinates
            val boxLeft = (width - cropBoxWidth) / 2F
            val boxTop = (height - cropBoxHeight) / 2F

            // Update box Rect
            boxRect = RectF(boxLeft, boxTop, boxLeft + cropBoxWidth, boxTop + cropBoxHeight)

            // Rotate bitmap
            rotateBitmap?.rotateMatrix?.let { rotation -> baseMatrix.postConcat(rotation) }

            // Scale
            baseMatrix.postScale(scale, scale)

            // Calculating and translating the top and left of the bitmap with scale consideration
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
            // Applies the matrix to this Rect and writes the transformed rect back into rect
            matrix.mapRect(displayRect)
            displayRect
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        path.reset()
        path.addRect(boxRect.left, boxRect.top, boxRect.right, boxRect.bottom, Path.Direction.CW)

        // This updates the drawing rect with matrix
        getDrawingRect(viewDrawingRect)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipOutPath(path)
        } else {
            canvas.clipPath(path, android.graphics.Region.Op.DIFFERENCE)
        }

        // Draw shadow in the clipped out area of box rect
        canvas.drawRect(viewDrawingRect, shadowPaint)

        canvas.drawPath(path, boxPaint)
    }

    /**
     * Provides the current scaling of the matrix
     */
    private fun getScale(matrix: Matrix): Float {
        return sqrt(
            getValueFromMatrix(matrix, Matrix.MSCALE_X).pow(2) + getValueFromMatrix(matrix, Matrix.MSKEW_Y).pow(2)
        )
    }

    private fun getValueFromMatrix(matrix: Matrix, type: Int): Float {
        matrix.getValues(matrixValues)
        return matrixValues[type]
    }

    /**
     * Checks bounds nad then updates image matrix
     */
    private fun updateMatrix() {
        if(checkMatrixBounds())
            imageMatrix = getDrawMatrix()
    }

    /**
     * Checks the image bounds with box bounds, If the view draw bound is being less (coming under the box bounds)
     * than it calculated the deltaX and deltaY then posts the translation for that
     */
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

            dragDetector?.let {
                intercepted = it.onTouchEvent(event)
            }

            scaleDetector?.let {
                intercepted = it.onTouchEvent(event)
            }
        }

        return intercepted
    }

    /**
     * Called when user pinch scale the bitmap
     */
    override fun onScale(scaleFactor: Float, focusX: Float, focusY: Float) {
        val scale = getScale(suppMatrix)

        var updatedScaleFactor = scaleFactor

        // Check if the updated scale factor is more than max or less than min scale factor.
        // if it is then update the max or min scale factor on support matrix to have the scale under bounds
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
        }

        suppMatrix.postScale(updatedScaleFactor, updatedScaleFactor, focusX, focusY)

        updateMatrix()
    }

    /**
     * Called when user drags the image
     */
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
                    // Compute the max and min fling allowed, This is to make sure that image does
                    // not go beyond the box bounds
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

    private fun cancelFling() {
        flingRunner?.cancelFling()
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

            this@RefactoredImageCropper.postOnAnimation(this)
        }

        fun cancelFling() {
            scrollerProxy?.forceFinished(true)
        }

        fun cleanup() {
            cancelFling()
            scrollerProxy = null
            listener = null
        }

        private fun WeakReference<Context>.safeRun(op: (context: Context) -> Unit) {
            get()?.let {
                op(it)
            }
        }
    }

    override fun onDetachedFromWindow() {
        cleanup()
        super.onDetachedFromWindow()
    }

    private fun cleanup() {
        viewTreeObserver?.let {
            if(it.isAlive) {
                it.removeOnGlobalLayoutListener(this)
            }
        }

        flingRunner?.cleanup()

        setImageBitmap(null)

        rotateBitmap?.recycle()
    }

    fun getOutput(): Bitmap? {
        if(boxRect == null || drawable == null) return null

        // Current Display
        val drawMatrix = getDrawMatrix()
        val displayRect = getDisplayRect(drawMatrix)

        return displayRect?.let { displayRect ->
            val leftOffset = boxRect.left - displayRect.left
            val topOffset = boxRect.top - displayRect.top

            Log.i("Cropper", "BoxRectLeft: " + boxRect.left)
            Log.i("Cropper", "BoxRectTop: " + boxRect.top)
            Log.i("Cropper", "DisplayRectLeft: " + displayRect.left)
            Log.i("Cropper", "DisplayRectTop: " + displayRect.top)

            val scale = getScale(drawMatrix)

            Log.i("Cropper", "Scale: $scale")
            Log.i("Cropper", "Sample: $sampleSize")

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
                    outputX,
                    outputY,
                    rotation
                )
            }
        }
    }
}