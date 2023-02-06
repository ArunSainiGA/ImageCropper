package com.asp.imsgepickerplayground.ui.util

import android.net.Uri

object URIAuthority {
    const val EXTERNAL_STORAGE_DOC = "com.android.externalstorage.documents"
    const val DOWNLOADS_DOC = "com.android.providers.downloads.documents"
    const val MEDIA_DOC = "com.android.providers.media.documents"
    const val GOOGLE_PHOTOS_LEGACY = "com.google.android.apps.photos.content"
    const val GOOGLE_PHOTOS = "com.google.android.apps.photos.contentprovider"
    const val WHATS_APP_MEDIA = "com.whatsapp.provider.media"
    const val DRIVE_DOCS = "com.google.android.apps.docs.storage"
    const val DRIVE_DOCS_LEGACY = "com.google.android.apps.docs.storage.legacy"

    fun isDriveDoc(uri: Uri) = uri.authority == DRIVE_DOCS || uri.authority == DRIVE_DOCS_LEGACY

    fun isGooglePhotos(uri: Uri) = uri.authority == GOOGLE_PHOTOS_LEGACY || uri.authority == GOOGLE_PHOTOS
}