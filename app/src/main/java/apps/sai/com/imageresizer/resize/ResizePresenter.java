package apps.sai.com.imageresizer.resize;

import android.content.Context;
import android.content.Intent;

import apps.sai.com.imageresizer.data.BitmapResult;
import apps.sai.com.imageresizer.data.ImageInfo;
import apps.sai.com.imageresizer.data.ResolutionInfo;
import apps.sai.com.imageresizer.util.OnImageProcessedListener;

/**
 * Created by sailesh on 03/01/18.
 */

public class ResizePresenter implements ResizeContract.Presenter {

    ResizeContract.View mView;
    @Override
    public void takeView(ResizeContract.View view) {
        mView =view;
    }

    @Override
    public void dropView() {
     mView =null;
    }

    @Override
    public void setImageSelected(String imageUriString) {

        if(mView!=null) {

            mView.setImageSelected(imageUriString);
        }

    }

    @Override
    public void setSelectedImage(BitmapResult bitmapResult) {
        if(mView!=null) {
            mView.setSelectedImage(bitmapResult);
        }
    }

    @Override
    public BitmapResult applyImageEffect(ImageInfo bitmap, ImageProcessingTask imageProcessingTask, OnImageProcessedListener onImageProcessedListener, ResolutionInfo resolutionInfo) {
        if(mView!=null) {
            return mView.applyImageEffect(bitmap, imageProcessingTask, onImageProcessedListener, resolutionInfo);
        }
        return null;
    }

    @Override
    public void saveImage() {
        if(mView!=null) {
            mView.saveImage();
        }
    }

    @Override
    public void shareImage(Context context ,String mCachedUrlString) {
        if(mView!=null) {
            mView.shareImage(context, mCachedUrlString);
        }
    }

    @Override
    public void onGalleryImageSelected(Intent data) {
        if(mView!=null) {
            mView.onGalleryImageSelected(data);
        }

    }

    @Override
    public void launchgalleryExternalApp(boolean singlePhoto) {
        if(mView!=null) {

            mView.launchGalleryExternalApp(singlePhoto);
        }
    }
}
