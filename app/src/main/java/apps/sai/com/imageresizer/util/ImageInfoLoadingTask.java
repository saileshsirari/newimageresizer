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

public   class ImageInfoLoadingTask extends AsyncTask<Void,Void ,ImageInfo> {
    private final Context context;
    OnImageInfoProcesedListener mOnImageInfoProcesedListener;

    private  ImageInfo mImageInfo;
    private final DataApi mDataApi;
    private TASKS mTask;

    public  enum TASKS{
        IMAGE_INFO_LOAD,
        IMAGE_FILE_DELETE,
        IMAGE_FILE_SAVE_CACHE_TO_GALLERY,
        IMAGE_FILE_SAVE_GALLERY_TO_MY_FOLDER

    }

    public interface OnImageInfoProcesedListener {
        /**
         * @param imageInfo
         */
        void onImageProcessed(ImageInfo imageInfo);
    }



    public ImageInfoLoadingTask(Context context, ImageInfo imageInfo,
                                DataApi dataApi, OnImageInfoProcesedListener imageInfoLoadedListener, TASKS tasks) {

        this.mImageInfo = imageInfo;
        this.context =context;
        this.mDataApi =dataApi;
        this.mOnImageInfoProcesedListener = imageInfoLoadedListener;
        this.mTask =tasks;

//        this.mDataFile =dataFile;

    }

    public void setOnImageUriLoadedListener(OnImageInfoProcesedListener mOnImageInfoProcesedListener) {
        this.mOnImageInfoProcesedListener = mOnImageInfoProcesedListener;
    }
    @Override
    protected ImageInfo doInBackground(Void... voids) {

        try {


            if (mTask == TASKS.IMAGE_INFO_LOAD) {

//                Uri orUri = mImageInfo.getImageUri();

                mImageInfo = Utils.getImageInfo(mImageInfo, context, mImageInfo.getImageUri(), mDataApi);

            } else if (mTask == TASKS.IMAGE_FILE_DELETE) {

//            imageInfo = Utils.getImageInfo(context, mImageInfo.getImageUri(), mDataApi);
                if (mImageInfo != null) {
                    String thumb = mImageInfo.getAbsoluteThumbFilePath();
                    if (mImageInfo.getAbsoluteFilePathUri() == null && mImageInfo.getImageUri() != null) {
                        mImageInfo = Utils.getImageInfo(mImageInfo, context, mImageInfo.getImageUri(), mDataApi);
                    }

                    if (mImageInfo != null) {

                        if (mImageInfo.getAbsoluteFilePathUri() != null) {
                       /* if(Build.VERSION.SDK_INT>=24.) {
                            boolean result = mDataApi.deleteImageFile(mImageInfo.getAbsoluteFilePathUri().getPath(), thumb);
                            mImageInfo.setDeleted(result);
                        }else{
                            boolean result = mDataApi.deleteImageFile(mImageInfo.getImageUri().getPath(), thumb);
                            mImageInfo.setDeleted(result);
                        }*/
                           /* if(Build.VERSION.SDK_INT>=24) {
                                boolean result = mDataApi.deleteImageFile(mImageInfo, thumb);
                                if(result==false){
                                    System.out.print("");
                                }
                                mImageInfo.setDeleted(result);

                            }else{*/
                                Uri orgUri = mImageInfo.getImageUri();
                                mImageInfo.setImageUri(mImageInfo.getAbsoluteFilePathUri());
//                            Toast.makeText(context,mImageInfo.getAbsoluteFilePathUri().toString())
                                boolean result = mDataApi.deleteImageFile((Activity) context, mImageInfo, thumb);
                                if(!result){

                                    System.out.print("");
                                }
                                mImageInfo.setDeleted(result);

                                mImageInfo.setImageUri(orgUri);
//                            }

                        }
                    }
                }
            } else if (mTask == TASKS.IMAGE_FILE_SAVE_CACHE_TO_GALLERY) {

                if (mImageInfo.getDataFile() != null) {


                    Uri uri = mDataApi.copyImageFromCacheToGallery((Activity) context,mImageInfo.getDataFile());
                    if (uri != null) {
                        mImageInfo.setSaved(true);
                    }
                }


            } else if (mTask == TASKS.IMAGE_FILE_SAVE_GALLERY_TO_MY_FOLDER) {
                DataFile destDatafile = new DataFile();
                if (mImageInfo.getAbsoluteFilePathUri() == null && mImageInfo.getImageUri() != null) {
                    mImageInfo = Utils.getImageInfo(mImageInfo, context, mImageInfo.getImageUri(), mDataApi);
                }
                if (mImageInfo.getDataFile() != null) {
                    destDatafile.setName(mImageInfo.getDataFile().getName());
                    destDatafile.setUri(mDataApi.getMyFolderUriFromCache(destDatafile.getName()));
//                    destDatafile =null;
                    Uri uri = mDataApi.copyImageFromSrcToDets((Activity) context,mImageInfo.getDataFile(), destDatafile);

                    if (uri != null) {
                        mImageInfo.setSaved(true);
                    }
                }
            }

        }catch (Throwable t){
            if(mImageInfo!=null){
                mImageInfo.setSaved(false);
                mImageInfo.setDeleted(false);
            }
            t.printStackTrace();
        }

//
        return mImageInfo;






    }

    @Override
    protected void onPostExecute(ImageInfo imageInfo) {
        super.onPostExecute(imageInfo);

        if(mOnImageInfoProcesedListener !=null){
            mOnImageInfoProcesedListener.onImageProcessed(imageInfo);
        }
    }
}
