package apps.sai.com.imageresizer.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

import apps.sai.com.imageresizer.BaseFragment;
import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.data.BitmapResult;
import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.DataFile;
import apps.sai.com.imageresizer.data.FileApi;
import apps.sai.com.imageresizer.data.ImageInfo;
import apps.sai.com.imageresizer.data.OpenCvFileApi;

/**
 * Created by sailesh on 20/01/18.
 */

public class ImageDetailFragment extends BaseFragment {
    private static final String IMAGE_URI_STRING ="_image_" ;
    private static final String IMAGE_URI_STRING_ORG ="_org" ;
    Handler mHandler = new Handler();
    ProgressBar mProgressBar;
    @Override
    public void showError(Throwable th) {
        
    }

    @Override
    public void showAd() {

    }

    @Override
    public void proccessGalleryImage(Intent data) {

    }

    public static ImageDetailFragment newInstance(Uri orgImageUri,Uri compressedImageUri) {

        Bundle args = new Bundle();


        if(compressedImageUri!=null) {
            args.putString(IMAGE_URI_STRING, compressedImageUri.toString());
        }
        if(orgImageUri!=null) {
            args.putString(IMAGE_URI_STRING_ORG, orgImageUri.toString());
        }


        ImageDetailFragment fragment = new ImageDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }
    DataApi mDataApi;
    boolean loading;
    int orgImageRotation = ExifInterface.ORIENTATION_NORMAL,processedImageRotation = ExifInterface.ORIENTATION_NORMAL;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(BaseFragment.openCvLoaded==true) {
            mDataApi = new OpenCvFileApi(getContext());
        }else{
            mDataApi = new FileApi(getContext());

        }

        Uri orgUri =getArguments().getString(IMAGE_URI_STRING_ORG)!=null?Uri.parse(getArguments().getString(IMAGE_URI_STRING_ORG)):null;

        if(orgUri!=null) {

            int orientation = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                orientation = Utils.getImageOrientation(getContext(),orgUri);
            }
            mImageInfoOrg = new ImageInfo();
            mImageInfoOrg.setImageUri(orgUri);
            mImageInfoOrg.setAbsoluteFilePath(orgUri);
            mImageInfoOrg = Utils.getImageInfo(mImageInfoOrg, getContext(),orgUri,mDataApi);
            if(orientation!= ExifInterface.ORIENTATION_NORMAL) {

                orgImageRotation = orientation;
//                if(orgUri.getScheme()!=null) {

                  if(false) {
                      DataFile dataFile = new DataFile();
                      dataFile.setUri(orgUri);
                      String path = orgUri.getPath();
                      String filename = path.substring(path.lastIndexOf("/") + 1);
                      dataFile.setName(filename);
                   /* if(orgUri.getScheme().equals(ContentResolver.SCHEME_FILE)==false) {

                      orgUri =   mDataApi.copyImageFromGalleryToCache(dataFile);

                    }*/
                      Bitmap bitmap = mDataApi.getBitmapFromAbsolutePathUri(getContext(), orgUri, mImageInfoOrg.getWidth(), mImageInfoOrg.getHeight()).getBitmap();
                      bitmap = Utils.getRotatedBitmap(bitmap, orientation);


                      mDataApi.saveImageInCache(dataFile, bitmap, 100);
                      orgUri = mDataApi.getImageUriFromCache(dataFile.getName());

                      mImageInfoOrg = Utils.getImageInfo(mImageInfoOrg, getContext(), orgUri, mDataApi);
                  }
//                    mImageInfoOrg.setImageUri(orgUri);
//                    mImageInfoOrg.setAbsoluteFilePath(orgUri);

                   // uriThumb = dataApi.getImageUriFromCache(dataFile.getName());
//                }
            }



        }
        Uri compressedUri =getArguments().getString(IMAGE_URI_STRING)!=null?Uri.parse(getArguments().getString(IMAGE_URI_STRING)):null;


        if(compressedUri!=null){
            mImageInfoCompressed = new ImageInfo();
            mImageInfoCompressed.setImageUri(compressedUri);
            mImageInfoCompressed = Utils.getImageInfo(mImageInfoCompressed, getContext(),compressedUri,mDataApi);

        }


    }
    public ImageView webviewOrg;
    public ImageView webviewCompressed;
    public TextView resTextView;
    public TextView sizeTextView;

    public TextView resTextViewCompressed;
    public TextView sizeTextViewCompressed;

    public TextView sepTextView;

    View parentCompressedView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        webviewOrg = view.findViewById(R.id.image_multiple);
        webviewCompressed = view.findViewById(R.id.image_multiple_compressed);
        resTextView = view.findViewById(R.id.text_name_resolution);
        sizeTextView = view.findViewById(R.id.text_name_size);
        resTextViewCompressed = view.findViewById(R.id.text_name_compressed_resolution);
        sizeTextViewCompressed = view.findViewById(R.id.text_name_compressed_size);
        sepTextView = view.findViewById(R.id.seperator);
        parentCompressedView = view.findViewById(R.id.parentCompressed);
        mProgressBar = view.findViewById(R.id.progressMyImages);

    }

    ImageInfo mImageInfoOrg,mImageInfoCompressed;
    public static ImageDetailFragment newInstance() {

        Bundle args = new Bundle();

        ImageDetailFragment fragment = new ImageDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }
    WeakReference<BitmapLoadingTask> mBitmapLoadingTaskWeakReference;

    private void  showImageView(final ImageView imageView, final ImageInfo mSingleFileImageInfo, final DataApi mDataApi) {

       /* if(true){
            return;
        }*/
        BitmapLoadingTask bitmapLoadingTask = new BitmapLoadingTask(getContext(),mSingleFileImageInfo,mDataApi,0,0);

        mBitmapLoadingTaskWeakReference = new WeakReference<>(bitmapLoadingTask);
        setLoadingIndicator(true);

        bitmapLoadingTask.setOnImageLoadedListener(new BitmapLoadingTask.OnImageLoadedListener() {
            @Override
            public void onImageLoaded(BitmapResult bitmapResult) {
                try {
                    if (bitmapResult != null) {
                        if (bitmapResult.getError() != null) {
                            if (bitmapResult.getError() instanceof OutOfMemoryError) {
                                showMessage(mHandler, getString(R.string.out_of_memory));

                            } else {
                                showMessage(mHandler, getString(R.string.unknown_error));


                            }

                            return;
                        }
                        Bitmap bitmap = bitmapResult.getBitmap();
                        if (bitmap == null) {
                            showMessage(mHandler, getString(R.string.unknown_error));
                            return;

                        }

                        int w = bitmap.getWidth();
                        int h = bitmap.getHeight();
                        int imageSize = imageView.getHeight();
                        if (w > imageSize && h > imageSize) {

                            if (w > h) {
                                h = (int) Utils.calculateAspectRatioHeight(new Point(w, h), imageSize).y;
                                w = imageSize;

                            } else {
                                w = (int) Utils.calculateAspectRatioWidth(new Point(w, h), imageSize).x;
                                h = imageSize;

                            }

                            try {

                                bitmap = mDataApi.scaleImage(getContext(), bitmap, w, h, 0);

                            } catch (Throwable e) {
                                e.printStackTrace();
                                bitmapResult.setError(e);
                                if (bitmapResult.getError() instanceof OutOfMemoryError) {
                                    showMessage(mHandler, getString(R.string.out_of_memory));

                                } else {
                                    showMessage(mHandler, getString(R.string.unknown_error));


                                }
                                return;


                            }

                        }
                        if (bitmap != null) {
                            mSingleFileImageInfo.setWidth(bitmap.getWidth());
                            mSingleFileImageInfo.setHeight(bitmap.getHeight());
//                 mSingleFileImageInfo.setFileSize();
                            imageView.setImageBitmap(bitmap);
                        }
                        setLoadingIndicator(false);

                    }
                }catch (Exception e){
                    showMessage(mHandler, getString(R.string.unknown_error));
                    setLoadingIndicator(false);
                    e.printStackTrace();
                }
            }
        });

        bitmapLoadingTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);



       /*  int w = mSingleFileImageInfo.getWidth();
         int h =mSingleFileImageInfo.getHeight();
         if(w>500 && h>500){
             while(w/2 >500 ){
                 w =w/2;
             }
             while(h/2 >500){
                 h =h/2;
             }
              ImageUtils.decodeSampledBitmap(getContext(),mSingleFileImageInfo.getImageUri(),w,h);
         imageView.setImageURI(mSingleFileImageInfo.getImageUri());
         }*/







    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.image_detail,null);
        webviewOrg = view.findViewById(R.id.image_multiple);
        webviewCompressed = view.findViewById(R.id.image_multiple_compressed);
        resTextView = view.findViewById(R.id.text_name_resolution);
        sizeTextView = view.findViewById(R.id.text_name_size);
        resTextViewCompressed = view.findViewById(R.id.text_name_compressed_resolution);
        sizeTextViewCompressed = view.findViewById(R.id.text_name_compressed_size);
        sepTextView = view.findViewById(R.id.seperator);
        parentCompressedView = view.findViewById(R.id.parentCompressed);

        Utils.showFacebookBanner(getContext(),view,R.id.banner_container,"179547122769778_179622146095609");

        if(mImageInfoOrg!=null){
//            showWebView(webviewOrg,mImageInfoOrg,mDataApi);
            showImageView(webviewOrg,mImageInfoOrg,mDataApi);
            showFIleSize(mImageInfoOrg,sizeTextView);//here too don't need file://
            sizeTextView.setText(String.format("Before %s ",sizeTextView.getText()));

        }
//        Utils.showFacebookBanner(getContext(),view,R.id.banner_container_top,"179547122769778_189237791800711");


        if( mImageInfoCompressed!=null){
            parentCompressedView.setVisibility(View.VISIBLE);
//            webviewCompressed.setVisibility(View.VISIBLE);
//            showWebView(webviewCompressed,mImageInfoCompressed,mDataApi);
            showImageView(webviewCompressed,mImageInfoCompressed,mDataApi);

            showFIleSize(mImageInfoCompressed,sizeTextViewCompressed);//here too don't need file://
            sizeTextViewCompressed.setText(String.format("After %s ",sizeTextViewCompressed.getText()));


        }

//        webviewCompressed.setImageURI(mImageInfoCompressed.getAbsoluteFilePathUri());

        setLoadingIndicator(true);



        setLoadingIndicator(false);
        return view;
    }

    public void setLoadingIndicator(final boolean b) {
        if(mHandler==null || mProgressBar ==null || getContext() ==null || isDetached()){
            return;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(b ==true){
                    mProgressBar.setVisibility(View.VISIBLE);
                }else{
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.clear();

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        appCompatActivity.getSupportActionBar().setDisplayShowCustomEnabled(false);
//        appCompatActivity.getSupportActionBar().show();
//        inflater.inflate(R.menu.myimages,menu);
    }
}
