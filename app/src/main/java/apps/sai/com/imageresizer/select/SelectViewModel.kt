package apps.sai.com.imageresizer.select

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import apps.sai.com.imageresizer.data.BitmapResult
import com.canhub.cropper.CropImageView.CropResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SelectViewModel:ViewModel() {
    val cropResult = MutableStateFlow<Result<CropResult>>(Result.failure(Exception("")));
}