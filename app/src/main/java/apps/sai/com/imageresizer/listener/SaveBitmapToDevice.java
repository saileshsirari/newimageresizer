package apps.sai.com.imageresizer.listener;

/**
 * Created by sailesh on 05/03/18.
 */

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.FileApi;
import apps.sai.com.imageresizer.data.ImageInfo;
import apps.sai.com.imageresizer.util.Utils;

/**
 * Will save our card (currently set as a bitmap) as a jpeg and save it into our devices
 * native photo gallery. Unfortunately, the default method for this will add the image to the bottom
 * of the gallery by default, that is no good. This more complex method that utilizes Android's
 * native methods will allow us to store the bitmap at the top of the gallery by setting it's
 * meta data to today's date.
 */
public class SaveBitmapToDevice extends AsyncTask<Bitmap, Void, String>{

//    private final ShareType type;
    private Context context;
    private Uri uri;

    public SaveBitmapToDevice(Context context ,Uri uri){
//        this.type = type;

        this.context =context;
        this.uri =uri;
    }

    @Override
    protected String doInBackground(Bitmap... cards) {

        if(uri==null) {
            return insertImageIntoGallery(context.getContentResolver(), cards[0], context.getString(R.string.gallery),
                    context.getString(R.string.gallery));
        }else{
            String result =  replaceImageIntoGallery((Activity) context,context.getContentResolver(), uri);

            if(savedOnSD){
                result =  insertImageIntoGallery(context.getContentResolver(), cards[0], context.getString(R.string.gallery),
                        context.getString(R.string.gallery));
            }
            return result;
        }

    }

    /**
     * A copy of the Android internals insertImage method, this method populates the
     * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media
     * that is inserted manually gets saved at the end of the gallery (because date is not populated).
     * @see android.provider.MediaStore.Images.Media#insertImage(android.content.ContentResolver, Bitmap, String, String).
     * If the MediaStore not available, we will redirect the file to our alternative source, the SD card.
     */
    public String insertImageIntoGallery(ContentResolver cr, Bitmap source, String title, String description) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        String stringUrl = null;    /* value to be returned */

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
                }

                long id = ContentUris.parseId(url);
                // Wait until MINI_KIND thumbnail is generated.
                Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                // This is for backward compatibility.
                storeThumbnail(cr, miniThumb, id, 50F, 50F, MediaStore.Images.Thumbnails.MICRO_KIND);
            } else {
                cr.delete(url, null, null);
                return storeToAlternateSd(source, title);
                // url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                return storeToAlternateSd(source, title);
                // url = null;
            }
        }

        savedOnSD = false;
        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
    }


    /**
     * A copy of the Android internals insertImage method, this method populates the
     * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media
     * that is inserted manually gets saved at the end of the gallery (because date is not populated).
     * @see android.provider.MediaStore.Images.Media#insertImage(android.content.ContentResolver, Bitmap, String, String).
     * If the MediaStore not available, we will redirect the file to our alternative source, the SD card.
     */
    public String replaceImageIntoGallery(Activity activity,ContentResolver cr, Uri url ) {

        if(true){
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
                    if (savedOnSD == false) {
                        ImageInfo imageInfo =null;
                        DataApi dataApi  =  new FileApi(context);
                        imageInfo = Utils.getImageInfo(imageInfo,context,url,dataApi);
                        dataApi.deleteImageFile(activity,imageInfo,null);



                        File file = new File(imageInfo.getAbsoluteFilePathUri().getPath());
                        if(file.exists()==true){
                            savedOnSD= file.delete();
                        }

                    }

                    if (savedOnSD == false){

                        int deleted = cr.delete(url, null, null);
                    if (deleted >= 1) {
                        savedOnSD = true;
                    }
                   }



//                    insertImageIntoGallery(context.getContentResolver(),)
                }catch (Exception e1){
                    e1.printStackTrace();
                }
//                return storeToAlternateSd(source, title);
                // url = null;
            }
            return null;
        }

      /*  ContentValues values = new ContentValues();
//        values.put(MediaStore.Images.Media.TITLE, title);
//        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
//        values.put(MediaStore.Images.Media.DESCRIPTION, description);
//        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis());
//        values.put(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,null);
//        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

//        Uri url = null;
        String stringUrl = null;    *//* value to be returned *//*

        try {
            long id =-1;
            String wholeID = url.toString();
            int index =wholeID.indexOf("%3A");
            if(index!=-1) {
                wholeID = wholeID.substring(wholeID.indexOf("%3A"));

                String ids = wholeID.split("%3A")[1];
                id= Integer.valueOf(ids);

            }else{
                id = ContentUris.parseId(url);
            }


//                 id = ContentUris.parseId(url);
            if(id!=-1) {
                int row = cr.update(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values,
                        MediaStore.Images.Media._ID, new String[]{id + ""});

                if (row > 0) {
                    if (source != null) {
                        OutputStream imageOut = cr.openOutputStream(url);
                        try {

                            source.compress(Bitmap.CompressFormat.PNG, 100, imageOut);
                        } finally {
                            imageOut.close();
                        }


                        // Wait until MINI_KIND thumbnail is generated.
                        Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                        // This is for backward compatibility.
                        storeThumbnail(cr, miniThumb, id, 50F, 50F, MediaStore.Images.Thumbnails.MICRO_KIND);
                    } else {
                        cr.delete(url, null, null);
//                return storeToAlternateSd(source, title);
                        // url = null;
                    }
                }
            }
        } catch (Exception e) {
            if (url != null) {

                try {

                    final int takeFlags =  (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            context.getContentResolver().takePersistableUriPermission(url, takeFlags);
                        }
                    }
                    catch (SecurityException se){
                        se.printStackTrace();
                    }

                    int deleted = cr.delete(url, null, null);
                    if(deleted>=1){
                        savedOnSD =true;
                    }

//                    insertImageIntoGallery(context.getContentResolver(),)
                }catch (Exception e1){
                    e1.printStackTrace();
                }
//                return storeToAlternateSd(source, title);
                // url = null;
            }
        }

        savedOnSD = false;
        if (url != null) {
            stringUrl = url.toString();
        }
                return stringUrl;

*/

        return  null;
    }

    /**
     * A copy of the Android internals StoreThumbnail method, it used with the insertImage to
     * populate the android.provider.MediaStore.Images.Media#insertImage with all the correct
     * meta data. The StoreThumbnail method is private so it must be duplicated here.
     * @see android.provider.MediaStore.Images.Media (StoreThumbnail private method).
     */
    private Bitmap storeThumbnail(
            ContentResolver cr,
            Bitmap source,
            long id,
            float width,
            float height,
            int kind) {

        // create the matrix to scale it
        Matrix matrix = new Matrix();

        float scaleX = width / source.getWidth();
        float scaleY = height / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(),
                source.getHeight(), matrix,
                true
        );

        ContentValues values = new ContentValues(4);
        values.put(MediaStore.Images.Thumbnails.KIND,kind);
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID,(int)id);
        values.put(MediaStore.Images.Thumbnails.HEIGHT,thumb.getHeight());
        values.put(MediaStore.Images.Thumbnails.WIDTH,thumb.getWidth());

        Uri url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream thumbOut = cr.openOutputStream(url);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            thumbOut.close();
            return thumb;
        } catch (FileNotFoundException ex) {
            Log.e("IMAGE_COMPRESSION_ERROR", "File not found");
            ex.printStackTrace();
            return null;
        } catch (IOException ex) {
            Log.e("IMAGE_COMPRESSION_ERROR", "IO Exception");
            ex.printStackTrace();
            return null;
        }
    }
    boolean savedOnSD;

    /**
     * If we have issues saving into our MediaStore, save it directly to our SD card. We can then interact with this file
     * directly, opposed to pulling from the MediaStore. Again, this is a backup method if things don't work out as we
     * would expect (seeing as most devices will have a MediaStore).
     *
     * @param src
     * @param title
     * @return - the file's path
     */
    private String storeToAlternateSd(Bitmap src, String title){
        if(src == null)
            return null;

        File sdCardDirectory = new File(Environment.getExternalStorageDirectory() + File.separator + "My Cards");
        if(!sdCardDirectory.exists())
            sdCardDirectory.mkdir();

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy - (hh.mm.a)", Locale.US);
        File image = new File(sdCardDirectory, title + " -- [" + sdf.format(new Date()) + "].jpg");
        try {
            FileOutputStream imageOut = new FileOutputStream(image);
            src.compress(Bitmap.CompressFormat.JPEG, 100, imageOut);
            imageOut.close();
            savedOnSD = true;
            return image.getAbsolutePath();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void onPostExecute(String url){
        if(url != null){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            if(savedOnSD){
                File file = new File(url);
                if(file.exists())
                    intent.setDataAndType(Uri.fromFile(file), "image/jpeg");
                else
                    return;
            }
            else
                intent.setDataAndType(Uri.parse(url), "image/jpeg");

            context.startActivity(intent);
        }
        else
            Toast.makeText(context, context.getString(R.string.error_compressing), Toast.LENGTH_SHORT).show();
    }

}
