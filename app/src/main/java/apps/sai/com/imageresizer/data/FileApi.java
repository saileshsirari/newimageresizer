package apps.sai.com.imageresizer.data;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.core.provider.DocumentsContractCompat;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.settings.SettingsManager;
import apps.sai.com.imageresizer.util.FileUtils;
import apps.sai.com.imageresizer.util.ImageUtils;
import apps.sai.com.imageresizer.util.Utils;


/**
 * Created by sailesh on 30/12/17.
 */

public class FileApi extends DataApi {
    private final static String TAG = "FileApi";

    private static final String CACHE_URI_PROVIDER = "apps.sai.com.imageresizer.fileprovider";

//    private final static String PREFIX = "resize_";


    protected static final String CACHE_FILE_NAME = "test_cached";
    Context context;
    private String PRIVATE_FOLDER_NAME;
//    private   String PUBLIC_FOLDER_NAME ;

    public FileApi(Context context) {


        super(context);

        try {
            if (context == null) {
                return;
            }
            this.context = context;
            PRIVATE_FOLDER_NAME = context.getString(R.string.non_shared_folder_name);
//             PUBLIC_FOLDER_NAME = context.getString(R.string.shared_folder_name);

            createFoldersIfRequired();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private boolean createFoldersIfRequired() {
        File f = new File(context.getFilesDir(), PRIVATE_FOLDER_NAME);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                return false;
            }
        }
        String rootPath = SettingsManager.getInstance().getFolderPath();//this will create folder if required
        File rootFolder = new File(rootPath);
        if (!rootFolder.exists()) {
            //might require permissions
            if (!rootFolder.mkdirs()) {
                return false;
            }
        }
        myFolderPath = rootPath;
        return true;
    }


    @Override
    public String getFileContent(Context context, String uri) {
        File file = new File(uri.toString());

        if (file.exists() == true) {

            return FileUtils.readFileContent(file);
        }

        return "";
    }


    @Override
    public Uri storeImageInCache(Bitmap image, DataFile dataFile) {


        if (!createFoldersIfRequired()) {
            return null;
        }

        String fileName = dataFile.getName();
        File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);


//        boolean result =  FileUtils.storeImage(image,file);
        boolean result = FileUtils.storeImage(image, file);

        if (result == true) {
//            Uri contentUri = FileProvider.getUriForFile(context, CACHE_URI_PROVIDER, file);
            return Uri.fromFile(file);
        }
        return null;
    }

    @Override
    public Bitmap scaleImage( Bitmap unscaledBitmap, int newWidth, int newHeight, int type) {
        return ImageUtils.scaleImage(unscaledBitmap, newWidth, newHeight);
    }
    public String getMyFolderPath() {
        if (myFolderPath == null) {
            myFolderPath = SettingsManager.getInstance().getFolderPath();
        }
        return myFolderPath;
    }

    public void setMyFolderPath(String myFolderPath) {
        this.myFolderPath = myFolderPath;
    }

    @Override
    public List<ImageInfo> getMyImages() {

        List<ImageInfo> imageInfoList = new ArrayList<>();
        if (!createFoldersIfRequired()) {
            return imageInfoList;
        }
        File file = new File(getMyFolderPath() + File.separator);
        if (!file.exists()) {
            return imageInfoList;
        }
        Uri uriBase = Uri.fromFile(file);
        String[] a = file.list();

        if (a != null) {
            for (String b : a) {
                File bfile = new File(file, b);
                if (bfile.length() == 0) {
                    bfile.delete();
                    continue;
                }
                if (b.endsWith(SUFFIX_JPEG) || b.endsWith(SUFFIX_JPG) || b.endsWith(SUFFIX_PNG)) {
                    try {
                        ImageInfo imageInfo = new ImageInfo();
                        Uri fileUri = Uri.withAppendedPath(uriBase, b);
                        Uri contentUri = null;
                        if (Build.VERSION.SDK_INT >= 24) {
                            contentUri = getUriFromPath(fileUri.getPath());
                        }
                        if (contentUri != null && !contentUri.toString().endsWith("-1")) {
                            imageInfo.setImageUri(contentUri);

                        } else {
                            if (new File(fileUri.getPath()).exists()) {
                                imageInfo.setImageUri(fileUri);
                            }
                        }
                        if (imageInfo.getImageUri() != null) {
                            imageInfoList.add(imageInfo);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return imageInfoList;
    }

    /**
     * Returns the Uri which can be used to delete/work with images in the photo gallery.
     *
     * @param filePath Path to IMAGE on SD card
     * @return Uri in the format of... content://media/external/images/media/[NUMBER]
     */
    private Uri getUriFromPath(String filePath) {
        long photoId = -1;
        Uri photoUri = MediaStore.Images.Media.getContentUri("external");

        String[] projection = {MediaStore.Images.ImageColumns._ID};
        // TODO This will break if we have no matching item in the MediaStore.
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(photoUri, projection, MediaStore.Images.ImageColumns.DATA + " LIKE ?", new String[]{filePath}, null);
            if (cursor.moveToFirst()) {

                int columnIndex = cursor.getColumnIndex(projection[0]);
                photoId = cursor.getLong(columnIndex);
            }
        } catch (Exception e) {

            return null;

        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return Uri.parse(photoUri.toString() + "/" + photoId);
    }

    private Uri getUriFromId(String id) {
        long photoId = -1;
        Uri photoUri = MediaStore.Images.Media.getContentUri("external");

        String[] projection = {MediaStore.Images.Media._ID};
        // TODO This will break if we have no matching item in the MediaStore.
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(photoUri, projection, MediaStore.Images.Media._ID + " = ? ", new String[]{id}, null);
            if (cursor.moveToFirst()) {

                int columnIndex = cursor.getColumnIndex(projection[0]);
                photoId = cursor.getLong(columnIndex);
            }
        } catch (Exception e) {

            return null;

        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (photoId != -1) {

            return Uri.parse(photoUri.toString() + "/" + photoId);
        }
        return null;
    }

   /* @Override
    public Uri storeImageInCache(Bitmap image) {

        if(!createFoldersIfRequired()){
            return  null;
        }

        String fileName  = getCacheFileName();
        File file = new File(context.getFilesDir()+File.separator+PRIVATE_FOLDER_NAME+File.separator,fileName);




        boolean result =  FileUtils.storeImage(image,file);
        if(result ==true){
//            Uri contentUri = FileProvider.getUriForFile(context, CACHE_URI_PROVIDER, file);
//            return contentUri;
            return Uri.fromFile(file);
        }
        return  null;
    }
*/
   /* @Override
    public Uri getImageUriFromCache() {

        if(!createFoldersIfRequired()){
            return  null;
        }

        try {

            String fileName =getCacheFileName();
            File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);
//            Uri contentUri = FileProvider.getUriForFile(context, CACHE_URI_PROVIDER, file);



            return Uri.fromFile(file);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }*/


    @Override
    public Uri copyImageFromSrcToDets(Activity activity, DataFile srcDataFile, DataFile destDataFile) {
        Uri uri = FileUtils.copyFile(activity, srcDataFile, destDataFile, this);

        if (uri != null) {
            galleryAddPic(uri.getPath());
        }

        return uri;
    }

    @Override
    public Uri getMyFolderUriFromCache(String fileName) {
        if (!createFoldersIfRequired()) {
            return null;
        }

        try {


//            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+PUBLIC_FOLDER_NAME+File.separator,fileName);
//            File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);
//            Uri contentUri = FileProvider.getUriForFile(context, CACHE_URI_PROVIDER, file);

            File file = new File(getMyFolderPath() + File.separator, fileName);

            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

   /* @Override
    public Uri getImageUriFromCacheWithFileProvider() {

        if(!createFoldersIfRequired()){
            return  null;
        }

        try {

            String fileName =getCacheFileName();
            File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);
            Uri contentUri = FileProvider.getUriForFile(context, CACHE_URI_PROVIDER, file);



            return contentUri;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }*/

    @Override
    public Uri getImageUriFromCacheWithFileProvider(String fileName) {
        if (!createFoldersIfRequired()) {
            return null;
        }

        try {

            File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);
            if (file.exists() == true) {
                Uri contentUri = FileProvider.getUriForFile(context, CACHE_URI_PROVIDER, file);
                return contentUri;
            }


            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Uri getImageUriFromCache(String fileName) {

        if (!createFoldersIfRequired()) {
            return null;
        }

        try {

            File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);
//            Uri contentUri = FileProvider.getUriForFile(context, CACHE_URI_PROVIDER, file);


            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* @Override
     public Uri getAbsoluteImagePathUriFromCache() {

         if(!createFoldersIfRequired()){
             return  null;
         }

         try {

             String fileName =CACHE_FILE_NAME+SettingsManager.getInstance().getFileExtensionPref();
             File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);
 //            Uri contentUri = FileProvider.getUriForFile(context, CACHE_URI_PROVIDER, file);



             return Uri.fromFile(file);
         }catch (Exception e){
             e.printStackTrace();
         }
         return null;
     }
 */
    @Override
    public Uri getAbsoluteImagePathUriFromCache(DataFile dataFile) {

        if (!createFoldersIfRequired()) {
            return null;
        }

        try {

            String fileName = dataFile.getName();
            File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);
//            Uri contentUri = FileProvider.getUriForFile(context, CACHE_URI_PROVIDER, file);


            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
   /* @Override
    public Uri getSharedFileUri() {
        return getImageUriFromCache();
    }*/

    public Uri galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);


        return contentUri;
    }

    @Override
    public String getCacheFolderPath() {
        return (context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME);
    }

    public Uri galleryDeletePic(Activity activity, Uri uri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = null;

        boolean isFile = uri.getScheme().equals(ContentResolver.SCHEME_FILE);

        File f = null;
        if (isFile) {
            f = new File(uri.getPath());
        }
        try {
            if (f != null) {
                contentUri = Uri.fromFile(f);
            } else {
                //content uri
                if (Utils.hasAndroid11()) {

                    ArrayList<Uri> collection = new ArrayList<>();
                    collection.add(uri);
                    // Perform the actual removal.
                    PendingIntent pendingIntent = MediaStore.createDeleteRequest(Objects.requireNonNull(activity).getContentResolver(), collection);
                    IntentSender sender = pendingIntent.getIntentSender();
                    try {
                        // Launch a system prompt requesting user permission for the operation.
                        activity.startIntentSenderForResult(sender, 101, null, 0, 0, 0, null);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        dismiss();
                    }
                }
                contentUri = uri;
            }
            mediaScanIntent.setData(contentUri);

            context.sendBroadcast(mediaScanIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return contentUri;
    }

    @Override
    public Uri copyImageFromCacheToGallery(Activity activity, DataFile destDataFile) {

        if (!createFoldersIfRequired()) {
            return null;
        }
        String fileName = destDataFile.getName();
//        fileName = PREFIX + fileName;
        File file = new File(context.getFilesDir()
                + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);

        DataFile srcDataFile = new DataFile();


        srcDataFile.setUri(Uri.fromFile(file));


        fileName = destDataFile.getName();


//         fileName = destDataFile.getName()+SUFFIX_JPEG;
//        fileName = PREFIX + System.currentTimeMillis()+fileName;
//        fileName = PREFIX + System.currentTimeMillis()+fileName;
        file = new File(getMyFolderPath() + File.separator, fileName);

//         file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
// +File.separator+PUBLIC_FOLDER_NAME+File.separator,fileName);
        destDataFile.setUri(Uri.fromFile(file));

        Uri result = copyImage(activity, srcDataFile, destDataFile);


        if (result != null) {
            galleryAddPic(result.getPath());
           /* if(Uri.fromFile(file).getPath().equals(result.getPath())==false){
                galleryDeletePic(Uri.fromFile(file).getPath());
            }*/


        }

        return result;
    }

   /*public String getCacheFileName(){
        return  CACHE_FILE_NAME+SettingsManager.getInstance().getFileExtensionPref();
    }*/
   /* @Override
    public Uri copyDefaultImageFromCacheToGallery(){

        if(!createFoldersIfRequired()){
            return  null;
        }
        DataFile destDataFile = new DataFile();
//        File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator);
//        String fileName = destDataFile.getName();
//        fileName = PREFIX + fileName;
//        File file = new File(context.getFilesDir()
//                + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);
        String fileName =CACHE_FILE_NAME+SettingsManager.getInstance().getFileExtensionPref();
        File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);
        DataFile srcDataFile = new DataFile();


        srcDataFile.setUri(Uri.fromFile(file));
        srcDataFile.setName(fileName);


        fileName = destDataFile.getName();


//         fileName = destDataFile.getName()+SUFFIX_JPEG;
//        fileName = PREFIX + System.currentTimeMillis()+fileName;
//        fileName = PREFIX + System.currentTimeMillis()+fileName;
        file = new File(getMyFolderPath()+File.separator,fileName);

//         file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
// +File.separator+PUBLIC_FOLDER_NAME+File.separator,fileName);
        destDataFile.setUri(Uri.fromFile(file));

        Uri result =  copyImage(srcDataFile,destDataFile);



        if(result!=null){
            galleryAddPic(result.getPath());
           *//* if(Uri.fromFile(file).getPath().equals(result.getPath())==false){
                galleryDeletePic(Uri.fromFile(file).getPath());
            }*//*




        }

        return result;
    }*/

    @Override
    public void deleteCache(Context context) {

        File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator);
        String[] b = file.list();
        if (b != null) {
            for (String s : b) {
                File fileInner = new File(file.getAbsolutePath(), s);
                if (fileInner.exists()) {
                    fileInner.delete();
                }
            }
        }
    }

    @Override
    public Uri copyImageFromGalleryToCache(DataFile srcDataFile) {

        if (!createFoldersIfRequired()) {
            return null;
        }
        String fileName = srcDataFile.getName();
//        fileName = PREFIX + fileName;
        File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator);

        DataFile destDataFile = new DataFile();


        destDataFile.setUri(Uri.withAppendedPath(Uri.fromFile(file), fileName));
        destDataFile.setName(fileName);


        return copyImage((Activity) context, srcDataFile, destDataFile);
    }


    @Override
    public boolean deleteImageFile(Activity activity, ImageInfo imageInfo, String thumbPathString) {
        File file = null;
        Uri uri = imageInfo.getImageUri();
        boolean isFile = uri.getScheme().equals(ContentResolver.SCHEME_FILE);

        if (isFile) {
            file = new File(uri.getPath());
            Uri u = Uri.fromFile(file);

            if (file.exists()) {
//                ImageInfo imageInfo = Utils.getImageInfo(null, context, Uri.fromFile(file), this);
                if (imageInfo.getAbsoluteThumbFilePath() != null) {
                    File fileThumb = new File(imageInfo.getAbsoluteThumbFilePath());
                    if (!fileThumb.delete()) {

                        imageInfo.setError("1 . file does not exists! " + uri.toString());
//                            return false; //ignore for now
                    }
                }


                if (!file.delete()) {
//                    imageInfo.setError("2 . file does not exists! " + uri.toString());
                    try {
                        if (Utils.hasAndroid11()) {
                            ArrayList<Uri> collection = new ArrayList<>();
                            Uri contentUri = getUriWithId(context, imageInfo.getOriginalContentUri());
                            collection.add(contentUri);
                            // Perform the actual removal.
                            PendingIntent pendingIntent = MediaStore.createTrashRequest(Objects.requireNonNull(activity).getContentResolver(), collection, true);
                            IntentSender sender = pendingIntent.getIntentSender();
                            try {
                                // Launch a system prompt requesting user permission for the operation.
                                activity.startIntentSenderForResult(sender, 101, null, 0, 0, 0, null);
                                return true;
                            } catch (Exception e) {
                                e.printStackTrace();
//                        dismiss();
                            }
                        }
                        int result = context.getContentResolver().delete(uri,
                                null, null);
                        if (result != -1) {
                            galleryDeletePic(activity, uri);

                            return true;
                        }


                    } catch (Exception e) {
                        imageInfo.setError("4. file does not exists! " + uri.toString() + " " + e.toString());
                        e.printStackTrace();
                    }

                    return false;
                }
                galleryDeletePic(activity, uri);
                if (thumbPathString != null) {
                    file = new File(thumbPathString);
                    if (!file.delete()) {
                        return true;
                    }


                }
                return true;

            } else {
                imageInfo.setError("3. file does not exists! " + uri.toString());
            }
        } else {
            //try content
            int result = -1;
            try {
                if (Utils.hasAndroid11()) {

                    ArrayList<Uri> collection = new ArrayList<>();
                    Uri contentUri = getUriWithId(context, imageInfo.getOriginalContentUri());
                    collection.add(contentUri);
                    // Perform the actual removal.
                    PendingIntent pendingIntent = MediaStore.createTrashRequest(Objects.requireNonNull(activity).getContentResolver(), collection, true);
                    IntentSender sender = pendingIntent.getIntentSender();
                    try {
                        // Launch a system prompt requesting user permission for the operation.
                        activity.startIntentSenderForResult(sender, 101, null, 0, 0, 0, null);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
//                        dismiss();
                    }
                }
                result = context.getContentResolver().delete(uri,
                        null, null);
                if (result != -1) {
                    galleryDeletePic(activity, uri);

                    return true;
                }


            } catch (Exception e) {
                imageInfo.setError("4. file does not exists! " + uri.toString() + " " + e.toString());
                e.printStackTrace();
            }


        }
        return false;
    }
   /* private Uri getUriFromId(Uri uri){

    }*/

    private Uri getUriWithId(Context context, Uri media_uri) {
        String id = DocumentsContractCompat.getDocumentId(media_uri).split(":")[1];
        return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Long.parseLong(id));
      /*  String[] column = {MediaStore.Images.Media._ID, String.valueOf(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)};

        Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, null, null, null);

      *//*  Cursor cursor = context.getContentResolver().query(media_uri, column,
                null, null, null);*//*
        cursor.moveToFirst();
        if(cursor.getCount()>0){
            int Media_ID = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
            return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Media_ID);
        }
        cursor.close();
        return null;*/
    }

    @Override
    public boolean deleteImageFiles(Activity activity, List<ImageInfo> imageInfoList) {
        ArrayList<Uri> collection = new ArrayList<>();
        for (ImageInfo imageInfo : imageInfoList) {
            File file;
            Uri uri = imageInfo.getImageUri();
            boolean isFile = uri.getScheme().equals(ContentResolver.SCHEME_FILE);
            if (imageInfo.getAbsoluteThumbFilePath() != null) {
                file = new File(imageInfo.getAbsoluteThumbFilePath());
                if (!file.delete()) {
                    return true;
                }
            }
            if (isFile) {
                file = new File(uri.getPath());

                if (file.exists()) {
                    if (imageInfo.getAbsoluteThumbFilePath() != null) {
                        File fileThumb = new File(imageInfo.getAbsoluteThumbFilePath());
                        if (!fileThumb.delete()) {
                            imageInfo.setError("1 . file does not exists! " + uri.toString());
                        }
                    }
                    if (!file.delete()) {
                        imageInfo.setError("2 . file does not exists! " + uri.toString());
                        return false;
                    }
                    galleryDeletePic(activity, uri);


                } else {
                    imageInfo.setError("3. file does not exists! " + uri.toString());
                }
            } else {
                //try content
                int result = -1;
                try {
                    if (Utils.hasAndroid11()) {
                        collection.add(uri);
                    } else {
                        result = context.getContentResolver().delete(uri,
                                null, null);
                        if (result != -1) {
//                            return true;
                        }
                    }
                } catch (Exception e) {
                    imageInfo.setError("4. file does not exists! " + uri.toString() + " " + e.toString());
                    e.printStackTrace();
                }

            }
        }
        if (collection.size() > 0 && Utils.hasAndroid11()) {
            ArrayList<Uri> collectionMediaUrls = new ArrayList<>();
            for (Uri a : collection) {
                String name = a.getPath();
                Log.d(TAG, name);
                Uri pa = getMediaUri(activity, a);
                if (pa != null) {
                    collectionMediaUrls.add(pa);
                }
            }
            if (!collectionMediaUrls.isEmpty()) {
                try {
                    PendingIntent pendingIntent = MediaStore.createDeleteRequest(Objects.requireNonNull(activity).getContentResolver(), collectionMediaUrls);
                    IntentSender sender = pendingIntent.getIntentSender();
                    activity.startIntentSenderForResult(sender, 101, null, 0, 0, 0, null);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    PendingIntent pendingIntent = MediaStore.createDeleteRequest(Objects.requireNonNull(activity).getContentResolver(), collection);
                    IntentSender sender = pendingIntent.getIntentSender();

                    activity.startIntentSenderForResult(sender, 101, null, 0, 0, 0, null);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static Uri getMediaUri(final Context context, final Uri uri) {
        // DocumentProvider
        String path = null;
        String idMedia = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    path = Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
                Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                idMedia = getIdColumn(context, mediaUri, path);
                if (idMedia != null) {
                    Log.d(TAG, idMedia);
                    return ContentUris.withAppendedId(
                            mediaUri,
                            Long.parseLong(idMedia));
                }

                // TODO: handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                // DownloadsProvider

                final String id = DocumentsContract.getDocumentId(uri);

                return ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.parseLong(id));
            }
        }
        if (isMediaPickerImage(uri)) {
            String mediaId = uri.getLastPathSegment();
            return ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    Long.parseLong(mediaId));
        }
        if (isMediaDocument(uri)) {
            // MediaProvider
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            Uri uriContent = null;
            if ("image".equals(type)) {
                uriContent = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                uriContent = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                uriContent = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{split[1]};

            path = getDataColumn(context, uriContent, selection,
                    selectionArgs);
            Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            idMedia = getIdColumn(context, mediaUri, path);
            if (idMedia != null) {
                Log.d(TAG, idMedia);
                return ContentUris.withAppendedId(
                        mediaUri,
                        Long.parseLong(idMedia));
            }

        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return uri;
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
            Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            idMedia = getIdColumn(context, mediaUri, path);
            if (idMedia != null) {
                Log.d(TAG, idMedia);
                return ContentUris.withAppendedId(
                        mediaUri,
                        Long.parseLong(idMedia));
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    public static boolean isMediaPickerImage(Uri uri) {
        return uri.getPath().contains("com.android.providers.media.photopicker");
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor
                        .getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    public static String getIdColumn(Context context, Uri uri, String path) {
        final String selection = MediaStore.Images.ImageColumns.DATA + "=?";
        final String[] selectionArgs = new String[]{path};
        Cursor cursor = null;
        final String column = MediaStore.Images.ImageColumns._ID;
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor
                        .getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    private Uri copyImage(Activity activity, DataFile dataFileSrc, DataFile destDataFile) {

//        return FileUtils.copyFile(dataFileSrc,destDataFile);

        return FileUtils.copyFile(activity, dataFileSrc, destDataFile, this);
    }

    public static FileApi newInstance(Context context) {
        return new FileApi(context);
    }

    final static public String RES_FILE_NAME = "res_camera.txt";

    @Override
    public String savePrivateTextFile(Context context, String fileName, String content) {

//        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+PUBLIC_FOLDER_NAME+File.separator,name);
        File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);

        try {


            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write(content);

            fileWriter.close();
        } catch (IOException fe) {

            return null;

        }

        return "";
    }


    @Override
    public String getPrivateTextFileContent(Context context, String fileName) {

        File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);

        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(file));

            String s = fileReader.readLine();
            StringBuilder sb = new StringBuilder();

            while (s != null) {
                sb.append(s);
                sb.append("\n");
                s = fileReader.readLine();

            }

            fileReader.close();

            return sb.toString();


        } catch (IOException fe) {


        }
        return null;
    }


    static int COMPRESSION_PERCENTAGE_START = 80;
    static int IMAGE_COMPRESSION_EXPECTED_MAX_ITERATIONS = 3;
    static int IMAGE_COMPRESSION_STEP_PERCENT = 5;

// For logging

    static byte[] compressCapture(byte[] capture, int maxSizeKB) {
        long maxSizeByte = ((long) maxSizeKB) * 1_000;

        if (capture.length <= maxSizeByte) return capture;

        byte[] compressed = capture;

        // Chosen arbitrarily so that we can compress multiple times to reach the expected size.
        int compression = COMPRESSION_PERCENTAGE_START;

        Bitmap bitmap = BitmapFactory.decodeByteArray(capture, 0, capture.length);
        ByteArrayOutputStream outputStream;
        int iterations = 0;
        while (compressed.length > maxSizeByte) {
            // Just a counter
            iterations++;

            compression -= IMAGE_COMPRESSION_STEP_PERCENT;

            outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, compression, outputStream);
            compressed = outputStream.toByteArray();

            if (iterations > IMAGE_COMPRESSION_EXPECTED_MAX_ITERATIONS) {
                break;
            }
        }

        if (iterations > IMAGE_COMPRESSION_EXPECTED_MAX_ITERATIONS) {
//            Log.w("Compression process has iterated more than expected, with " + iterations + " iterations.");
        }

        return compressed;
    }

    private Uri storeImageInCache(Bitmap image, DataFile dataFile, int quality) {


        if (!createFoldersIfRequired()) {
            return null;
        }

        String fileName = dataFile.getName();
        File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);


//        boolean result =  FileUtils.storeImage(image,file);
        boolean result = FileUtils.storeImage(image, file, quality);

        if (result == true) {
//            Uri contentUri = FileProvider.getUriForFile(context, CACHE_URI_PROVIDER, file);
            return Uri.fromFile(file);
        }
        return null;
    }

    private Uri storeImageInCacheWithSizeLimit(Bitmap image, DataFile dataFile, int quality, int maxSizeKb) {

        if (!createFoldersIfRequired()) {
            return null;
        }

        String fileName = dataFile.getName();
        File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);
        boolean result = false;

        if (SettingsManager.getInstance().isJpg(dataFile.getName())) {
            result = FileUtils.storeImageWithSizeLimit(image, file, quality, maxSizeKb);
        } else {
            result = FileUtils.storeImage(image, file, quality);
        }
        if (result) {
//            Uri contentUri = FileProvider.getUriForFile(context, CACHE_URI_PROVIDER, file);
            return Uri.fromFile(file);
        }
        return null;
    }

    @Override
    public boolean saveImageInCacheWithSizeLimit(DataFile dataFile, Bitmap bitmapRes, int quality, int maxSizeKb) {

        Uri uri = storeImageInCacheWithSizeLimit(bitmapRes, dataFile, quality, maxSizeKb);

        if (uri == null) {
            throw new RuntimeException("Unable to save image in cache");
        }
        bitmapRes.recycle();

//        String path = getAbsoluteImagePathFromCache();
//        mCachedUrlString =path;
        return true;
    }

    @Override
    public boolean saveImageInCache(DataFile dataFile, Bitmap bitmapRes, int quality) {

//

//        Uri uri = saveRawImageInCache(bitmapRes);

        Uri uri = storeImageInCache(bitmapRes, dataFile, quality);

        if (uri == null) {
            throw new RuntimeException("Unable to save image in cache");
        }
        bitmapRes.recycle();

//        String path = getAbsoluteImagePathFromCache();
//        mCachedUrlString =path;
        return true;
    }

  /*  @Override
    public boolean saveImageInCache(Bitmap bitmapRes, int quality) {



        DataFile dataFile = new DataFile();

        dataFile.setName(getCacheFileName());

        return saveImageInCache(dataFile,bitmapRes,quality);
//
//        return null;
    }*/


    @Override
    public void getBitmapFromAbsolutePathUriAsync(Context context, Uri fileUri, OnBitMapLoaded onBitMapLoaded) {
    }


    @Override
    public BitmapResult getBitmapFromAbsolutePathUri(Context context, Uri fileUri, int width, int height) {

        if (fileUri == null) {
            return null;
        }
        int orientation = Utils.getImageOrientation(context, fileUri);


        Bitmap bitmap = null;

        if (fileUri.getScheme() != null && fileUri.getScheme().startsWith("file")) {
            bitmap = FileUtils.getBitmapFromAbsoluteFileUri(fileUri);

        } else {
            DataFile dataFile = new DataFile();
            String name = new File(fileUri.getPath()).getName();
            dataFile.setName(name);
            dataFile.setUri(fileUri);

//             fileUri = copyImageFromGalleryToCache(dataFile);
            bitmap = FileUtils.getBitmapFromAbsoluteFileUri(context.getContentResolver(), fileUri);
            try {
//                 bitmap = FileUtils.getBitmapFromUri(context.getContentResolver(),fileUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BitmapResult bitmapResult = new BitmapResult();

        if (orientation != ExifInterface.ORIENTATION_NORMAL) {
            bitmap = Utils.getRotatedBitmap(bitmap, orientation);
        }
        bitmapResult.setBitmap(bitmap);
        bitmapResult.setContentUri(fileUri);
        return bitmapResult;
    }

    @Override
    public BitmapResult getBitmapFromAbsolutePathUriGallery(Context context, Uri fileUri, int width, int height) {

        BitmapResult bitmapResult = new BitmapResult();
        Bitmap bitmap = BitmapFactory.decodeFile(fileUri.toString());

        bitmapResult.setBitmap(bitmap);

       /*  Bitmap bitmap = FileUtils.getBitmapFromAbsoluteFileUri(fileUri);
         BitmapResult bitmapResult = new BitmapResult();
         bitmapResult.setBitmap(bitmap);
         bitmapResult.setContentUri(fileUri);*/
        return bitmapResult;
    }


    private Uri saveRawImageInCache(DataFile dataFile, Bitmap bitmap) {
        byte[] data = ImageUtils.imageToBytes(bitmap);
        String fileName = dataFile.getName();
        File file = new File(context.getFilesDir() + File.separator + PRIVATE_FOLDER_NAME + File.separator, fileName);
        boolean saved = FileUtils.storeRawFile(data, file);

        if (saved) {
            return Uri.fromFile(file);
        }
        return null;

    }

    /*@NonNull
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
    }*/
}
