package apps.sai.com.imageresizer.util

import android.app.Activity
import android.content.Context
import apps.sai.com.imageresizer.data.DataApi
import apps.sai.com.imageresizer.data.DataFile
import apps.sai.com.imageresizer.data.ImageInfo

class ImageInfoLoader(
    imageInfo: ImageInfo,
    dataApi: DataApi,
    tasks: ImageOperations
) {
    private var mImageInfo: ImageInfo = imageInfo
    private var mDataApi: DataApi = dataApi
    private var mTask: ImageOperations = tasks
    fun process( context: Context): ImageInfo {
        try {
            if (mTask == ImageOperations.IMAGE_INFO_LOAD) {
                mImageInfo = Utils.getImageInfo(mImageInfo, context, mImageInfo.imageUri, mDataApi)
            } else if (mTask == ImageOperations.IMAGE_FILE_DELETE) {
                val thumb = mImageInfo.absoluteThumbFilePath
                if (mImageInfo.absoluteFilePathUri == null && mImageInfo.imageUri != null) {
                    mImageInfo =
                        Utils.getImageInfo(mImageInfo, context, mImageInfo.imageUri, mDataApi)
                }
                if (mImageInfo.absoluteFilePathUri != null) {
                    val orgUri = mImageInfo.imageUri
                    mImageInfo.imageUri = mImageInfo.absoluteFilePathUri
                    val result =
                        mDataApi.deleteImageFile(context as Activity?, mImageInfo, thumb)
                    if (!result) {
                        print("")
                    }
                    mImageInfo.isDeleted = result

                    mImageInfo.imageUri = orgUri
                }
            } else if (mTask == ImageOperations.IMAGE_FILE_SAVE_CACHE_TO_GALLERY) {
                if (mImageInfo.dataFile != null) {
                    val uri = mDataApi.copyImageFromCacheToGallery(
                        context as Activity?, mImageInfo.dataFile
                    )
                    if (uri != null) {
                        mImageInfo.isSaved = true
                    }
                }
            } else if (mTask == ImageOperations.IMAGE_FILE_SAVE_GALLERY_TO_MY_FOLDER) {
                val destDatafile = DataFile()
                if (mImageInfo.absoluteFilePathUri == null && mImageInfo.imageUri != null) {
                    mImageInfo =
                        Utils.getImageInfo(mImageInfo, context, mImageInfo.imageUri, mDataApi)
                }
                if (mImageInfo.dataFile != null) {
                    destDatafile.name = mImageInfo.dataFile.name
                    destDatafile.uri = mDataApi.getMyFolderUriFromCache(destDatafile.name)
                    val uri = mDataApi.copyImageFromSrcToDets(
                        context as Activity?, mImageInfo.dataFile, destDatafile
                    )
                    if (uri != null) {
                        mImageInfo.isSaved = true
                    }
                }
            }
        } catch (t: Throwable) {
            mImageInfo.isSaved = false
            mImageInfo.isDeleted = false
            t.printStackTrace()
        }
        return mImageInfo
    }
}