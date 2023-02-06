package com.asp.imsgepickerplayground.ui.newapproach

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.asp.imsgepickerplayground.ui.wf_approach.BitmapProcessorWF
import com.asp.imsgepickerplayground.ui.wf_approach.ImageFileMetaWF

class GalleryImagePickerManager(
    private val uriResolver: URIResolverNewApproach,
    private val bitmapProcessor: BitmapProcessorWF
) {

//    private fun getFilePath(context: Context, uri: Uri, listener: (URIMeta?) -> Unit) {
//        uriResolver.resolve(context, uri, listener)
//    }
//
//    private fun scaleBitmap(path: String, width: Int, height: Int): Bitmap? {
//        return bitmapProcessor.hardScaleBitmap(path, height, width)
//    }
//
//    fun getScaledBitmap(context: Context, uri: Uri, width: Int, height: Int, listener: (ImageFileMetaWF?) -> Unit) {
//        getFilePath(context, uri) { meta ->
//            meta?.let {
//                scaleBitmap(meta.path, width, height)?.let {
//                    listener(
//                        ImageFileMetaWF(
//                        it,
//                        meta.fileName,
//                        meta.mimeType,
//                        meta.extension
//                    )
//                    )
//                } ?: listener(null)
//            } ?: listener(null)
//        }
//    }
}