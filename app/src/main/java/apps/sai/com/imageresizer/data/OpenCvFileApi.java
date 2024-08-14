package apps.sai.com.imageresizer.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import apps.sai.com.imageresizer.settings.SettingsManager;
import apps.sai.com.imageresizer.util.ImageUtils;

/**
 * Created by sailesh on 15/01/18.
 */

public class OpenCvFileApi extends FileApi {


    public OpenCvFileApi(Context context) {
        super(context);
    }
    @Override
    public boolean saveImageInCache(DataFile dataFile, Bitmap bitmapRes, int quality) {

       /* if(SettingsManager.getInstance().isFileExtensionJpg() ==true) {
            return saveImageInCacheOpenCV(dataFile,bitmapRes, Imgcodecs.CV_IMWRITE_JPEG_QUALITY, quality);
        }else{*/

            if(SettingsManager.getInstance().isJpg(dataFile.getName())==false) {
                return saveImageInCacheOpenCV(dataFile, bitmapRes, Imgcodecs.CV_IMWRITE_PNG_COMPRESSION, quality);
            }else {
                return saveImageInCacheOpenCV(dataFile,bitmapRes, Imgcodecs.CV_IMWRITE_JPEG_QUALITY, quality);

            }

//        }

//     return saveImageInCacheOpenCV(dataFile,bitmapRes,Imgcodecs.CV_IMWRITE_JPEG_QUALITY,quality);
//
//        return true;
    }

   /* @Override
    public boolean saveImageInCache(Bitmap bitmapRes, int quality) {





       if(SettingsManager.getInstance().isFileExtensionJpg() ==true) {
           return saveImageInCacheOpenCV(bitmapRes, Imgcodecs.CV_IMWRITE_JPEG_QUALITY, quality);
       }else{
           return saveImageInCacheOpenCV(bitmapRes, Imgcodecs.CV_IMWRITE_PNG_COMPRESSION, quality);

       }
//
//        return true;
    }*/




    private static BitmapResult getBitmapFromOpenCV(Context context,Uri fileUri) {
        Bitmap bitmapRes =null;


        try {

            Mat mat = Imgcodecs.imread(fileUri.getPath());
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
            bitmapRes = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
            org.opencv.android.Utils.matToBitmap(mat, bitmapRes);
            BitmapResult bitmapResult = new BitmapResult();
            bitmapResult.setBitmap(bitmapRes);
            bitmapResult.setAbsolutePathUri(fileUri);


            return bitmapResult;
        }catch (Exception e){
            e.printStackTrace();
        }catch (OutOfMemoryError oe){

            System.gc();
            throw new RuntimeException(oe);

        }
        return  null;
    }

    @Override
    public Bitmap scaleImage(Context context, Bitmap unscaledBitmap, int newWidth, int newHeight, int type) {
        return ImageUtils.scaleImage(unscaledBitmap,newWidth,newHeight,type);
    }


   /* private boolean saveImageInCacheOpenCV(Bitmap bitmap, int type, int quality){

        try {
            ArrayList<Integer> parameters;

            DataFile dataFile = new DataFile();

            dataFile.setName(getCacheFileName());
            parameters = new ArrayList();

            parameters.add(type);
            parameters.add(quality);
            MatOfInt par = new MatOfInt();
            par.fromList(parameters);


            Mat mat = new Mat();
            org.opencv.android.Utils.bitmapToMat(bitmap, mat);

//        createAlphaMat(mat);
       *//* if(type ==Imgcodecs.CV_IMWRITE_PNG_COMPRESSION){
            par = new MatOfInt(quality);
            Imgcodecs.imencode(".png", mat, matOfByte, par);

        }else {
            Imgcodecs.imencode(".jpg", mat, matOfByte, par);
        }

        mat = Imgcodecs.imdecode(matOfByte,Imgcodecs.CV_LOAD_IMAGE_COLOR);*//*
            Mat mIntermediateMat = new Mat();
            Imgproc.cvtColor(mat, mIntermediateMat, Imgproc.COLOR_BGR2RGB, 3);
            Imgcodecs.imwrite(getAbsoluteImagePathUriFromCache().getPath(), mIntermediateMat, par);
            mIntermediateMat.release();
            mat.release();

        }catch (Throwable t){
            return false;
        }
//        bitmap.recycle();
//
//        Bitmap bitmapRes = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
//        org.opencv.android.Utils.matToBitmap(mat, bitmapRes);



        return true;





//        Highgui.imwrite("temp.jpg", image_entry, parameters);
    }*/
    private boolean saveImageInCacheOpenCV(DataFile dataFile, Bitmap bitmap, int type, int quality){

        try {


            ArrayList<Integer> parameters;


            parameters = new ArrayList();

            parameters.add(type);
            parameters.add(quality);
            MatOfInt par = new MatOfInt();
            par.fromList(parameters);


            Mat mat = new Mat();
            org.opencv.android.Utils.bitmapToMat(bitmap, mat);

//        createAlphaMat(mat);
       /* if(type ==Imgcodecs.CV_IMWRITE_PNG_COMPRESSION){
            par = new MatOfInt(quality);
            Imgcodecs.imencode(".png", mat, matOfByte, par);

        }else {
            Imgcodecs.imencode(".jpg", mat, matOfByte, par);
        }

        mat = Imgcodecs.imdecode(matOfByte,Imgcodecs.CV_LOAD_IMAGE_COLOR);*/
            Mat mIntermediateMat = new Mat();
            Imgproc.cvtColor(mat, mIntermediateMat, Imgproc.COLOR_BGR2RGB, 3);
            Imgcodecs.imwrite(getAbsoluteImagePathUriFromCache(dataFile).getPath(), mIntermediateMat, par);
            mIntermediateMat.release();
            mat.release();

        }catch (Throwable e){
            e.printStackTrace();
            return false;
        }
//        bitmap.recycle();
//
//        Bitmap bitmapRes = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
//        org.opencv.android.Utils.matToBitmap(mat, bitmapRes);



        return true;





//        Highgui.imwrite("temp.jpg", image_entry, parameters);
    }



}
