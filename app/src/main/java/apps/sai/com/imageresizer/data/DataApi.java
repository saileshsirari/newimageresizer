package apps.sai.com.imageresizer.data;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.util.List;


/**
 * Created by sailesh on 30/12/17.
 */

public abstract class DataApi {
    public final static String SUFFIX_JPG = ".jpg";
    public final static String SUFFIX_JPEG = ".jpeg";
    public final static String SUFFIX_PNG = ".png";
    public Context context;
    public static String myFolderPath;

    public DataApi(Context context){
     this.context =context;
    }

    public abstract List<ImageInfo> getMyImages();

    public abstract boolean deleteImageFile(Activity activity,ImageInfo imageInfo, String thumbPathString);
    public abstract boolean deleteImageFiles(Activity activity,List<ImageInfo> imageInfoList);

    public abstract void deleteCache(Context context);

    public interface OnBitMapLoaded{

        void onBitmapLoaded(BitmapResult bitmap,String uri);
        void onLoadError(String uri,Exception e);
    }

    public abstract String  getFileContent(Context context, String uri);

    public abstract void  getBitmapFromAbsolutePathUriAsync(Context context, Uri fileUri,OnBitMapLoaded onBitMapLoaded);


    public abstract BitmapResult getBitmapFromAbsolutePathUri(Context context, Uri fileUri,int width,int height);

    public abstract Uri getAbsoluteImagePathUriFromCache(DataFile dataFile);
//    public abstract Uri storeImageInCache(Bitmap image) ;
    public abstract Uri storeImageInCache(Bitmap image,DataFile dataFile);
//    public abstract Uri getImageUriFromCache() ;
    public abstract Uri galleryDeletePic(Activity activity, Uri uri);
    public abstract Uri galleryAddPic(String path);


    public abstract Uri getImageUriFromCache(String fileName);



//    public abstract Uri getImageUriFromCacheWithFileProvider() ;

    public abstract Uri getImageUriFromCacheWithFileProvider(String fileName);
    public abstract Uri getMyFolderUriFromCache(String fileName);

    //    public abstract boolean copyImage(DataFile dataFileSrc, DataFile destDataFile) ;
    public abstract Uri copyImageFromCacheToGallery(Activity activity,DataFile destDataFile);
//    public abstract Uri copyDefaultImageFromCacheToGallery();
    public abstract Uri copyImageFromSrcToDets(Activity activity,DataFile srcDataFile, DataFile destDataFile);
    public abstract Uri copyImageFromGalleryToCache(DataFile srcDataFile);
//       public abstract Uri getSharedFileUri();
//    public abstract Uri getAbsoluteImagePathUriFromCache() ;

    public abstract String savePrivateTextFile(Context context, String fileName, String content) ;
    public abstract String getPrivateTextFileContent(Context context, String fileName) ;
//    public abstract  boolean saveImageInCache(Bitmap bitmapRes,int quality) ;
    public abstract  boolean saveImageInCache(DataFile dataFile,Bitmap bitmapRes,int quality) ;
    public abstract  boolean saveImageInCacheWithSizeLimit(DataFile dataFile,Bitmap bitmapRes,int quality,int maxSizeKb) ;
    public abstract BitmapResult getBitmapFromAbsolutePathUriGallery(Context context, Uri fileUri,int width,int height);

    public abstract String getCacheFolderPath();



    public abstract Bitmap scaleImage( Bitmap unscaledBitmap,int newWidth,int newHeight,int type) ;
//    public  abstract Bitmap saveImageInCacheOpenCV(Bitmap bitmap, int type,int quality);
//    public  abstract Bitmap saveImageInCacheOpenCV(DataFile dataFile,Bitmap bitmap, int type,int quality);

    }
