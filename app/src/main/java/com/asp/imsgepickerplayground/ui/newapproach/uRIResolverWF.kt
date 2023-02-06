package com.asp.imsgepickerplayground.ui.newapproach

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap

class URIResolverNewApproach {
    private val schemeContent = "content"

    fun resolve(context: Context, uri: Uri, listener: URIResolutionCallback) {
        when {
            isContentScheme(uri) -> {
                listener(getFilePathFromContentScheme(context, uri))
            }
            else -> null
        }
    }

    private fun getFilePathFromContentScheme(context: Context, uri: Uri): URIMeta? {
        return getFilePathFromContentProvider(context, uri)?.let {
            it
        } ?: readPathFromDisplayNameOpenableColumn(
            context,
            uri
        )
    }

    private fun readPathFromDisplayNameOpenableColumn(
        context: Context,
        uri: Uri
    ): URIMeta? {
        return runCatching<URIMeta> {
            val mime = context.contentResolver.getType(uri)
            val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
            Log.d("URIResolver", mime.toString() + "     " + ext)
            val dataColumn = OpenableColumns.DISPLAY_NAME
            return readFromContentResolver(context, uri, arrayOf(dataColumn))
                ?.let { cursor ->
                    cursor.executeOnCursor {
                        val columnIndex = this.getColumnIndexOrThrow(dataColumn)
                        buildUriMeta(
                            context, uri, uri.toString(), getString(columnIndex)
                        )
                    }
                }
        }.onFailure {
            //TODO: Log exception
        }.getOrNull()
    }

    private fun getFilePathFromContentProvider(
        context: Context,
        uri: Uri,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        order: String? = null
    ) =
        runCatching<URIMeta?> {
            val dataColumn = MediaStore.Images.Media.DATA
            val nameColumn = OpenableColumns.DISPLAY_NAME
            return readFromContentResolver(
                context,
                uri,
                arrayOf(dataColumn, nameColumn),
                selection,
                selectionArgs,
                order
            )
                ?.let { cursor ->
                    cursor.executeOnCursor {
                        val dataIndex = this.getColumnIndexOrThrow(dataColumn)
                        val nameColumnIndex = this.getColumnIndexOrThrow(nameColumn)
                        if (this.moveToFirst()) {
                            buildUriMeta(
                                context, uri, this.getString(dataIndex), this.getString(nameColumnIndex)
                            )
                        }
                        else null
                    }
                }
        }.onFailure {
            // TODO: Add proper logging exception
        }.getOrNull()

    private fun readFromContentResolver(
        context: Context,
        uri: Uri,
        projections: Array<String>,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        order: String? = null
    ) =
        context.contentResolver.query(
            uri, projections, selection, selectionArgs, order
        )

    private fun isContentScheme(uri: Uri) = schemeContent.equals(uri.scheme, true)

    private fun <T> Cursor.executeOnCursor(operation: Cursor.() -> T): T {
        val response = operation.invoke(this)
        this.close()
        return response
    }

    private fun getFileMimeType(context: Context, uri: Uri): String? {
        return runCatching<String?> {
            return context.contentResolver.getType(uri)
        }.onFailure {
            // TODO: Log exception
        }.getOrNull()
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        return runCatching<String?> {
            return getFileMimeType(context, uri)?.let {
                MimeTypeMap.getSingleton().getExtensionFromMimeType(it)
            }
        }.onFailure {
            // TODO: Log exception
        }.getOrNull()
    }

    private fun buildUriMeta(context: Context, uri: Uri, path: String, fileName: String) = URIMeta(
        path, fileName, getFileMimeType(context, uri), getFileExtension(context, uri)
    )
}

typealias URIResolutionCallback = (meta: URIMeta?) -> Unit