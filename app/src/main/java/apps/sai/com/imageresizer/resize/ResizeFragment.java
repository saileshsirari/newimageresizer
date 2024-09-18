package apps.sai.com.imageresizer.resize;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import apps.sai.com.imageresizer.BaseFragment;
import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.data.BitmapResult;
import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.DataFile;
import apps.sai.com.imageresizer.data.FileApi;
import apps.sai.com.imageresizer.data.ImageInfo;
import apps.sai.com.imageresizer.data.ResolutionInfo;
import apps.sai.com.imageresizer.listener.OnPreferenceChangedListener;
import apps.sai.com.imageresizer.listener.OnResolutionSelectedListener;
import apps.sai.com.imageresizer.myimages.MyImagesFragment;
import apps.sai.com.imageresizer.select.MenuItem;
import apps.sai.com.imageresizer.select.SelectActivity;
import apps.sai.com.imageresizer.select.SelectViewModel;
import apps.sai.com.imageresizer.settings.SettingsManager;
import apps.sai.com.imageresizer.util.ImageInfoLoader;
import apps.sai.com.imageresizer.util.ImageOperations;
import apps.sai.com.imageresizer.util.ImageProcessor;
import apps.sai.com.imageresizer.util.MultipleImagesAdaptor;
import apps.sai.com.imageresizer.util.NestedWebView;
import apps.sai.com.imageresizer.util.OnImageProcessedListener;
import apps.sai.com.imageresizer.util.Utils;

/**
 * Created by Sailesh on 03/01/18.
 */

public class ResizeFragment extends BaseFragment implements ResizeContract.View, OnResolutionSelectedListener, CropImageView.OnCropImageCompleteListener, OnPreferenceChangedListener {

    private static final String IMAGE_PATH = "_image_";
    private static String mResFileContent;
    private static String mImageUrlString;
    private RecyclerView mMenuRecyclerView;
    private final ResizeContract.Presenter mResizePresenter;
    RecyclerView mMultipleImagesRecyclerView;
    private static Intent mIntent;
    ResizeViewModel resizeViewModel;
    private SelectViewModel selectViewModel = null;

    public ResizeFragment() {
        mResizePresenter = new ResizePresenter();
    }

    private ImageInfo mImageInfoMin;
    DataApi mDataApi;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        autoSave = SettingsManager.getInstance().getAutoSaveImages();
        mDataApi = new FileApi(getContext());
        mMyOnImageProcessedListener = new MyOnImageProcessedListener();
        selectViewModel = new ViewModelProvider(requireActivity()).get(SelectViewModel.class);
        setHasOptionsMenu(true);
    }

    @Override
    public void processGalleryImage(Intent data) {
        mResizePresenter.onGalleryImageSelected(data);
    }


    @Override
    public void onGalleryImageSelected(final Intent data) {
        try {
            deleteCache(mDataApi);
            if (tobedeletedTextView != null) {
                tobedeletedTextView.setVisibility(View.GONE);
            }
            if ((data.getClipData() != null) && (data.getClipData().getItemCount() > 0)) {
                setLoadingIndicator(true);
                mSingleFileImageInfo = null;
                mhHandler.postDelayed(() -> {
                    try {
                        List<ImageInfo> imageInfoList = new ArrayList<>(data.getClipData().getItemCount());
                        int count = data.getClipData().getItemCount();
                        if (count > MAX_LOAD_IMAGES) {
                            Toast.makeText(getContext(), getString(R.string.showing_max_images, MAX_LOAD_IMAGES), Toast.LENGTH_LONG).show();
                            count = MAX_LOAD_IMAGES;
                        }

                        for (int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            ImageInfo imageInfo = new ImageInfo();
                            imageInfo.setImageUri(imageUri);
                            imageInfoList.add(imageInfo);
                        }

                        if (imageInfoList.size() == 1) {
                            mWebView.setVisibility(View.VISIBLE);
                            initSingleGalleryImageConfig();
                            mImageUrlString = imageInfoList.get(0).getImageUri().toString();
                            mResizePresenter.setImageSelected(imageInfoList.get(0).getImageUri().toString());
                        } else {
                            mWebView.setVisibility(View.GONE);
                            setMultipleImagesView(imageInfoList);
                        }
                        setLoadingIndicator(false);
                        requireActivity().invalidateOptionsMenu();
                    } catch (Exception e) {
                        showError(R.string.unknown_error);
                    }
                }, 100);

            } else {
                initSingleGalleryImageConfig();
                Uri imageUri = data.getData();
                mImageUrlString = imageUri.toString();
                mResizePresenter.setImageSelected(imageUri.toString());
                requireActivity().invalidateOptionsMenu();
            }

        } catch (Exception e) {
            showError(R.string.unable_to_load_image);
        }
    }

    private void initSingleGalleryImageConfig() {
        mSingleFileImageInfo = new ImageInfo();
        mMultipleImagesAdaptor = null;
        mWebView.setVisibility(View.VISIBLE);
        sizeTextView.setVisibility(View.VISIBLE);
        mMultiple = false;

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private Uri getUriWithId(Context context, Uri media_uri) {
        String[] column = {MediaStore.Images.Media._ID, String.valueOf(MediaStore.Images.Media.EXTERNAL_CONTENT_URI), MediaStore.MediaColumns.RELATIVE_PATH};
        String selection = MediaStore.MediaColumns.RELATIVE_PATH + " LIKE ?";

        Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(),
                media_uri,
                column, null, null, null);

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
    private static final int OPEN_ID = 2;
    private static final int COMPRESS_ID = 3;
    private static final int SCALE_ID = 4;
    private static final int CROP_ID = 5;
    private static final int SHARE_ID = 6;
    private static final int SAVE_ID = 7;
    private static final int RESET_ID = 9;

    @Override
    public void showAd() {
        SelectActivity selectActivity = (SelectActivity) getActivity();
        if (selectActivity != null) {
            selectActivity.showFullScreenAd();
        }
    }

    final ActivityResultLauncher<Intent> cropLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();

                }
            });


    final ActivityResultLauncher<CropImageContractOptions> cropImageLauncher = registerForActivityResult(new CropImageContract(), result -> {
        if (result.isSuccessful()) {
            Uri uri = result.getUriContent();
            String croppedImageFilePath = result.getUriFilePath(requireContext(), false);

        } else {
            Exception error = result.getError();
        }
    });

    private void fillMenuItems(final Context context, RecyclerView mRecyclerView, final BaseFragment baseFragment) {
        List<MenuItem> menuItemList = new ArrayList<>();
        menuItemList.add(MenuItem.newInstance(OPEN_ID, context.getString(R.string.action_open),
                R.drawable.ic_folder));
        menuItemList.add(MenuItem.newInstance(COMPRESS_ID, context.getString(R.string.action_compress),
                R.drawable.ic_compress));
        menuItemList.add(MenuItem.newInstance(SCALE_ID, context.getString(R.string.action_scale),
                R.drawable.ic_scale));
        if (mSingleFileImageInfo != null) {
            menuItemList.add(MenuItem.newInstance(CROP_ID, context.getString(R.string.main_action_crop),
                    R.drawable.ic_crop));
        }
        if (!autoSave) {
            menuItemList.add(MenuItem.newInstance(SAVE_ID, context.getString(R.string.action_save),
                    R.drawable.ic_save));
        }
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), menuItemList.size());

        MenuAdaptor mResizeAdaptor = new MenuAdaptor(getContext(), menuItemList, menuItem -> {

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
                        CropImageOptions cropImageOptions = new CropImageOptions();
                        cropImageOptions.guidelines = CropImageView.Guidelines.ON;
                        Intent intent = new Intent();
                        intent.putExtra(CropImageActivity.PARAM_URI, mSingleFileImageInfo.getImageUri().toString());
                        intent.setClass(requireActivity(), CropImageActivity.class);
                        Utils.getCropFragment((AppCompatActivity) requireActivity(), mSingleFileImageInfo.getImageUri().toString());

                        requireActivity().getSupportFragmentManager().setFragmentResultListener(CropImageViewFragment.CROP_REQUEST, getViewLifecycleOwner(), (requestKey, bundle) -> {
                                    String uri = bundle.getString(CropImageViewFragment.RESULT_URI);
                                    initSingleGalleryImageConfig();
                                    Uri imageUri = Uri.parse(uri);
                                    mImageUrlString = imageUri.toString();
                                    mResizePresenter.setImageSelected(imageUri.toString());
                                    if (autoSave) {
                                        mResizePresenter.applyImageEffect(mSingleFileImageInfo, ImageProcessingTask.SHARPEN, mMyOnImageProcessedListener, null);
                                    }
                                }
                        );
                        break;
                    case SCALE_ID:
                        if (mSingleFileImageInfo != null) {
                            showCustomScaleAlert(false, null, mSingleFileImageInfo, ResizeFragment.this);
                        } else {
                            if (mMultipleImagesAdaptor == null) {
                                showError(R.string.no_images);
                                return;
                            }
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
                        }
                        mResizePresenter.shareImage(getContext(), uri != null ? uri.toString() : "");
                        break;
                    case RESET_ID:
                        if (tobedeletedTextView != null) {
                            tobedeletedTextView.setVisibility(View.GONE);
                        }
                        mResizePresenter.applyImageEffect(null, ImageProcessingTask.RESET, mMyOnImageProcessedListener, null);
                        break;
                    case COMPRESS_ID:
                        mCompressPercentage = 0;
                        mKbEnteredValue = 0;
                        if (mOnCompressTypeChangedListen == null) {
                            mOnCompressTypeChangedListen = (quality, kbEntered) -> {
                                if (!kbEntered) {
                                    mCompressPercentage = quality;
                                } else {
                                    mKbEnteredValue = quality;
                                }
                                if (mSingleFileImageInfo != null) {
                                    mResizePresenter.applyImageEffect(mSingleFileImageInfo, ImageProcessingTask.COMPRESS, mMyOnImageProcessedListener, null);

                                } else {
                                    doMultipleImageProcessing(ImageProcessingTask.COMPRESS, null);
                                }
                            };
                        }
                        baseFragment.showCompressAlert(getContext(), mOnCompressTypeChangedListen, hashMap, mSingleFileImageInfo != null);
                        break;
                    case SAVE_ID:
                        mResizePresenter.saveImage();

                        break;

                }
            } catch (Throwable e) {
                showError(R.string.unknown_error);
            }

        });
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mResizeAdaptor);
    }

    private final static String TAG = ResizeFragment.class.getSimpleName();
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
                mMultipleImagesAdaptor.setAppearance(gridApperaance);
            }
        }
    }

    public static class MenuAdaptor extends RecyclerView.Adapter<MenuAdaptor.ResizeHolder> {
        List<MenuItem> mMenuItemList;
        Context mContext;
        OnMenuSelectedListener mMenuSelectedListener;

        public interface OnMenuSelectedListener {
            void onMenuSelected(MenuItem menuItem);
        }

        public MenuAdaptor(Context context, List<MenuItem> menuItemList, OnMenuSelectedListener menuSelectedListener) {
            mMenuItemList = menuItemList;
            this.mContext = context;
            this.mMenuSelectedListener = menuSelectedListener;
        }

        @NonNull
        @Override
        public MenuAdaptor.ResizeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ResizeHolder(LayoutInflater.from(mContext).inflate(R.layout.menu_row, null));
        }

        public void clear() {
            mMenuItemList.clear();
            this.notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(MenuAdaptor.ResizeHolder holder, int position) {
            TextView textView = holder.textView;
            ImageView imageView = holder.imageView;
            final MenuItem menuItem = mMenuItemList.get(position);
            textView.setText(menuItem.getName());
            imageView.setImageResource(menuItem.getImageResourcePath());
            holder.itemView.setOnClickListener(v -> mMenuSelectedListener.onMenuSelected(menuItem));
        }

        @Override
        public int getItemCount() {
            return mMenuItemList.size();
        }

        static class ResizeHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            View itemView;
            TextView textView;

            public ResizeHolder(View itemView) {
                super(itemView);
                this.itemView = itemView;
                imageView = itemView.findViewById(R.id.image_menu);
                textView = itemView.findViewById(R.id.text_name_menu);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.resize_menu, menu);
        menu.setGroupVisible(R.id.singleImageItem, mSingleFileImageInfo != null);
        menu.setGroupVisible(R.id.groupSave, !autoSave);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        try {
            int itemId = item.getItemId();
            if (itemId == R.id.action_open) {
                mResizePresenter.launchgalleryExternalApp(false);
                return true;
            } else if (itemId == R.id.action_crop) {
                if (mSingleFileImageInfo == null) {
                    showError(R.string.no_images);
                    return false;
                }
                CropImageOptions cropImageOptions = new CropImageOptions();
                cropImageOptions.guidelines = CropImageView.Guidelines.ON;
                Intent intent = new Intent();
                intent.putExtra(CropImageActivity.PARAM_URI, mSingleFileImageInfo.getImageUri().toString());
                intent.setClass(requireActivity(), CropImageActivity.class);
                Utils.getCropFragment((AppCompatActivity) requireActivity(), mSingleFileImageInfo.getImageUri().toString());

                requireActivity().getSupportFragmentManager().setFragmentResultListener(CropImageViewFragment.CROP_REQUEST, getViewLifecycleOwner(), (requestKey, bundle) -> {
                            String uri = bundle.getString(CropImageViewFragment.RESULT_URI);
                            initSingleGalleryImageConfig();
                            Uri imageUri = Uri.parse(uri);
                            mImageUrlString = imageUri.toString();
                            mResizePresenter.setImageSelected(imageUri.toString());
//                                    showCustomAppBar((AppCompatActivity) getActivity(), false);
                            if (autoSave) {
                                mResizePresenter.applyImageEffect(mSingleFileImageInfo, ImageProcessingTask.SHARPEN, mMyOnImageProcessedListener, null);
                            }
                        }
                );
                return true;

//                        mhHandler.postDelayed(() -> showCustomAppBar((AppCompatActivity) getActivity(), true), 100);
            } else if (itemId == R.id.action_scale) {
                if (mSingleFileImageInfo != null) {
                    showCustomScaleAlert(false, null, mSingleFileImageInfo, ResizeFragment.this);
                } else {
                    if (mMultipleImagesAdaptor == null) {
                        showError(R.string.no_images);
                        return false;
                    }
                    showCustomScaleAlert(true, null, mImageInfoMin, ResizeFragment.this);
                }
                return true;
            } else if (itemId == R.id.action_share) {
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
                        return false;
                    }
                }
                mResizePresenter.shareImage(getContext(), uri != null ? uri.toString() : "");
                return true;
            } else if (itemId == R.id.action_reset) {
                if (tobedeletedTextView != null) {
                    tobedeletedTextView.setVisibility(View.GONE);
                }
                mResizePresenter.applyImageEffect(null, ImageProcessingTask.RESET, mMyOnImageProcessedListener, null);
                return true;
            } else if (itemId == R.id.action_compress) {
                mCompressPercentage = 0;
                mKbEnteredValue = 0;
                if (mOnCompressTypeChangedListen == null) {
                    mOnCompressTypeChangedListen = (quality, kbEntered) -> {
                        if (!kbEntered) {
                            mCompressPercentage = quality;
                        } else {
                            mKbEnteredValue = quality;
                        }
                        if (mSingleFileImageInfo != null) {
                            mResizePresenter.applyImageEffect(mSingleFileImageInfo, ImageProcessingTask.COMPRESS, mMyOnImageProcessedListener, null);

                        } else {
                            doMultipleImageProcessing(ImageProcessingTask.COMPRESS, null);
                        }
                    };
                }
                baseFragment.showCompressAlert(getContext(), mOnCompressTypeChangedListen, hashMap, mSingleFileImageInfo != null);
                return true;
            } else if (itemId == R.id.action_save) {
                mResizePresenter.saveImage();
                return true;
            }
        } catch (Throwable e) {
            showError(R.string.unknown_error);
        }
        return false;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("");
        this.baseFragment = this;
        /*requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                   }

            @Override
            public boolean onMenuItemSelected(@NonNull android.view.MenuItem menuItem) {
                try {
                    int itemId = menuItem.getItemId();
                    if (itemId == R.id.action_open) {
                        mResizePresenter.launchgalleryExternalApp(false);
                        return true;
                    } else if (itemId == R.id.action_crop) {
                        if (mSingleFileImageInfo == null) {
                            showError(R.string.no_images);
                            return false;
                        }
                        CropImageOptions cropImageOptions = new CropImageOptions();
                        cropImageOptions.guidelines = CropImageView.Guidelines.ON;
                        Intent intent = new Intent();
                        intent.putExtra(CropImageActivity.PARAM_URI, mSingleFileImageInfo.getImageUri().toString());
                        intent.setClass(requireActivity(), CropImageActivity.class);
                        Utils.getCropFragment((AppCompatActivity) requireActivity(), mSingleFileImageInfo.getImageUri().toString());

                        requireActivity().getSupportFragmentManager().setFragmentResultListener(CropImageViewFragment.CROP_REQUEST, getViewLifecycleOwner(), (requestKey, bundle) -> {
                                    String uri = bundle.getString(CropImageViewFragment.RESULT_URI);
                                    initSingleGalleryImageConfig();
                                    Uri imageUri = Uri.parse(uri);
                                    mImageUrlString = imageUri.toString();
                                    mResizePresenter.setImageSelected(imageUri.toString());
//                                    showCustomAppBar((AppCompatActivity) getActivity(), false);
                                    if (autoSave) {
                                        mResizePresenter.applyImageEffect(mSingleFileImageInfo, ImageProcessingTask.SHARPEN, mMyOnImageProcessedListener, null);
                                    }
                                }
                        );
                        return true;

//                        mhHandler.postDelayed(() -> showCustomAppBar((AppCompatActivity) getActivity(), true), 100);
                    } else if (itemId == R.id.action_scale) {
                        if (mSingleFileImageInfo != null) {
                            showCustomScaleAlert(false, null, mSingleFileImageInfo, ResizeFragment.this);
                        } else {
                            if (mMultipleImagesAdaptor == null) {
                                showError(R.string.no_images);
                                return false;
                            }
                            showCustomScaleAlert(true, null, mImageInfoMin, ResizeFragment.this);
                        }
                        return true;
                    } else if (itemId == R.id.action_share) {
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
                                return false;
                            }
                        }
                        mResizePresenter.shareImage(getContext(), uri != null ? uri.toString() : "");
                        return true;
                    } else if (itemId == R.id.action_reset) {
                        if (tobedeletedTextView != null) {
                            tobedeletedTextView.setVisibility(View.GONE);
                        }
                        mResizePresenter.applyImageEffect(null, ImageProcessingTask.RESET, mMyOnImageProcessedListener, null);
                        return true;
                    } else if (itemId == R.id.action_compress) {
                        mCompressPercentage = 0;
                        mKbEnteredValue = 0;
                        if (mOnCompressTypeChangedListen == null) {
                            mOnCompressTypeChangedListen = (quality, kbEntered) -> {
                                if (!kbEntered) {
                                    mCompressPercentage = quality;
                                } else {
                                    mKbEnteredValue = quality;
                                }
                                if (mSingleFileImageInfo != null) {
                                    mResizePresenter.applyImageEffect(mSingleFileImageInfo, ImageProcessingTask.COMPRESS, mMyOnImageProcessedListener, null);

                                } else {
                                    doMultipleImageProcessing(ImageProcessingTask.COMPRESS, null);
                                }
                            };
                        }
                        baseFragment.showCompressAlert(getContext(), mOnCompressTypeChangedListen, hashMap, mSingleFileImageInfo != null);
                        return true;
                    } else if (itemId == R.id.action_save) {
                        mResizePresenter.saveImage();
                        return true;
                    }
                } catch (Throwable e) {
                    showError(R.string.unknown_error);
                }
                return false;
            }
        },getViewLifecycleOwner(), Lifecycle.State.RESUMED);*/
    }


    //    MultipleImageProcessingDialog multipleImageProcessingListener;
    volatile boolean mCancelMutipleTask;

    int adCount = 1;

    private void doMultipleImageProcessing(final ImageProcessingTask imageProcessingTask, final ResolutionInfo resolutionInfo) {
        if (mMultipleImagesAdaptor == null) {
            showError(R.string.no_images);
            return;
        }
        setLoadingIndicator(true);
        List<ImageInfo> imageInfoList = mMultipleImagesAdaptor.getImageInfoList();
        final List<ImageInfo> mImageInfoListCached = new ArrayList<>();
        for (int i = 0; i < imageInfoList.size(); i++) {
            ImageInfo imageInfo = imageInfoList.get(i);
            BitmapResult bitmapResult = mResizePresenter.applyImageEffect(imageInfo, imageProcessingTask, null, resolutionInfo);
            if (bitmapResult.getError() != null || bitmapResult.getContentUri() == null) {
                if (bitmapResult.getError() instanceof OutOfMemoryError) {
                    Toast.makeText(getContext(), getString(R.string.out_of_memory), Toast.LENGTH_LONG).show();
                    setLoadingIndicator(false);
                    return;
                } else {
                    showError(R.string.unknown_error);
                    continue;
                }
            }
            ImageInfo compressedImageInfo = Utils.getImageInfo(imageInfo, getContext(), bitmapResult.getContentUri(), mDataApi);
            if (compressedImageInfo != null) {
                compressedImageInfo.setImageUri(bitmapResult.getContentUri());
                mMultipleImagesAdaptor.showProcessedInfo(compressedImageInfo);
                mImageInfoListCached.add(compressedImageInfo);
            }

        }
        mMultipleImagesAdaptor.showProcessedInfoList(mImageInfoListCached, imageProcessingTask);
        if (autoSave) {
            showDeleteTextView(requireActivity());
        }
        setLoadingIndicator(false);
    }

    @Override
    public BitmapResult applyImageEffect(ImageInfo imageInfo, ImageProcessingTask imageProcessingTask, OnImageProcessedListener onImageProcessedListener, ResolutionInfo resolutionInfo) {

        if (imageProcessingTask != null) {
            if (imageProcessingTask == ImageProcessingTask.SCALE) {
                int w = resolutionInfo.getWidth();
                int h = resolutionInfo.getHeight();
                if (mMultiple) {
                    int orgWidth = imageInfo.getWidth();
                    int orgHeight = imageInfo.getHeight();
                    if (resolutionInfo.isPreResolutionSelected()) {
                        if (resolutionInfo.isAspect()) {
                            if (orgWidth > orgHeight) {
                                h = Utils.calculateAspectRatioHeight(new Point(orgWidth, orgHeight), w).y;
                            } else {
                                w = Utils.calculateAspectRatioWidth(new Point(orgWidth, orgHeight), h).x;
                            }
                        }
                    } else if (resolutionInfo.isPercentageSelected()) {
                        if (orgWidth > orgHeight) {
                            w = (int) (orgWidth * w / (float) 100);
                            h = Utils.calculateAspectRatioHeight(new Point(orgWidth, orgHeight), w).y;
                        } else {
                            h = (int) (orgHeight * h / (float) 100);
                            w = Utils.calculateAspectRatioWidth(new Point(orgWidth, orgHeight), h).x;
                        }

                    } else { //custom resolution
                        if (resolutionInfo.isAspect()) {
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
                return scaleImage(imageInfo, w, h, onImageProcessedListener);
            } else if (imageProcessingTask == ImageProcessingTask.ROTATE_CLOCKWISE) {
                //do rotate
                return rotateImageClockWise(imageInfo, onImageProcessedListener);
            } else if (imageProcessingTask == ImageProcessingTask.BLUR) {
                return blurImage(imageInfo, onImageProcessedListener);
            } else if (imageProcessingTask == ImageProcessingTask.RESET) {
                if (mSingleFileImageInfo != null) {
                    mSingleFileImageInfo.setAbsoluteFilePath(mSingleFileImageInfo.getImageUri());
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
                }

            } else if (imageProcessingTask == ImageProcessingTask.SHARPEN) {
                //do rotate
                return sharpenImage(imageInfo, onImageProcessedListener);
            } else if (imageProcessingTask == ImageProcessingTask.COMPRESS) {
                return compressImage(imageInfo, onImageProcessedListener);
            }
        }
        return null;
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
            if (imageInfoList != null && !imageInfoList.isEmpty()) {
                for (int i = 0; i < imageInfoList.size(); i++) {
                    DataFile dataFile = imageInfoList.get(i).getDataFile();
                    Uri uri = mDataApi.getImageUriFromCacheWithFileProvider(dataFile.getName());
                    uriList.add(uri);
                }

            } else {
                imageInfoList = mMultipleImagesAdaptor.getImageInfoList();
                if (imageInfoList == null || imageInfoList.isEmpty()) {
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

    List<ImageInfoLoader> mImageInfoLoadingTasks;
    boolean saved;
    List<ImageInfo> mImageInfoList;

    @Override
    public void saveImage() {
        try {
            Uri uriResult;
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
                }
            } else {
                saved = false;
                if (mMultipleImagesAdaptor == null) {
                    showError(R.string.no_images);
                    return;
                }
                mImageInfoLoadingTasks = new ArrayList<>();
                mImageInfoList = mMultipleImagesAdaptor.getProcessedImageInfoList();
                boolean original = true;
                if (mImageInfoList != null && !mImageInfoList.isEmpty()) {
                    //images are original yet
                    original = false;
                } else {
                    mImageInfoList = mMultipleImagesAdaptor.getImageInfoList();
                }
                if (mImageInfoList == null) {
                    return;
                }
                setLoadingIndicator(true);
                for (ImageInfo imageInfo : mImageInfoList) {
                    count++;
                    if (original) {
                        ImageInfoLoader imageInfoLoader =
                                new ImageInfoLoader(
                                        imageInfo, mDataApi,
                                        ImageOperations.IMAGE_FILE_SAVE_GALLERY_TO_MY_FOLDER
                                );
                        ImageInfo imageInfo1 = imageInfoLoader.process(requireContext());
                        int count1 = mImageInfoList.indexOf(imageInfo1);
                        if (count1 == mImageInfoList.size() - 1) {
                            if (!imageInfo1.isSaved()) {
                                showError(R.string.unable_to_save_image);
                            }
                            imageInfo1.setSaved(true);
                            if (!saved) {
                                showDeleteTextView(requireActivity());
                                saved = true;
                            }
                        }

                    } else {
                        ImageInfoLoader imageInfoLoader =
                                new ImageInfoLoader(
                                        imageInfo, mDataApi,
                                        ImageOperations.IMAGE_FILE_SAVE_CACHE_TO_GALLERY
                                );
                        ImageInfo imageInfo12 = imageInfoLoader.process(requireContext());
                        int count12 = mImageInfoList.indexOf(imageInfo12);
                        if (count12 == mImageInfoList.size() - 1) {
                            if (!imageInfo12.isSaved()) {
                                showError(R.string.unable_to_save_image);
                            }
                            imageInfo12.setSaved(true);
                        }
                    }
                }
                setLoadingIndicator(false);
            }

        } catch (Throwable e) {
            showError(R.string.unknown_error);
        }
    }


    public BitmapResult blurImage(ImageInfo bitmap, OnImageProcessedListener onImageProcessedListener) {
        return processImage(bitmap, 0, 0, ImageProcessingTask.BLUR, onImageProcessedListener);
    }

    public BitmapResult sharpenImage(ImageInfo bitmap, OnImageProcessedListener onImageProcessedListener) {
        return processImage(bitmap, 0, 0, ImageProcessingTask.SHARPEN, onImageProcessedListener);
    }

    public BitmapResult compressImage(ImageInfo bitmap, OnImageProcessedListener onImageProcessedListener) {
        return processImage(bitmap, 0, 0, ImageProcessingTask.COMPRESS, onImageProcessedListener);
    }

    public BitmapResult rotateImageClockWise(ImageInfo bitmap, OnImageProcessedListener onImageProcessedListener) {
        return processImage(bitmap, 0, 0, ImageProcessingTask.ROTATE_CLOCKWISE, onImageProcessedListener);
    }


    public BitmapResult scaleImage(ImageInfo bitmap, int w, int h, OnImageProcessedListener onImageProcessedListener) {
        return processImage(bitmap, w, h, ImageProcessingTask.SCALE, onImageProcessedListener);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mResizePresenter.takeView(this);
        resizeViewModel = new ViewModelProvider(this).get(ResizeViewModel.class);
        SelectActivity selectActivity = (SelectActivity) getActivity();
        if (selectActivity != null) {
            selectActivity.setCurrentFragment(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mResizePresenter.dropView();
    }

    private ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_resize, null);
        mWebView = mView.findViewById(R.id.webview);
        tobedeletedTextView = mView.findViewById(R.id.tobedeletedTextview);
        if (mhHandler == null) {
            mhHandler = new Handler(Looper.getMainLooper());
        }
        mProgressBar = requireActivity().findViewById(R.id.activityProgressBar);
        sizeTextView = mView.findViewById(R.id.size);
        tobedeletedTextView = mView.findViewById(R.id.tobedeletedTextview);
        mMultipleImagesRecyclerView = mView.findViewById(R.id.multiple_image_recycler_view);
        onGalleryImageSelected(mIntent);
        return mView;
    }

    private BaseFragment baseFragment;


    int count = 0;
    int lastIndex;

    public void setMultipleImagesView(final List<ImageInfo> imageInfoList) {
        count = 0;
        mMultiple = true;
        sizeTextView.setVisibility(View.GONE);
        mMultipleImagesRecyclerView.setVisibility(View.VISIBLE);
        gridSize = SettingsManager.getInstance().getGridSize();
        mMultipleImagesAdaptor = new MultipleImagesAdaptor(this, imageInfoList,
                mMultipleImagesRecyclerView,
                new GridLayoutManager(getContext(), gridSize, GridLayoutManager.VERTICAL,
                        false), mDataApi);
        mMultipleImagesRecyclerView.setAdapter(mMultipleImagesAdaptor);
        mhHandler.post(() -> {
            try {
                final ImageInfo processedImageInfo = Utils.getImageInfo(null, getContext(), imageInfoList.get(0).getImageUri(), mDataApi);
                if (isEmpty(processedImageInfo)) {
                    showError(R.string.unable_to_load_image);
                }
                mImageInfoMin = processedImageInfo;
                imageInfoList.remove(0);
                imageInfoList.add(0, processedImageInfo);
                mhHandler.post(() -> {
                    mImageInfoLoadingTasks = new ArrayList<>();
                    for (int i = 0; i < imageInfoList.size(); i++) {
                        ImageInfo imageInfoInner = imageInfoList.get(i);
                        ImageInfoLoader imageInfoLoadingTask =
                                new ImageInfoLoader(imageInfoInner, mDataApi
                                        , ImageOperations.IMAGE_INFO_LOAD);
                        ImageInfo imageInfo = imageInfoLoadingTask.process(requireContext());
                        try {
                            Log.e(TAG, imageInfo.getFileSize() + " Loaded ");
                            if (mCancelMutipleTask) {
                                lastIndex = imageInfoList.indexOf(imageInfo);
                                return;
                            }

                            mMultipleImagesAdaptor.setImageInfo(imageInfo);
                            if (isEmpty(processedImageInfo)) {
                                showError(R.string.unable_to_load_image);
                                mMultipleImagesAdaptor.remove(imageInfo);
                                return;
                            }
                            long resInner = (long) imageInfo.getWidth() * imageInfo.getHeight();
                            if (resInner < (long) mImageInfoMin.getWidth() * mImageInfoMin.getHeight()) {
                                mImageInfoMin = imageInfo;
                            }
                        } catch (Exception e) {
                            showError(R.string.unknown_error);
                        }
                        mImageInfoLoadingTasks.add(imageInfoLoadingTask);
                    }
                });
            } catch (Throwable t) {
                showError(R.string.unknown_error);
            }

        });
    }

    private boolean isEmpty(ImageInfo imageInfo) {

        return imageInfo == null || imageInfo.getWidth() == 0 ||
                imageInfo.getHeight() == 0 || imageInfo.getFileSize() == 0;
    }

    MultipleImagesAdaptor mMultipleImagesAdaptor;

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
            if (show) {
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

  /*  public void setCurrentFragment(CropFragment fragment) {
        mCurrentFragment = fragment;
    }
*/
    // private CropFragment mCurrentFragment;

    @Override
    public void setSelectedImage(final BitmapResult bitmapResult) {
        try {
            setLoadingIndicator(false);
            showWebView(bitmapResult);

        } catch (Throwable e) {
            showError(R.string.unknown_error);
        }

    }

    private final static String CACHE_FILE = "cached_";
    static ImageInfo mSingleFileImageInfo;

    private void showWebView(BitmapResult bitmapResult) {
        if (getContext() == null) {
            return;
        }

        if (mSingleFileImageInfo == null) {//if it is loaded from gallery it becomes null
            mSingleFileImageInfo = new ImageInfo();
        }
        if (bitmapResult == null) {
            showError(R.string.unable_to_load_image);
            return;
        }
        if (isEmpty(mSingleFileImageInfo) /*|| path ==null*/) {
            showError(R.string.unable_to_load_image);
            return;
        }
        showFIleSize(mSingleFileImageInfo, sizeTextView);//here too don't need file://
        final String mimeType = "text/html";
        final String encoding = "utf-8";
        mWebView.clearCache(true);
        String data = "<html><head><title>Example</title><meta name=\"viewport\"\"content=\"width=100%"

                + ", initial-scale=1.0 \" /></head>";
        data = data + "<body><center><img width=100%  src=\"" + mSingleFileImageInfo.getAbsoluteFilePathUri() + "\" /></center></body></html>";
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
    TextView tobedeletedTextView;

    @Override
    public void setImageSelected(String imageUriString) {
        try {
            mSingleFileImageInfo = new ImageInfo();
            mSingleFileImageInfo.setImageUri(Uri.parse(imageUriString));
            mSingleFileImageInfo.setOriginalContentUri(mSingleFileImageInfo.getImageUri());
            sizeTextView.setVisibility(View.VISIBLE);
            mMultipleImagesRecyclerView.setVisibility(View.GONE);
            ImageInfoLoader imageInfoLoader = new
                    ImageInfoLoader(
                    mSingleFileImageInfo, mDataApi,
                    ImageOperations.IMAGE_INFO_LOAD
            );
            ImageInfo imageInfo = imageInfoLoader.process(requireContext());
            if (imageInfo.getImageUri() == null) {
                showError(R.string.image_does_not_exists);
                return;
            }
            imageInfo.setOriginalContentUri(mSingleFileImageInfo.getOriginalContentUri());
            mSingleFileImageInfo = imageInfo;
            BitmapResult bitmapResult = new BitmapResult();
            bitmapResult.setContentUri(mSingleFileImageInfo.getImageUri());
            mResizePresenter.setSelectedImage(bitmapResult);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError oe) {

        }

    }

    /**
     * Task used to load mBitmap async from UI thread
     */
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

    static HashMap<Compress_TYPES, String> hashMap = new HashMap<>();
    OnCompressTypeChangedListener mOnCompressTypeChangedListen;
    OnResolutionSelectedListener mOnResolutionSelectedListener;

    @Override
    public void onResolutionSelected(ResolutionInfo res) {

        try {
            if (res.getFormatedString().equals("Custom")) {
                if (mOnResolutionSelectedListener == null) {
                    mOnResolutionSelectedListener = ResizeFragment.this::onResolutionSelected;
                }
                if (mSingleFileImageInfo != null) {

                    mDataApi.getBitmapFromAbsolutePathUri(getContext(), mSingleFileImageInfo.getAbsoluteFilePathUri(),
                            res.getWidth(), res.getHeight());

                    showCustomResolutionAlert(mOnResolutionSelectedListener, maxResolution, maxWidth, meanDimension);
                } else {
                    //multiple files
                    mDataApi.getBitmapFromAbsolutePathUri(getContext(), mImageInfoMin.getAbsoluteFilePathUri(),
                            res.getWidth(), res.getHeight());

                    showCustomResolutionAlert(mOnResolutionSelectedListener, maxResolution, maxWidth, meanDimension);
                }

            } else if (res.getFormatedString().startsWith("Scale")) {

            } else {
                if (mSingleFileImageInfo != null) {
                    processImage(mSingleFileImageInfo, res.getWidth(), res.getHeight(), ImageProcessingTask.SCALE, mMyOnImageProcessedListener);
                } else {
                    doMultipleImageProcessing(ImageProcessingTask.SCALE, res);
                }
            }
        } catch (Throwable t) {
            showError(R.string.unknown_error);
        }
    }

    @Override
    public void showError(final Throwable ex) {
        System.gc();
        Toast.makeText(getActivity(), ex.toString(), Toast.LENGTH_LONG).show();
    }

    NestedWebView mWebView;

    class MyOnImageProcessedListener implements OnImageProcessedListener {
        @Override
        public void onImageLoaded(BitmapResult bitmapResult, final ImageInfo imageInfoOrg) {

            try {


                if (bitmapResult == null || bitmapResult.getError() != null) {
                    if (getContext() == null) {
                        return;
                    }
                    if (bitmapResult != null && bitmapResult.getError() instanceof OutOfMemoryError) {

                        showError(R.string.not_enough_memory);
                        setLoadingIndicator(false);
                        return;

                    }
                }
                if (mSingleFileImageInfo != null && imageInfoOrg != null) {
                    if (bitmapResult != null) {
                        mSingleFileImageInfo.setProcessedUri(bitmapResult.getContentUri());
                    }
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
                mhHandler.postDelayed(() -> {
                    setLoadingIndicator(false);
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
                if (imageInfoList != null && !imageInfoList.isEmpty()) {
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

                        }
                    }
                }
            }

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
                            showDeleteAlert(getContext(), () -> {

                                if (mSingleFileImageInfo.getTobeDeletedUri() != null) {
                                    mSingleFileImageInfo.setImageUri(mSingleFileImageInfo.getTobeDeletedUri());

                                    boolean deleted = mDataApi.deleteImageFile(activity, mSingleFileImageInfo, null);
                                    if (deleted) {
                                        showError(R.string.file_deleted);
                                        tobedeletedTextView.setVisibility(View.GONE);
                                        sizeTextView.setVisibility(View.GONE);
//                                            showAd();
                                    } else {
                                        showError(R.string.unable_to_delete);
                                    }
                                }


                            });

                        } else {
                            //multiple
                            List<ImageInfo> imageInfoList = mMultipleImagesAdaptor.getImageInfoList();
                            mDataApi.deleteImageFiles(activity, imageInfoList);
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
                    MyImagesFragment myImagesFragment = MyImagesFragment.newInstance();
                    Utils.addFragment((AppCompatActivity) requireActivity(), myImagesFragment, R.id.contentFrame, true);
//                    MenuHost menuHost= requireActivity();
                    requireActivity().invalidateOptionsMenu();
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(true);
                }
            };
            spannableStringBuilder.setSpan(click,
                    startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    private BitmapResult processImage(final ImageInfo imageInfo, int w, int h,
                                      final ImageProcessingTask imageProcessingTask,
                                      OnImageProcessedListener onImageProcessedListener) {
        setLoadingIndicator(true);
        ImageProcessor imageProcessor = new ImageProcessor(imageInfo, getContext(),
                w, h, maxResolution, imageInfo.getDataFile(),
                imageProcessingTask, mCompressPercentage, mKbEnteredValue, mDataApi, mMultiple, autoSave);
        imageProcessor.setOnImageProcessedListener(onImageProcessedListener);
        setLoadingIndicator(false);
        return imageProcessor.process();
    }


  /*  @Override
    public void onCropImageStart() {
        mhHandler.post(() -> setLoadingIndicator(true));
    }*/

    @Override
    public void onCropImageComplete(CropImageView view, final CropImageView.CropResult result) {

    }
}
