package apps.sai.com.imageresizer.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;

import org.opencv.imgproc.Imgproc;

import apps.sai.com.imageresizer.data.BitmapResult;
import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.DataFile;
import apps.sai.com.imageresizer.data.ImageInfo;
import apps.sai.com.imageresizer.resize.ResizeFragment;

/**
 * Created by sailesh on 13/01/18.
 */

public class BitmapProcessingTask extends AsyncTask<Void, Void, BitmapResult> {
    private final Context context;
    private final ResizeFragment.IMAGE_PROCESSING_TASKS image_PROCESSING_tasks;
    private final boolean mAutoSave;
    private final int mKbEnteredValue;
    private OnImageProcessedListener mOnImageProcessedListener;

    private int width, height, maxResolution;
    private DataFile mDataFile;

    private ImageInfo mImageInfo;
    private DataApi mDataApi;

    private int mQuality;
    private Uri mUri;
    private boolean mMultipleTask;
//    private boolean mSaveImage =true;

    public interface OnImageProcessedListener {
        void onImageLoaded(BitmapResult bitmapResult, ImageInfo imageInfoOrg);
    }


    public BitmapProcessingTask(ImageInfo imageInfo, Context context,
                                int desiredWidth, int desiredHeight,
                                int maxResolution, DataFile dataFile,
                                ResizeFragment.IMAGE_PROCESSING_TASKS image_PROCESSING_tasks,
                                int mCompressPercentage, int mKbEnteredValue,
                                DataApi dataApi, boolean multipleTask, boolean autoSave) {

        this.context = context;
        this.width = desiredWidth;
        this.height = desiredHeight;
        this.mImageInfo = imageInfo;
        this.maxResolution = maxResolution;
        this.mDataFile = dataFile;
        this.image_PROCESSING_tasks = image_PROCESSING_tasks;
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

        Bitmap bitmap = null;
        BitmapResult bitmapResult = new BitmapResult();

//        Uri orgUri = mImageInfo.getImageUri();

        try {


            if (mImageInfo.getDataFile() == null) {
                mImageInfo = Utils.getImageInfo(mImageInfo, context, mImageInfo.getImageUri(), mDataApi);
                mDataFile = mImageInfo.getDataFile();

            }
            bitmap = mDataApi.getBitmapFromAbsolutePathUri(context, mImageInfo.getAbsoluteFilePathUri(),
                    mImageInfo.getWidth(), mImageInfo.getHeight()).getBitmap();


            int quality = 95;
            Bitmap bitmapRes = null;

            if (image_PROCESSING_tasks == ResizeFragment.IMAGE_PROCESSING_TASKS.SCALE) {

                int oldRes = mImageInfo.getWidth() * mImageInfo.getHeight();
                if (width == 0) {
                    //calculate new width
                    width = (int) Utils.calculateAspectRatioWidth(new Point(mImageInfo.getWidth(), mImageInfo.getHeight()), height).x;
                } else if (height == 0) {
                    height = (int) Utils.calculateAspectRatioHeight(new Point(mImageInfo.getWidth(), mImageInfo.getHeight()), width).y;

                }
                int newRes = width * height;

            /*long available = Runtime.getRuntime().freeMemory()/4;
            long memUsageOld = width*height*4 ;
//            memUsageOld/=bitmap.getHeight()*bitmap.getWidth();
            if(available<memUsageOld){

//                Toast.makeText(context,
//                        context.getString(R.string.not_enough_memory) ,Toast.LENGTH_SHORT).show();


                return null;
            }*/


                if (newRes > maxResolution || newRes == 0) {

//                Toast.makeText(context,context.getString(R.string.max_length_error),Toast.LENGTH_SHORT).show();

//                return null;
                }
                int type = Imgproc.INTER_LANCZOS4;
                if (oldRes > newRes) {
                    //down sampling
                    type = Imgproc.INTER_AREA;
                }

//                this.bitmap =bitmap;
//            String res = "( " + width + "x" + height + ")_";


                String name = mDataFile.getName();
                String newName = name;
                mDataFile.setName(newName);


                try {
                    bitmapRes = mDataApi.scaleImage(context, bitmap, width, height, type);

                } catch (Throwable e) {
                    e.printStackTrace();
                    bitmapResult.setError(e);


                }

                if (bitmapRes != null) {


                    if (mMultipleTask == false) {
                        DataFile dataFile = mImageInfo.getDataFile();
                        mDataApi.saveImageInCache(dataFile, bitmapRes, quality);
                        Uri path = mDataApi.getAbsoluteImagePathUriFromCache(dataFile);

                        mUri = path;
                        bitmapResult.setBitmap(bitmapRes);
                        mImageInfo.setAbsoluteFilePath(mUri);
                    } else {

                        DataFile dataFile = mImageInfo.getDataFile();

//                dataFile.setName(new SimpleDateFormat("MM_dd_yyyy_hh_mm_ss").format(new Date())+".jpg");
//                     name = new File(mImageInfo.getAbsoluteFilePathUri().getPath()).getName();

//                    dataFile.setName(name);
                        mDataApi.saveImageInCache(dataFile, bitmapRes, quality);
                        mUri = mDataApi.getAbsoluteImagePathUriFromCache(dataFile);
                        mImageInfo.setAbsoluteFilePath(mUri);

                    }
                }

//                    return bitmapRes;
            } else if (image_PROCESSING_tasks == ResizeFragment.IMAGE_PROCESSING_TASKS.COMPRESS) {


                quality = mQuality;
//                mDataApi = new FileApi(context);

//             mDataApi.saveImageInCache(bitmapRes,quality);
                if (mMultipleTask == false) {


//                    mDataApi.saveImageInCache(mImageInfo.getDataFile(), bitmap, quality);
                    int size = 0; //200 kb
                    if (mKbEnteredValue != 0) {
                        size = mKbEnteredValue;
                    }
                    mDataApi.saveImageInCacheWithSizeLimit(mImageInfo.getDataFile(), bitmap, quality, size);
//            bitmapRes = FileApi.getBitmapFromOpenCV(context,mDataApi.getAbsoluteImagePathFromCache());
//            mDataApi.saveImageInCacheOpenCV(bitmapRes, Imgcodecs.CV_IMWRITE_JPEG_QUALITY,quality);

                    Uri path = mDataApi.getAbsoluteImagePathUriFromCache(mImageInfo.getDataFile());
                    mUri = path;


                    mImageInfo.setAbsoluteFilePath(mUri);

                } else {
                   /* if(mDataFile.getUri().getScheme()!=null && mDataFile.getUri().getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                        mUri =    mDataApi.copyImageFromGalleryToCache(mDataFile);
                        mDataFile.setUri(mUri);
                    }*/

//                    DataFile dataFile = new DataFile();

//                dataFile.setName(new SimpleDateFormat("MM_dd_yyyy_hh_mm_ss").format(new Date())+".jpg");
//                    String name = new File(mImageInfo.getAbsoluteFilePathUri().getPath()).getName();

//                    dataFile.setName(name);
                    try {
                        int size = 0; //200 kb
                        if (mKbEnteredValue != 0) {
                            size = mKbEnteredValue;
                        }
                        mDataApi.saveImageInCacheWithSizeLimit(mImageInfo.getDataFile(), bitmap, quality, size);
                        mUri = mDataApi.getAbsoluteImagePathUriFromCache(mImageInfo.getDataFile());
                        mImageInfo.setAbsoluteFilePath(mUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }


//                    return bitmap;
            } else if (image_PROCESSING_tasks == ResizeFragment.IMAGE_PROCESSING_TASKS.BLUR) {
                bitmapRes = ImageUtils.blurImage(bitmap);

//                    return bitmapRes;
            } else if (image_PROCESSING_tasks == ResizeFragment.IMAGE_PROCESSING_TASKS.SHARPEN) {
                bitmapRes = ImageUtils.sharpenImage(bitmap);

//                    return bitmapRes;
            } else if (image_PROCESSING_tasks == ResizeFragment.IMAGE_PROCESSING_TASKS.ROTATE_CLOCKWISE) {
                bitmapRes = ImageUtils.rotateImageClockWise(bitmap);

//                    return bitmapRes;
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

               /* if (mImageInfo.getAbsoluteFilePathUri() == null && mImageInfo.getImageUri() != null) {
                    mImageInfo = Utils.getImageInfo(mImageInfo, context, mImageInfo.getImageUri(), mDataApi);
                }*/

                if (mAutoSave) {

                    DataFile destDatafile = new DataFile();

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
//                mImageInfo.setImageUri(orgUri);
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
       /* if(mBitmapProcessingTaskWeakReference!=null) {
            mBitmapProcessingTaskWeakReference.clear();
        }*/
    }
}
