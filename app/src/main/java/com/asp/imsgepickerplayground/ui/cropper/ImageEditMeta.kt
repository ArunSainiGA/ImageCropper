package com.asp.imsgepickerplayground.ui.cropper

import android.graphics.Bitmap
import android.net.Uri

data class ImageEditMeta (
    val bitmap: Bitmap, // TODO: Temporary. must be removed
    val uri: Uri? = null,
    val outputX: Int = 0,
    val outputY: Int = 0,
    val aspectX: Int = 1,
    val aspectY: Int = 1,
    val orientation: Int = 0,
//    val filePath: String? = null
)