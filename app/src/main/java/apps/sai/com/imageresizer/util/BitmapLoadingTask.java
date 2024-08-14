package apps.sai.com.imageresizer.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;

import apps.sai.com.imageresizer.data.BitmapResult;
import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.DataFile;
import apps.sai.com.imageresizer.data.ImageInfo;

/**
 * Created by sailesh on 13/01/18.
 */

public   class BitmapLoadingTask extends AsyncTask<Void,Void ,BitmapResult> {
    private final Context context;
    OnImageUriLoadedListener mOnImageUriLoadedListener;

    private final int width,height;
    private final ImageInfo mImageInfo;
    private final DataApi mDataApi;
    private Uri mUri;
    DataFile mDataFile;

    public interface OnImageUriLoadedListener{
        /**
         * Absolute file path of the loaded bitmap from uri or file path
         * @param bitmapResult
         */
        void onImageLoaded(BitmapResult bitmapResult);
    }
    public interface OnImageLoadedListener{
        /**
         * Absolute file path of the loaded bitmap from uri or file path
         * @param bitmapResult
         */
        void onImageLoaded(BitmapResult bitmapResult);
    }

    private OnImageLoadedListener mOnImageLoadedListener;

    public void setOnImageLoadedListener(OnImageLoadedListener mOnImageLoadedListener) {
        this.mOnImageLoadedListener = mOnImageLoadedListener;
    }

    public OnImageLoadedListener getOnImageLoadedListener() {
        return mOnImageLoadedListener;
    }

    public BitmapLoadingTask(Context context, ImageInfo imageInfo, DataApi dataApi, int maxWidth , int maxHeight) {

        this.mImageInfo = imageInfo;
        this.context =context;
        this.width =maxWidth;
        this.height = maxHeight;
        this.mDataApi =dataApi;
//        this.mDataFile =dataFile;

    }

    public void setOnImageUriLoadedListener(OnImageUriLoadedListener mOnImageUriLoadedListener) {
        this.mOnImageUriLoadedListener = mOnImageUriLoadedListener;
    }
    @Override
    protected BitmapResult doInBackground(Void... voids) {
        BitmapResult bitmapResult = new BitmapResult();

      try {

          DataFile dataFile = new DataFile();
          String name = new File(mImageInfo.getAbsoluteFilePathUri().getPath()).getName();
          dataFile.setName(name);
          dataFile.setUri(mImageInfo.getAbsoluteFilePathUri());
//        Bitmap bitmap = mDataApi.getBitmapFromAbsolutePathUriGallery(context,Uri.parse(dataFile.getUri()),0,0).getBitmap();
//        boolean result = mDataApi.saveImageInCache(bitmap,100);


          if (mOnImageLoadedListener != null) {
              //load image
              Bitmap bitmapRes = mDataApi.getBitmapFromAbsolutePathUri(context, mImageInfo.getAbsoluteFilePathUri(), 0, 0).getBitmap();

              bitmapResult.setBitmap(bitmapRes);
              mImageInfo.setWidth(bitmapRes.getWidth());
              mImageInfo.setHeight(bitmapRes.getHeight());
              bitmapResult.setContentUri(mImageInfo.getAbsoluteFilePathUri());

          } else {
              Uri uri = mDataApi.copyImageFromGalleryToCache(dataFile);
//        bitmapResult.setContentUri(mDataApi.getImageUriFromCache(dataFile.getName()));
              bitmapResult.setContentUri(uri);
          }

      }catch (Throwable t){
          bitmapResult.setError(t);
      }

//       Bitmap bitmapRes= mDataApi.getBitmapFromAbsolutePathUri(context, mImageInfo.getAbsoluteFilePathUri()).getBitmap();


//        mUri = mImageInfo.getImageUri();
//        bitmapSampled =ImageUtils.decodeSampledBitmap(context, mUri, width, height);

//        bitmapSampled =ImageUtils.decodeSampledBitmap(context, mUri, width, height);
        return bitmapResult;






    }

    @Override
    protected void onPostExecute(BitmapResult bitmapResult) {
        super.onPostExecute(bitmapResult);

        if(mOnImageUriLoadedListener!=null){
            mOnImageUriLoadedListener.onImageLoaded(bitmapResult);
        }

        if(mOnImageLoadedListener!=null){
            mOnImageLoadedListener.onImageLoaded(bitmapResult);
        }
    }
}
