package com.asp.imsgepickerplayground.ui.newapproach


data class URIMeta(
    val path: String,
    val fileName: String,
    val mimeType: String?,
    val extension: String?
){
    private val contentTypePath = "content"

    fun getPathType() = if (path.startsWith(contentTypePath))
        PathType.CONTENT
    else
        PathType.FILE
}

enum class PathType {
    CONTENT, FILE
}