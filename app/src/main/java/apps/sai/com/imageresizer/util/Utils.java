package apps.sai.com.imageresizer.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import apps.sai.com.imageresizer.BaseFragment;
import apps.sai.com.imageresizer.ImageResizeApplication;
import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.DataFile;
import apps.sai.com.imageresizer.data.FileApi;
import apps.sai.com.imageresizer.data.ImageInfo;
import apps.sai.com.imageresizer.resize.CropImageViewFragment;
import apps.sai.com.imageresizer.settings.SettingsManager;

/**
 * Created by sailesh on 05/11/17.
 */

public class Utils {
    private static final String TAG = "Utils";
    public static UiState mUiState;
    public static Uri mImgeUri;

    public static void addFragment(AppCompatActivity activity, Fragment fragment, int resId, boolean isBackStack) {
        FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(resId, fragment, fragment.getClass().getSimpleName());
        if (isBackStack) {
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        fragmentTransaction.commit();
    }

    public static void showFragment(AppCompatActivity activity, String fragmentClassSimpleName) {
        try {
            FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
            fragmentTransaction.show(activity.getSupportFragmentManager().findFragmentByTag(fragmentClassSimpleName));
            fragmentTransaction.commit();
        }catch (Exception e){
            Log.e(TAG, e.toString());
        }

    }

    public static void removeFragment(AppCompatActivity activity, Fragment fragment) {
        try {
            activity.getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        } catch (Exception e) {

        }
    }


    public static boolean isUpgradedMy() {
        if (ImageResizeApplication.getInstance().getIsUpgraded()) {
            return true;
        }
        return SettingsManager.getInstance().isLegacyUpgraded();
        //If something goes wrong, assume the user has the pro version
    }
    /**
     * In most cases you need only to set crop aspect ration and max size for resulting image.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
   /* public static UCrop basisConfig(@NonNull UCrop uCrop) {




        return uCrop;
    }*/

    /**
     * Sometimes you want to adjust more options, it's done via UCrop} class.
     *
     * @param - ucrop builder instance
     * @return - ucrop builder instance
     */
   /* public static UCrop advancedConfig(@NonNull UCrop uCrop) {
        UCrop.Options options = new UCrop.Options();



        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(true);*/

//        options.setBrightnessEnabled(mCheckBoxBrigtness.isChecked());
//        options.setContrastEnabled(mCheckBoxContrast.isChecked());
//        options.setSaturationEnabled(mCheckBoxSaturation.isChecked());
//        options.setSharpnessEnabled(mCheckBoxSharpness.isChecked());

        /*
        If you want to configure how gestures work for all UCropActivity tabs

        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        * */

        /*
        This sets max size for bitmap that will be decoded from source Uri.
        More size - more memory allocation, default implementation uses screen diagonal.

        options.setMaxBitmapSize(640);
        * */


       /*

        Tune everything (ﾉ◕ヮ◕)ﾉ*:･ﾟ✧

        options.setMaxScaleMultiplier(5);
        options.setImageToCropBoundsAnimDuration(666);
        options.setDimmedLayerColor(Color.CYAN);
        options.setCircleDimmedLayer(true);
        options.setShowCropFrame(false);
        options.setCropGridStrokeWidth(20);
        options.setCropGridColor(Color.GREEN);
        options.setCropGridColumnCount(2);
        options.setCropGridRowCount(1);
        options.setToolbarCropDrawable(R.drawable.your_crop_icon);
        options.setToolbarCancelDrawable(R.drawable.your_cancel_icon);

        // Color palette
        options.setToolbarColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setRootViewBackgroundColor(ContextCompat.getColor(this, R.color.your_color_res));

        // Aspect ratio options
        options.setAspectRatioOptions(1,
            new AspectRatio("WOW", 1, 2),
            new AspectRatio("MUCH", 3, 4),
            new AspectRatio("RATIO", CropImageView.DEFAULT_ASPECT_RATIO, CropImageView.DEFAULT_ASPECT_RATIO),
            new AspectRatio("SO", 16, 9),
            new AspectRatio("ASPECT", 1, 1));

       */

    /*    return uCrop.withOptions(options);
    }*/
    public static boolean isEmpty(String value) {

        return value == null || value.length() == 0;
    }

    public static void replaceFragment(AppCompatActivity activity, BaseFragment fragment, int resId, boolean isBackStack) {

        FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(resId, fragment, fragment.getClass().getSimpleName());
        if (isBackStack) {
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());

        }
        fragmentTransaction.commit();
    }



    public static CropImageViewFragment getCropFragment(AppCompatActivity activity, String imageUri) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        CropImageViewFragment cropFragment = CropImageViewFragment.newInstance( imageUri);
//        cropFragment.setImageUri(imageUri);
        fragmentManager
                .beginTransaction().addToBackStack(cropFragment.getClass().getSimpleName())
                .add(R.id.contentFrame, cropFragment, cropFragment.getClass().getSimpleName())
                .commit();

        return cropFragment;
    }

    public static Point calculateAspectRatioHeight(Point point, int desiredWidth) {


        Point destPoint = new Point();
        double orgHeight = point.y;
        double orgWidth = point.x;

        double ratio = orgHeight / orgWidth;
        double destHeight = ratio * desiredWidth;

        destPoint.y = (int) destHeight;
        destPoint.x = desiredWidth;


        return destPoint;


    }

    /***
     * calculate new width with respect to aspect ratio of old image and desired height
     * @param point
     * @param desiredHeight
     * @return
     */
    public static Point calculateAspectRatioWidth(Point point, int desiredHeight) {


        Point destPoint = new Point();
        double orgHeight = point.y;
        double orgWidth = point.x;

        double ratio = orgWidth / orgHeight;
        double destWidth = ratio * desiredHeight;

        destPoint.x = (int) destWidth;
        destPoint.y = desiredHeight;


        return destPoint;


    }

    public static String loadResolutionsFromAssets(Context context, int resId) {
        InputStream in = context.getResources().openRawResource(resId);
        BufferedReader bufferedReader = null;
        bufferedReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String s = null;
        try {

            while ((s = bufferedReader.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            {
                try {
                    bufferedReader.close();

                } catch (Exception e) {

                }
            }
        }

        return sb.toString();


    }

    public static String getPath(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    } else if (isExternalStorageDocument(uri)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }

            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // MediaStore (and general)
            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // File

            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * A copy of the Android internals insertImage method, this method populates the
     * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media
     * that is inserted manually gets saved at the end of the gallery (because date is not populated).
     *
     * @see android.provider.MediaStore.Images.Media#insertImage(android.content.ContentResolver, Bitmap, String, String).
     * If the MediaStore not available, we will redirect the file to our alternative source, the SD card.
     */
    public String deleteImageIntoGallery(Activity context, ContentResolver cr, Uri url) {
        boolean savedOnSD = false;
//            Uri url =imageInfo.getImageUri();
        if (url != null) {

            try {

                   /* final int takeFlags =  (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            context.getContentResolver().takePersistableUriPermission(url, takeFlags);
                        }
                    }
                    catch (SecurityException se){
                        se.printStackTrace();


                    }*/
                   /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        savedOnSD = DocumentsContract.deleteDocument(context.getContentResolver(), url);
                    }*/
                if (!savedOnSD) {
                    ImageInfo imageInfo = null;
                    DataApi dataApi = new FileApi(context);
                    imageInfo = Utils.getImageInfo(imageInfo, context, url, dataApi);
                    dataApi.deleteImageFile(context, imageInfo, null);


                    File file = new File(imageInfo.getAbsoluteFilePathUri().getPath());
                    if (file.exists()) {
                        savedOnSD = file.delete();
                    }

                }

                if (!savedOnSD) {
                    cr.delete(url, null, null);
                }


//                    insertImageIntoGallery(context.getContentResolver(),)
            } catch (Exception e1) {
                e1.printStackTrace();
            }
//                return storeToAlternateSd(source, title);
            // url = null;
        }
        return null;
    }


    public static Uri getTImageFilePathUri(Context context, Uri mUri) {


        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        // Get the cursor
        Cursor cursor = null;

        try {

            if (true) {
                String returned = (getPath(context, mUri));
                if (returned != null) {
                    File file = new File(returned);
                    if (file.exists()) {
                        return Uri.fromFile(new File(returned));
                    }
                }
                return mUri;
            }
            String wholeID = mUri.toString();
            int index = wholeID.indexOf("%3A");
            if (index != -1) {
                wholeID = wholeID.substring(wholeID.indexOf("%3A"));

                String id = wholeID.split("%3A")[1];

//             String[] column = { MediaStore.Images.Media.DATA };

// where id is equal to
                String sel = MediaStore.Images.Media._ID + "=?";

                cursor = context.getContentResolver().
                        query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                filePathColumn, sel, new String[]{id}, null);

                if (cursor != null && cursor.moveToFirst()) {
                    // Move to first row
//            cursor.moveToFirst();
//            mCachedUrlString = null;

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String imgDecodableString = cursor.getString(columnIndex);

                    if (imgDecodableString != null) {

                        return Uri.fromFile(new File(imgDecodableString));

                    }

                } else {

                    cursor = context.getContentResolver().query(mUri,
                            filePathColumn, null, null, null);

                }

            } else {

                cursor = context.getContentResolver().query(mUri,
                        filePathColumn, null, null, null);

            }

            if (cursor != null && cursor.moveToFirst()) {
                // Move to first row
//            cursor.moveToFirst();
//            mCachedUrlString = null;

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);

                if (imgDecodableString != null) {

                    return Uri.fromFile(new File(imgDecodableString));

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return mUri;


    }

    public static long getRealSizeFromUri(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.SIZE};
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            cursor.moveToFirst();
            return cursor.getLong(column_index);
        } catch (Exception e) {

        } finally {
            try {

                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                //ignored
            }
        }
        return 0;
    }


    // capture image orientation

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static int getImageOrientation(Context context, Uri imageUri
    ) {
        int rotate = ExifInterface.ORIENTATION_NORMAL;
        if (true) {
            return rotate;//to do
        }
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            ExifInterface exif = new ExifInterface(context.getContentResolver().openInputStream(imageUri));
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

//            Log.i("RotateImage", "Exif orientation: " + orientation);
//            Log.i("RotateImage", "Rotate value: " + rotate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static Bitmap getRotatedBitmap(Bitmap source, float angle) {

        if (true) {
            return source;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static ImageInfo getImageInfo(ImageInfo imageInfoOrg, Context context, Uri uri, DataApi dataApi) {
        try {
            ContentResolver resolver = context.getContentResolver();
            ImageInfo imageInfo = new ImageInfo();

            Uri path = uri;
            boolean isFilePath = false;
            if (!uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
                path = Utils.getTImageFilePathUri(context, uri);
            } else {
                isFilePath = true;
            }
            BitmapFactory.Options options = ImageUtils.decodeImageForOption(resolver, uri);
            imageInfo.setAbsoluteFilePath(path);
            imageInfo.setTobeDeletedUri(path);
            if (imageInfoOrg != null) {
                imageInfo.setAbsoluteThumbFilePath(imageInfoOrg.getAbsoluteThumbFilePath());
                imageInfo.setImageUri(imageInfoOrg.getImageUri());
            } else {
                imageInfo.setImageUri(uri);
            }
            imageInfo.setWidth(options.outWidth);
            imageInfo.setHeight(options.outHeight);
            if (isFilePath) {
                imageInfo.setFileSize(new File(path.getPath()).length());
            } else {
                imageInfo.setFileSize(getRealSizeFromUri(context, imageInfo.getImageUri()));
            }
            imageInfo.setFormatedFileSize(Utils.getFormattedFileSize(imageInfo));
            String filename = path.getPath().substring(path.getPath().lastIndexOf("/") + 1);
            if (filename.indexOf(".") == -1) {
                filename += SettingsManager.getInstance().getFileExtensionPref();
            }
            DataFile dataFile = new DataFile();
            dataFile.setName(filename);
            dataFile.setUri(imageInfo.getAbsoluteFilePathUri());
            imageInfo.setDataFile(dataFile);
            Uri uriThumb = null;
            if (imageInfo.getAbsoluteThumbFilePath() != null) {
                uriThumb = Uri.fromFile(new File(imageInfo.getAbsoluteThumbFilePath()));
            }
            if (uriThumb == null) {
                imageInfo.setAbsoluteThumbFilePath(getThumbFilePath(context, uri));
                if (imageInfo.getAbsoluteThumbFilePath() != null) {
                    uriThumb = Uri.fromFile(new File(imageInfo.getAbsoluteThumbFilePath()));
                } else if (imageInfo.getAbsoluteFilePathUri() != null) {
                    uriThumb = imageInfo.getAbsoluteFilePathUri();
                }
            }
            if (uriThumb != null) {
                try {

                    if (uriThumb.getScheme() != null && uriThumb.getScheme().equals(ContentResolver.SCHEME_FILE)) {
                        if (!new File(uriThumb.getPath()).exists()) {
                            return imageInfo;
                        }
                    }
                    ImageUtils.BitmapSampled result = ImageUtils.decodeSampledBitmap(context, uri, 100, 100);

                    if (result.bitmap != null) {
//                        File file = new File(uriThumb.getPath());
                        String s = uriThumb.getPath();
                        s = s.substring(s.lastIndexOf(File.separator) + File.separator.length());
//                        if(s.indexOf("_thumb")==-1) {
                        s = "_thumb" + s;

//                        }
                        if (!s.contains(".")) {
                            s = s + SettingsManager.getInstance().getFileExtensionPref();
                        }
                        dataFile = new DataFile();
                        dataFile.setName(s);
                        Bitmap bitmap = result.bitmap;


                        int orientation = 0;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            orientation = getImageOrientation(context, uri);
                        }
                        if (orientation != ExifInterface.ORIENTATION_NORMAL) {
                            bitmap = getRotatedBitmap(bitmap, orientation);
                        }
                        dataApi.saveImageInCache(dataFile, bitmap, 100);
                        uriThumb = dataApi.getImageUriFromCache(dataFile.getName());
                        imageInfo.setAbsoluteThumbFilePath(uriThumb.getPath());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            return imageInfo;

        } catch (Throwable e) {
            e.printStackTrace();
//            throw new RuntimeException("Failed to load sampled bitmap: " + uri + "\r\n" + e.getMessage(), e);
        }

        return null;

    }

    public static void hideFacebookBanner(Context context, final View view, int resid) {
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(this);


        // Find the Ad container
        LinearLayout adContainer = view.findViewById(resid);

        if (adContainer != null && adContainer.getChildCount() > 0) {
            adContainer.removeAllViews();
        }
    }

    public static void showFacebookBanner(Context context, final View view, int resid, String id) {

        if (Utils.isUpgradedMy()) {

            return;
        }
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(this);


        // Find the Ad container
        LinearLayout adContainer = view.findViewById(resid);

        // Instantiate an AdView view


        // Add the ad view to container
        if (adContainer.getChildCount() == 0) {
            AdView adView = new AdView(context, id, AdSize.BANNER_HEIGHT_50);

            adContainer.addView(adView);

            AdListener adListener = new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
// Ad error callback
                }

                @Override
                public void onAdLoaded(Ad ad) {
// Ad loaded callback
                }

                @Override
                public void onAdClicked(Ad ad) {
// Ad clicked callback
                }

                @Override
                public void onLoggingImpression(Ad ad) {
// Ad impression logged callback
                }
            };
// Request an ad
            adView.loadAd(adView.buildLoadAdConfig().withAdListener(adListener).build());

        }


    }


    private static String getThumbFilePath(Context context, Uri mUri) {
        String[] filePathColumn = {MediaStore.Images.Thumbnails.DATA};
        Bitmap bitmapRes = null;

        // Get the cursor
        Cursor cursor = null;

        try {


            String wholeID = mUri.toString();
            int index = wholeID.indexOf("%3A");
            if (index != -1) {
                wholeID = wholeID.substring(index);
                String id = wholeID.split("%3A")[1];

//             String[] column = { MediaStore.Images.Media.DATA };

// where id is equal to
                String sel = MediaStore.Images.Thumbnails.IMAGE_ID + "=?";

                cursor = context.getContentResolver().
                        query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                                filePathColumn, sel, new String[]{id}, null);

                if (cursor != null && cursor.moveToFirst()) {
                    // Move to first row
//            cursor.moveToFirst();
//            mCachedUrlString = null;

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String imgDecodableString = cursor.getString(columnIndex);

                    if (imgDecodableString != null) {

                        return imgDecodableString;

                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {

                cursor.close();
            }
        }

        return null;


    }

    public static String getFormattedFileSize(ImageInfo imageInfo) {
        String unit = "bytes";
        float fileSize = imageInfo.getFileSize();

//        if(fileSize/1024 >1) {
        fileSize = fileSize / 1024;//kb
        unit = "kb";
//        }
        if (fileSize / 1024 > 1) {
            fileSize = fileSize / 1024;//mb
            unit = "mb";

        }
        return String.format("%.1f %s", fileSize, unit);

//        return String.format(" %dx%d, %.1f %s",imageInfo.getWidth(),imageInfo.getHeight(),fileSize,unit);
    }

    /**
     * @return true if device is running API >= 30
     */
    public static boolean hasAndroid11() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }
}
