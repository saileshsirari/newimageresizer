package apps.sai.com.imageresizer.settings;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import apps.sai.com.imageresizer.BasePresenter;
import apps.sai.com.imageresizer.BaseView;
import apps.sai.com.imageresizer.listener.OnPreferenceChangedListener;

/**
 * Created by sailesh on 15/02/18.
 */

public interface SettingsContract {


    public interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean b);

        void showResult(String result);
        void showError(int errorId);

        void showRateView();
        void showUpgradeView();


        void shareFeedback(Context context);
    }

    public interface Presenter extends BasePresenter<View> {


//            void onImageSelected(Intent data); //selected image from gallery or camera

        void handleRateClick();
        void handleUpgradeVClick();

        void autoSavedClicked(Context context, Object newValue);

        void rateUsClicked(Context context);

        void setOnPreferenceChangedListener(OnPreferenceChangedListener mOnPreferenceChangedListener);

        void changeOutputFolderClicked(FragmentActivity fragment, FragmentManager fragmentManager);

        void fileExtensionoPreferenceChanged(Context context, Object newValue);

        void shareFeedback(Context context);
    }
}
