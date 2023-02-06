package com.asp.imsgepickerplayground.ui.wf_approach

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Size
import com.asp.imsgepickerplayground.ui.util.FileUtil
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class GalleryImagePickerManagerWF(
    private val uriResolver: URIResolverWF,
    private val bitmapProcessor: BitmapProcessorWF
) {

    /**
     * Fetches the [URIMeta] with the help of [URIResolver]
     */
    fun getFilePath(context: Context, uri: Uri, listener: (URIMetaWF?) -> Unit) {
        uriResolver.resolve(context.applicationContext, uri, listener)
    }

    /**
     * Scales the bitmap to a given resolution
     */
    fun scaleBitmap(context: Context, uriMeta: URIMetaWF, size: Size): Bitmap? {
        return bitmapProcessor.hardScaleBitmap(context.applicationContext, uriMeta, size)
    }

    /**
     * Fetches the URI details and Scales bitmap to a given resolution
     */
    fun getScaledBitmap(context: Context, uri: Uri, size: Size, listener: (ImageFileMetaWF?) -> Unit) {
        getFilePath(context, uri) { meta ->
            meta?.let {
                scaleBitmap(context, meta, size)?.let {

                    val updatedBitmap = if(meta.orientation != 0) {
                        Bitmap.createBitmap(it, 0, 0, it.width, it.height, Matrix().apply {
                            postRotate(meta.orientation.toFloat())
                        }, true)
                    } else {
                        it
                    }

                    val outputStream = ByteArrayOutputStream()
                    updatedBitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        100, outputStream)
                    val file = File( context.cacheDir.path + "/" + meta?.fileName)
                    val fos = FileOutputStream(file).apply {
                        write(bitmapProcessor.bitmapToByteArray(updatedBitmap))
                    }
                    fos.flush()
                    fos.close()

                    listener(
                        ImageFileMetaWF(
                            bitmap = it,
                            completeName = meta.fileName,
                            mimeType = meta.mimeType,
                            extension = meta.extension,
                            filePath = file.absolutePath,
                            orientation = meta.orientation
                        )
                    )
                } ?: listener(null)
            } ?: listener(null)
        }
    }
}