package apps.sai.com.imageresizer.select;

import android.content.Intent;
import android.graphics.Bitmap;

import androidx.appcompat.app.AppCompatActivity;

import apps.sai.com.imageresizer.BaseFragment;
import apps.sai.com.imageresizer.BasePresenter;
import apps.sai.com.imageresizer.BaseView;
import apps.sai.com.imageresizer.settings.SettingsFragment;


/**
 * Created by sailesh on 30/12/17.
 */

public interface SelectContract {




        public interface View extends BaseView<Presenter> {

            void setLoadingIndicator(boolean b);

            void showResult(String result);
            void showError(int errorId);
            void selectPicture();



            void setSelectedImage(Bitmap bitmap);


            void startResizeView(Intent mResizeIntent);


        }

        public interface Presenter extends BasePresenter<View> {


//            void onImageSelected(Intent data); //selected image from gallery or camera


            void setSelectedImage(Bitmap bitmap);

            void newResizeView(Intent mResizeIntent);


            void showMyImages(AppCompatActivity appCompatActivity, BaseFragment baseFragment);

            void showSettings(AppCompatActivity activity, SettingsFragment settingsFragment);

            void showMoreApps(AppCompatActivity activity);
        }

}
