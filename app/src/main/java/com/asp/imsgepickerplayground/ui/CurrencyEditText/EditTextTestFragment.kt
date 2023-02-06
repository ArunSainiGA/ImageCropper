package com.asp.imsgepickerplayground.ui.CurrencyEditText

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.asp.imsgepickerplayground.R
import com.asp.imsgepickerplayground.ui.newapproach.GalleryContract

class EditTextTestFragment : Fragment() {

    companion object {
        fun newInstance() = EditTextTestFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        return view
    }

}