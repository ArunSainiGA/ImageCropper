package com.asp.imsgepickerplayground.ui.util

import android.content.Context
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*

class FileUtil {

    fun getFile(path: String): File? =
        try {
            File(path)
        } catch (e: FileNotFoundException) {
            // TODO: Add proper logging exception
            null
        }

    fun getFileSize(path: String): Long? = getFile(path)?.length()

    fun getFileSize(file: File): Long = file.length()

    fun writeFile(
        context: Context,
        fileName: String,
        inputStream: InputStream
    ) =
        writeFileInDirectory(
            context, DIRECTORY_USER_FILES, fileName, inputStream
        )

    fun writeFile(context: Context, path: String) =
        try {
            getFileName(path)?.let { name ->
                getFile(path)?.let { file ->
                    writeFileInDirectory(
                        context, DIRECTORY_USER_FILES, name, file.inputStream()
                    )
                }
            }
        } catch (e: Exception) {
            // TODO: Add proper logging exception
            null
        }

    fun writeFileInDirectory(
        context: Context,
        directory: String,
        fileName: String,
        inputStream: InputStream): File? {
        return try {
            val file = createFileInDirectory(
                context, directory, fileName
            )
            val outputStream = file.outputStream()

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            inputStream.close()
            outputStream.close()

            file

        } catch (e: Exception) {
            // TODO: Add proper logging exception
            null
        }

    }

    private fun createFileInDirectory(context: Context, directory: String, fileName: String) =
        File(createDirectory(context, directory) + FileUtil.PATH_SEPARATOR + fileName)

    private fun createDirectory(context: Context, directory: String): String {
        val path = when {
            directory.isBlank() -> getRootDirectory(context)
            else -> getRootDirectory(context) + FileUtil.PATH_SEPARATOR + directory
        }

        val file = File(path)

        if(file.exists().not())
            file.mkdir()

        return file.path.toString()
    }

    private fun getRootDirectory(context: Context) = context.cacheDir.path.toString()

    private fun getFileName(path: String): String? {

        return try {
            val nameWithExtension = path.substring(path.lastIndexOf(PATH_SEPARATOR))
            val lastIndexOfDot = nameWithExtension.lastIndexOf(".")
            val extension = nameWithExtension.substring(lastIndexOfDot)
            if (extension.length > 1) {
                val name = nameWithExtension.substring(0, lastIndexOfDot)
                name + "_" + Calendar.getInstance().timeInMillis + extension
            } else null
        } catch (e: Exception) {
            // TODO: Log exception
            null
        }

    }

    companion object {
        val TAG = FileUtil::class.java
        const val DIRECTORY_USER_FILES = "user_files"
        const val PATH_SEPARATOR = "/"
    }
}