package apps.sai.com.imageresizer.resize;

import android.content.Context;
import android.graphics.Bitmap;

import apps.sai.com.imageresizer.BasePresenter;
import apps.sai.com.imageresizer.BaseView;
import apps.sai.com.imageresizer.data.BitmapResult;
import apps.sai.com.imageresizer.data.ImageInfo;
import apps.sai.com.imageresizer.data.ResolutionInfo;
import apps.sai.com.imageresizer.util.BitmapProcessingTask;


/**
 * Created by sailesh on 30/12/17.
 */

public interface ResizeContract {




        public interface View extends BaseView<Presenter>  {

            void setLoadingIndicator(boolean b);

            void showResult(String result);
            void showError(int errorId);

            void setSelectedImage(BitmapResult bitmapResult);

            public void setImageSelected(String imageUriString);

            Bitmap applyImageEffect(ImageInfo bitmap, ResizeFragment.IMAGE_PROCESSING_TASKS image_PROCESSING_tasks, BitmapProcessingTask.OnImageProcessedListener onImageProcessedListener, ResolutionInfo resolutionInfo);

            void saveImage();

            void shareImage(Context context , String mUrlString);
        }

        public interface Presenter extends BasePresenter<View> {


            public void setImageSelected(String imageUriString) ;

            void setSelectedImage(BitmapResult bitmapResult);


            Bitmap applyImageEffect(ImageInfo imageInfo, ResizeFragment.IMAGE_PROCESSING_TASKS image_PROCESSING_tasks, BitmapProcessingTask.OnImageProcessedListener onImageProcessedListener, ResolutionInfo resolutionInfo);


            void saveImage();

            void shareImage(Context context , String mUrlString);

        }



}
