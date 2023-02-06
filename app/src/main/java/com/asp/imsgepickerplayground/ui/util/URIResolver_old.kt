//package com.asp.imsgepickerplayground.ui.util
//
//import android.content.ContentUris
//import android.content.Context
//import android.database.Cursor
//import android.net.Uri
//import android.os.Build
//import android.os.Environment
//import android.provider.DocumentsContract
//import android.provider.MediaStore
//import android.provider.OpenableColumns
//import android.util.Log
//import java.io.File
//
//class URIResolver_old(private val fileUtil: FileUtil) {
//    private val contentScheme = "content"
//    private val primaryTypeStorage = "primary"
//    private val secondaryStorage = "SECONDARY_STORAGE"
//    private val externalStorage = "EXTERNAL_STORAGE"
//    private val downloadDirectory = "Download"
//    private val prefixRaw = "raw:"
//    private val prefixRawRegEx = "^/raw:"
//    private val prefixDocumentRawRegEx = "^/document/raw:"
//
//    private val contentURIPrefixPublicDownloads = "content://downloads/public_downloads"
//    private val contentURIPrefixMyDownloads = "content://downloads/my_downloads"
//
//    private val mediaTypeImage = "image"
//    private val mediaTypeVideo = "video"
//    private val mediaTypeAudio = "audio"
//
//    private val schemeFile = "file"
//    private val schemeContent = "content"
//
//    fun getPath(context: Context, uri: Uri): String? {
//        return when (isAtLeastKitkat()) {
//            true -> getPathAfterKitkat(context, uri)
//            false -> getPathBeforeKitkat(context, uri)
//        }
//    }
//
//    private fun getPathAfterKitkat(context: Context, uri: Uri): String? {
//        Log.d("Test", context.contentResolver.openInputStream(uri).toString())
//        return when {
//            isExternalStorageDocument(uri) -> {
//                getPathFromExternalStorage(context, uri)
//            }
//            isDownloadsDocument(uri) -> {
//                getPathFromDownloadDocuments(context, uri)
//            }
//            isMediaDocument(uri) -> {
//                getPathFromMediaDocuments(context, uri)
//            }
//            isGoogleDriveUri(uri) -> {
//                getPathForDriveDocuments(context, uri)
//            }
//            isWhatsAppFile(uri) -> {
//                getPathForWhatsApp(context, uri)
//            }
//            isGooglePhotosUri(uri) -> {
//                getFilePathForGooglePhotos(context, uri)
//            }
//            isContentScheme(uri) -> {
//                getFilePathFromContentScheme(context, uri)
//            }
//            isFileScheme(uri) -> {
//                getPathFromFileURI(uri)
//            }
//            else -> null
//        }
//    }
//
//    /**
//     * Before kitkat, file picker was simpler and did not include the
//     * image picking from different provides directly.
//     */
//    private fun getPathBeforeKitkat(context: Context, uri: Uri): String? =
//        when (isWhatsAppFile(uri)) {
//            true -> getPathForWhatsApp(context, uri)
//            else -> {
//                when (uri.scheme) {
//                    contentScheme -> getFilePathFromContentProvider(context, uri)
//                    else -> null
//                }
//            }
//        }
//
//    private fun getPathFromExternalStorage(context: Context, uri: Uri): String? {
//        return DocumentsContract.getDocumentId(uri).split(":").let { split ->
//            val type = split[0]
//            val relativePath = FileUtil.PATH_SEPARATOR + split[1]
//
//            return when (type) {
//                primaryTypeStorage -> {
//                    val possiblePath =
//                        Environment.getExternalStorageDirectory().path.toString() + relativePath
//                    if (File(possiblePath).exists())
//                        possiblePath
//                    else null
//                }
//                else -> {
//                    var possiblePath = System.getenv(secondaryStorage) + relativePath
//                    if (File(possiblePath).exists())
//                        return possiblePath
//
//                    possiblePath = System.getenv(externalStorage) + relativePath
//                    if (File(possiblePath).exists())
//                        return possiblePath
//
//                    possiblePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path.toString() + relativePath
//                    if (File(possiblePath).exists())
//                        return possiblePath
//
//                    null
//                }
//            }
//        }
//    }
//
//    private fun getPathFromDownloadDocuments(context: Context, uri: Uri): String? {
//        return if (isAtLeastM()) {
//            val dataColumn = MediaStore.MediaColumns.DISPLAY_NAME
//            readFromContentResolver(context, uri, arrayOf(dataColumn), null, null, null)
//                ?.let { cursor ->
//
//                    cursor.executeOnCursor {
//                        val columnIndex = this.getColumnIndexOrThrow(dataColumn)
//                        if (this.moveToFirst()) {
//                            val fileName = this.getString(columnIndex)
//                            Environment.getExternalStorageDirectory().path.toString() + FileUtil.PATH_SEPARATOR + downloadDirectory + FileUtil.PATH_SEPARATOR + fileName
//                        } else {
//                            // TODO: Add proper logging exception
//                            null
//                        }
//                    }
//
//                }
//                ?: run {
//                    val id = DocumentsContract.getDocumentId(uri)
//
//                    if (id.isNullOrBlank().not()) {
//                        return when {
//                            id.startsWith(prefixRaw) -> id.replaceFirst(prefixRaw, "")
//                            else -> {
//                                try {
//                                    val contentUriPublicDownloads = ContentUris.withAppendedId(
//                                        Uri.parse(contentURIPrefixPublicDownloads), id.toLong()
//                                    )
//                                    val contentUriMyDownloads = ContentUris.withAppendedId(
//                                        Uri.parse(contentURIPrefixMyDownloads), id.toLong()
//                                    )
//                                    getFilePathFromContentProvider(
//                                        context,
//                                        contentUriPublicDownloads
//                                    ) ?: getFilePathFromContentProvider(
//                                        context,
//                                        contentUriMyDownloads
//                                    )
//                                } catch (e: NumberFormatException) {
//                                    uri.path?.replaceFirst(prefixDocumentRawRegEx, "")
//                                        ?.replaceFirst(prefixRawRegEx, "")
//                                }
//                            }
//                        }
//                    } else null
//                }
//        } else {
//            val id = DocumentsContract.getDocumentId(uri)
//
//            if (id.isNullOrBlank().not()) {
//                return when {
//                    id.startsWith(prefixRaw) -> id.replaceFirst(prefixRaw, "")
//                    else -> {
//                        try {
//                            val contentUriPublicDownloads = ContentUris.withAppendedId(
//                                Uri.parse(contentURIPrefixPublicDownloads),
//                                id.toLong()
//                            )
//                            getFilePathFromContentProvider(context, contentUriPublicDownloads)
//                        } catch (e: NumberFormatException) {
//                            // TODO: Add proper logging exception
//                            null
//                        }
//                    }
//                }
//            } else null
//        }
//    }
//
//    private fun getPathFromMediaDocuments(context: Context, uri: Uri): String? {
//        return DocumentsContract.getDocumentId(uri).split(":").let { split ->
//            val type = split[0]
//            val id = split[1]
//            when (type) {
//                mediaTypeImage -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                mediaTypeVideo -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//                mediaTypeAudio -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//                else -> null
//            }?.let { mediaContentProviderURI ->
//                getFilePathFromContentProvider(
//                    context, mediaContentProviderURI, "_id=?", arrayOf(id), null
//                )
//            }
//        }
//    }
//
////    private fun getPathForWhatsApp(context: Context, uri: Uri): String? {
////        return readPathFromDisplayNameOpenableColumn(context, uri, FileUtil.DIRECTORY_WHATS_APP)
////    }
////
////    private fun getPathForDriveDocuments(context: Context, uri: Uri): String? {
////        return readPathFromDisplayNameOpenableColumn(context, uri, FileUtil.DIRECTORY_GOOGLE_DRIVE)
////    }
//
//    private fun getFilePathFromContentScheme(context: Context, uri: Uri): String? {
//        return getFilePathFromContentProvider(context, uri) ?: readPathFromDisplayNameOpenableColumn(
//            context,
//            uri,
//            FileUtil.DIRECTORY_USER_FILES
//        )
//    }
//
//    private fun getFilePathForGooglePhotos(context: Context, uri: Uri): String? {
//        return readPathFromDisplayNameOpenableColumn(
//            context,
//            uri,
//            FileUtil.DIRECTORY_USER_FILES
//        )
//    }
//
//    private fun readPathFromDisplayNameOpenableColumn(
//        context: Context,
//        uri: Uri,
//        directory: String
//    ): String? {
//        uri
//        return try {
//            val dataColumn = OpenableColumns.DISPLAY_NAME
//            readFromContentResolver(context, uri, arrayOf(dataColumn))
//                ?.let { cursor ->
//                    cursor.executeOnCursor {
//                        val columnIndex = this.getColumnIndexOrThrow(dataColumn)
//                        if (this.moveToFirst()) {
//                            val name = this.getString(columnIndex)
//                            context.contentResolver.openInputStream(uri)?.let { ios ->
//                                fileUtil.writeFileInDirectory(
//                                    context, directory, name,
//                                    ios
//                                )
//                            }?.path.toString() ?: kotlin.run {
//                                // TODO: Add proper logging exception
//                                null
//                            }
//                        } else {
//                            // TODO: Add proper logging exception
//                            null
//                        }
//                    }
//                }
//        } catch (e: Exception) {
//            // TODO: Add proper logging exception
//            null
//        }
//    }
//
//    private fun getFilePathFromContentProvider(
//        context: Context,
//        uri: Uri,
//        selection: String? = null,
//        selectionArgs: Array<String>? = null,
//        order: String? = null
//    ): String? =
//        try {
//            val dataColumn = MediaStore.Images.Media.DATA
//            readFromContentResolver(
//                context,
//                uri,
//                arrayOf(dataColumn),
//                selection,
//                selectionArgs,
//                order
//            )
//                ?.let { cursor ->
//                    cursor.executeOnCursor {
//                        val columnIndex = this.getColumnIndexOrThrow(dataColumn)
//                        if (this.moveToFirst())
//                            this.getString(columnIndex)
//                        else null
//                    }
//                }
//        } catch (e: Exception) {
//            // TODO: Add proper logging exception
//            null
//        }
//
//    private fun readFromContentResolver(
//        context: Context,
//        uri: Uri,
//        projections: Array<String>,
//        selection: String? = null,
//        selectionArgs: Array<String>? = null,
//        order: String? = null
//    ) =
//        context.contentResolver.query(
//            uri, projections, selection, selectionArgs, order
//        )
//
//    private fun getPathFromFileURI(uri: Uri) = uri.path.toString()
//
//    private fun isExternalStorageDocument(uri: Uri) =
//        URIAuthority.EXTERNAL_STORAGE_DOC == uri.authority
//
//    private fun isDownloadsDocument(uri: Uri) = URIAuthority.DOWNLOADS_DOC == uri.authority
//
//    private fun isMediaDocument(uri: Uri) = URIAuthority.MEDIA_DOC == uri.authority
//
//    private fun isGooglePhotosUri(uri: Uri) = URIAuthority.isGooglePhotos(uri)
//
//    private fun isWhatsAppFile(uri: Uri) = URIAuthority.WHATS_APP_MEDIA == uri.authority
//
//    private fun isGoogleDriveUri(uri: Uri) = URIAuthority.isDriveDoc(uri)
//
//    private fun isFileScheme(uri: Uri) = schemeFile.equals(uri.scheme, true)
//
//    private fun isContentScheme(uri: Uri) = schemeContent.equals(uri.scheme, true)
//
//    private fun isAtLeastKitkat() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
//
//    private fun isAtLeastM() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
//
//    private fun isAtLeastQ() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
//
//    private fun <T> Cursor.executeOnCursor(operation: Cursor.() -> T): T {
//        val response = operation.invoke(this)
//        if (this != null) this.close()
//        return response
//    }
//
//}