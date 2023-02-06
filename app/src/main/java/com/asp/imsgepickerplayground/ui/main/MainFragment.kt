package com.asp.imsgepickerplayground.ui.main

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.asp.imsgepickerplayground.R
import com.asp.imsgepickerplayground.ui.newapproach.GalleryContract
import com.asp.imsgepickerplayground.ui.wf_approach.GalleryContractWF
import com.asp.imsgepickerplayground.ui.util.*
import com.asp.imsgepickerplayground.ui.util.BitmapProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

//    private lateinit var resultLauncher: ActivityResultLauncher<Uri>
    private lateinit var resultLauncher: ActivityResultLauncher<String>
    private lateinit var firstImageView: ImageView
    private lateinit var secondImageView: ImageView

    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>? = null

//    private val bitmapProcessor: BitmapProcessor by lazy {
//        BitmapProcessor(requireContext())
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachResultCallback()
    }

    private fun attachResultCallback() {
//        resultLauncher = registerForActivityResult(GalleryContractWF()) { uri ->
        resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            lifecycleScope.launch {
                uri?.let {
                    val uriProcessor = URIProcessor(URIResolver(), FileUtil(), BitmapProcessor())
                    uriProcessor.getFilePath(requireContext(), uri) { filePath ->
                        filePath?.let {
//                            val processor = BitmapProcessor()
                            uriProcessor.scaleBitmap(it, 640, 480)?.let {
                               setImageBitmap(it)
                            }
                        }

                    }
//                    uriResolver.resolve(requireContext(), uri)?.let { path ->
//                        val processor = BitmapProcessor()
//                        processor.hardScaleBitmap(path, 480, 640)?.let {
//                            secondImageView.setImageBitmap(it)
//                        }
//                    }

//                    val bitmap = bitmapProcessor.getSoftScaledImage(uri!!, 640, 480)
//                    withContext(Dispatchers.Main) {
//                        secondImageView.setImageBitmap(bitmap)
//                    }
//                }
                }
            }
        }

    //        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
//            // Callback is invoked after the user selects a media item or closes the
//            // photo picker.
//            lifecycleScope.launch {
//                uri?.let {
//                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
//                    requireContext().contentResolver.takePersistableUriPermission(uri, flag)
//                    val uriResolver = URIResolver(FileUtil())
//                    uriResolver.getPath(requireContext(), uri)?.let { path ->
//                        val processor = BitmapProcessor()
//                        processor.hardScaleBitmap(path, 480, 640)?.let {
//                            secondImageView.setImageBitmap(it)
//                        }
//                    }
//                }
//            }
//        }

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

        firstImageView.setOnClickListener {
//            val i = Intent(
//                Intent.ACTION_PICK,
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//            )
//            startActivityForResult(i, 2)
//            resultLauncher.launch(GalleryContract.GALLERY_URI)
//            resultLauncher.launch("image/*")
//            pickMedia?.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

}