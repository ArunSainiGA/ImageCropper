package com.asp.imsgepickerplayground.ui.newapproach

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract

class GalleryContract: ActivityResultContract<Uri, Uri?>() {
    override fun createIntent(context: Context, uriToFilter: Uri) = Intent(
        Intent.ACTION_PICK,
        uriToFilter
    )

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return when {
            resultCode != Activity.RESULT_OK -> null
            else -> intent?.data
        }
    }

    companion object {
        val GALLERY_URI: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
}