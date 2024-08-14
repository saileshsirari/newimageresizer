package apps.sai.com.imageresizer.settings;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;

import java.io.File;

import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.data.FileApi;
import apps.sai.com.imageresizer.listener.OnPreferenceChangedListener;

/**
 * Created by sailesh on 15/02/18.
 */

public class SettingsPresenter implements  SettingsContract.Presenter {

    SettingsContract.View mView;


    @Override
    public void takeView(SettingsContract.View view) {
        this.mView =view;

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
    public void handleRateClick() {
      mView.showRateView();
    }

    @Override
    public void handleUpgradeVClick() {

    }

    @Override
    public void autoSavedClicked(Context context, Object newValue) {

        SettingsManager.getInstance().setAutoSaveImages((Boolean) newValue);
        if(mOnPreferenceChangedListener!=null){
            mOnPreferenceChangedListener.onPreferenceChanged();
        }

    }

    @Override
    public void shareFeedback(Context context) {
      mView.shareFeedback(context);
    }

    @Override
    public void fileExtensionoPreferenceChanged(Context context, Object newValue) {
        if(mOnPreferenceChangedListener!=null){
            mOnPreferenceChangedListener.onPreferenceChanged();
        }
    }

    @Override
    public void rateUsClicked(Context context) {
        handleRateClick();
    }


    OnPreferenceChangedListener mOnPreferenceChangedListener;
    @Override
    public void setOnPreferenceChangedListener(OnPreferenceChangedListener onPreferenceChangedListener) {
        this.mOnPreferenceChangedListener = onPreferenceChangedListener;
    }

    @Override
    public void changeOutputFolderClicked(FragmentActivity activity, FragmentManager fragmentManager) {
        File file = new File( new FileApi(activity).getMyFolderUriFromCache("test.txt").getPath());
        String root  = file.getParent();
        new FolderChooserDialog.Builder(activity)
                .chooseButton(R.string.md_choose_label)  // changes label of the choose button
                .initialPath(root)  // changes initial path, defaults to external storage directory
                .tag("output_folder")
                .allowNewFolder(true, R.string.new_folder)  // pass 0 in the second parameter to use default button label
                .show(activity.getSupportFragmentManager());
    }
}
