package apps.sai.com.imageresizer.settings;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.listener.OnPreferenceChangedListener;
import apps.sai.com.imageresizer.select.MenuItem;
import apps.sai.com.imageresizer.select.SelectFragment;
import apps.sai.com.imageresizer.util.Utils;

/**
 * Created by sailesh on 15/02/18.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SettingsContract.View {

    SettingsAdaptor mSettingsAdaptor;

    SettingsContract.Presenter mSettingsPresenter;
    public static SettingsFragment newInstance() {

        Bundle args = new Bundle();

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mSettingsPresenter = new SettingsPresenter();
        mSettingsPresenter.takeView(this);

        if(mSettingsPresenter!=null){
            mSettingsPresenter.setOnPreferenceChangedListener(mOnPreferenceChangedListener);
        }
//        SelectActivity selectActivity = (SelectActivity)getActivity();
//        selectActivity.setCurrentFragment(this);


    }



   /* @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_settings,null);
        ButterKnife.bind(this,view);

//        fillMenuItems();
        return view;
    }*/
    OnPreferenceChangedListener mOnPreferenceChangedListener;

    public void setOnPreferenceChangedListener(OnPreferenceChangedListener mOnPreferenceChangedListener) {
        this.mOnPreferenceChangedListener = mOnPreferenceChangedListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
//        mRecyclerView = getActivity().findViewById(R.id.settings_recycler_view);



//        mSettingsPresenter = new SettingsPresenter();
//        mLayoutManager = new LinearLayoutManager(getContext());

        Preference autosavePreference = findPreference(SettingsManager.KEY_PREF_AUTO_SAVE);
        if (autosavePreference != null) {
            autosavePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    mSettingsPresenter.autoSavedClicked(SettingsFragment.this.getContext(),newValue);
                    return true;
                }
            });
        }

        Preference rateUsPreference = findPreference(SettingsManager.KEY_PREF_RATE_US);
        if (rateUsPreference != null) {
            rateUsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    mSettingsPresenter.rateUsClicked(SettingsFragment.this.getContext());


                    return true;
                }
            });
        }

            Preference shareFeedbackPreference = findPreference(SettingsManager.KEY_PREF_SHARE_FEEDBACK);
            if (shareFeedbackPreference != null) {
                shareFeedbackPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        mSettingsPresenter.shareFeedback(SettingsFragment.this.getContext());


                        return true;
                    }
                });

        }

        Preference shareAppPreference = findPreference(SettingsManager.KEY_PREF_SHARE_APP);
        if (shareAppPreference != null) {
            shareAppPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    shareApp(SettingsFragment.this.getContext());


                    return true;
                }
            });

        }


        Preference gridSizePreference = findPreference(SettingsManager.KEY_PREF_GRID_SIZE);
        if (gridSizePreference != null) {
            gridSizePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {

                    String value = (String) newValue;
                    SettingsManager.getInstance().setGridSize(value);

                    if(mOnPreferenceChangedListener!=null){
                        mOnPreferenceChangedListener.onPreferenceChanged();
                    }
                    return false;
                }
            });

        }

        Preference gridSizeApp = findPreference(SettingsManager.KEY_PREF_GRID_APPEARANCE);
        /*gridSizeApp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                 preference.setDefaultValue(SettingsManager.getInstance().getGridAppearnece());
                return false;
            }
        });*/
        if (gridSizeApp != null) {
            gridSizeApp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {

                    String value = (String) newValue;
                    SettingsManager.getInstance().setKeyPrefGridAppearance(value);

                    if(mOnPreferenceChangedListener!=null){
                        mOnPreferenceChangedListener.onPreferenceChanged();
                    }
                    return false;
                }
            });

        }
/*


        Preference fileExtensionoPreference = findPreference(SettingsManager.KEY_PREF_FILE_EXTENSION);
        if (fileExtensionoPreference != null) {
            fileExtensionoPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    mSettingsPresenter.fileExtensionoPreferenceChanged(SettingsFragment.this.getContext(),newValue);
                    return true;
                }
            });
        }*/
        Preference folderPreference = findPreference(SettingsManager.KEY_PREF_FOLDER_PATH);
        if (folderPreference != null) {
            folderPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                   if (  mayRequestExternalStorage() ==true) {
//                    mSettingsPresenter.rateUsClicked(SettingsFragment.this.getContext());


                       mSettingsPresenter.changeOutputFolderClicked(getActivity(), SettingsFragment.this.getActivity().getSupportFragmentManager());

                   }
                   return true;
                }
            });



        }

        Preference versionPreference = findPreference(SettingsManager.KEY_PREF_VERSION);
        if (versionPreference != null) {
          // versionPreference.setSummary(BuildConfig.VERSION_NAME);
        }


        Preference musicAppPreference = findPreference(SettingsManager.KEY_PREF_MUSIC_APP);
        if (musicAppPreference != null) {
            musicAppPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {

                    Intent  intent = new Intent(android.content.Intent.ACTION_VIEW);

                    //Copy App URL from Google Play Store.
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=frees.com.beautifulapps.media.music.musicplayer.search.player"));

                    startActivity(intent);

                   return true;
                }
            });



        }


    }


    @Override
    public void shareFeedback(Context context) {
        shareFeedbackGmail(context);
    }
    public void shareFeedbackGmail(Context context ) {


        Intent gmail = new Intent(Intent.ACTION_VIEW);
        gmail.setClassName("com.google.android.gm","com.google.android.gm.ComposeActivityGmail");
        gmail.putExtra(Intent.EXTRA_EMAIL, new String[] { "androbbmidlet@@gmail.com" });
//        gmail.setData(Uri.parse("jckdsilva@gmail.com"));
        gmail.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
//        gmail.setType("message/rfc822");
        PackageManager packageManager = getActivity().getPackageManager();
        if (gmail.resolveActivity(packageManager) != null) {

            try {
                startActivity(gmail);
                return;
            }catch (Exception e){

            }
        }


            Intent email = new Intent(android.content.Intent.ACTION_SEND);
            email.setType("message/rfc822");
            email.putExtra(Intent.EXTRA_EMAIL, new String[] { "androbbmidlet@gmail.com" });
            email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");

//        gmail.putExtra(Intent.EXTRA_TEXT, "");
            startActivity(email);

    }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_preferences);

    }
    public static void shareApp(Context context) {
        final String appPackageName = context.getPackageName();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out multiple image resizer app at : https://play.google.com/store/apps/details?id=" + appPackageName);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    private void fillMenuItems() {

        List<MenuItem> menuItemList = new ArrayList<>();


        menuItemList.add(MenuItem.newInstance( MenuItem.RATE_APP_ID , getString(R.string.rate_app) ,
                R.drawable.star));
//        menuItemList.add(MenuItem.newInstance( MenuItem.TAKE_PHOTO_ID , MenuItem.TAKE_PHOTO_TEXT ,
//                MenuItem.TAKE_PHOTO_RES_ID));
//        menuItemList.add(MenuItem.newInstance( MenuItem.RESIZED_PHOTO_ID , getString(R.string.resized_photo) ,
//                R.drawable.my_images_camera));


        mSettingsAdaptor = new SettingsAdaptor(getContext(), menuItemList, new SettingsAdaptor.OnMenuSelectedListener() {
            @Override
            public void onMenuSelected(MenuItem menuItem) {
                   if(menuItem.getId()==MenuItem.RATE_APP_ID){

                       mSettingsPresenter.handleRateClick();
//                    / / mSettingsPresenter.showMyImages((AppCompatActivity) getActivity(), MyImagesFragment.newInstance());
                }

            }
        });


//        mRecyclerView.setLayoutManager(mLayoutManager);

//        mRecyclerView.setAdapter(mSettingsAdaptor);






    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        appCompatActivity.getSupportActionBar().setDisplayShowCustomEnabled(false);
    }



    public static class SettingsAdaptor extends RecyclerView.Adapter<SettingsAdaptor.SettingsHolder>{


        List<MenuItem> mMenuItemList;
        Context mContext;
        OnMenuSelectedListener mMenuSelectedListener;
        public interface OnMenuSelectedListener{

            void onMenuSelected(MenuItem menuItem);
        }
        public SettingsAdaptor(Context context, List<MenuItem> menuItemList, OnMenuSelectedListener menuSelectedListener){
            mMenuItemList = menuItemList;
            this.mContext =context;
            this.mMenuSelectedListener = menuSelectedListener;
        }
        @Override
        public SettingsHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            return new SettingsHolder(LayoutInflater.from(mContext).inflate(R.layout.file_row,null));
        }

        @Override
        public void onBindViewHolder(SettingsHolder holder, int position) {
            TextView nameTextView = holder.nameTextView;
            ImageView imageView = holder.imageView;

            final MenuItem menuItem = mMenuItemList.get(position);
            nameTextView.setText(menuItem.getName());

            imageView.setImageResource(menuItem.getImageResourcePath());


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mMenuSelectedListener.onMenuSelected(menuItem);



                }
            });


        }

        @Override
        public int getItemCount() {
            return mMenuItemList.size();
        }

        class SettingsHolder extends RecyclerView.ViewHolder {

            TextView nameTextView;

            ImageView imageView;
            View itemView;
            public SettingsHolder(View itemView) {
                super(itemView);
                this.itemView= itemView;

                nameTextView = itemView.findViewById(R.id.text_name_menu);
                imageView = itemView.findViewById(R.id.image_menu);


            }
        }
    }
    public static final int REQUEST_WRITE_STORAGE = 10;
    public boolean mayRequestExternalStorage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (getActivity().checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && getActivity().checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                ) {
            return true;
        }

//        Snackbar.make(mView,R.string.permission_rationale,Snackbar.LENGTH_LONG).show();
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE,ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION}, REQUEST_WRITE_STORAGE);
//        }
        return false;
    }
    public void showRateDialog(Context context){


        if(false){

            Intent  intent = new Intent(android.content.Intent.ACTION_VIEW);

            //Copy App URL from Google Play Store.
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=apps.sai.com.imageresizer.demo"));

            startActivity(intent);

            return;
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setMessage(getString(R.string.rate_us));


        final AlertDialog alertDialog = dialogBuilder.create();
//    listView.setAdapter();

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent  intent = new Intent(android.content.Intent.ACTION_VIEW);

                //Copy App URL from Google Play Store.
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=apps.sai.com.imageresizer.demo"));

                startActivity(intent);

            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
        // final View dialogView = inflater.inflate(R.layout.alert_compress, null);


    }


    @Override
    public void onGalleryImageSelected(Intent data) {

    }

    @Override
    public void launchGalleryExternalApp(boolean singlePhoto) {

    }

    @Override
    public void setLoadingIndicator(boolean b) {

    }

    @Override
    public void showResult(String result) {

    }

    @Override
    public void showError(int errorId) {

    }

    @Override
    public void showRateView() {

        showRateDialog(getContext());

    }

    @Override
    public void showUpgradeView() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSettingsPresenter.dropView();
    }

    @Override
    public void onDestroyView() {
        Utils.showFragment((AppCompatActivity) getActivity(), SelectFragment.class.getSimpleName());
        super.onDestroyView();
    }
}
