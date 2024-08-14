package apps.sai.com.imageresizer.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;


/**
 * Created by sailesh on 03/01/18.
 */

public class ImageUtils {

    public static Bitmap scaleImage(Context context , Bitmap unscaledBitmap,int newWidth,int newHeight) {
        final long startTime = SystemClock.uptimeMillis();
//        int mDstWidth =(int)(unscaledBitmap.getWidth()*times);// (int)(times*context.getResources().getDimensionPixelSize(R.dimen.destination_width));
//        int  mDstHeight =(int)(unscaledBitmap.getHeight()*times);
//        int  mDstHeight =(int) (times*context.getResources().getDimensionPixelSize(R.dimen.destination_height));
        // Part 1: Decode image
        /*Bitmap unscaledBitmap = ScalingUtilities.decodeResource(getResources(), mSourceId,
                mDstWidth, mDstHeight, ScalingLogic.FIT);*/

        // Part 2: Scale image
        Bitmap scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, newWidth,
                newHeight, ScalingUtilities.ScalingLogic.FIT);

        // Calculate memory usage and performance statistics
        final int memUsageKb = (unscaledBitmap.getRowBytes() * unscaledBitmap.getHeight()) / 1024;
        final long stopTime = SystemClock.uptimeMillis();

        // Publish results
        unscaledBitmap.recycle();

        return scaledBitmap;
    }

    //The imagePath consist the path of the image from camereFunction()
    public static Bitmap scaleImage(Bitmap image, int newWidth, int newHeight, int interPolationType ) {

        try {

            //my image file
//        Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imagePath);

            //For Testing Purpose
            int image_w = image.getWidth();
            int image_h = image.getHeight();
            Log.d("Captured Image Prop: ", "Height = " + image_h + " Width = " + image_w);

            if (image != null) {


                Mat imageMat = new Mat();
                Mat image_res = new Mat();
                //changes bitmap to Mat
                org.opencv.android.Utils.bitmapToMat(image, imageMat);

                image = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

                Imgproc.resize(imageMat, image_res, new Size(newWidth, newHeight), 0, 0, interPolationType);


                org.opencv.android.Utils.matToBitmap(image_res, image);
//
                image_w = image.getWidth();
                image_h = image.getHeight();
                Log.d("Resize Image Prop: ", "Height = " + image_h + " Width = " + image_w);

//            return image;
            }
        }catch (Throwable t){
            return null;
        }
        return image;
    }

    public static byte[] imageToBytes(Bitmap bitmap) {


        byte[] imageInByte = null;

        int numOfbytes = bitmap.getByteCount();

        ByteBuffer buffer = ByteBuffer.allocate(numOfbytes);
        bitmap.copyPixelsToBuffer(buffer);
        imageInByte = buffer.array();

        return imageInByte;
    }




    public static Bitmap rotateImageClockWise(Bitmap bitmap) {
        Mat imageMat = new Mat();

        //changes bitmap to Mat
        org.opencv.android.Utils.bitmapToMat(bitmap, imageMat);

//        Mat imageRes =new Mat();
//                new Mat(imageMat.height(),imageMat.width(), CvType.CV_8U);

       // imageMat.copyTo(imageRes);



//            Core.rotate(imageMat, imageRes, Core.ROTATE_180); //ROTATE_180 or ROTATE_90_COUNTERCLOCKWISE

            // Rotate clockwise 90 degrees
            Core.flip(imageMat.t(), imageMat, 1);

            Bitmap destBitmap1 = Bitmap.createBitmap(imageMat.width(), imageMat.height(), Bitmap.Config.ARGB_8888);

            org.opencv.android.Utils.matToBitmap(imageMat, destBitmap1);

            return destBitmap1;




    }

    public static Bitmap sharpenImage(Bitmap bitmap){
        Mat imageMat = new Mat();

        //changes bitmap to Mat
        org.opencv.android.Utils.bitmapToMat(bitmap, imageMat);
          Mat dest = new Mat();

         Imgproc.GaussianBlur(imageMat, dest, new Size(0, 0), 3);
         Core.addWeighted(imageMat, 1.5, dest, -0.5, 0, dest);

        Bitmap destBitmap1 = Bitmap.createBitmap(dest.width(), dest.height(), Bitmap.Config.ARGB_8888);

        org.opencv.android.Utils.matToBitmap(dest, destBitmap1);

        return  destBitmap1;


    }

    public static Bitmap blurImage(Bitmap bitmap){
        Mat imageMat = new Mat();

        //changes bitmap to Mat
        org.opencv.android.Utils.bitmapToMat(bitmap, imageMat);
        Mat dest = new Mat();

        Imgproc.GaussianBlur(imageMat, dest, new Size(0, 0), 3);
//        Core.addWeighted(imageMat, 1.5, dest, -0.5, 0, dest);

        Bitmap destBitmap1 = Bitmap.createBitmap(dest.width(), dest.height(), Bitmap.Config.ARGB_8888);

        org.opencv.android.Utils.matToBitmap(dest, destBitmap1);

        return  destBitmap1;


    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
    // region: Inner class: BitmapSampled

    /** Holds bitmap instance and the sample size that the bitmap was loaded/cropped with. */
    public static final class BitmapSampled {

        /** The bitmap instance */
        public final Bitmap bitmap;



        /** The sample size used to lower the size of the bitmap (1,2,4,8,...) */
        final int sampleSize;

        public BitmapSampled(Bitmap bitmap, int sampleSize) {
            this.bitmap = bitmap;
            this.sampleSize = sampleSize;
        }
    }

    /**
     * Calculate the largest inSampleSize value that is a power of 2 and keeps both height and width
     * larger than the requested height and width.
     */
    private static int calculateInSampleSizeByReqestedSize(
            int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            while ((height / 2 / inSampleSize) > reqHeight && (width / 2 / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    /** Decode bitmap from stream using sampling to get bitmap with the requested limit. */
    public static ImageUtils.BitmapSampled decodeSampledBitmap(Context context, Uri uri, int reqWidth, int reqHeight) {

        try {
            ContentResolver resolver = context.getContentResolver();

            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options options = decodeImageForOption(resolver, uri);

            // Calculate inSampleSize
            options.inSampleSize =
                    Math.max(
                            calculateInSampleSizeByReqestedSize(
                                    options.outWidth, options.outHeight, reqWidth, reqHeight),
                            calculateInSampleSize(options,options.outWidth, options.outHeight));

            // Decode bitmap with inSampleSize set
            Bitmap bitmap = decodeImage(resolver, uri, options);

            return new ImageUtils.BitmapSampled(bitmap, options.inSampleSize);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load sampled bitmap: " + uri + "\r\n" + e.getMessage(), e);
        }
    }

    /**
     * Decode image from uri using given "inSampleSize", but if failed due to out-of-memory then raise
     * the inSampleSize until success.
     */
    private static Bitmap decodeImage(
            ContentResolver resolver, Uri uri, BitmapFactory.Options options)
            throws FileNotFoundException {
        do {
            InputStream stream = null;
            try {
                stream = resolver.openInputStream(uri);
                return BitmapFactory.decodeStream(stream, EMPTY_RECT, options);
            } catch (OutOfMemoryError e) {
                options.inSampleSize *= 2;
            } finally {
                closeSafe(stream);
            }
        } while (options.inSampleSize <= 512);
        throw new RuntimeException("Failed to decode image: " + uri);
    }
    /**
     * Close the given closeable object (Stream) in a safe way: check if it is null and catch-log
     * exception thrown.
     *
     * @param closeable the closable object to close
     */
    private static void closeSafe(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
    static final Rect EMPTY_RECT = new Rect();

    /** Decode image from uri using "inJustDecodeBounds" to get the image dimensions. */
    public static BitmapFactory.Options decodeImageForOption(ContentResolver resolver, Uri uri)
            throws FileNotFoundException {
        InputStream stream = null;
        try {
            stream = resolver.openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, EMPTY_RECT, options);
            options.inJustDecodeBounds = false;
            return options;
        } finally {
            closeSafe(stream);
        }
    }

    public static BitmapFactory.Options setupDimension(ContentResolver contentResolver,Uri uri) {
        ParcelFileDescriptor input = null;
        try {
            input = contentResolver.openFileDescriptor(uri, "r");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(
                    input.getFileDescriptor(),null, options);
            int mWidth = options.outWidth;
            int mHeight = options.outHeight;
            return options;
        } catch (FileNotFoundException ex) {
//            mWidth = 0;
//            mHeight = 0;
        } finally {
            closeSafe(input);
        }
        return null;
    }
}
