package apps.sai.com.imageresizer.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.DataFile;
import apps.sai.com.imageresizer.settings.SettingsManager;


/**
 * Created by sailesh on 30/12/17.
 */

public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

    public static ArrayList<String> getDataFilesAsString(List<DataFile> dataFileList){
        ArrayList<String> fileList = new ArrayList<>();
        for(int i =0 ;i<dataFileList.size();i++){
            String uri =dataFileList.get(i).getUri().getPath();
            fileList.add(uri);
        }

        return  fileList;

    }

    public static List<DataFile> getDataFilesAsList(Iterable<DataFile> dataFileIterable) {
        Iterator<DataFile> dataFileIterator = dataFileIterable.iterator();
        List<DataFile> dataFileList = new ArrayList<>();
        while (dataFileIterator.hasNext()){
            DataFile dataFile = dataFileIterator.next();
            dataFileList.add(dataFile);
//
        }

        return dataFileList;
    }

    public static String readFileContent(File file) {
        BufferedReader reader =null;
        StringBuilder sb = new StringBuilder();
        try {
             reader = new BufferedReader(new FileReader(file));


            sb.append(reader.readLine());


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return sb.toString();
    }

    public static boolean storeImage(Bitmap image,File pictureFile) {
        if (pictureFile == null) {

            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return false;
        }
        try {
            if(pictureFile.exists()){
                pictureFile.delete();
                pictureFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(pictureFile,false);
            if(SettingsManager.getInstance().isFileExtensionJpg() ==true) {

                image.compress(Bitmap.CompressFormat.JPEG, 91, fos);
            }else{
                image.compress(Bitmap.CompressFormat.PNG, 100, fos);

            }
//            image.compress(Bitmap.CompressFormat.JPEG, 91, fos);
            fos.close();
            return  true;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

        return  false;
    }
    public static boolean storeImage(Bitmap image,File pictureFile,int quality) {
        if (pictureFile == null) {

            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return false;
        }
        try {
            if(pictureFile.exists()){
                pictureFile.delete();
                pictureFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(pictureFile,false);



            if(SettingsManager.getInstance().isJpg(pictureFile.getPath()) ==true) {

                image.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            }else{
                image.compress(Bitmap.CompressFormat.PNG, quality, fos);

            }
            fos.close();
            return  true;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

        return  false;
    }
    static int COMPRESSION_PERCENTAGE_START = 90;
    static int IMAGE_COMPRESSION_EXPECTED_MAX_ITERATIONS = 3;
    static int IMAGE_COMPRESSION_STEP_PERCENT = 4;
    public static boolean storeImageWithSizeLimit(Bitmap image,File pictureFile,int quality,int maxSizeKb) {
        if (pictureFile == null) {

            return false;
        }
        try {
            if(pictureFile.exists()){
                pictureFile.delete();
                pictureFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(pictureFile,false);



            if(SettingsManager.getInstance().isJpg(pictureFile.getPath()) ==true) {
                ByteArrayOutputStream outputStream;

                long maxSizeByte = ((long) maxSizeKb) * 1_000;

                byte[] compressed = null;
                int compression = COMPRESSION_PERCENTAGE_START;
                int sizeImage = image.getByteCount();
                if(maxSizeByte ==0){
                    //it is percentage of origin
                    if(sizeImage>100) {
                        compression = 75;

                        outputStream = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, compression, outputStream);
                        compressed = outputStream.toByteArray();
                        sizeImage=compressed.length;
                        if(sizeImage>100) {
                            maxSizeByte = sizeImage / 100;
                            maxSizeByte = maxSizeByte * quality;
                        }else{

                            maxSizeByte =sizeImage;
                        }
                    }else{
                        maxSizeByte =sizeImage;
                    }

                }else{

                    if(sizeImage>maxSizeKb) {
                        compression = 75;


                        outputStream = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, compression, outputStream);
                        compressed = outputStream.toByteArray();
                        sizeImage = compressed.length;
                    }
                }
//                image.compress(Bitmap.CompressFormat.JPEG, quality, fos);

                int iterations = 20;
                while (sizeImage > maxSizeByte && iterations>=0 && compression>10) {
                    // Just a counter
                    iterations--;

                    compression -= IMAGE_COMPRESSION_STEP_PERCENT;

                    outputStream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, compression, outputStream);
                    compressed = outputStream.toByteArray();
                    sizeImage=compressed.length;
                    quality =compression;
                }
//                if(sizeImage<maxSizeByte){
                    image = BitmapFactory.decodeByteArray(compressed,0,compressed.length);
                    image.compress(Bitmap.CompressFormat.JPEG, quality, fos);
//                }

            }else{
                image.compress(Bitmap.CompressFormat.PNG, quality, fos);

            }
            fos.close();
            return  true;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

        return  false;
    }
    public static boolean storeRawImage(Bitmap image,File pictureFile) {
        if (pictureFile == null) {

            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return false;
        }
      /*  try {
          *//*  if(pictureFile.exists()){
                pictureFile.delete();
                pictureFile.createNewFile();
            }*//*

            FileOutputStream fos = new FileOutputStream(pictureFile,false);
            byte[] byteData = new byte[2048];
            int length;
            while((length=inputStream.read(byteData))!=-1) {
                outputStream.write(byteData, 0, length);
            }
            inputStream.close();
            outputStream.close();
            fos.close();
            return  true;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }*/

        return  false;
    }

    public   static long getFileSize(Context context, Uri uri){
        long fileSize =0;
        // Uri uri  = data.getData();
        String scheme = uri.getScheme();
        if(scheme!=null) {
            System.out.println("Scheme type " + scheme);
            if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                try {
                    InputStream fileInputStream = context.getContentResolver().openInputStream(uri);
                    fileSize = fileInputStream.available();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (scheme.equals(ContentResolver.SCHEME_FILE)) {
                String path = uri.getPath();
                try {
                    File f = new File(path);
                    fileSize = f.length();
                } catch (Exception e) {
                    e.printStackTrace();
                }
//          System.out.println("File size in bytes"+f.length());
            }
        }else{
            //normal file
            String path = uri.toString();
            try {
                File f = new File(path);
                fileSize = f.length();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return fileSize;

    }

    public static boolean copyFileNew(DataFile srcDataFile, DataFile destDataFile)  {

        File sourceFile = new File(srcDataFile.getUri().getPath());
        File destFile = new File(destDataFile.getUri().getPath());




        BufferedReader source = null;
        BufferedWriter destination = null;

        try {

            if (!destFile.exists()) {
                destFile.createNewFile();
            }
            source = new BufferedReader(new FileReader(sourceFile));
            destination = new BufferedWriter(new FileWriter(destFile,false));
             int b =-1;

             String s = null;
              do{
                 s = source.readLine();
                 if(s!=null) {
                     destination.write(s);
                 }
             } while(s!=null) ;

            return  true;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {

            try{
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }}catch (Exception e){

            }
        }
        return  false;
    }

    public static Bitmap getBitmapFromUri(ContentResolver contentResolver,Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                contentResolver.openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
    public static Bitmap getBitmapFromAbsoluteFileUri(Uri fileUri){
        try {


            Bitmap bitmap =  BitmapFactory.decodeFile(fileUri.getPath());
            return  bitmap;
        }catch (Exception e){
            return  null;
        }

    }

    public static Bitmap getBitmapFromAbsoluteFileUri(ContentResolver contentResolver,Uri fileUri){
        try {


            Bitmap bitmap =  BitmapFactory.decodeStream(contentResolver.openInputStream(fileUri));
            return  bitmap;
        }catch (Exception e){
            return  null;
        }

    }


    public static Uri copyFileStream(DataFile destDataFile, Uri uri, Context context)
            throws IOException {
        InputStream is = null;
        OutputStream os = null;
        File destFile = new File(destDataFile.getUri().getPath());

        try {
            is = context.getContentResolver().openInputStream(uri);
            os = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);

            }

//         String sizeStr =   Utils.getRealSizeFromUri(context,uri);
//            if(sizeStr!=null){
//                long size = Long.valueOf(sizeStr);
//            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            is.close();
            os.close();
        }

        return Uri.fromFile(destFile);
    }
    public static Uri copyFile(Activity activity, DataFile srcDataFile, DataFile destDataFile, DataApi dataApi)  {



        File sourceFile = new File(srcDataFile.getUri().getPath());
        File destFile = new File(destDataFile.getUri().getPath());

        if(false){
            try {
                return copyFileStream(destDataFile, srcDataFile.getUri(), dataApi.context);
            }catch (Exception e){
                return null;
            }
        }




        FileChannel source = null;
        FileChannel destination = null;

        try {

            if (!destFile.exists()) {
                destFile.createNewFile();
            }
                long size =0;
                long srcSize = sourceFile.length();
                Uri destUri = dataApi.getImageUriFromCache(destDataFile.getName());
                //override cache image
                String folderDest = destFile.getParent();
//                String cachefolder = new File(dataApi.getImageUriFromCache().getPath()).getParent();
               String cachefolder = dataApi.getCacheFolderPath();

                if (!folderDest.equals(cachefolder)) {
                    if (!destUri.equals(destDataFile.getUri())) {

                        String name = destDataFile.getName();//get all similar files
                        String b[] = new File(folderDest).list();
//                        boolean existsDuplicate = false;
                        int count =0;

                        if (b != null) {
                            for (int i = 0; i < b.length; i++) {
                                if (b[i] != null && b[i].endsWith(name)) {
                                    File innerFile = new File(folderDest, b[i]);
                                    size = innerFile.length();
                                    count++;
                                    if (srcSize == size) {

//                                        existsDuplicate = true;
                                        //delete
                                        innerFile.delete();
                                        dataApi.galleryDeletePic(activity, Uri.fromFile(innerFile));
                                        count--;
                                        continue;
//                                        break;
                                    }
                                    //even after deleting some file may be of same name
//                                    existsDuplicate = false;

                                }

                            }

                            if (count != 0) {//no file with same size so need to create new file
                                //the images are not same  keep a copy
                                String time = getCurrentDate();
                                name = time + "_" + name;
//                            destDataFile.setName(name);
                                String folder = destFile.getParent();
                                destFile = new File(folder, name);
                            }
                        }
                    }
                }

            String scheme = srcDataFile.getUri().getScheme();
                if(scheme.startsWith(ContentResolver.SCHEME_FILE)) {

                    source = new FileInputStream(sourceFile).getChannel();
                    destination = new FileOutputStream(destFile, false).getChannel();
                    long bytes = destination.transferFrom(source, 0, source.size());
                    if (bytes <= 0) {
                        return null;
                    }
                }else {

                        destDataFile.setName(destFile.getName());
                        destDataFile.setUri(Uri.fromFile(destFile));

                        try {
                            return copyFileStream(destDataFile, srcDataFile.getUri(), dataApi.context);
                        } catch (Exception e) {
                            return null;
                        }

                }

            return  Uri.fromFile(destFile);
        } catch (Throwable e) {
            e.printStackTrace();
        }finally {

            try{
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }}catch (Exception e){

            }
        }
        return  null;
    }

    private static String getCurrentDate() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM_dd_yy_hh_mm_ss");
        return dateFormat.format(new Date());
    }

    public static boolean storeRawFile(byte[] data, File file) {
        try {
           FileOutputStream fileOutputStream = new FileOutputStream(file,false);
           fileOutputStream.write(data,0,data.length);
           fileOutputStream.close();
        } catch (Throwable e) {
            e.printStackTrace();
            return  false;
        }
        return  true;
    }

    public static byte []  getRawFileData( Uri uri) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            byte [] b = new byte[1024*10];
            int len =-1;
            FileInputStream fileInputStream = new FileInputStream(new File(uri.getPath()));
            while(  (len =fileInputStream.read(b))!=-1){
                byteArrayOutputStream.write(b,0,len);
            }
            fileInputStream.close();


        } catch (Throwable e) {
            e.printStackTrace();
            return  null;
        }
        return  byteArrayOutputStream.toByteArray();
    }
}
