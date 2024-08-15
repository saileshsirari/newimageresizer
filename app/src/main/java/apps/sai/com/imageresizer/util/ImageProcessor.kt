package apps.sai.com.imageresizer.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import apps.sai.com.imageresizer.data.BitmapResult
import apps.sai.com.imageresizer.data.DataApi
import apps.sai.com.imageresizer.data.DataFile
import apps.sai.com.imageresizer.data.ImageInfo
import apps.sai.com.imageresizer.resize.ImageProcessingTask
import apps.sai.com.imageresizer.util.BitmapProcessingTask.OnImageProcessedListener

class ImageProcessor(
    imageInfo: ImageInfo,
    private var context: Context?,
    desiredWidth: Int,
    desiredHeight: Int,
    private var maxResolution: Int,
    dataFile: DataFile?,
    private var imageProcessingTask: ImageProcessingTask?,
    mCompressPercentage: Int,
    private var mKbEnteredValue: Int,
    dataApi: DataApi,
    multipleTask: Boolean,
    autoSave: Boolean
) {
    private var mAutoSave = autoSave
    private var mOnImageProcessedListener: OnImageProcessedListener? = null
    private var width = desiredWidth
    private var height = desiredHeight
    private var mDataFile: DataFile? = dataFile
    private var mImageInfo: ImageInfo = imageInfo
    private var mDataApi: DataApi = dataApi
    private var mQuality = mCompressPercentage
    private var mUri: Uri? = null
    private var mMultipleTask = multipleTask

    fun setOnImageProcessedListener(onImageProcessedListener: OnImageProcessedListener?) {
        this.mOnImageProcessedListener = onImageProcessedListener
    }

    fun process(): BitmapResult {
        val bitmap: Bitmap?
        val bitmapResult = BitmapResult()
        try {
            if (mImageInfo.dataFile == null) {
                mImageInfo =
                    Utils.getImageInfo(mImageInfo, context, mImageInfo.imageUri, mDataApi)
                mDataFile = mImageInfo.dataFile
            }
            bitmap = mDataApi.getBitmapFromAbsolutePathUri(
                context, mImageInfo.absoluteFilePathUri,
                mImageInfo.width, mImageInfo.height
            ).bitmap
            var quality = 95
            var bitmapRes: Bitmap? = null
            if (imageProcessingTask == ImageProcessingTask.SCALE) {
                if (width == 0) {
                    //calculate new width
                    width = Utils.calculateAspectRatioWidth(
                        Point(
                            mImageInfo.width, mImageInfo.height
                        ), height
                    ).x
                } else if (height == 0) {
                    height = Utils.calculateAspectRatioHeight(
                        Point(
                            mImageInfo.width, mImageInfo.height
                        ), width
                    ).y
                }
                val newName = mDataFile!!.name
                mDataFile!!.name = newName
                try {
                    bitmapRes = mDataApi.scaleImage(bitmap, width, height, 0)
                } catch (e: Throwable) {
                    bitmapResult.error = e
                }
                if (bitmapRes != null) {
                    if (!mMultipleTask) {
                        val dataFile = mImageInfo.dataFile
                        mDataApi.saveImageInCache(dataFile, bitmapRes, quality)
                        mUri = mDataApi.getAbsoluteImagePathUriFromCache(dataFile)
                        bitmapResult.bitmap = bitmapRes
                        mImageInfo.setAbsoluteFilePath(mUri)
                    } else {
                        val dataFile = mImageInfo.dataFile
                        mDataApi.saveImageInCache(dataFile, bitmapRes, quality)
                        mUri = mDataApi.getAbsoluteImagePathUriFromCache(dataFile)
                        mImageInfo.setAbsoluteFilePath(mUri)
                    }
                }
            } else if (imageProcessingTask == ImageProcessingTask.COMPRESS) {
                quality = mQuality
                if (!mMultipleTask) {
                    mDataApi.saveImageInCacheWithSizeLimit(
                        mImageInfo.dataFile,
                        bitmap,
                        quality,
                        mKbEnteredValue
                    )
                    mUri = mDataApi.getAbsoluteImagePathUriFromCache(mImageInfo.dataFile)
                    mImageInfo.setAbsoluteFilePath(mUri)
                } else {
                    try {
                        mDataApi.saveImageInCacheWithSizeLimit(
                            mImageInfo.dataFile,
                            bitmap,
                            quality,
                            mKbEnteredValue
                        )
                        mUri = mDataApi.getAbsoluteImagePathUriFromCache(mImageInfo.dataFile)
                        mImageInfo.setAbsoluteFilePath(mUri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            bitmapRes?.recycle()
            bitmap?.recycle()
            bitmapResult.contentUri = mUri
            try {
                val originalContentUri = mImageInfo.originalContentUri
                mImageInfo = Utils.getImageInfo(
                    mImageInfo,
                    context,
                    mImageInfo.absoluteFilePathUri,
                    mDataApi
                )
                mImageInfo.isSaved = false
                mImageInfo.originalContentUri = originalContentUri
                if (mAutoSave) {
                    val destDatafile = DataFile()
                    if (mImageInfo.dataFile != null) {
                        destDatafile.name = mImageInfo.dataFile.name
                        destDatafile.uri = mDataApi.getMyFolderUriFromCache(destDatafile.name)
                        val uri = mDataApi.copyImageFromSrcToDets(
                            context as Activity?,
                            mImageInfo.dataFile,
                            destDatafile
                        )
                        if (uri != null) {
                            mImageInfo.isSaved = true
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            bitmapResult.error = e
            return bitmapResult
        }
        if (mOnImageProcessedListener != null) {
            mOnImageProcessedListener?.onImageLoaded(bitmapResult, mImageInfo)
        }
        return bitmapResult
    }

}