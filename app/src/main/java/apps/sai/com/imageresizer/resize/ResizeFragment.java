package apps.sai.com.imageresizer.resize;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import apps.sai.com.imageresizer.BaseFragment;
import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.crop.CropDemoPreset;
import apps.sai.com.imageresizer.crop.CropFragment;
import apps.sai.com.imageresizer.data.BitmapResult;
import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.DataFile;
import apps.sai.com.imageresizer.data.FileApi;
import apps.sai.com.imageresizer.data.ImageInfo;
import apps.sai.com.imageresizer.data.OpenCvFileApi;
import apps.sai.com.imageresizer.data.ResolutionInfo;
import apps.sai.com.imageresizer.listener.MultipleImageProcessingDialog;
import apps.sai.com.imageresizer.listener.OnPreferenceChangedListener;
import apps.sai.com.imageresizer.listener.OnProcessingCancelListener;
import apps.sai.com.imageresizer.listener.OnResolutionSelectedListener;
import apps.sai.com.imageresizer.myimages.MyImagesFragment;
import apps.sai.com.imageresizer.select.SelectActivity;
import apps.sai.com.imageresizer.settings.SettingsFragment;
import apps.sai.com.imageresizer.settings.SettingsManager;
import apps.sai.com.imageresizer.util.BitmapLoadingTask;
import apps.sai.com.imageresizer.util.BitmapProcessingTask;
import apps.sai.com.imageresizer.util.ImageInfoLoadingTask;
import apps.sai.com.imageresizer.util.MultipleImagesAdaptor;
import apps.sai.com.imageresizer.util.NestedWebView;
import apps.sai.com.imageresizer.util.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by sailesh on 03/01/18.
 */

public class ResizeFragment extends BaseFragment implements ResizeContract.View, OnResolutionSelectedListener, CropImageView.OnCropImageCompleteListener, OnPreferenceChangedListener {


    private static final String IMAGE_PATH = "_image_";
    private static String mResFileContent;

    private static String mImageUrlString;

    private RecyclerView mMenuRecyclerView;

    private final ResizeContract.Presenter mResizePresenter;
//    @BindView(R.id.resolutions_recycler_view)
//    RecyclerView mRecyclerView;

    RecyclerView mMultipleImagesRecyclerView;

    private static Intent mIntent;

//    @BindView(R.id.menuRecyclerView_resize)
//    RecyclerView mResizeMenuRecyclerView;

    //    @BindView(R.id.skeletonGroup)
//    SkeletonGroup mSkeletonGroup;
    private Unbinder unbinder;


    public ResizeFragment() {
        mResizePresenter = new ResizePresenter();
    }

    //    private Timer mTimer;
//    private Bitmap mBitmap;
//    private Bitmap image;
//    private DataFile mDataFile ;
    private ImageInfo mImageInfoMin;
//    private GestureCropImageView mGestureCropImageView;
//    private View mOverlay;
//    @BindView(R.id.resizeImageview)
//    UCropView mImageView;


    static DataApi mDataApi;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        autoSave = SettingsManager.getInstance().getAutoSaveImages();

//        getActivity().getActionBar().setTitle("");

        if (BaseFragment.openCvLoaded) {

            mDataApi = new OpenCvFileApi(getContext());
        } else {
            mDataApi = new FileApi(getContext());
        }
//        mDataApi = FileApi.newInstance(getContext());
        mMyOnImageProcessedListener = new MyOnImageProcessedListener();

    }

    @Override
    public void proccessGalleryImage(Intent data) {
        mResizePresenter.onGalleryImageSelected(data);
    }


    @Override
    public void onGalleryImageSelected(final Intent data) {

        try {


            deleteCache(mDataApi);


            if (tobedeletedTextView != null) {
                tobedeletedTextView.setVisibility(View.GONE);
            }
//            data.setClassName(getContext(),null);

            if (data.getClipData() != null && data.getClipData().getItemCount() > 0) {

                setLoadingIndicator(true);
                mSingleFileImageInfo = null;
                mhHandler.postDelayed(() -> {
                    try {

                        List<ImageInfo> imageInfoList = new ArrayList<>(data.getClipData().getItemCount());
                        int count = data.getClipData().getItemCount();
                        if (count > MAX_LOAD_IMAGES) {
                            Toast.makeText(getContext(), getString(R.string.showing_max_images,MAX_LOAD_IMAGES), Toast.LENGTH_LONG).show();
                            count = MAX_LOAD_IMAGES;
                        }

                        for (int i = 0; i < count; i++) {


                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
//                ImageInfo imageInfo = Utils.getImageInfo(getContext(),imageUri,mDataApi);

                            ImageInfo imageInfo = new ImageInfo();
                            imageInfo.setImageUri(imageUri);

                            imageInfoList.add(imageInfo);

                        }
                        mWebView.setVisibility(View.GONE);

                        setMultipleImagesView(imageInfoList);
                        mSingleFileImageInfo = null;
                        setLoadingIndicator(false);
                    } catch (Exception e) {
                        showError(R.string.unknown_error);
                        return;

                    }


                }, 100);

            } else {

                mSingleFileImageInfo = new ImageInfo();

                mMultipleImagesAdaptor = null;

//                mWebView =null;
                mWebView.setVisibility(View.VISIBLE);
                sizeTextView.setVisibility(View.VISIBLE);


                mMultiple = false;
                Uri imageUri = data.getData();
                mImageUrlString = imageUri.toString();
                showCustomAppBar((AppCompatActivity) getActivity(), false);
                mResizePresenter.setImageSelected(imageUri.toString());
            }

//          imageUri =data.getClipData().getItemAt(0).getUri();// data.getData();

//        mResizeIntent.setData(imageUri);

//        mSelectPresenter.newResizeView(mResizeIntent);


        } catch (Exception e) {
            showError(R.string.unable_to_load_image);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private Uri getUriWithId(Context context, Uri media_uri) {
        String[] column = {MediaStore.Images.Media._ID, String.valueOf(MediaStore.Images.Media.EXTERNAL_CONTENT_URI), MediaStore.MediaColumns.RELATIVE_PATH};
        String selection = MediaStore.MediaColumns.RELATIVE_PATH + " LIKE ?";

        Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(),
                media_uri,
                column, null, null, null);

      /*  Cursor cursor = context.getContentResolver().query(media_uri, column,
                null, null, null);*/
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            int Media_ID = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
            return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Media_ID);
        }
        cursor.close();
        return null;
    }

    private void deleteCache(DataApi mDataApi) {

        mDataApi.deleteCache(getContext());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    Menu menu;
    MenuAdaptor mResizeAdaptor;
    RecyclerView.LayoutManager mLayoutManager;
    public static final int OPEN_ID = 2;
    public static final int COMPRESS_ID = 3;
    public static final int SCALE_ID = 4;

    public static final int CROP_ID = 5;
    public static final int SHARE_ID = 6;

    public static final int SAVE_ID = 7;
    public static final int RESET_ID = 9;
    public static final int SETTINGS_ID = 10;


    @Override
    public void showAd() {
        SelectActivity selectActivity = (SelectActivity) getActivity();
        selectActivity.showFullScreenAd();
    }

    private void fillMenuItems(final Context context, RecyclerView mRecyclerView, final BaseFragment baseFragment) {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        List<apps.sai.com.imageresizer.select.MenuItem> menuItemList = new ArrayList<>();


        menuItemList.add(apps.sai.com.imageresizer.select.MenuItem.newInstance(OPEN_ID, context.getString(R.string.action_open),
                R.drawable.ic_folder));


        menuItemList.add(apps.sai.com.imageresizer.select.MenuItem.newInstance(COMPRESS_ID, context.getString(R.string.action_compress),
                R.drawable.ic_compress));


        menuItemList.add(apps.sai.com.imageresizer.select.MenuItem.newInstance(SCALE_ID, context.getString(R.string.action_scale),
                R.drawable.ic_scale));


        if (mSingleFileImageInfo != null) {

            menuItemList.add(apps.sai.com.imageresizer.select.MenuItem.newInstance(CROP_ID, context.getString(R.string.main_action_crop),

                    R.drawable.ic_crop));
        }
        if (!autoSave) {
            menuItemList.add(apps.sai.com.imageresizer.select.MenuItem.newInstance(SAVE_ID, context.getString(R.string.action_save),
                    R.drawable.ic_save));
        }
//        if(mSingleFileImageInfo!=null) {
        menuItemList.add(apps.sai.com.imageresizer.select.MenuItem.newInstance(RESET_ID, context.getString(R.string.action_reset),
                R.drawable.ic_reset));


        menuItemList.add(apps.sai.com.imageresizer.select.MenuItem.newInstance(SHARE_ID, context.getString(R.string.action_share),
                R.drawable.ic_share));


//        }
        MenuAdaptor mResizeAdaptor = new MenuAdaptor(getContext(), menuItemList, new MenuAdaptor.OnMenuSelectedListener() {
            @Override
            public void onMenuSelected(apps.sai.com.imageresizer.select.MenuItem menuItem) {

                try {
                    switch (menuItem.getId()) {
                        case OPEN_ID:
                            mResizePresenter.launchgalleryExternalApp(false);
                            break;
                        case CROP_ID:

                            if (mSingleFileImageInfo == null) {
                                showError(R.string.no_images);
                                return;
                            }


                            String path = mSingleFileImageInfo.getAbsoluteFilePathUri().toString();

                            if (path == null) {
                                showError(R.string.no_images);
                                return;
                            }

//                 startUCrop(mSingleFileImageInfo.getAbsoluteFilePathUri());

                            Utils.setCropFragmentByPreset((AppCompatActivity) getActivity(), CropDemoPreset.RECT, (path));
                            mhHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    showCustomAppBar((AppCompatActivity) getActivity(), true);
                                }
                            }, 100);


                            break;

                        case SCALE_ID:
                            if (mSingleFileImageInfo != null) {

                                showCustomScaleAlert(false, null, mSingleFileImageInfo, ResizeFragment.this);
                            } else {

                                if (mMultipleImagesAdaptor == null) {
                                    showError(R.string.no_images);
                                    return;
                                }
                                //mImageInfoMin
                                showCustomScaleAlert(true, null, mImageInfoMin, ResizeFragment.this);

                            }
                            break;

                        case SHARE_ID:
                            Uri uri = null;
                            setLoadingIndicator(true);
                            if (mSingleFileImageInfo != null && mSingleFileImageInfo.getDataFile() != null
                                    && mSingleFileImageInfo.getDataFile().getName() != null) {


                                uri = mDataApi.getImageUriFromCacheWithFileProvider(mSingleFileImageInfo.getDataFile().getName());
                                if (uri == null) {
                                    uri = mSingleFileImageInfo.getImageUri();
                                }
                            } else {
                                if (mMultipleImagesAdaptor == null) {
                                    showError(R.string.no_images);
                                    return;
                                }

//                                uri = mDataApi.getImageUriFromCacheWithFileProvider();//cached image
                            }


                            mResizePresenter.shareImage(getContext(), uri != null ? uri.toString() : "");
                            break;
                        case RESET_ID:
                            if (tobedeletedTextView != null) {
                                tobedeletedTextView.setVisibility(View.GONE);
                            }

                            mResizePresenter.applyImageEffect(null, IMAGE_PROCESSING_TASKS.RESET, mMyOnImageProcessedListener, null);

                            break;


                        case COMPRESS_ID:
                            mCompressPercentage = 0;
                            mKbEnteredValue = 0;
                            if (mOnCompressTypeChangedListen == null) {
                                mOnCompressTypeChangedListen = new OnCompressTypeChangedListener() {


                                    @Override
                                    public void doCompress(int quality, boolean kbEntered) {


                                        if (kbEntered == false) {

                                            mCompressPercentage = quality;
                                        } else {
                                            mKbEnteredValue = quality;
                                        }
                                        if (mSingleFileImageInfo != null) {
//                                 final BitmapResult bitmapResult = mDataApi.getBitmapFromAbsolutePathUri(getContext(), mSingleFileImageInfo.getAbsoluteFilePathUri());

                                            mResizePresenter.applyImageEffect(mSingleFileImageInfo, ResizeFragment.IMAGE_PROCESSING_TASKS.COMPRESS, mMyOnImageProcessedListener, null);

                                        } else {

                                       /* List<ImageInfo> imageInfoList = mMultipleImagesAdaptor.getImageInfoList();

                                        for(int i=0;i<imageInfoList.size();i++) {

                                            ImageInfo imageInfoInner =imageInfoList.get(i);

                                           *//* BitmapProcessingTask bitmapProcessingTask = new BitmapProcessingTask(imageInfoInner,getContext()
                                            ,0,0,0,null,IMAGE_PROCESSING_TASKS.COMPRESS
                                            ,quality,mDataApi,true);

                                            bitmapProcessingTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);*//*

                                            compressImage(imageInfoInner, new BitmapProcessingTask.OnImageProcessedListener() {
                                                @Override
                                                public void onImageProcessed(BitmapResult bitmapResult, ImageInfo imageInfoOrg) {

                                                }
                                            });
//
                                        }*/
                                            doMultipleImageProcessing(IMAGE_PROCESSING_TASKS.COMPRESS, null);

                                        }
//                                      doMultipleImageProcessing(IMAGE_PROCESSING_TASKS.COMPRESS, null);

                                    }


                                };
                            }


                            baseFragment.showCompressAlert(getContext(), mOnCompressTypeChangedListen, hashMap, mSingleFileImageInfo != null);
//                 mResizePresenter.applyImageEffect(mBitmap, IMAGE_PROCESSING_TASKS.COMPRESS);
                            break;
                        case SAVE_ID:
                            mResizePresenter.saveImage();

                            break;

                    }
                } catch (Throwable e) {
                    showError(R.string.unknown_error);
                }

            }
        });

        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(mResizeAdaptor);


    }

    private final static String TAG = ResizeFragment.class.getSimpleName();

    public ImageInfo getImageInfoAtIndex(int i) {

        if (mSingleFileImageInfo != null) {
            return mSingleFileImageInfo;
        }
        if (mMultipleImagesAdaptor != null && mMultipleImagesAdaptor.getProcessedImageInfoList() != null) {
            if (mMultipleImagesAdaptor.getProcessedImageInfoList().size() >= i + 1) {
                return mMultipleImagesAdaptor.getProcessedImageInfoList().get(i);
            }

        }
        return null;
    }

    boolean autoSave;
    int gridSize;
    int gridApperaance;

    @Override
    public void onPreferenceChanged() {
        autoSave = SettingsManager.getInstance().getAutoSaveImages();
        int size = SettingsManager.getInstance().getGridSize();
        if (gridSize != size) {
            gridSize = size;
            if (mMultipleImagesAdaptor != null) {
                mMultipleImagesAdaptor.setLayoutManager(new GridLayoutManager(getContext(), gridSize));
            }
        }
        int gridAppe = SettingsManager.getInstance().getGridAppearnece();

        if (gridApperaance != gridAppe) {
            gridApperaance = gridAppe;
            if (mMultipleImagesAdaptor != null) {
                mMultipleImagesAdaptor.setAppereance(gridApperaance);
            }
        }
    }

    public static class MenuAdaptor extends RecyclerView.Adapter<MenuAdaptor.ResizeHolder> {
        List<apps.sai.com.imageresizer.select.MenuItem> mMenuItemList;
        Context mContext;
        OnMenuSelectedListener mMenuSelectedListener;

        public interface OnMenuSelectedListener {
            void onMenuSelected(apps.sai.com.imageresizer.select.MenuItem menuItem);
        }

        public MenuAdaptor(Context context, List<apps.sai.com.imageresizer.select.MenuItem> menuItemList, OnMenuSelectedListener menuSelectedListener) {
            mMenuItemList = menuItemList;
            this.mContext = context;
            this.mMenuSelectedListener = menuSelectedListener;
        }

        @Override
        public MenuAdaptor.ResizeHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            return new MenuAdaptor.ResizeHolder(LayoutInflater.from(mContext).inflate(R.layout.menu_row, null));
        }

        public void clear() {
            mMenuItemList.clear();
            this.notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(MenuAdaptor.ResizeHolder holder, int position) {
//            TextView nameTextView = holder.nameTextView;
            ImageView imageView = holder.imageView;

            final apps.sai.com.imageresizer.select.MenuItem menuItem = mMenuItemList.get(position);
//            nameTextView.setText(menuItem.getName());

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

        class ResizeHolder extends RecyclerView.ViewHolder {

            //            TextView nameTextView;
            ImageView imageView;
            View itemView;

            public ResizeHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;

//                nameTextView = itemView.findViewById(R.id.text_name_menu);
                imageView = itemView.findViewById(R.id.image_menu);


            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        this.menu = menu;
        showCustomAppBar((AppCompatActivity) getActivity(), false);

//         inflater.inflate(R.menu.main, menu);
//        super.onCreateOptionsMenu(menu,inflater);


    }

    List<ImageInfo> mImageInfoListCached;


    MultipleImageProcessingDialog multipleImageProcessingListener;

    //static String mCachedUrlString;

    volatile boolean mCancelMutipleTask;

    public void cancelMultipleImageProcessing(boolean cancel) {

        mCancelMutipleTask = cancel;
        if (mImageInfoLoadingTasks != null) {
            for (int i = 0; i < mImageInfoLoadingTasks.size(); i++) {
                mImageInfoLoadingTasks.get(i).cancel(false);
            }
        }

        if (compositeDisposable != null) {
            compositeDisposable.clear();
            compositeDisposable.dispose();
        }
    }

    private CompositeDisposable _disposables;

    private Observable<ImageInfo> getImageInfoObservable(List<ImageInfo> imageInfoList) {
        return Observable.fromIterable(imageInfoList);

    }

    /**
     * Will process the data with the callable by splitting the data into the specified number of threads.
     * <b>T</b> is ths type of data being parsed, and <b>U</b> is the type of data being returned.
     */
    public static <T, U> Iterable<U> parseDataInParallel(@NonNull List<T> data, final Function<List<T>, Observable<U>> worker) {
        final int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(threadCount);
        final Scheduler scheduler = Schedulers.from(threadPoolExecutor);

        final AtomicInteger groupIndex = new AtomicInteger();
        return Observable.fromIterable(data).groupBy(new Function<T, Object>() {
            @Override
            public Object apply(T t) throws Exception {
                return groupIndex.getAndIncrement() % threadCount;
            }
        }).flatMap(new Function<GroupedObservable<Object, T>, ObservableSource<T>>() {
            @Override
            public ObservableSource<T> apply(GroupedObservable<Object, T> group) throws Exception {


                return group;
//                return group;

            }
        }).observeOn(scheduler).toList().flatMapObservable(worker).blockingIterable();

      /*  return Observable.fromIterable(data).groupBy(k -> groupIndex.getAndIncrement() % threadCount)
                .flatMap(group -> group.observeOn(scheduler).toList().flatMapObservable(worker)).blockingIterable();*/

    }

    //    public ResolutionInfo mResolutionInfo;
    //  private CompositeDisposable _disposables;
    CompositeDisposable compositeDisposable;
    int adCount = 1;

    private void doMultipleImageProcessing(final IMAGE_PROCESSING_TASKS image_processing_tasks, final ResolutionInfo resolutionInfo) {
        try {

            if (mMultipleImagesAdaptor == null) {
                showError(R.string.no_images);
                return;
            }
            cancelMultipleImageProcessing(false);


            multipleImageProcessingListener = new MultipleImageProcessingDialog(
                    new OnProcessingCancelListener() {
                        @Override
                        public void onProcessingCanceled(ImageInfo imageInfo, int pos) {


                            cancelMultipleImageProcessing(true);

                        }
                    }, getContext(), image_processing_tasks);

            List<ImageInfo> imageInfoListProcessed = mMultipleImagesAdaptor.getProcessedImageInfoList();

            final List<ImageInfo> imageInfoList = imageInfoListProcessed != null && imageInfoListProcessed.size() > 0 ? imageInfoListProcessed : mMultipleImagesAdaptor.getImageInfoList();
            final int count = imageInfoList.size();
            final Iterator<ImageInfo> imageInfoIterator = imageInfoList.iterator();
            final ImageInfo imageInfo = imageInfoIterator.hasNext() ? imageInfoIterator.next() : null;
            final List<ImageInfo> mImageInfoListCached = new ArrayList<>();
            if (imageInfo != null) {

                int index = imageInfoList.indexOf(imageInfo);
                if (index != -1) {
                    index = index + 1;
                    mMultipleImagesAdaptor.showProcessedInfoList(new ArrayList<>(), image_processing_tasks);
                    multipleImageProcessingListener.onProcessingStarted(imageInfo, index, count);

                }
                multipleImageProcessingListener.setmOnDialogDismmistedListener(() -> mhHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (autoSave) {
                            showDeleteTextView(requireActivity());
                        }
                    }
                }));
                mResizePresenter.applyImageEffect(imageInfo, image_processing_tasks, new BitmapProcessingTask.OnImageProcessedListener() {
                    @Override
                    public void onImageLoaded(final BitmapResult bitmapResult, final ImageInfo imageInfoOrg) {
                        try {
                            setLoadingIndicator(false);
                            if (bitmapResult == null || bitmapResult.getError() != null) {
                                if (getContext() == null) {
                                    return;
                                }
                                if (bitmapResult != null && bitmapResult.getError() instanceof OutOfMemoryError) {
                                    Toast.makeText(getContext(), getString(R.string.out_of_memory), Toast.LENGTH_LONG).show();
                                    multipleImageProcessingListener.onProcessingDone(mImageInfoListCached);
                                    return;

                                } else {
                                    showError(R.string.unknown_error);
                                }
                            }
                            int index = imageInfoList.indexOf(imageInfo);
                            if (index != -1) {
                                index = index + 1;
                                ImageInfo compressedImageInfo = new  ImageInfo();
                                if (bitmapResult == null) {
                                    showError(R.string.unknown_error);
                                    multipleImageProcessingListener.onProcessingDone(mImageInfoListCached);
                                    return;
                                }
                                compressedImageInfo = Utils.getImageInfo(imageInfoOrg, getContext(), bitmapResult.getContentUri(), mDataApi);
                                compressedImageInfo.setImageUri(bitmapResult.getContentUri());
                                mMultipleImagesAdaptor.showProcessedInfo(compressedImageInfo);
                                multipleImageProcessingListener.onProcessingFinished(compressedImageInfo, index, count);
                                mImageInfoListCached.add(compressedImageInfo);

                            }

                            if (mBitmapProcessingTaskWeakReference != null) {
                                mBitmapProcessingTaskWeakReference.clear();
                            }
                            final ImageInfo imageInfo = imageInfoIterator.hasNext() ? imageInfoIterator.next() : null;
                            if (imageInfo != null) {
                                mhHandler.post(() -> {
                                    int index1 = imageInfoList.indexOf(imageInfo);
                                    if (index1 != -1) {
                                        index1 = index1 + 1;
                                        multipleImageProcessingListener.onProcessingStarted(imageInfo, index1, count);

                                    }
                                });
                                if (mCancelMutipleTask) {
                                    multipleImageProcessingListener.onProcessingDone(mImageInfoListCached);
                                    mCancelMutipleTask = false;
                                    return;
                                }
                                mResizePresenter.applyImageEffect(imageInfo, image_processing_tasks, this, resolutionInfo);
                            } else {
                                mMultipleImagesAdaptor.showProcessedInfoList(mImageInfoListCached, image_processing_tasks);

                                multipleImageProcessingListener.onProcessingDone(mImageInfoListCached);
                            }

                        } catch (Exception e) {
                            if (mCancelMutipleTask) {
                                return;
                            }
                            e.printStackTrace();
                        }
                        //
                    }
                }, resolutionInfo);

            }
        } catch (Exception e) {
            showError(R.string.unknown_error);
        }
    }


    @Override
    public void shareImage(Context context, String mUrlString) {

        setLoadingIndicator(false);
        if (mSingleFileImageInfo != null) {
            if (mUrlString == null) {
                showError(R.string.no_images);
                return;

            }
            super.shareImage(context, mUrlString);
        } else {
            ArrayList<Uri> uriList = new ArrayList<>();
            if (mMultipleImagesAdaptor == null) {
                showError(R.string.no_images);
                return;
            }
            //multiple
            List<ImageInfo> imageInfoList = mMultipleImagesAdaptor.getProcessedImageInfoList();
            if (imageInfoList != null && imageInfoList.size() > 0) {
                for (int i = 0; i < imageInfoList.size(); i++) {
                    DataFile dataFile = imageInfoList.get(i).getDataFile();
                    Uri uri = mDataApi.getImageUriFromCacheWithFileProvider(dataFile.getName());
                    uriList.add(uri);
                }

            } else {
                imageInfoList = mMultipleImagesAdaptor.getImageInfoList();
                if (imageInfoList == null || imageInfoList.size() == 0) {
                    showError(R.string.no_images);
                    return;
                }
                for (int i = 0; i < imageInfoList.size(); i++) {
                    uriList.add(imageInfoList.get(i).getImageUri());
                }
            }


            if (uriList.size() > 0) {
                shareImageMultiple(context, uriList);
            }
        }

//        super.shareImage(context,mUrlString);
    }

    List<ImageInfoLoadingTask> mImageInfoLoadingTasks;

    public void cancelTask() {

    }

    Reference<OnImagedSavedListener> mOnImagedSavedListenerWeakReference;
    OnImagedSavedListener mOnImagedSavedListener;

    public interface OnImagedSavedListener {
        void onImageSaved(ImageInfo imageInfo);
    }

    public void setOnImagedSavedListener(OnImagedSavedListener onImagedSavedListener) {

//        if(mOnImagedSavedListenerWeakReference==null){
//            mOnImagedSavedListenerWeakReference = new SoftReference<OnImagedSavedListener>(onImagedSavedListener);
//        }
        this.mOnImagedSavedListener = mOnImagedSavedListener;
    }

    boolean saved;
    List<ImageInfo> mImageInfoList;

    @Override
    public void saveImage() {

        try {

//           final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


            Uri uriResult = null;


//        result =mDataApi.saveImageInCache()

            if (mSingleFileImageInfo != null) {


                uriResult = mDataApi.copyImageFromCacheToGallery(requireActivity(), mSingleFileImageInfo.getDataFile());

                if (uriResult == null) {
                    mDataApi.copyImageFromGalleryToCache(mSingleFileImageInfo.getDataFile());
                    uriResult = mDataApi.copyImageFromCacheToGallery(requireActivity(), mSingleFileImageInfo.getDataFile());

                }

                if (uriResult == null) {
                    Toast.makeText(getActivity(), "Unable to save image", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Image saved ", Toast.LENGTH_LONG).show();
                    mSingleFileImageInfo.setSaved(true);

                    showDeleteTextView(requireActivity());
//                if(mOnImagedSavedListenerWeakReference!=null){
//                   OnImagedSavedListener onImagedSavedListener =  mOnImagedSavedListenerWeakReference.get();
                    if (mOnImagedSavedListener != null) {
                        mOnImagedSavedListener.onImageSaved(mSingleFileImageInfo);
                    }
//                }

                }
            } else {
                saved = false;
                if (mMultipleImagesAdaptor == null) {
                    showError(R.string.no_images);

                    return;
                }

                cancelMultipleImageProcessing(true);
                mImageInfoLoadingTasks = new ArrayList<>();

                mhHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mImageInfoList = mMultipleImagesAdaptor.getProcessedImageInfoList();
                        boolean original = true;
                        if (mImageInfoList != null && mImageInfoList.size() > 0) {
                            //images are original yet
                            original = false;
                        } else {
                            mImageInfoList = mMultipleImagesAdaptor.getImageInfoList();
                        }
                        if (mImageInfoList == null) {
                            return;
                        }

                        setLoadingIndicator(true);
//                    boolean [] result = new boolean[imageInfoList.size()];
                        int count = 0;


                        final MultipleImageProcessingDialog multipleImageProcessingDialog = new MultipleImageProcessingDialog(
                                new OnProcessingCancelListener() {
                                    @Override
                                    public void onProcessingCanceled(ImageInfo imageInfo, int pos) {


                                        cancelMultipleImageProcessing(true);
                                    }
                                }, getContext(), null);


                        for (Iterator<ImageInfo> imageInfoIterator = mImageInfoList.iterator(); imageInfoIterator.hasNext(); ) {
                            ImageInfo imageInfo = imageInfoIterator.next();
                            count++;
                            if (count == 1) {
                                multipleImageProcessingDialog.onProcessingStarted(imageInfo, count, mImageInfoList.size());
                            }
                            ImageInfoLoadingTask imageInfoLoadingTask = null;
                            if (original) {

                                imageInfoLoadingTask = new ImageInfoLoadingTask(getContext(), imageInfo,
                                        mDataApi, imageInfo1 -> {
                                    int count1 = mImageInfoList.indexOf(imageInfo1);
                                    if (count1 == mImageInfoList.size() - 1) {
                                        if (!imageInfo1.isSaved()) {
                                            showError(R.string.unable_to_save_image);
                                        }
                                        imageInfo1.setSaved(true);

                                        if (mOnImagedSavedListenerWeakReference != null) {
                                            OnImagedSavedListener onImagedSavedListener = mOnImagedSavedListenerWeakReference.get();
                                            if (onImagedSavedListener != null) {
                                                onImagedSavedListener.onImageSaved(imageInfo1);
                                            }
                                        }
                                        if (!saved) {
                                            showDeleteTextView(requireActivity());
                                            saved = true;
                                        }

                                    }

                                    multipleImageProcessingDialog.onProcessingFinished(imageInfo1, count1 + 1, mImageInfoList.size());


                                }, ImageInfoLoadingTask.TASKS.IMAGE_FILE_SAVE_GALLERY_TO_MY_FOLDER);
                            } else {
                                imageInfoLoadingTask = new ImageInfoLoadingTask(getContext(), imageInfo,
                                        mDataApi, new ImageInfoLoadingTask.OnImageInfoProcesedListener() {
                                    @Override
                                    public void onImageProcessed(ImageInfo imageInfo) {

                                  /*  if(imageInfo.isSaved()){
//                                        imageInfoList.indexOf(imageInfo);
                                    }*/
                                        int count = mImageInfoList.indexOf(imageInfo);
                                        if (count == mImageInfoList.size() - 1) {
                                            if (!imageInfo.isSaved()) {
                                                showError(R.string.unable_to_save_image);
                                            }
                                            if (mOnImagedSavedListenerWeakReference != null) {
                                                OnImagedSavedListener onImagedSavedListener = mOnImagedSavedListenerWeakReference.get();
                                                if (onImagedSavedListener != null) {
                                                    onImagedSavedListener.onImageSaved(imageInfo);
                                                }
                                            }
                                            imageInfo.setSaved(true);
                                        }
                                        if (!saved) {
                                            showDeleteTextView(requireActivity());
                                            saved = true;
                                        }
                                        multipleImageProcessingDialog.onProcessingFinished(imageInfo, count + 1, mImageInfoList.size());
                                    }
                                }, ImageInfoLoadingTask.TASKS.IMAGE_FILE_SAVE_CACHE_TO_GALLERY);
                            }
                            mImageInfoLoadingTasks.add(imageInfoLoadingTask);
                            imageInfoLoadingTask.executeOnExecutor(executor);
                        }


                  /*  if (count !=imageInfoList.size()) {
                        Toast.makeText(getActivity(), "Unable to save some images", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Image saved ", Toast.LENGTH_LONG).show();

                    }*/
                        setLoadingIndicator(false);


                    }
                });
//        result =mDataApi.storeImage(mBitmap,dataFile)!=null?true:false;


            }
        } catch (Throwable e) {
            showError(R.string.unknown_error);
        }
    }

    @Override
    public Bitmap applyImageEffect(ImageInfo imageInfo, IMAGE_PROCESSING_TASKS image_PROCESSING_tasks, BitmapProcessingTask.OnImageProcessedListener onImageProcessedListener, ResolutionInfo resolutionInfo) {

        if (image_PROCESSING_tasks != null) {


            if (image_PROCESSING_tasks == IMAGE_PROCESSING_TASKS.SCALE) {
                int w = resolutionInfo.getWidth();
                int h = resolutionInfo.getHeight();
                if (mMultiple == true) {
                    int orgWidth = imageInfo.getWidth();
                    int orgHeight = imageInfo.getHeight();
                    if (resolutionInfo.isPreResolutionSelected() == true) {
                        if (resolutionInfo.isAspect() == true) {

                            if (orgWidth > orgHeight) {

                                h = Utils.calculateAspectRatioHeight(new Point(orgWidth, orgHeight), w).y;
                            } else {
                                w = Utils.calculateAspectRatioWidth(new Point(orgWidth, orgHeight), h).x;

                            }

                        }
                    } else if (resolutionInfo.isPercentageSelected() == true) {

                        if (orgWidth > orgHeight) {

                            w = (int) (orgWidth * w / (float) 100);


                            h = Utils.calculateAspectRatioHeight(new Point(orgWidth, orgHeight), w).y;
                        } else {
                            h = (int) (orgHeight * h / (float) 100);


                            w = Utils.calculateAspectRatioWidth(new Point(orgWidth, orgHeight), h).x;

                        }

                    } else { //custom resolution
                        if (resolutionInfo.isAspect() == true) {

                            if (w > 0) {

                                h = Utils.calculateAspectRatioHeight(new Point(orgWidth, orgHeight), w).y;
                            } else if (h > 0) {
                                w = Utils.calculateAspectRatioWidth(new Point(orgWidth, orgHeight), h).x;

                            } else {
                                return null;
                            }

                        }


                    }


                }
                //do scaling

                scaleImage(imageInfo, w, h, onImageProcessedListener);
            } else if (image_PROCESSING_tasks == IMAGE_PROCESSING_TASKS.ROTATE_CLOCKWISE) {
                //do rotate
                rotateImageClockWise(imageInfo, onImageProcessedListener);
            } else if (image_PROCESSING_tasks == IMAGE_PROCESSING_TASKS.BLUR) {
                //do rotate
                blurImage(imageInfo, onImageProcessedListener);
            } else if (image_PROCESSING_tasks == IMAGE_PROCESSING_TASKS.RESET) {
                //do rotate
                if (mSingleFileImageInfo != null) {

                    mSingleFileImageInfo.setAbsoluteFilePath(mSingleFileImageInfo.getImageUri());
//                    mImageUrlString = mSingleFileImageInfo.getImageUri().getPath();
//                    mSingleFileImageInfo = Utils.getImageInfo(mSingleFileImageInfo,getContext(),mSingleFileImageInfo.getImageUri(),mDataApi);
                    mDataApi.copyImageFromGalleryToCache(mSingleFileImageInfo.getDataFile());
                    mResizePresenter.setImageSelected(mImageUrlString);
                } else {

                    mMultipleImagesAdaptor.setmProcessedImageInfoList(new ArrayList<ImageInfo>());
                    List<ImageInfo> imageInfoList = mMultipleImagesAdaptor.getImageInfoList();
                    if (imageInfoList != null) {
                        for (ImageInfo imageInfoOrg : imageInfoList) {
                            Uri orgUri = imageInfoOrg.getImageUri();
                            imageInfoOrg.setAbsoluteFilePath(orgUri);
                        }
                    }



                    /*ArrayList<Uri> uriList = new ArrayList<>();
                    //multiple
                    List<ImageInfo> imageInfoList = mMultipleImagesAdaptor.getImageInfoList();
                    for (int i = 0; i < imageInfoList.size(); i++) {
                        uriList.add(imageInfoList.get(i).getImageUri());
                    }


                    if (uriList.size() > 0) {
//                        shareImageMultiple(context, uriList);
                    }*/
                }


            } else if (image_PROCESSING_tasks == IMAGE_PROCESSING_TASKS.SHARPEN) {
                //do rotate
                sharpenImage(imageInfo, onImageProcessedListener);
            } else if (image_PROCESSING_tasks == IMAGE_PROCESSING_TASKS.COMPRESS) {
                //do compress

//                showCompressAlert();

                compressImage(imageInfo, onImageProcessedListener);
            }


        }
        return null;
    }

    public Bitmap blurImage(ImageInfo bitmap, BitmapProcessingTask.OnImageProcessedListener onImageProcessedListener) {
        processImage(bitmap, 0, 0, IMAGE_PROCESSING_TASKS.BLUR, onImageProcessedListener);
        return null;
    }

    public Bitmap sharpenImage(ImageInfo bitmap, BitmapProcessingTask.OnImageProcessedListener onImageProcessedListener) {
        processImage(bitmap, 0, 0, IMAGE_PROCESSING_TASKS.SHARPEN, onImageProcessedListener);
        return null;
    }

    public Bitmap compressImage(ImageInfo bitmap, BitmapProcessingTask.OnImageProcessedListener onImageProcessedListener) {

//       mBitmap= mDataApi.getBitmapFromAbsolutePath(getActivity(),mImageFileUrlString);


        processImage(bitmap, 0, 0, IMAGE_PROCESSING_TASKS.COMPRESS, onImageProcessedListener);
        return null;
    }

    public Bitmap rotateImageClockWise(ImageInfo bitmap, BitmapProcessingTask.OnImageProcessedListener onImageProcessedListener) {
        processImage(bitmap, 0, 0, IMAGE_PROCESSING_TASKS.ROTATE_CLOCKWISE, onImageProcessedListener);
        return null;
    }


    public Bitmap scaleImage(ImageInfo bitmap, int w, int h, BitmapProcessingTask.OnImageProcessedListener onImageProcessedListener) {
        processImage(bitmap, w, h, IMAGE_PROCESSING_TASKS.SCALE, onImageProcessedListener);
        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mResizePresenter.takeView(this);

        SelectActivity selectActivity = (SelectActivity) getActivity();
        selectActivity.setCurrentFragment(this);
    }

    @Override
    public void onDestroyView() {

        showCustomAppBar((AppCompatActivity) getActivity(), true);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        removeAd();
        cancelMultipleImageProcessing(true);
        mResizePresenter.dropView();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
//    protected SkeletonConfig skeletonConfig = new SkeletonConfig();

    private ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // setHasOptionsMenu(true);
        mView = inflater.inflate(R.layout.fragment_resize, null);
        mWebView = mView.findViewById(R.id.webview);
        tobedeletedTextView = mView.findViewById(R.id.tobedeletedTextview);
        if (mhHandler == null) {
            mhHandler = new Handler(Looper.getMainLooper());
        }
        mProgressBar = getActivity().findViewById(R.id.activityProgressBar);

        unbinder = ButterKnife.bind(this, mView);

        sizeTextView = mView.findViewById(R.id.size);
        mMultipleImagesRecyclerView = mView.findViewById(R.id.multiple_image_recycler_view);

        onGalleryImageSelected(mIntent);

//       View adParent =  getActivity().findViewById(R.id.coordinatorLayout);
//       if(adParent!=null) {

//           Utils.showFacebookBanner(getContext(), mView,R.id.banner_container, "179547122769778_189046365153187");
//       }


        return mView;
    }

    private void removeAd() {

        try {
            View adParent = getActivity().findViewById(R.id.coordinatorLayout);

            LinearLayout adContainer = adParent.findViewById(R.id.banner_container_top);
            if (adContainer != null) {
                adContainer.removeAllViews();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showCustomAppBar(AppCompatActivity appCompatActivity, boolean hide) {
        try {

            if (hide == false) {

                appCompatActivity.getSupportActionBar().setCustomView(R.layout.custom_menu_layout);

                View view = ((AppCompatActivity) getActivity()).getSupportActionBar().getCustomView();
                mMenuRecyclerView = view.findViewById(R.id.menuRecyclerView);

                fillMenuItems(appCompatActivity, mMenuRecyclerView, this);
//            fillMenuItems(appCompatActivity,mResizeMenuRecyclerView,this);

//                appCompatActivity.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
//                        | ActionBar.DISPLAY_SHOW_HOME);
                appCompatActivity.getSupportActionBar().setDisplayShowCustomEnabled(true);
                appCompatActivity.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);


            } else {
                appCompatActivity.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);
                MenuAdaptor menuAdaptor = (MenuAdaptor) mMenuRecyclerView.getAdapter();
                menuAdaptor.clear();
                appCompatActivity.getSupportActionBar().setDisplayShowCustomEnabled(false);


            }
//            appCompatActivity.getSupportActionBar().hide();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private void setResolutions(ImageInfo imageInfo) {

        if (true) {
            if (imageInfo == null) {
                imageInfo = new ImageInfo();
                imageInfo.setWidth(100);
                imageInfo.setHeight(100);
            }
        }

        mResFileContent = getArguments().getString(FileApi.RES_FILE_NAME);

        if (mResFileContent == null) {
            mResFileContent = Utils.loadResolutionsFromAssets(getContext(), R.raw.res_camera);
        }
        if (mResFileContent == null) {
            return;
        }
        List<String> nameList;

        String[] a = mResFileContent.split("\n");

        if (a != null && a.length > 0) {
            nameList = Arrays.asList(a);


            String maxRes = null;//a[0];

            int orgWidth = imageInfo.getWidth();
            int orgHeight = imageInfo.getHeight();

            //calculate aspect ratio height , lets make width fix
            int newHeight = 0;
//                    (int )Utils.calculateAspectRatioHeight(new Point(orgWidth,orgHeight),mWidth).y;
            int newWidth = 0;


            int newMaxResolution = 0;
            int index = -1;
            List<ResolutionInfo> newResolutionsList = new ArrayList<>();
            int orgResolution = orgWidth * orgHeight;
            int THRESHOLD = maxResolution;//allow 4 times up scale only
            /*if(THRESHOLD<maxResolution/4){
                THRESHOLD =maxResolution;
            }else {
                THRESHOLD = Math.min(THRESHOLD, maxResolution);
            }*/

            for (int i = 0; i < nameList.size(); i++) {
                maxRes = nameList.get(i);
                index = maxRes.indexOf("x");
                if (orgWidth > orgHeight) {
                    newWidth = Integer.valueOf(maxRes.substring(0, index));
                    newHeight = Utils.calculateAspectRatioHeight(new Point(orgWidth, orgHeight), newWidth).y;
                } else {
                    newHeight = Integer.valueOf(maxRes.substring(index + 1));
                    newWidth = Utils.calculateAspectRatioWidth(new Point(orgWidth, orgHeight), newHeight).x;
                }

                int newResolution = newWidth * newHeight;
                float ratio = ((float) newResolution / (float) orgResolution);
                ratio = ratio * 100;

                if (newResolution < THRESHOLD) {
                    //remove it
                    //nameList.remove(maxRes);
//                    newMaxResolution =Math.max(newMaxResolution,newResolution);
                    if (newResolution > newMaxResolution) {
                        newMaxResolution = newResolution;
//                        maxWidth = newWidth;
//                        maxHeight =newHeight;


                    }
                    ResolutionInfo resolutionInfo = new ResolutionInfo();

                    //nameList.remove(maxRes);

                    if (ratio >= 1) {
                        maxRes = String.format("%d %s %d (%.0f", newWidth, "x", newHeight, ratio) + "%)";
                    } else {
                        maxRes = String.format("%d %s %d (%.1f", newWidth, "x", newHeight, ratio) + "%)";

                    }
                    resolutionInfo.setHeight(newHeight);
                    resolutionInfo.setWidth(newWidth);
                    resolutionInfo.setPercentageOfOriginal((int) ratio);
                    resolutionInfo.setFormatedString(maxRes);
                    if (newResolutionsList.contains(resolutionInfo) == false) {
                        newResolutionsList.add(resolutionInfo);
                    }
                    // nameList.add(i,maxRes);
//                    break;


//                a[i] = newWidth +"x"+newHeight;
                }
            }
            ResolutionInfo resolutionInfo = new ResolutionInfo();
            resolutionInfo.setFormatedString("Custom");
            newResolutionsList.add(0, resolutionInfo);

            resolutionInfo = new ResolutionInfo();
            resolutionInfo.setFormatedString("Scale->");
            newResolutionsList.add(0, resolutionInfo);
//            nameList = newResolutionsList;
//            nameList.addAll(newResolutionsList);
//            maxResolution = newMaxResolution;


//            mResolutionAdaptor = new ResolutionAdaptor(getContext(),
//                    newResolutionsList, ResizeFragment.this,
//                    mRecyclerView, new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));


          /*  mResolutionAdaptorMenu =new ResolutionAdaptor(getContext(),
                    newResolutionsList, ResizeFragment.this,
                    mMenuRecyclerView, new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));*/
//            mRecyclerView.setAdapter(mResolutionAdaptor);

          /*  if(mBitmap==null){
                mRecyclerView.setVisibility(View.GONE);//shwo only whwn image is loaded
            }*/
        }
    }

    //    Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    Executor executor = AsyncTask.SERIAL_EXECUTOR;

    int count = 0;
    int lastIndex;

    public void setMultipleImagesView(final List<ImageInfo> imageInfoList) {

        count = 0;
        mMultiple = true;

        sizeTextView.setVisibility(View.GONE);
        showCustomAppBar((AppCompatActivity) getActivity(), false);
//        showHideMutipleImageMenu(true);


        mMultipleImagesRecyclerView.setVisibility(View.VISIBLE);

        gridSize = SettingsManager.getInstance().getGridSize();

        mMultipleImagesAdaptor = new MultipleImagesAdaptor(this, imageInfoList,
                mMultipleImagesRecyclerView,
                new GridLayoutManager(getContext(), gridSize, GridLayoutManager.VERTICAL,
                        false), mDataApi);
        mMultipleImagesRecyclerView.setAdapter(mMultipleImagesAdaptor);

        /*if(mMultipleImagesAdaptor.showAd() ==true){
            Utils.showFacebookBanner(getContext(),mView,R.id.banner_container_top,"179547122769778_189237791800711");
        }else{
            Utils.hideFacebookBanner(getContext(),mView,R.id.banner_container_top);
        }*/

        mhHandler.post(new Runnable() {
            @Override
            public void run() {
                try {


                    final ImageInfo processedImageInfo = Utils.getImageInfo(null, getContext(), imageInfoList.get(0).getImageUri(), mDataApi);
                    if (isEmpty(processedImageInfo)) {
                        showError(R.string.unable_to_load_image);
                    }
                    mImageInfoMin = processedImageInfo;
                    imageInfoList.remove(0);
                    imageInfoList.add(0, processedImageInfo);
                    long resMin = mImageInfoMin.getWidth() * mImageInfoMin.getHeight();

                    mhHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mImageInfoLoadingTasks = new ArrayList<>();
                            cancelMultipleImageProcessing(false);


                            for (int i = 0; i < imageInfoList.size(); i++) {

                                ImageInfo imageInfoInner = imageInfoList.get(i);
                                ImageInfoLoadingTask imageInfoLoadingTask =
                                        new ImageInfoLoadingTask(getContext(), imageInfoInner, mDataApi
                                                , new ImageInfoLoadingTask.OnImageInfoProcesedListener() {

                                            @Override
                                            public void onImageProcessed(ImageInfo imageInfo) {

                                                try {
                                                    Log.e(TAG, imageInfo.toString() + " Loaded ");


                                                    if (mCancelMutipleTask == true) {
                                                        lastIndex = imageInfoList.indexOf(imageInfo);
                                                        return;
                                                    }

//                                             multipleImageProcessingDialog.onProcessingFinished(
//                                                     imageInfo,imageInfoList.indexOf(imageInfo),imageInfoList.size());
//                                             imageInfoList.remove(i);
//                                             imageInfoList.add(i, procesedImageInfoInner);
                                                    mMultipleImagesAdaptor.setImageInfo(imageInfo);
                                                    if (isEmpty(processedImageInfo)) {
                                                        showError(R.string.unable_to_load_image);
                                                        mMultipleImagesAdaptor.remove(imageInfo);
                                                      /*  if(mMultipleImagesAdaptor.showAd()==false) {
                                                            Utils.hideFacebookBanner(getContext(), mView, R.id.banner_container_top);
                                                        }*/

                                                        return;
                                                    }
                                                    long resInner = imageInfo.getWidth() * imageInfo.getHeight();
                                                    if (resInner < mImageInfoMin.getWidth() * mImageInfoMin.getHeight()) {
                                                        mImageInfoMin = imageInfo;
                                                    }
                                                } catch (Exception e) {
                                                    showError(R.string.unknown_error);
                                                }


                                            }
                                        }, ImageInfoLoadingTask.TASKS.IMAGE_INFO_LOAD);
                                mImageInfoLoadingTasks.add(imageInfoLoadingTask);
                                imageInfoLoadingTask.executeOnExecutor(executor);

//                                imageInfoLoadingTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
//
                            }
                        }
                    });
                } catch (Throwable t) {
                    showError(R.string.unknown_error);
                }

            }
        });
//        setResolutions(mImageInfoMin);

//        setResolutionsByPercentages(mImageInfoMin,mRecyclerView);
    }

    private boolean isEmpty(ImageInfo imageInfo) {

        return imageInfo == null || imageInfo.getWidth() == 0 ||
                imageInfo.getHeight() == 0 || imageInfo.getFileSize() == 0;
    }

    private void showHideMutipleImageMenu(boolean multiple) {

        if (menu != null) {
            if (multiple) {
//                menu.setGroupVisible(R.id.groupSave,false);

                menu.setGroupVisible(R.id.singleImageItemTwo, false);
                menu.setGroupVisible(R.id.singleImageItem, false);
            } else {
//                menu.setGroupVisible(R.id.groupSave,true);

                menu.setGroupVisible(R.id.singleImageItemTwo, true);
                menu.setGroupVisible(R.id.singleImageItem, true);
            }

        }
        mMenuRecyclerView.getAdapter().notifyDataSetChanged();
//        mMenuAdaptor.notifyDataSetChanged();
    }

    private View mView;


    MultipleImagesAdaptor mMultipleImagesAdaptor;

    ResolutionAdaptor mResolutionAdaptor;
    ResolutionAdaptor mResolutionAdaptorMenu;

    public static ResizeFragment newInstance(Intent intent) {


        Bundle args = new Bundle();
        Uri uri = intent.getData();

        if (uri != null) {
            args.putString(IMAGE_PATH, uri.toString());

        }

        mIntent = intent;
        mResFileContent = intent.getStringExtra(FileApi.RES_FILE_NAME);
        args.putString(FileApi.RES_FILE_NAME, mResFileContent);

        ResizeFragment fragment = new ResizeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setLoadingIndicator(boolean show) {

        if (mProgressBar != null) {
            if (true == show) {
                mProgressBar.setVisibility(View.VISIBLE);
            } else {
                mProgressBar.setVisibility(View.GONE);

            }
        }

    }

    @Override
    public void showResult(String result) {

    }

    @Override
    public void showError(final int errorId) {
        if (mhHandler != null && getContext() != null) {
            mhHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        String s = getString(errorId);
                        if (s != null && s.length() > 0) {
                            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                        }
                        setLoadingIndicator(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    public void setCurrentFragment(CropFragment fragment) {
        mCurrentFragment = fragment;
    }

    private CropFragment mCurrentFragment;

//    private Uri mCropImageUri;

    @Override
    public void setSelectedImage(final BitmapResult bitmapResult) {

        try {

            setLoadingIndicator(false);

            showWebView(bitmapResult);
            if (mSingleFileImageInfo != null) {
//               setResolutions(mSingleFileImageInfo);
//               setResolutionsByPercentages(mSingleFileImageInfo,mRecyclerView);

            }

        } catch (Throwable e) {
            showError(R.string.unknown_error);
        }


    }

    private final static String CACHE_FILE = "cached_";


    private void showImageView(Bitmap bitmap, ImageInfo imageInfo) {
        mhHandler.post(new Runnable() {
            @Override
            public void run() {
                setLoadingIndicator(true);
            }
        });


        showFIleSize(imageInfo, sizeTextView);

        bitmap = mDataApi.getBitmapFromAbsolutePathUri(getActivity(), mSingleFileImageInfo.getAbsoluteFilePathUri(),
                imageInfo.getWidth(), imageInfo.getHeight()).getBitmap();

//            mImageView.getCropImageView().setImageBitmap(bitmap);

//        }

    }

    static ImageInfo mSingleFileImageInfo;

    private void showWebView(BitmapResult bitmapResult) {


        if (getContext() == null) {
            return;
        }


        String path = null;
        if (mSingleFileImageInfo == null) {//if it is loaded from gallery it becomes null
            mSingleFileImageInfo = new ImageInfo();
        }
        if (bitmapResult == null) {
            showError(R.string.unable_to_load_image);
            return;
        }
//        path = mSingleFileImageInfo.getImageUri().getPath();
          /*  if(bitmapResult.getContentUri()!=null){

//               boolean isSaved = mSingleFileImageInfo.isSaved();
//                Uri processedUri =mSingleFileImageInfo.getImageUri();

                mSingleFileImageInfo = Utils.getImageInfo(mSingleFileImageInfo,getContext(),bitmapResult.getContentUri(),mDataApi);
//                mSingleFileImageInfo.setProcessedUri(processedUri);
                if(mSingleFileImageInfo.getAbsoluteFilePathUri()!=null) {
                    path = mSingleFileImageInfo.getAbsoluteFilePathUri().toString();
                }
            }*/

        if (isEmpty(mSingleFileImageInfo) /*|| path ==null*/) {
            showError(R.string.unable_to_load_image);
            return;
        }


        showFIleSize(mSingleFileImageInfo, sizeTextView);//here too don't need file://
//        mWebView.setVisibility(View.VISIBLE);

        final String mimeType = "text/html";
        final String encoding = "utf-8";
        mWebView.clearCache(true);
        String data = "<html><head><title>Example</title><meta name=\"viewport\"\"content=\"width=100%"

                + ", initial-scale=1.0 \" /></head>";
        data = data + "<body><center><img width=100%  src=\"" + mSingleFileImageInfo.getAbsoluteFilePathUri() + "\" /></center></body></html>";
//        webView.loadData(data, "text/html", null);

//        mWebView.setVisibility(View.GONE);
        mWebView.loadDataWithBaseURL("", data, mimeType, encoding, "");
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(true);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setLoadingIndicator(false);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }
        });


    }


    Handler mhHandler;
    TextView sizeTextView;

    @BindView(R.id.tobedeletedTextview)
    TextView tobedeletedTextView;


    @Override
    public void setImageSelected(String imageUriString) {


        try {

//            Uri imageUri = Uri.parse(imageUriString);
//            mSingleFileImageInfo= Utils.getImageInfo(new ImageInfo(), getContext(),imageUri,mDataApi);
            mSingleFileImageInfo = new ImageInfo();
            mSingleFileImageInfo.setImageUri(Uri.parse(imageUriString));
            mSingleFileImageInfo.setOriginalContentUri(mSingleFileImageInfo.getImageUri());

            sizeTextView.setVisibility(View.VISIBLE);
//            showHideMutipleImageMenu(false);
//            mWebView.setVisibility(View.VISIBLE);

            mMultipleImagesRecyclerView.setVisibility(View.GONE);


//           setImageUriAsyncNew(mSingleFileImageInfo.getImageUri());

            ImageInfoLoadingTask imageInfoLoadingTask =
                    new ImageInfoLoadingTask(getContext(), mSingleFileImageInfo, mDataApi
                            , new ImageInfoLoadingTask.OnImageInfoProcesedListener() {


                        @Override
                        public void onImageProcessed(ImageInfo imageInfo) {

                            try {

                                if (imageInfo == null || imageInfo.getImageUri() == null) {
                                    showError(R.string.image_does_not_exists);
                                    return;

                                }
                                imageInfo.setOriginalContentUri(mSingleFileImageInfo.getOriginalContentUri());
                                mSingleFileImageInfo = imageInfo;
                                BitmapResult bitmapResult = new BitmapResult();
                                bitmapResult.setContentUri(mSingleFileImageInfo.getImageUri());
                                mResizePresenter.setSelectedImage(bitmapResult);

                            } catch (Exception e) {
                                showError(R.string.unknown_error);
                            }

                        }
                    }, ImageInfoLoadingTask.TASKS.IMAGE_INFO_LOAD);
            imageInfoLoadingTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);


        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError oe) {

        }

    }


    /**
     * Task used to load mBitmap async from UI thread
     */
    private WeakReference<BitmapLoadingTask> mBitmapLoadingWorkerTask;

    private static WeakReference<BitmapProcessingTask> mBitmapProcessingTaskWeakReference;


    public void setImageUriAsyncNew(Uri uri) {
        if (uri != null) {
            setLoadingIndicator(true);
            BitmapLoadingTask currentTask =
                    mBitmapLoadingWorkerTask != null ? mBitmapLoadingWorkerTask.get() : null;
            if (currentTask != null) {
                // cancel previous loading (no check if the same URI because camera URI can be the same for
                // different images)
                currentTask.cancel(true);
            }


            int desiredWidth = maxWidth;
            int desiredHeight = maxHeight;

            // either no existing task is working or we canceled it, need to load new URI

            BitmapLoadingTask bitmapLoadingTask = new BitmapLoadingTask(getContext(), mSingleFileImageInfo, mDataApi, desiredWidth, desiredHeight);


            bitmapLoadingTask.setOnImageUriLoadedListener(new BitmapLoadingTask.OnImageUriLoadedListener() {


                @Override
                public void onImageLoaded(BitmapResult bitmapResult) {

                    //   mDataApi.copyImageFromCacheToGallery()
//                    mImageUrlString =bitmapResult.getContentUri().toString();
//                    mSingleFileImageInfo =getImageInfo(getContext(),bitmapResult.getContentUri());
                    mSingleFileImageInfo = null;
                    mResizePresenter.setSelectedImage(bitmapResult);
                }


            });
            mBitmapLoadingWorkerTask = new WeakReference<>(bitmapLoadingTask);

//            bitmapLoadingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            bitmapLoadingTask.executeOnExecutor(executor);


        }
    }


    public enum IMAGE_PROCESSING_TASKS {
        ROTATE_CLOCKWISE,
        SCALE,
        BLUR,
        SHARPEN,
        RESET,
        COMPRESS

    }

    static int mCompressPercentage, mKbEnteredValue;


    //    boolean mChecked =true;\
    public enum Compress_TYPES {
        TEN(90),
        TWENTY(80), THIRTY(70), FORTY(60), FIFTY(50), SIXTY(40), SEVENTY(30), EIGHTY(8);
        int code;

        Compress_TYPES(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private static class Compress {
        String name;


        Compress_TYPES code;

        Compress(String name, Compress_TYPES code) {
            this.name = name;
            this.code = code;

        }
    }


    private class CompressAdaptor extends ArrayAdapter<Compress> {
        List<Compress> mCompressList;
        private final long fileSize;

        public CompressAdaptor(@NonNull Context context, List<Compress> compressList, long fileSize) {
            super(context, 0);
            mCompressList = compressList;
            this.fileSize = fileSize;
        }

        @Override
        public int getCount() {
            return mCompressList.size();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View row = LayoutInflater.from(getContext()).inflate(R.layout.compress_row, null);

            Compress compress = getItem(position);
            TextView textView = row.findViewById(R.id.text_name_percentage);
            textView.setText(compress.name);

            return row;
        }

        @Nullable
        @Override
        public Compress getItem(int position) {
            return mCompressList.get(position);
        }
    }

    CompressAdaptor mCompressAdaptor;
    Compress mCompress;

    static long mFileSize;


    static HashMap<Compress_TYPES, String> hashMap = new HashMap<>();


    OnCompressTypeChangedListener mOnCompressTypeChangedListen;
    OnResolutionSelectedListener mOnResolutionSelectedListener;


    @Override
    public void onResolutionSelected(ResolutionInfo res) {

        try {
            if (res.getFormatedString().equals("Custom")) {
                if (mOnResolutionSelectedListener == null) {
                    mOnResolutionSelectedListener = new OnResolutionSelectedListener() {
                        @Override
                        public void onResolutionSelected(ResolutionInfo resolutionInfo) {
                            ResizeFragment.this.onResolutionSelected(resolutionInfo);
                        }
                    };

                }

                if (mSingleFileImageInfo != null) {

                    BitmapResult bitmapResult = mDataApi.getBitmapFromAbsolutePathUri(getContext(), mSingleFileImageInfo.getAbsoluteFilePathUri(),
                            res.getWidth(), res.getHeight());

                    showCustomResolutionAlert(mOnResolutionSelectedListener, bitmapResult.getBitmap(), maxResolution, maxWidth, meanDimension);
                } else {
                    //multiple files
                    BitmapResult bitmapResult = mDataApi.getBitmapFromAbsolutePathUri(getContext(), mImageInfoMin.getAbsoluteFilePathUri(),
                            res.getWidth(), res.getHeight());

                    showCustomResolutionAlert(mOnResolutionSelectedListener, bitmapResult.getBitmap(), maxResolution, maxWidth, meanDimension);
                }

            } else if (res.getFormatedString().startsWith("Scale")) {

            } else {
//            String a[] = res.split("x");
//            BitmapResult bitmapResult =mDataApi.getBitmapFromAbsolutePathUri(getContext(),mSingleFileImageInfo.getAbsoluteFilePathUri());
                if (mSingleFileImageInfo != null) {
                    processImage(mSingleFileImageInfo, res.getWidth(), res.getHeight(), IMAGE_PROCESSING_TASKS.SCALE, mMyOnImageProcessedListener);
                } else {


                    doMultipleImageProcessing(IMAGE_PROCESSING_TASKS.SCALE, res);

                }
            }
        } catch (Throwable t) {
            showError(R.string.unknown_error);
        }
    }


    @Override
    public void showError(final Throwable ex) {
        System.gc();
        if (mBitmapProcessingTaskWeakReference != null) {
            mBitmapProcessingTaskWeakReference.clear();
            setLoadingIndicator(false);
        }
        Toast.makeText(getActivity(), ex.toString(), Toast.LENGTH_LONG).show();
        /*if(ex instanceof OutOfMemoryError || ex instanceof Error) {
            Toast.makeText(getActivity(), R.string.memory_error, Toast.LENGTH_LONG).show();
            System.gc();

        }else{
            Toast.makeText(getActivity(), R.string.some_error, Toast.LENGTH_LONG).show();

        }
       mhHandler.post(new Runnable() {
           @Override
           public void run() {



               if(ex instanceof OutOfMemoryError || ex instanceof Error) {
                   Toast.makeText(getActivity(), R.string.memory_error, Toast.LENGTH_LONG).show();
                   System.gc();

               }else{
                   Toast.makeText(getActivity(), R.string.some_error, Toast.LENGTH_LONG).show();

               }
           }
       });
*/
    }

    NestedWebView mWebView;
    String mCroppedUrlString;

    class MyOnImageProcessedListener implements BitmapProcessingTask.OnImageProcessedListener {


        @Override
        public void onImageLoaded(BitmapResult bitmapResult, final ImageInfo imageInfoOrg) {

            try {


                if (bitmapResult == null || bitmapResult.getError() != null) {
                    if (getContext() == null) {
                        return;
                    }
                    if (bitmapResult.getError() instanceof OutOfMemoryError) {

                        showError(R.string.not_enough_memory);
                        setLoadingIndicator(false);
                        return;

                    } else {
                        showError(R.string.unknown_error);
                        setLoadingIndicator(false);

                        return;
                    }
                }


                if (mSingleFileImageInfo != null && imageInfoOrg != null) {
                    mSingleFileImageInfo.setProcessedUri(bitmapResult.getContentUri());
                    mSingleFileImageInfo.setSaved(imageInfoOrg.isSaved());
                    mSingleFileImageInfo = imageInfoOrg;
                }

                if (autoSave) {
                    showDeleteTextView(requireActivity());
                }
                if (adCount++ % 2 == 0) {
                    showAd();
                }


                mResizePresenter.setSelectedImage(bitmapResult);


//            SaveBitmapToDevice saveBitmapToDevice = new SaveBitmapToDevice(getContext(),imageInfoOrg.getImageUri());
//
//            saveBitmapToDevice.execute(bitmapResult.getBitmap());

                mhHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setLoadingIndicator(false);

                        if (mBitmapProcessingTaskWeakReference != null) {
                            mBitmapProcessingTaskWeakReference.clear();
                        }
                    }
                }, 100);

            } catch (Exception e) {
                e.printStackTrace();
                showError(R.string.unknown_error);
            }
        }


    }

    private void showDeleteTextView(Activity activity) {



        try {
            boolean canDeleteFile = true;
            boolean muliple = false;
            if (mMultipleImagesAdaptor != null && mSingleFileImageInfo == null) {
                muliple = true;
                List<ImageInfo> imageInfoList = mMultipleImagesAdaptor.getImageInfoList();
                if (imageInfoList != null && imageInfoList.size() > 0) {

                    mSingleFileImageInfo = imageInfoList.get(0);

                }
            }

            if (mSingleFileImageInfo != null) {
                ImageInfo tobeDeleted = Utils.getImageInfo(null, getContext(), mSingleFileImageInfo.getImageUri(), mDataApi);
                if (muliple) {
                    mSingleFileImageInfo = null;
                }
                if (new File(tobeDeleted.getAbsoluteFilePathUri().getPath()).exists()) {
                    Uri tobedeleteduri = tobeDeleted.getAbsoluteFilePathUri();
                    if (tobedeleteduri != null) {
                        if (new File(tobedeleteduri.getPath()).exists()) {
                            canDeleteFile = true;
                            if (!muliple) {

                                mSingleFileImageInfo.setTobeDeletedUri(tobedeleteduri);
                            }

                        } else {

//                            return;
                        }
                    }

                } else {
//                    return;
                }
            }

//            tobedeletedTextView.setVisibility(View.VISIBLE);

            String thisLink = getString(R.string.delete_org);
            String resizePics = getString(R.string.resized_photo);
            if (!canDeleteFile) {
                thisLink = "";
            }
            String yourString = getString(R.string.saved_file, resizePics, thisLink);
            SpannableString spannableStringBuilder = new SpannableString(yourString);
            int startIndex = yourString.indexOf(thisLink);
            int endIndex = yourString.length();
            ClickableSpan click = null;
            if (canDeleteFile) {
                tobedeletedTextView.setVisibility(View.VISIBLE);
                click = new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        if (mSingleFileImageInfo != null) {
                            showDeleteAlert(getContext(), new OnDeleteSelectedListener() {
                                @Override
                                public void onDeleteSelected() {

                                    if (mSingleFileImageInfo.getTobeDeletedUri() != null) {
                                        mSingleFileImageInfo.setImageUri(mSingleFileImageInfo.getTobeDeletedUri());

                                        boolean deleted = mDataApi.deleteImageFile(activity, mSingleFileImageInfo, null);
                                        if (deleted) {
                                            showError(R.string.file_deleted);
                                            tobedeletedTextView.setVisibility(View.GONE);
//                                            mWebView.setVisibility(View.GONE);
                                            sizeTextView.setVisibility(View.GONE);
//                                            showAd();
                                   /*         mhHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        Utils.removeFragment((AppCompatActivity) getActivity(), ResizeFragment.this);
                                                        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(MyImagesFragment.class.getSimpleName());
                                                        if (fragment != null) {
                                                            Utils.removeFragment((AppCompatActivity) getActivity(), fragment);
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }, 100);*/


                                        } else {
                                            showError(R.string.unable_to_delete);
                                        }
                                    }


                                }
                            });

                        } else {
                            //multiple
                            List<ImageInfo> imageInfoList = mMultipleImagesAdaptor.getImageInfoList();
                            mDataApi.deleteImageFiles(activity,imageInfoList);
                        }
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(true);

                    }
                };
                spannableStringBuilder.setSpan(click,
                        startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            startIndex = yourString.indexOf(resizePics);
            endIndex = startIndex + resizePics.length();
            click = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    mhHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showCustomAppBar((AppCompatActivity) getActivity(), true);
                        }
                    }, 100);

                    MyImagesFragment myImagesFragment = MyImagesFragment.newInstance();
                    Utils.addFragment((AppCompatActivity) getActivity(), myImagesFragment, R.id.contentFrame, true);
                    myImagesFragment.setOnContextCreatedListener(new MyImagesFragment.OnContextCreatedListener() {
                        @Override
                        public void onContextCreated() {
                            myImagesFragment.loadMyImages();
                        }
                    });


                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(true);
                }
            };
            spannableStringBuilder.setSpan(click,
                    startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        /*UnderlineSpan underlineSpan = new UnderlineSpan();
                        spannableStringBuilder.setSpan(underlineSpan,
                                startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);*/
            tobedeletedTextView.setText(spannableStringBuilder);
            tobedeletedTextView.setMovementMethod(LinkMovementMethod.getInstance());

        } catch (Exception e) {
            showError(R.string.unknown_error);
            e.printStackTrace();
        }
    }

    MyOnImageProcessedListener mMyOnImageProcessedListener;
    boolean mMultiple;
    static int counter;

    private void processImage(final ImageInfo imageInfo, int w, int h,
                              final IMAGE_PROCESSING_TASKS image_PROCESSING_tasks,
                              BitmapProcessingTask.OnImageProcessedListener onImageProcessedListener) {
        int nw = w;
        int nh = h;





/*
        BitmapProcessingTask currentTask = mBitmapProcessingTaskWeakReference!= null ? mBitmapProcessingTaskWeakReference.get() : null;
        if (currentTask != null ) {
            currentTask.cancel(true);
            setLoadingIndicator(false);
                Toast.makeText(getActivity(),"Processing old task..",Toast.LENGTH_SHORT).show();
            mBitmapProcessingTaskWeakReference.clear();
                return;
            // cancel previous loading (no check if the same URI because camera URI can be the same for
            // different images)
//            currentTask.cancel(true);
        }*/

        if (imageInfo == null) {
        }
        mhHandler.post(new Runnable() {
            @Override
            public void run() {
                setLoadingIndicator(true);

            }
        });


//        mBitmapProcessingTaskWeakReference = new WeakReference<BitmapProcessingTask>(
        BitmapProcessingTask bitmapProcessingTask = new BitmapProcessingTask(imageInfo, getContext(),
                nw, nh, maxResolution, imageInfo.getDataFile(),
                image_PROCESSING_tasks, mCompressPercentage, mKbEnteredValue, mDataApi, mMultiple, autoSave);
        bitmapProcessingTask.setOnImageProcessedListener(onImageProcessedListener);
//        bitmapProcessingTask.execute();
        bitmapProcessingTask.executeOnExecutor(executor);


    }

    @Override
    public void onCropImageStart() {
        mhHandler.post(new Runnable() {
            @Override
            public void run() {
                setLoadingIndicator(true);

            }
        });
    }

    @Override
    public void onCropImageComplete(CropImageView view, final CropImageView.CropResult result) {
        mhHandler.post(new Runnable() {
            @Override
            public void run() {
                if (result.getBitmap() != null) {
                    BitmapResult bitmapResult = new BitmapResult();

                    Uri path = null;
                    try {

                        DataFile dataFile = mSingleFileImageInfo.getDataFile();
                        if (dataFile != null && dataFile.getName() != null) {
                            mDataApi.saveImageInCache(dataFile, result.getBitmap(), 95);
                            path = mDataApi.getAbsoluteImagePathUriFromCache(dataFile);
                        }/*else {
                           mDataApi.saveImageInCache(result.getBitmap(), 95);
                           path = mDataApi.getImageUriFromCache();
                       }*/


//                    mSingleFileImageInfo = getImageInfo(getContext(),path);

//                    mImageUrlString =path.toString();
//                    mCroppedUrlString =path.toString();

//                     mCachedUrlString =path;
//                        bitmapResult.setBitmap(result.getBitmap());
                        bitmapResult.setContentUri(path);
                        if (autoSave == true) {
//                              mDataApi.saveImageInCache(mSingleFileImageInfo.getDataFile(),result.getBitmap(), 95);
                            mDataApi.copyImageFromCacheToGallery(requireActivity(), mSingleFileImageInfo.getDataFile());
//                            DataFile srcDatafile =mSingleFileImageInfo.
//                            mDataApi.copyImageFromCacheToGallery(mDataApi.getImageUriFromCache());
                        }
                        mSingleFileImageInfo.setAbsoluteFilePath(path);

                        mSingleFileImageInfo = Utils.getImageInfo(mSingleFileImageInfo, getContext(), path, mDataApi);

                        result.getBitmap().recycle();

                        if (autoSave) {

                            showDeleteTextView(requireActivity());
                        }
                    } catch (Throwable t) {
                        bitmapResult.setError(t);
                    }

                    mResizePresenter.setSelectedImage(bitmapResult);
//                        showWebView(result.getBitmap());

//                    mImageView.setImageBitmap(mBitmap);

                }
            }
        });
    }
}
