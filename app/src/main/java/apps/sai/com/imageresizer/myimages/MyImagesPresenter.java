package apps.sai.com.imageresizer.myimages;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.FileApi;
import apps.sai.com.imageresizer.data.ImageInfo;

/**
 * Created by sailesh on 20/01/18.
 */

public class MyImagesPresenter implements MyImagesContract.Presenter {

    MyImagesContract.View mView;
    @Override
    public void takeView(MyImagesContract.View view) {
        mView =view;
    }

    @Override
    public void launchgalleryExternalApp(boolean singlePhoto) {

    }

    @Override
    public void onGalleryImageSelected(Intent data) {

    }

    @Override
    public void dropView() {
    mView =null;
    }

    @Override
    public void showSelectedImage(ImageInfo imageInfo) {

    }

    @Override
    public void deleteImage(ImageInfo imageInfo) {

    }

    @Override
    public void shareImages(Context context, List<ImageInfo> imageInfoList) {

    }

    @Override
    public List<ImageInfo> getImages(Context context) {

        DataApi dataApi = new FileApi(context);
        List<ImageInfo> imageInfoList = dataApi.getMyImages();

//        for (int i = 0; i <imageInfoList.size() ; i++) {
//            ImageInfo imageInfo = imageInfoList.get(i);
//
//        }

        return imageInfoList;


    }
}
