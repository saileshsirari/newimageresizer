package apps.sai.com.imageresizer.select;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import apps.sai.com.imageresizer.BaseFragment;
import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.settings.SettingsFragment;
import apps.sai.com.imageresizer.util.Utils;

/**
 * Created by sailesh on 03/01/18.
 */

public class SelectPresenter implements SelectContract.Presenter {

    SelectContract.View mView;


    public void launchgalleryExternalApp(boolean singlePhoto) {

     mView.launchGalleryExternalApp(singlePhoto);
    }


   /* @Override
    public void onImageSelected(Intent data) {
    mView.onImageSelected(data);
    }*/

    @Override
    public void onGalleryImageSelected(Intent data) {
    mView.onGalleryImageSelected(data);
    }

    @Override
    public void setSelectedImage(Bitmap bitmap) {
        mView.setSelectedImage(bitmap);
    }

    @Override
    public void newResizeView( Intent mResizeIntent) {
        mView.startResizeView(mResizeIntent);
    }

    @Override
    public void showMyImages(AppCompatActivity appCompatActivity, BaseFragment baseFragment) {
        Utils.addFragment(appCompatActivity,baseFragment, R.id.contentFrame,true);
    }

    @Override
    public void takeView(SelectContract.View view) {

        mView =view;

    }
    boolean checkNotNull(SelectContract.View view){
        return view!=null;
    }

    @Override
    public void dropView() {
     mView =null;
    }

    @Override
    public void showSettings(AppCompatActivity activity, SettingsFragment settingsFragment) {
        Utils.addFragment(activity,settingsFragment, R.id.contentFrame,true);
        FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.hide(activity.getSupportFragmentManager().findFragmentByTag(SelectFragment.class.getSimpleName())).commit();
    }

    @Override
    public void showMoreApps(AppCompatActivity activity) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setMessage(activity.getString(R.string.pref_summary_music_app));


        final AlertDialog alertDialog = dialogBuilder.create();
//    listView.setAdapter();

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, activity.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent  intent = new Intent(android.content.Intent.ACTION_VIEW);

//                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=apps.sai.com.imageresizer.demo"));
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=frees.com.beautifulapps.media.music.musicplayer.search.player"));

                activity.startActivity(intent);

            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
       /* Intent  intent = new Intent(android.content.Intent.ACTION_VIEW);

        try {

            //Copy App URL from Google Play Store.
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=frees.com.beautifulapps.media.music.musicplayer.search.player"));

            activity.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }*/
    }
}
