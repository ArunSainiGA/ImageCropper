package com.asp.imsgepickerplayground.ui.newapproach

import android.graphics.Bitmap


data class ImageFileMeta (
    val bitmap: Bitmap,
    val completeName: String,
    val mimeType: String?,
    val extension: String?,
    val sizeInBytes: Int = bitmap.byteCount
) {
    private val kb = 1024
    private val mb = kb * kb
    private val gb = kb * mb
    private val labelKB = "KB"
    private val labelMB = "MB"
    private val labelGB = "GB"

    fun getSizeUIRepresentation() =
        when {
            sizeInBytes/kb > 0 -> (sizeInBytes/kb).toString() + " $labelKB"
            sizeInBytes/mb > 0 -> (sizeInBytes/mb).toString() + " $labelMB"
            sizeInBytes/gb > 0 -> (sizeInBytes/gb).toString() + " $labelGB"
            else -> sizeInBytes.toString() + "bytes"
        }

//    fun getFileExtension(): String? {
//        return kotlin.runCatching {
//            val index = completeName.lastIndexOf(".")
//            if(index == -1) return null
//            return completeName.substring(index + 1)
//        }.onFailure {
//                // TODO: Log exception
//        }.getOrNull()
//    }
//
//    fun getFileNameWithoutExtension(): String? {
//        return kotlin.runCatching {
//            val index = completeName.lastIndexOf(".")
//            val lastPathSeparator = completeName.lastIndexOf("/")
//            if(index == -1) return null
//            return if(lastPathSeparator == -1)
//                completeName.substring(0, index)
//            else
//                completeName.substring(lastPathSeparator + 1, index)
//        }.onFailure {
//            // TODO: Log exception
//        }.getOrNull()
//    }
}