package com.asp.imsgepickerplayground.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.*

class URIProcessor(private val uriResolver: URIResolver, private val fileUtil: FileUtil, private val bitmapProcessor: BitmapProcessor) {
    private val fileBackedURIPrefix = "content://"
    private val supervisorJob = SupervisorJob()
    private val scope = Dispatchers.IO + supervisorJob

    // Use application context directly in FileUtils
    fun getFilePath(context: Context, uri: Uri, listener: (String?) -> Unit) {
        uriResolver.resolve(context, uri) { path: String?, name: String? ->
            path?.let { path ->
                try {
                    when {
                        path.startsWith(fileBackedURIPrefix) -> {
                            name?.let { name ->
                                CoroutineScope(scope).launch {
                                    context.contentResolver.openInputStream(uri)?.let {
                                        listener(
                                            fileUtil.writeFileInDirectory(
                                                context,
                                                FileUtil.DIRECTORY_USER_FILES,
                                                name,
                                                it
                                            )?.path?.toString()
                                        )
                                    }
                                }
                            }
                        }
                        else -> listener(fileUtil.writeFile(context, path)?.path?.toString())
                    }
                } catch (e: Exception) {
                    // TODO: Proper logging
                    listener(null)
                }
            }
        }
    }

    fun scaleBitmap(path: String, width: Int, height: Int): Bitmap? {
        return bitmapProcessor.hardScaleBitmap(path, height, width)
    }

    fun cleanup() {
        supervisorJob.cancel()
    }
}