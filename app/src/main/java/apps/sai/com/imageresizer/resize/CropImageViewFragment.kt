package apps.sai.com.imageresizer.resize

import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import apps.sai.com.imageresizer.R
import apps.sai.com.imageresizer.databinding.FragmentCropImageViewBinding
import apps.sai.com.imageresizer.select.SelectViewModel
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.canhub.cropper.CropImageView.CropResult
import com.canhub.cropper.CropImageView.OnCropImageCompleteListener
import com.canhub.cropper.CropImageView.OnSetImageUriCompleteListener


internal class CropImageViewFragment :
    Fragment(),
    OnSetImageUriCompleteListener,
    OnCropImageCompleteListener {
    private var _binding: FragmentCropImageViewBinding? = null
    private val binding get() = _binding!!
    private var options: CropImageOptions? = null
    private var selectViewModel: SelectViewModel? =null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // (activity as ComponentActivity).addMenuProvider(this)
        _binding = FragmentCropImageViewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       selectViewModel = ViewModelProvider(requireActivity())[SelectViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.cropImageView.setOnSetImageUriCompleteListener(null)
        binding.cropImageView.setOnCropImageCompleteListener(null)
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOptions()
        arguments?.let {
            it.getString("imageUri")?.let {
                binding.cropImageView.setImageUriAsync(Uri.parse(it))
            }
        }
        binding.cropImageView.setOnSetImageUriCompleteListener(this)
        binding.cropImageView.setOnCropImageCompleteListener(this)
        requireActivity().addMenuProvider(object :MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.crop_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean  =
                when (menuItem.itemId) {
                    R.id.main_action_crop -> {
                        binding.cropImageView.croppedImageAsync()
                        true
                    }
                    R.id.main_action_rotate -> {
                        binding.cropImageView.rotateImage(90)
                        true
                    }
                    R.id.main_action_flip_horizontally -> {
                        binding.cropImageView.flipImageHorizontally()
                        true
                    }
                    R.id.main_action_flip_vertically -> {
                        binding.cropImageView.flipImageVertically()
                        true
                    }
                    else -> false
                }

        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    private fun handleCropImageResult(result: CropResult) {
//        FileApi(context).saveImageInCache(dataFile, result.bitmap, 95)
        val croppedImageFilePath = result.getUriFilePath(requireContext(), false)
        // Set the new result
        // Use the Kotlin extension in the fragment-ktx artifact.
        activity?.supportFragmentManager?.setFragmentResult(
            CROP_REQUEST, bundleOf(
                RESULT_URI to result.uriContent.toString(),
                RESULT_FILE_PATH to croppedImageFilePath,
                RESULT_WIDTH to result.bitmap?.width,
                RESULT_HEIGHT to result.bitmap?.height
            )
        )
        activity?.supportFragmentManager?.popBackStack()
    }



    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        if (error != null) {
            Toast.makeText(activity, "Image load failed: " + error.message, Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onCropImageComplete(view: CropImageView, result: CropResult) {
        handleCropImageResult(result)
    }

    private fun setOptions() {
        // binding.cropImageView.cropRect = Rect(100, 300, 500, 1200)
        binding.cropImageView.cropRect = Rect(100, 300, 500, 1200)
       // binding.cropImageView.guidelines = cropImageOptions.guidelines
       // onOptionsApplySelected(cropImageOptions)
    }


    companion object {
        const val RESULT_URI = "result_uri"
        const val CROP_REQUEST = "input_uri"
        const val RESULT_FILE_PATH = "result_file_path"
        const val RESULT_WIDTH = "width"
        const val RESULT_HEIGHT = "height"

        @JvmStatic
        fun newInstance(imageUri: String): CropImageViewFragment {
            val fragment = CropImageViewFragment()
            val args = Bundle()
            args.putString("imageUri", imageUri)
            fragment.arguments = args
            return fragment
        }
    }

}
