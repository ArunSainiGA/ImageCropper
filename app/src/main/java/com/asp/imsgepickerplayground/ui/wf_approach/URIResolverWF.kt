package com.asp.imsgepickerplayground.ui.wf_approach

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import com.asp.imsgepickerplayground.ui.newapproach.URIResolutionCallback

class URIResolverWF {
    private val schemeContent = "content"

    /**
     * Resolves the Uri and returns the [URIMeta] as part of the result callback
     * [URIResolutionCallback]
     * @param context Application context
     * @param uri is the URI received from result launcher
     * @param listener is the callback that provides the [URIMeta] back to caller
     */
    fun resolve(context: Context, uri: Uri, listener: URIResolutionCallbackWF) {
        when {
            isContentScheme(uri) -> {
                listener(getFilePathFromContentScheme(context, uri))
            }
            else -> listener(null)
        }
    }

    /**
     * Fetches the file path from content provider
     */
    private fun getFilePathFromContentScheme(context: Context, uri: Uri): URIMetaWF? {
        return getFilePathFromContentProvider(context, uri)
    }

    /**
     * Gets file path from the content provider
     * It uses the data and name column as projection and rest of the query
     * params can be defined by the caller
     */
    private fun getFilePathFromContentProvider(
        context: Context,
        uri: Uri,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        order: String? = null
    ) =
        runCatching<URIMetaWF?> {
            val idColumn = MediaStore.Images.Media._ID
            val nameColumn = OpenableColumns.DISPLAY_NAME
            val externalMediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            return readFromContentResolver(
                context,
                uri,
                arrayOf(idColumn, nameColumn),
                selection,
                selectionArgs,
                order
            )
                ?.let { cursor ->
                    cursor.executeOnCursor {
                        val idIndex = this.getColumnIndexOrThrow(idColumn)
                        val nameColumnIndex = this.getColumnIndexOrThrow(nameColumn)

                        if (this.moveToNext()) {
                            val resultantUri = ContentUris.withAppendedId(externalMediaUri, this.getLong(idIndex))
                            buildUriMeta(
                                context,
                                uri,
                                resultantUri,
                                this.getString(nameColumnIndex),
                                getOrientation(context, resultantUri) ?: 0
                            )
                        } else null
                    }
                }
        }.onFailure {
//            Timber.e(it, "Could not read the data from Uri: $uri")
        }.getOrNull()

    private fun getOrientation(context: Context, uri: Uri): Int? =
        runCatching<Int> {
            val orientationColumn = MediaStore.Images.ImageColumns.ORIENTATION
            return readFromContentResolver(
                context,
                uri,
                arrayOf(orientationColumn),
                null,
                null,
                null
            )
                ?.let { cursor ->
                    cursor.executeOnCursor {
                        val orientationColumnIndex = this.getColumnIndex(orientationColumn)

                        var orientation = 0

                        if (this.moveToNext()) {
                            orientation = try {
                                this.getInt(orientationColumnIndex)
                            } catch (ex: Exception) {
                                // TODO: Log exception
                                0
                            }
                        }
                        orientation
                    }
                }
        }.onFailure {
//            Timber.e(it, "Could not read the data from Uri: $uri")
        }.getOrNull()

    /**
     * Queries the content resolver for the provided query params
     */
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

    /**
     * Checks if the URI has a content scheme
     */
    private fun isContentScheme(uri: Uri) = schemeContent.equals(uri.scheme, true)

    /**
     * Executes some operation on Cursor and returns the result after closing the cursor
     */
    private fun <T> Cursor.executeOnCursor(operation: Cursor.() -> T): T {
        val response = operation.invoke(this)
        this.close()
        return response
    }

    /**
     * Returns the MIME type from the URI
     */
    private fun getFileMimeType(context: Context, uri: Uri): String? {
        return runCatching<String?> {
            return context.contentResolver.getType(uri)
        }.onFailure {
//            Timber.e(it, "Could not fetch the file mime type for $uri")
        }.getOrNull()
    }

    /**
     * Returns file extension from the URI
     */
    private fun getFileExtension(context: Context, uri: Uri): String? {
        return runCatching<String?> {
            return getFileMimeType(context, uri)?.let {
                MimeTypeMap.getSingleton().getExtensionFromMimeType(it)
            }
        }.onFailure {
//            Timber.e(it, "Could not fetch the file extension for $uri")
        }.getOrNull()
    }

    /**
     * Builds the [URIMeta] from the given information
     */
    private fun buildUriMeta(context: Context, uri: Uri, path: Uri, fileName: String, orientation: Int) = URIMetaWF(
        path, fileName, getFileMimeType(context, uri), getFileExtension(context, uri), orientation
    )
}

typealias URIResolutionCallbackWF = (meta: URIMetaWF?) -> Unit