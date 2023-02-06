package com.asp.imsgepickerplayground.ui.wf_approach

import android.net.Uri
import com.asp.imsgepickerplayground.ui.newapproach.PathType

data class URIMetaWF(
    val path: Uri,
    val fileName: String,
    val mimeType: String?,
    val extension: String?,
    val orientation: Int = 0
)