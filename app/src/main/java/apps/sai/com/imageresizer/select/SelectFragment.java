package apps.sai.com.imageresizer.select;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;

import apps.sai.com.imageresizer.BaseFragment;
import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.FileApi;
import apps.sai.com.imageresizer.data.OpenCvFileApi;
import apps.sai.com.imageresizer.myimages.MyImagesFragment;
import apps.sai.com.imageresizer.resize.ResizeFragment;
import apps.sai.com.imageresizer.settings.SettingsFragment;
import apps.sai.com.imageresizer.util.UpgradeDialog;
import apps.sai.com.imageresizer.util.Utils;

/**
 * Created by sailesh on 03/01/18.
 */

public class SelectFragment extends BaseFragment implements SelectContract.View {

    RecyclerView mRecyclerView;
    SelectContract.Presenter mSelectPresenter;
    RecyclerView.LayoutManager mLayoutManager;
    ResizeAdaptor mResizeAdaptor;
    @Override
    public void showAd() {
        SelectActivity selectActivity = (SelectActivity) getActivity();
        // selectActivity.showFullScreenAd();
    }

    public static SelectFragment newInstance() {
        Bundle args = new Bundle();
        SelectFragment fragment = new SelectFragment();
        fragment.setArguments(args);
        return fragment;
    }
    DataApi mDataApi;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMyBaseLoaderCallback = new MyBaseLoaderCallback(getContext());
        mHandler.postDelayed(() -> {
            if (!OpenCVLoader.initDebug()) {
                Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
//                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, getContext(), mMyBaseLoaderCallback);
            } else {
                if (mMyBaseLoaderCallback != null) {
                    mMyBaseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                }
            }
        }, 1000);
        mLayoutManager = new LinearLayoutManager(getContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = view.findViewById(R.id.res_recycler_view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void showError(Throwable th) {

    }

    private void fillMenuItems() {
        List<MenuItem> menuItemList = new ArrayList<>();

        menuItemList.add(MenuItem.newInstance(MenuItem.SELECT_PHOTO_ID, getString(R.string.gallery),
                R.drawable.image_gallery));
//        menuItemList.add(MenuItem.newInstance( MenuItem.TAKE_PHOTO_ID , MenuItem.TAKE_PHOTO_TEXT ,
//                MenuItem.TAKE_PHOTO_RES_ID));
        menuItemList.add(MenuItem.newInstance(MenuItem.RESIZED_PHOTO_ID, getString(R.string.resized_photo),
                R.drawable.my_images_camera));

        menuItemList.add(MenuItem.newInstance(MenuItem.SETTINGS_ID, getString(R.string.settings_label),
                R.drawable.ic_settings));
        if (!Utils.isUpgradedMy()) {
            menuItemList.add(MenuItem.newInstance(MenuItem.REMOVE_ADS, getString(R.string.remove_ads),
                    R.drawable.ic_remove_ads_24dp));
        }

        menuItemList.add(MenuItem.newInstance(MenuItem.MORE_APPS, getString(R.string.more_apps),
                R.drawable.image_gallery));
        mResizeAdaptor = new ResizeAdaptor(getContext(), menuItemList, new ResizeAdaptor.OnMenuSelectedListener() {
            @Override
            public void onMenuSelected(MenuItem menuItem) {
                if (menuItem.getId() == MenuItem.SELECT_PHOTO_ID) {
                    mayRequestExternalStorage();
                    mSelectPresenter.launchgalleryExternalApp(false);
                } else if (menuItem.getId() == MenuItem.RESIZED_PHOTO_ID) {
                    mSelectPresenter.showMyImages((AppCompatActivity) getActivity(), MyImagesFragment.newInstance());
                } else if (menuItem.getId() == MenuItem.SETTINGS_ID) {
                    mSelectPresenter.showSettings((AppCompatActivity) getActivity(), SettingsFragment.newInstance());
                } else if (menuItem.getId() == MenuItem.MORE_APPS) {
                    mSelectPresenter.showMoreApps((AppCompatActivity) getActivity());
                } else if (menuItem.getId() == MenuItem.REMOVE_ADS) {
                    UpgradeDialog.getUpgradeDialog(getActivity()).show();
                }
            }
        });
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mResizeAdaptor);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSelectPresenter = new SelectPresenter();
        mSelectPresenter.takeView(this);
        SelectActivity selectActivity = (SelectActivity) getActivity();
        selectActivity.setCurrentFragment(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSelectPresenter.dropView();
    }

    private static final String TAG = SelectFragment.class.getSimpleName();

    private class MyBaseLoaderCallback extends BaseLoaderCallback {

        MyBaseLoaderCallback(Context context) {
            super(context);

        }

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    openCvLoaded = true;
                    BaseFragment.openCvLoaded = false;
                    if (getContext() == null) {
                        return;
                    }
                    try {
                        mDataApi = new OpenCvFileApi(getContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
                default: {

                }
                break;
            }
            try {
                mDataApi = new FileApi(getContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private MyBaseLoaderCallback mMyBaseLoaderCallback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select, null);
        mRecyclerView = view.findViewById(R.id.res_recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);
        fillMenuItems();
        return view;
    }

    Handler mHandler = new Handler();

    @Override
    public void proccessGalleryImage(Intent data) {
        mSelectPresenter.onGalleryImageSelected(data);
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
    public void selectPicture() {

    }

    //
    Intent mResizeIntent = new Intent();


    @Override
    public void onGalleryImageSelected(Intent data) {


        if (!mayRequestExternalStorage()) {
            Toast.makeText(getContext(), getString(R.string.permission_rationale), Toast.LENGTH_SHORT).show();
            return;
        }
        mResizeIntent = data;
        mSelectPresenter.newResizeView(mResizeIntent);

    }

    @Override
    public void startResizeView(Intent mResizeIntent) {
        ResizeFragment resizeFragment = ResizeFragment.newInstance(mResizeIntent);
        Utils.addFragment((AppCompatActivity) requireActivity(), resizeFragment, R.id.contentFrame, true);
    }

    @Override
    public void setSelectedImage(Bitmap selectedImage) {
        if (selectedImage != null) {

        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void checkPictureSizesNew() {
        android.hardware.Camera camera = android.hardware.Camera.open();
        android.hardware.Camera.Parameters parameters = camera.getParameters();
        List sizes = parameters.getSupportedPictureSizes();
        android.hardware.Camera.Size result = null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sizes.size(); i++) {
            result = (android.hardware.Camera.Size) sizes.get(i);
//            Log.i("PictureSize", "Supported Size. Width: " + result.width + "height : " + result.height);
            sb.append(result.width);
            sb.append("x");
            sb.append(result.height);
            sb.append("\n");

        }

        DataApi dataApi = FileApi.newInstance(getActivity());
        dataApi.savePrivateTextFile(getContext(), FileApi.RES_FILE_NAME, sb.toString());

    }

    private void checkPictureSizes() {
        android.hardware.Camera camera = android.hardware.Camera.open();
        android.hardware.Camera.Parameters parameters = camera.getParameters();
        List sizes = parameters.getSupportedPictureSizes();
        android.hardware.Camera.Size result = null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sizes.size(); i++) {
            result = (android.hardware.Camera.Size) sizes.get(i);
            sb.append(result.height);
            sb.append("x");
            sb.append(result.width);
            sb.append("\n");

        }
        DataApi dataApi = FileApi.newInstance(getActivity());
        dataApi.savePrivateTextFile(getContext(), FileApi.RES_FILE_NAME, sb.toString());
        mResizeIntent.putExtra(FileApi.RES_FILE_NAME, sb.toString());
    }

    public static class ResizeAdaptor extends RecyclerView.Adapter<ResizeAdaptor.ResizeHolder> {


        List<MenuItem> mMenuItemList;
        Context mContext;
        OnMenuSelectedListener mMenuSelectedListener;

        public interface OnMenuSelectedListener {

            void onMenuSelected(MenuItem menuItem);
        }

        public ResizeAdaptor(Context context, List<MenuItem> menuItemList, OnMenuSelectedListener menuSelectedListener) {
            mMenuItemList = menuItemList;
            this.mContext = context;
            this.mMenuSelectedListener = menuSelectedListener;
        }

        @NonNull
        @Override
        public ResizeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return new ResizeHolder(LayoutInflater.from(mContext).inflate(R.layout.file_row, null));
        }

        @Override
        public void onBindViewHolder(ResizeHolder holder, int position) {
            TextView nameTextView = holder.nameTextView;
            ImageView imageView = holder.imageView;
            final MenuItem menuItem = mMenuItemList.get(position);
            nameTextView.setText(menuItem.getName());
            imageView.setImageResource(menuItem.getImageResourcePath());
            holder.itemView.setOnClickListener(v -> mMenuSelectedListener.onMenuSelected(menuItem));
        }

        @Override
        public int getItemCount() {
            return mMenuItemList.size();
        }

        static class ResizeHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            ImageView imageView;
            View itemView;
            public ResizeHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
                nameTextView = itemView.findViewById(R.id.text_name_menu);
                imageView = itemView.findViewById(R.id.image_menu);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }
}


