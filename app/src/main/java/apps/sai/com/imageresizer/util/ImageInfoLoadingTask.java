package apps.sai.com.imageresizer.util;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.DataFile;
import apps.sai.com.imageresizer.data.ImageInfo;

/**
 * Created by sailesh on 13/01/18.
 */

public class ImageInfoLoadingTask extends AsyncTask<Void, Void, ImageInfo> {
    private final Context context;
    OnImageInfoProcessedListener mOnImageInfoProcessedListener;
    private ImageInfo mImageInfo;
    private final DataApi mDataApi;
    private ImageOperations mTask;

    public interface OnImageInfoProcessedListener {
        /**
         * @param imageInfo
         */
        void onImageProcessed(ImageInfo imageInfo);
    }

    public ImageInfoLoadingTask(Context context, ImageInfo imageInfo,
                                DataApi dataApi, OnImageInfoProcessedListener imageInfoLoadedListener, ImageOperations tasks) {
        this.mImageInfo = imageInfo;
        this.context = context;
        this.mDataApi = dataApi;
        this.mOnImageInfoProcessedListener = imageInfoLoadedListener;
        this.mTask = tasks;
    }

    public void setOnImageUriLoadedListener(OnImageInfoProcessedListener mOnImageInfoProcessedListener) {
        this.mOnImageInfoProcessedListener = mOnImageInfoProcessedListener;
    }

    @Override
    protected ImageInfo doInBackground(Void... voids) {
        try {
            if (mTask == ImageOperations.IMAGE_INFO_LOAD) {
                mImageInfo = Utils.getImageInfo(mImageInfo, context, mImageInfo.getImageUri(), mDataApi);
            } else if (mTask == ImageOperations.IMAGE_FILE_DELETE) {
                if (mImageInfo != null) {
                    String thumb = mImageInfo.getAbsoluteThumbFilePath();
                    if (mImageInfo.getAbsoluteFilePathUri() == null && mImageInfo.getImageUri() != null) {
                        mImageInfo = Utils.getImageInfo(mImageInfo, context, mImageInfo.getImageUri(), mDataApi);
                    }
                    if (mImageInfo != null) {
                        if (mImageInfo.getAbsoluteFilePathUri() != null) {
                            Uri orgUri = mImageInfo.getImageUri();
                            mImageInfo.setImageUri(mImageInfo.getAbsoluteFilePathUri());
                            boolean result = mDataApi.deleteImageFile((Activity) context, mImageInfo, thumb);
                            if (!result) {
                                System.out.print("");
                            }
                            mImageInfo.setDeleted(result);

                            mImageInfo.setImageUri(orgUri);
                        }
                    }
                }
            } else if (mTask == ImageOperations.IMAGE_FILE_SAVE_CACHE_TO_GALLERY) {
                if (mImageInfo.getDataFile() != null) {
                    Uri uri = mDataApi.copyImageFromCacheToGallery((Activity) context, mImageInfo.getDataFile());
                    if (uri != null) {
                        mImageInfo.setSaved(true);
                    }
                }
            } else if (mTask == ImageOperations.IMAGE_FILE_SAVE_GALLERY_TO_MY_FOLDER) {
                DataFile destDatafile = new DataFile();
                if (mImageInfo.getAbsoluteFilePathUri() == null && mImageInfo.getImageUri() != null) {
                    mImageInfo = Utils.getImageInfo(mImageInfo, context, mImageInfo.getImageUri(), mDataApi);
                }
                if (mImageInfo.getDataFile() != null) {
                    destDatafile.setName(mImageInfo.getDataFile().getName());
                    destDatafile.setUri(mDataApi.getMyFolderUriFromCache(destDatafile.getName()));
                    Uri uri = mDataApi.copyImageFromSrcToDets((Activity) context, mImageInfo.getDataFile(), destDatafile);
                    if (uri != null) {
                        mImageInfo.setSaved(true);
                    }
                }
            }
        } catch (Throwable t) {
            if (mImageInfo != null) {
                mImageInfo.setSaved(false);
                mImageInfo.setDeleted(false);
            }
            t.printStackTrace();
        }
        return mImageInfo;
    }

    @Override
    protected void onPostExecute(ImageInfo imageInfo) {
        super.onPostExecute(imageInfo);

        if (mOnImageInfoProcessedListener != null) {
            mOnImageInfoProcessedListener.onImageProcessed(imageInfo);
        }
    }
}
