package apps.sai.com.imageresizer.resize

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import apps.sai.com.imageresizer.R
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView

class CropImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)
//        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_HOME
//        supportActionBar?.setDisplayShowCustomEnabled(false)
        val uriString = intent.getStringExtra(PARAM_URI)
        val cropImageOptions = CropImageOptions()
        findViewById<CropImageView>(R.id.cropImageView).setImageUriAsync(Uri.parse(uriString))
        cropImageOptions.guidelines = (CropImageView.Guidelines.ON)
          cropImageOptions.outputCompressFormat = Bitmap.CompressFormat.PNG
          cropImage.launch(
              CropImageContractOptions(
                  Uri.parse(uriString),
                  cropImageOptions
              )
          )
    }


    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        when {
            result.isSuccessful -> {
                handleCropImageResult(result)

            }

            result is CropImage.CancelledResult -> showErrorMessage("cropping image was cancelled by the user")
            else -> showErrorMessage("cropping image failed")
        }
    }

    private fun handleCropImageResult(result: CropImageView.CropResult) {
        val intent = Intent()
        intent.putExtra(RESULT_URI, result.uriContent.toString())
        val croppedImageFilePath = result.getUriFilePath(this, false)
        intent.putExtra(RESULT_FILE_PATH, croppedImageFilePath)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, "Crop failed: $message", Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        const val RESULT_URI = "result_uri"
        const val PARAM_URI = "input_uri"
        const val RESULT_FILE_PATH = "result_file_path"
    }
}