package apps.sai.com.imageresizer.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;


import apps.sai.com.imageresizer.data.BitmapResult;
import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.DataFile;
import apps.sai.com.imageresizer.data.ImageInfo;
import apps.sai.com.imageresizer.resize.ImageProcessingTasks;

/**
 * Created by sailesh on 13/01/18.
 */

public class BitmapProcessingTask extends AsyncTask<Void, Void, BitmapResult> {
    private final Context context;
    private final ImageProcessingTasks imageProcessingTasks;
    private final boolean mAutoSave;
    private final int mKbEnteredValue;
    private OnImageProcessedListener mOnImageProcessedListener;
    private int width;
    private int height;
    private final int maxResolution;
    private DataFile mDataFile;
    private ImageInfo mImageInfo;
    private final DataApi mDataApi;
    private final int mQuality;
    private Uri mUri;
    private final boolean mMultipleTask;

    public interface OnImageProcessedListener {
        void onImageLoaded(BitmapResult bitmapResult, ImageInfo imageInfoOrg);
    }

    public BitmapProcessingTask(ImageInfo imageInfo, Context context,
                                int desiredWidth, int desiredHeight,
                                int maxResolution, DataFile dataFile,
                                ImageProcessingTasks imageProcessingTasks,
                                int mCompressPercentage, int mKbEnteredValue,
                                DataApi dataApi, boolean multipleTask, boolean autoSave) {

        this.context = context;
        this.width = desiredWidth;
        this.height = desiredHeight;
        this.mImageInfo = imageInfo;
        this.maxResolution = maxResolution;
        this.mDataFile = dataFile;
        this.imageProcessingTasks = imageProcessingTasks;
        this.mQuality = mCompressPercentage;
        this.mDataApi = dataApi;
        this.mMultipleTask = multipleTask;
        this.mAutoSave = autoSave;
        this.mKbEnteredValue = mKbEnteredValue;
    }

    public void setOnImageProcessedListener(OnImageProcessedListener onImageProcessedListener) {
        this.mOnImageProcessedListener = onImageProcessedListener;
    }

    @Override
    protected BitmapResult doInBackground(Void... voids) {
        Bitmap bitmap;
        BitmapResult bitmapResult = new BitmapResult();
        try {
            if (mImageInfo.getDataFile() == null) {
                mImageInfo = Utils.getImageInfo(mImageInfo, context, mImageInfo.getImageUri(), mDataApi);
                mDataFile = mImageInfo.getDataFile();
            }
            bitmap = mDataApi.getBitmapFromAbsolutePathUri(context, mImageInfo.getAbsoluteFilePathUri(),
                    mImageInfo.getWidth(), mImageInfo.getHeight()).getBitmap();
            int quality = 95;
            Bitmap bitmapRes = null;
            if (imageProcessingTasks == ImageProcessingTasks.SCALE) {
                if (width == 0) {
                    //calculate new width
                    width = Utils.calculateAspectRatioWidth(new Point(mImageInfo.getWidth(), mImageInfo.getHeight()), height).x;
                } else if (height == 0) {
                    height = Utils.calculateAspectRatioHeight(new Point(mImageInfo.getWidth(), mImageInfo.getHeight()), width).y;
                }
                String newName = mDataFile.getName();
                mDataFile.setName(newName);
                try {
                    bitmapRes = mDataApi.scaleImage( bitmap, width, height, 0);
                } catch (Throwable e) {
                    bitmapResult.setError(e);
                }
                if (bitmapRes != null) {
                    if (!mMultipleTask) {
                        DataFile dataFile = mImageInfo.getDataFile();
                        mDataApi.saveImageInCache(dataFile, bitmapRes, quality);
                        mUri = mDataApi.getAbsoluteImagePathUriFromCache(dataFile);
                        bitmapResult.setBitmap(bitmapRes);
                        mImageInfo.setAbsoluteFilePath(mUri);
                    } else {
                        DataFile dataFile = mImageInfo.getDataFile();
                        mDataApi.saveImageInCache(dataFile, bitmapRes, quality);
                        mUri = mDataApi.getAbsoluteImagePathUriFromCache(dataFile);
                        mImageInfo.setAbsoluteFilePath(mUri);
                    }
                }
            } else if (imageProcessingTasks == ImageProcessingTasks.COMPRESS) {
                quality = mQuality;
                if (!mMultipleTask) {
                    mDataApi.saveImageInCacheWithSizeLimit(mImageInfo.getDataFile(), bitmap, quality, mKbEnteredValue);
                    mUri = mDataApi.getAbsoluteImagePathUriFromCache(mImageInfo.getDataFile());
                    mImageInfo.setAbsoluteFilePath(mUri);

                } else {
                    try {
                        mDataApi.saveImageInCacheWithSizeLimit(mImageInfo.getDataFile(), bitmap, quality, mKbEnteredValue);
                        mUri = mDataApi.getAbsoluteImagePathUriFromCache(mImageInfo.getDataFile());
                        mImageInfo.setAbsoluteFilePath(mUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (bitmapRes != null) {
                bitmapRes.recycle();
            }
            if (bitmap != null) {
                bitmap.recycle();
            }
            bitmapResult.setContentUri(mUri);
            try {
                Uri originalContentUri = mImageInfo.getOriginalContentUri();
                mImageInfo = Utils.getImageInfo(mImageInfo, context, mImageInfo.getAbsoluteFilePathUri(), mDataApi);
                mImageInfo.setSaved(false);
                mImageInfo.setOriginalContentUri(originalContentUri);
                if (mAutoSave) {
                    DataFile destDatafile = new DataFile();
                    if (mImageInfo.getDataFile() != null) {
                        destDatafile.setName(mImageInfo.getDataFile().getName());
                        destDatafile.setUri(mDataApi.getMyFolderUriFromCache(destDatafile.getName()));
                        Uri uri = mDataApi.copyImageFromSrcToDets((Activity) context, mImageInfo.getDataFile(), destDatafile);
                        if (uri != null) {
                            mImageInfo.setSaved(true);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (bitmapResult != null) {
                bitmapResult.setError(e);
            }
            return bitmapResult;
        }
        return bitmapResult;
    }


    @Override
    protected void onPostExecute(BitmapResult bitmapResult) {
        super.onPostExecute(bitmapResult);
        if (mOnImageProcessedListener != null) {
            mOnImageProcessedListener.onImageLoaded(bitmapResult, mImageInfo);
        }
    }
}
