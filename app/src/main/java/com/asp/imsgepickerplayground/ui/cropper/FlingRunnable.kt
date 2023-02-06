package com.asp.imsgepickerplayground.ui.cropper

import android.content.Context
import com.oginotihiro.cropview.scrollerproxy.ScrollerProxy
import java.lang.ref.WeakReference

interface FlingChangeListener {
    fun handleFling(dx: Int, dy: Int)
}