package com.asp.imsgepickerplayground.ui.wf_approach

import android.graphics.Bitmap


data class ImageFileMetaWF (
    val bitmap: Bitmap,
    val completeName: String,
    val mimeType: String?,
    val extension: String?,
    val sizeInBytes: Int = bitmap.byteCount,
    val filePath: String? = null,
    val orientation: Int = 0
)