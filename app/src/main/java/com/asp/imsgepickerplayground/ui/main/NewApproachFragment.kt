package com.asp.imsgepickerplayground.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ext.SdkExtensions.getExtensionVersion
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.asp.imsgepickerplayground.R
import com.asp.imsgepickerplayground.ui.cropper.ImageEditMeta
import com.asp.imsgepickerplayground.ui.cropper.RefactoredImageCropper
import com.asp.imsgepickerplayground.ui.cropper.WFImageEditView
import com.asp.imsgepickerplayground.ui.newapproach.GalleryContract
import com.asp.imsgepickerplayground.ui.wf_approach.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NewApproachFragment : Fragment() {

    companion object {
        fun newInstance() = NewApproachFragment()
    }

    private lateinit var viewModel: MainViewModel

    private lateinit var resultLauncher: ActivityResultLauncher<Uri>
    private lateinit var firstImageView: ImageView
    private lateinit var secondImageView: ImageView
    private lateinit var button: Button
    private lateinit var cropView: RefactoredImageCropper

    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachResultCallback()
    }

    private fun attachResultCallback() {
        resultLauncher = registerForActivityResult(GalleryContractWF()) { uri ->
            lifecycleScope.launch {
                uri?.let {
                    val uriProcessor = GalleryImagePickerManagerWF(
                        URIResolverWF(),
                        BitmapProcessorWF()
                    )
                    uriProcessor.getScaledBitmap(requireContext().applicationContext, uri, Size(640, 480)) { fileMeta ->
                        if(fileMeta != null) {

//                            val file = File(requireContext().applicationContext.cacheDir, "filename"+ Calendar.getInstance().timeInMillis +".jpeg")
//                            file.createNewFile()

//                            val bitmap = fileMeta.bitmap
//                            val bos = ByteArrayOutputStream()
//                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
//                            val bitmapdata = bos.toByteArray()
//
//                            FileOutputStream(file).apply {
//                                write(bitmapdata)
//                                flush()
//                                close()
//                            }


//                            setImageBitmap(fileMeta.bitmap)

                            cropView.visibility = View.VISIBLE
                            button.visibility = View.VISIBLE
                            firstImageView.visibility = View.GONE
                            cropView.setCropMeta(ImageEditMeta(
                                uri = uri,
                                bitmap = fileMeta.bitmap,
                                aspectX = 4,
                                aspectY = 3,
//                                outputX = 400,
//                                outputY = 400,
                                orientation = fileMeta.orientation
//                                filePath = fileMeta.filePath
                            ))
                            cropView.initialize()
//                            cropView.of(uri)
//                                .withAspect(16, 9)
//                                .withOutputSize(640, 400)
//                                .initialize(context);
                            button.setOnClickListener {
                               cropView.getOutput()?.let {
                                   setImageBitmap(it)
                               }
                                cropView.visibility = View.GONE
                                button.visibility = View.GONE
                                firstImageView.visibility = View.VISIBLE
                            }

                            Log.i("NewApproachFragment", fileMeta.toString())
                        }
                    }
                }
            }
        }

        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            lifecycleScope.launch {
                uri?.let {
                    val uriProcessor = GalleryImagePickerManagerWF(
                        URIResolverWF(),
                        BitmapProcessorWF()
                    )
                    uriProcessor.getScaledBitmap(requireContext().applicationContext, uri, Size(640, 480)) { fileMeta ->
                        if(fileMeta != null) {
//                            cropView.visibility = View.VISIBLE
//                            button.visibility = View.VISIBLE
//                            cropView.of(uri)
//                                .withAspect(16, 9)
//                                .withOutputSize(1900, 1200)
//                                .initialize(context);
//                            button.setOnClickListener {
//                                val bitmap =  cropView.getOutput()
//                                setImageBitmap(bitmap)
//                                cropView.visibility = View.GONE
//                                button.visibility = View.GONE
//                            }
//                            setImageBitmap(fileMeta.bitmap)
                            Log.i("NewApproachFragment", fileMeta.toString())
                        }
                    }
                }
            }
        }

    }

    private fun setImageBitmap(it: Bitmap) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                secondImageView.setImageBitmap(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        firstImageView = view.findViewById<ImageView>(R.id.firstImageView)
        secondImageView = view.findViewById<ImageView>(R.id.secondImageView)
        cropView = view.findViewById<RefactoredImageCropper>(R.id.cropView)
        button = view.findViewById<Button>(R.id.button)

        if(ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 1);
        }

        firstImageView.setOnClickListener {
//            if (isPhotoPickerAvailable())
//                pickMedia?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//             else
                     resultLauncher.launch(GalleryContract.GALLERY_URI)

        }
        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {

            // Checking whether user granted the permission or not.
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Showing the toast message
                Toast.makeText(this.requireContext(), "Camera Permission Granted", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this.requireContext(), "Camera Permission Denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

    @Suppress("NewApi")
    private fun isPhotoPickerAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getExtensionVersion(Build.VERSION_CODES.R) >= 2
        } else {
            false
        }
    }
}