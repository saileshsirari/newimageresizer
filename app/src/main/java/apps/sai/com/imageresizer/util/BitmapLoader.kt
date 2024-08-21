package apps.sai.com.imageresizer.util

import android.content.Context
import android.net.Uri
import apps.sai.com.imageresizer.data.BitmapResult
import apps.sai.com.imageresizer.data.DataApi
import apps.sai.com.imageresizer.data.DataFile
import apps.sai.com.imageresizer.data.ImageInfo
import java.io.File

class BitmapLoader//        this.mDataFile =dataFile;
    (
    imageInfo: ImageInfo,
    dataApi: DataApi,
) {
    private var mImageInfo: ImageInfo = imageInfo
    private var mDataApi: DataApi = dataApi
    fun process(context: Context): BitmapResult {
        val bitmapResult = BitmapResult()

        try {
            val dataFile = DataFile()
            val name = mImageInfo.absoluteFilePathUri.path?.let { File(it).name }
            dataFile.name = name
            dataFile.uri = mImageInfo.absoluteFilePathUri
            //load image
            val bitmapRes = mDataApi.getBitmapFromAbsolutePathUri(
                context,
                mImageInfo.absoluteFilePathUri,
                0,
                0
            ).bitmap

            bitmapResult.bitmap = bitmapRes
            mImageInfo.width = bitmapRes.width
            mImageInfo.height = bitmapRes.height
            bitmapResult.contentUri = mImageInfo.absoluteFilePathUri
        } catch (t: Throwable) {
            bitmapResult.error = t
        }
        return bitmapResult
    }
}