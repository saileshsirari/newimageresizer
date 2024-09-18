package apps.sai.com.imageresizer.myimages;

import android.content.Context;

import java.util.List;

import apps.sai.com.imageresizer.BasePresenter;
import apps.sai.com.imageresizer.BaseView;
import apps.sai.com.imageresizer.data.ImageInfo;
import kotlinx.coroutines.flow.Flow;


/**
 * Created by sailesh on 30/12/17.
 */

public interface MyImagesContract {




        public interface View extends BaseView<Presenter> {

            void setLoadingIndicator(boolean b);

            void showResult(String result);
            void showError(int errorId);
            void setImages(List<ImageInfo> imageInfoList);



        }

        public interface Presenter extends BasePresenter<View> {


            public void showSelectedImage(ImageInfo imageInfo) ;

            void deleteImage(ImageInfo imageInfo);

            void shareImages(Context context, List<ImageInfo> imageInfoList);

            Flow<List<ImageInfo>> getImages(Context context);

        }



}
