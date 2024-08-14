package apps.sai.com.imageresizer.myimages;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import apps.sai.com.imageresizer.BaseFragment;
import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.FileApi;
import apps.sai.com.imageresizer.data.ImageInfo;
import apps.sai.com.imageresizer.data.OpenCvFileApi;
import apps.sai.com.imageresizer.select.SelectActivity;
import apps.sai.com.imageresizer.settings.SettingsManager;
import apps.sai.com.imageresizer.util.ImageInfoLoadingTask;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by sailesh on 20/01/18.
 */

public class MyImagesFragment extends BaseFragment implements MyImagesContract.View, MyImagesAdaptor.OnImagesSelectedListener {

    public interface OnContextCreatedListener {
        void onContextCreated();
    }

    public interface OnImageDeletedListener {
        void onImageDeleted(ImageInfo imageInfo);

        void onAllImagesDeleted();
    }

    public static MyImagesFragment newInstance() {

        Bundle args = new Bundle();

        MyImagesFragment fragment = new MyImagesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    OnContextCreatedListener onContextCreatedListener;
    OnImageDeletedListener onImageDeletedListener;

    public void setOnContextCreatedListener(OnContextCreatedListener onContextCreatedListener) {
        this.onContextCreatedListener = onContextCreatedListener;
    }

    public void setOnImageDeletedListener(OnImageDeletedListener onImageDeletedListener) {
        this.onImageDeletedListener = onImageDeletedListener;
    }

    @Override
    public void showAd() {
        SelectActivity selectActivity = (SelectActivity) getActivity();
//        selectActivity.showFullScreenAd();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void showError(Throwable th) {

    }

    @Override
    public void proccessGalleryImage(Intent data) {

    }

    DataApi dataApi;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myImagesPresenter = new MyImagesPresenter();
        mImageInfoLoadingTaskList = new ArrayList<>();

        dataApi = BaseFragment.openCvLoaded ? new OpenCvFileApi(getContext()) : new FileApi(getContext());

        setHasOptionsMenu(true);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 101 && resultCode != -1) {
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    RecyclerView mRecyclerView;

    ProgressBar mProgressBar;

    TextView mNoImagesTextView;

    Handler mHandler = new Handler();

    MyImagesPresenter myImagesPresenter;
    MyImagesAdaptor myImagesAdaptor;
    Menu menu;

    List<ImageInfoLoadingTask> mImageInfoLoadingTaskList;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.resized_images_view, null);
        mRecyclerView = view.findViewById(R.id.multiple_image_recycler_view);
        mProgressBar = view.findViewById(R.id.progressMyImages);
        mNoImagesTextView = view.findViewById(R.id.no_images);
        setLoadingIndicator(false);
        final List<ImageInfo> imageInfoList = new ArrayList<>();
        int size = SettingsManager.getInstance().getGridSize();
        myImagesAdaptor = new MyImagesAdaptor(myImagesPresenter, this, imageInfoList, mRecyclerView, new GridLayoutManager(getContext(), size)
                , new MyImagesAdaptor.OnUiUpdateListener() {
            @Override
            public void onDataChanged(int newAdaptorSize) {
                if (newAdaptorSize == 0) {
                    showNoImagesMenu(menu, false);
                }
            }

            @Override
            public void onImageDeleted(ImageInfo imageInfo) {
                if (onImageDeletedListener != null) {
                    onImageDeletedListener.onImageDeleted(imageInfo);
                }

            }

            @Override
            public void onAllImagesDeleted(List<ImageInfo> mImageInfoList) {
                if (onImageDeletedListener != null) {
                    onImageDeletedListener.onAllImagesDeleted();
                }
            }
        }, () -> cancelLoading(false));

        myImagesAdaptor.setOnDetailedScreenLaunchedListener(() -> {
            menu.clear();

//                MyImagesFragment.super.onCreateOptionsMenu(menu,);
        });
        mRecyclerView.setAdapter(myImagesAdaptor);
       /* mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
             setLoadingIndicator(false);
            }
        },imageInfoList.size()*100);*/
//        setLoadingIndicator(true);


        if (onContextCreatedListener != null) {
            onContextCreatedListener.onContextCreated();
            onContextCreatedListener = null;//only one tim init required
        }

        return view;
    }

    public synchronized void loadMyImages() {

        Observable.fromCallable(() -> myImagesPresenter.getImages(getContext())).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<ImageInfo>>() {
                    @Override
                    public void accept(List<ImageInfo> imageInfoList) throws Exception {
                        myImagesAdaptor.setItems(imageInfoList);
                        if (imageInfoList.size() > 0) {
                            showNoImagesMenu(menu, true);
                        }


                        loadImages(imageInfoList);

                    }
                });
    }


    private void loadImages(List<ImageInfo> imageInfoList) {
        for (int i = 0; i < imageInfoList.size(); i++) {

            ImageInfo imageInfoInner = imageInfoList.get(i);


            ImageInfoLoadingTask imageInfoLoadingTask =
                    new ImageInfoLoadingTask(getContext(), imageInfoInner, dataApi
                            , new ImageInfoLoadingTask.OnImageInfoProcesedListener() {

                        @Override
                        public void onImageProcessed(ImageInfo imageInfo) {


                            if (imageInfo == null || imageInfo.getWidth() == 0 || imageInfo.getHeight() == 0) {
                                myImagesAdaptor.remove(imageInfo);
                                return;
                            }

//                                             imageInfoList.remove(i);
//                                             imageInfoList.add(i, procesedImageInfoInner);
                            myImagesAdaptor.setImageInfo(imageInfo);
                        /*
                          if(index==imageInfoList.size()-1){
                              setLoadingIndicator(false);
                              myImagesAdaptor.notifyDataSetChanged();

                          }*/


                        }
                    }, ImageInfoLoadingTask.TASKS.IMAGE_INFO_LOAD);
            imageInfoLoadingTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            mImageInfoLoadingTaskList.add(imageInfoLoadingTask);
//
        }
    }

    private volatile boolean mDone;

    public void cancelLoading(boolean delete) {
        mDone = true;
        cancelPendingTasks();
        myImagesAdaptor.cancelAllTasks();
        if (delete) {
            myImagesAdaptor.deleteAllItems();
        }
    }

    private void cancelPendingTasks() {

        if (mImageInfoLoadingTaskList != null) {

            for (int i = 0; i < mImageInfoLoadingTaskList.size(); i++) {
                ImageInfoLoadingTask imageInfoLoadingTask = mImageInfoLoadingTaskList.get(i);
                imageInfoLoadingTask.cancel(true);

            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);
        appCompatActivity.getSupportActionBar().setDisplayShowCustomEnabled(true);
//        appCompatActivity.getSupportActionBar().show();
        inflater.inflate(R.menu.myimages, menu);
        this.menu = menu;

        if (myImagesAdaptor.getItemCount() == 0) {
            showNoImagesMenu(menu, false);

        }

//        menu.setGroupVisible(R.id.my_images_group,false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showNoImagesMenu(Menu menu, boolean hide) {
        if (menu == null) {
            return;
        }
        if (hide == false) {
            mNoImagesTextView.setVisibility(View.VISIBLE);
            menu.setGroupEnabled(R.id.my_images_group, false);
            menu.setGroupEnabled(R.id.my_images_select_all_group, false);
            menu.setGroupEnabled(R.id.my_images_clear_all_group, false);
        } else {
            mNoImagesTextView.setVisibility(View.GONE);
            menu.setGroupEnabled(R.id.my_images_group, true);
            menu.setGroupEnabled(R.id.my_images_select_all_group, true);
            menu.setGroupEnabled(R.id.my_images_clear_all_group, true);
        }
    }

    @Override
    public void onDestroy() {

//        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
//        appCompatActivity.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
//        appCompatActivity.getSupportActionBar().setDisplayShowCustomEnabled(false);
//        appCompatActivity.getSupportActionBar().show();
        super.onDestroy();
        cancelLoading(true);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_select_all_my_images:

                myImagesAdaptor.selectAllItems();
                menu.setGroupEnabled(R.id.my_images_clear_all_group, true);
                menu.setGroupEnabled(R.id.my_images_select_all_group, false);
                menu.setGroupEnabled(R.id.my_images_group, true);

                break;
            case R.id.action_clear_all_my_images:
                myImagesAdaptor.clearAllItems();
                menu.setGroupEnabled(R.id.my_images_select_all_group, true);
                menu.setGroupEnabled(R.id.my_images_clear_all_group, false);
                menu.setGroupEnabled(R.id.my_images_group, false);

                break;
            case R.id.action_delete_my_image:

//                cancelPendingTasks();

                showDeleteAlert(getContext(), new OnDeleteSelectedListener() {
                    @Override
                    public void onDeleteSelected() {
                        myImagesAdaptor.setOnUiUpdateListener(new MyImagesAdaptor.OnUiUpdateListener() {
                            @Override
                            public void onDataChanged(int newAdaptorSize) {

                            }

                            @Override
                            public void onImageDeleted(ImageInfo imageInfo) {

                            }

                            @Override
                            public void onAllImagesDeleted(List<ImageInfo> mImageInfoList) {
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    for(ImageInfo imageInfo:mImageInfoList) {
                                        myImagesAdaptor.remove(imageInfo);
                                    }
                                    mProgressBar.setVisibility(View.GONE);

                                },2000);

                            }
                        });
                        mProgressBar.setVisibility(View.VISIBLE);

                        myImagesAdaptor.deleteSelectedImages();


                    }
                });

                break;
            case R.id.action_share_my_image:


                shareImage(getContext(), null);
                break;


        }
        if (myImagesAdaptor.getItemCount() == 0) {
            showNoImagesMenu(menu, false);
        } else {
            mNoImagesTextView.setVisibility(View.GONE);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGalleryImageSelected(Intent data) {

    }

    @Override
    public void shareImage(Context context, String mUrlString) {

        ArrayList<Uri> uriList = new ArrayList<>();
        //multiple
        List<ImageInfo> imageInfoList = myImagesAdaptor.getSelectedImageInfoList();
        for (int i = 0; i < imageInfoList.size(); i++) {
            uriList.add(imageInfoList.get(i).getImageUri());
        }


        if (uriList.size() > 0) {
            shareImageMultiple(context, uriList);
        }
    }

    @Override
    public void setLoadingIndicator(final boolean b) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (b == true) {
                    mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });


    }

    @Override
    public void showResult(String result) {

    }

    @Override
    public void showError(int errorId) {

    }

    @Override
    public void setImages(List<ImageInfo> imageInfoList) {

        if (imageInfoList.size() > 0) {
            showNoImagesMenu(menu, true);
            myImagesAdaptor.setItems(imageInfoList);

        }
        Observable.just(imageInfoList)
                .subscribe(new Consumer<List<ImageInfo>>() {
                    @Override
                    public void accept(List<ImageInfo> imageInfoList) throws Exception {
                        loadImages(imageInfoList);
                    }
                });


    }

    @Override
    public void onAnyImageSelected() {
        menu.setGroupEnabled(R.id.my_images_group, true);
        menu.setGroupEnabled(R.id.my_images_select_all_group, true);

    }

    @Override
    public void onNoneImageSelected() {
        //hide menu bar items
        menu.setGroupEnabled(R.id.my_images_group, false);
        menu.setGroupEnabled(R.id.my_images_select_all_group, true);

    }
}
