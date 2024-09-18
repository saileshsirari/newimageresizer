package apps.sai.com.imageresizer.myimages

import android.content.Context
import android.content.Intent
import apps.sai.com.imageresizer.data.DataApi
import apps.sai.com.imageresizer.data.FileApi
import apps.sai.com.imageresizer.data.ImageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Created by sailesh on 20/01/18.
 */
class MyImagesPresenter : MyImagesContract.Presenter {
    private var mView: MyImagesContract.View? = null
    override fun takeView(view: MyImagesContract.View) {
        mView = view
    }

    override fun launchgalleryExternalApp(singlePhoto: Boolean) {
    }

    override fun onGalleryImageSelected(data: Intent) {
    }

    override fun dropView() {
        mView = null
    }

    override fun showSelectedImage(imageInfo: ImageInfo) {
    }

    override fun deleteImage(imageInfo: ImageInfo) {
    }

    override fun shareImages(context: Context, imageInfoList: List<ImageInfo>) {
    }

    override fun getImages(context: Context): Flow<List<ImageInfo>> = flow {
        val dataApi: DataApi = FileApi(context)
        val imageInfoList = dataApi.myImages
        emit(imageInfoList)
    }.flowOn(Dispatchers.IO)

}
