package apps.sai.com.imageresizer.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import apps.sai.com.imageresizer.BaseFragment;
import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.data.BitmapResult;
import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.FileApi;
import apps.sai.com.imageresizer.data.ImageInfo;

/**
 * Created by sailesh on 20/01/18.
 */

public class ImageDetailFragment extends BaseFragment implements View.OnLayoutChangeListener {
    private static final String IMAGE_URI_STRING = "_image_";
    private static final String IMAGE_URI_STRING_ORG = "_org";
    Handler mHandler = new Handler();
    ProgressBar mProgressBar;

    @Override
    public void showError(Throwable th) {
    }

    @Override
    public void showAd() {
    }

    @Override
    public void processGalleryImage(Intent data) {
    }

    public static ImageDetailFragment newInstance(Uri orgImageUri, Uri compressedImageUri) {
        Bundle args = new Bundle();
        if (compressedImageUri != null) {
            args.putString(IMAGE_URI_STRING, compressedImageUri.toString());
        }
        if (orgImageUri != null) {
            args.putString(IMAGE_URI_STRING_ORG, orgImageUri.toString());
        }
        ImageDetailFragment fragment = new ImageDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    DataApi mDataApi;
    int orgImageRotation = ExifInterface.ORIENTATION_NORMAL, processedImageRotation = ExifInterface.ORIENTATION_NORMAL;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mDataApi = new FileApi(getContext());
        if (getArguments() != null) {
            Uri orgUri = getArguments().getString(IMAGE_URI_STRING_ORG, null) != null ? Uri.parse(getArguments().getString(IMAGE_URI_STRING_ORG)) : null;
            if (orgUri != null) {
                int orientation = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    orientation = Utils.getImageOrientation(getContext(), orgUri);
                }
                mImageInfoOrg = new ImageInfo();
                mImageInfoOrg.setImageUri(orgUri);
                mImageInfoOrg.setAbsoluteFilePath(orgUri);
                mImageInfoOrg = Utils.getImageInfo(mImageInfoOrg, getContext(), orgUri, mDataApi);
                if (orientation != ExifInterface.ORIENTATION_NORMAL) {
                    orgImageRotation = orientation;
                }
            }
            Uri compressedUri = getArguments().getString(IMAGE_URI_STRING) != null ? Uri.parse(getArguments().getString(IMAGE_URI_STRING)) : null;
            if (compressedUri != null) {
                mImageInfoCompressed = new ImageInfo();
                mImageInfoCompressed.setImageUri(compressedUri);
                mImageInfoCompressed = Utils.getImageInfo(mImageInfoCompressed, getContext(), compressedUri, mDataApi);

            }
        }

    }

    public ImageView webviewOrg;
    public ImageView webviewCompressed;
    public TextView resTextView;
    public TextView sizeTextView;

    public TextView resTextViewCompressed;
    public TextView sizeTextViewCompressed;

    public TextView sepTextView;

    View parentCompressedView;
    boolean loaded = false;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mImageInfoCompressed != null) {
            parentCompressedView.setVisibility(View.VISIBLE);
            webviewCompressed.addOnLayoutChangeListener(this);
        }
        webviewOrg.addOnLayoutChangeListener(this);
    }

    ImageInfo mImageInfoOrg, mImageInfoCompressed;

    public static ImageDetailFragment newInstance() {

        Bundle args = new Bundle();

        ImageDetailFragment fragment = new ImageDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private void showImageView(final ImageView imageView, final ImageInfo mSingleFileImageInfo, final DataApi mDataApi) {
        BitmapLoader bitmapLoader = new BitmapLoader(mSingleFileImageInfo, mDataApi);
        BitmapResult bitmapResult = bitmapLoader.process(requireContext());
        if (bitmapResult.getError() != null) {
            if (bitmapResult.getError() instanceof OutOfMemoryError) {
                showMessage(mHandler, getString(R.string.out_of_memory));
            } else {
                showMessage(mHandler, getString(R.string.unknown_error));
            }
            return;
        }
        Bitmap bitmap = bitmapResult.getBitmap();
        if (bitmap == null) {
            showMessage(mHandler, getString(R.string.unknown_error));
            return;

        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int imageSize = imageView.getHeight();
        if (w > imageSize && h > imageSize) {

            if (w > h) {
                h = Utils.calculateAspectRatioHeight(new Point(w, h), imageSize).y;
                w = imageSize;

            } else {
                w = Utils.calculateAspectRatioWidth(new Point(w, h), imageSize).x;
                h = imageSize;
            }
            try {
                bitmap = mDataApi.scaleImage(bitmap, w, h, 0);

            } catch (Throwable e) {
                bitmapResult.setError(e);
                if (bitmapResult.getError() instanceof OutOfMemoryError) {
                    showMessage(mHandler, getString(R.string.out_of_memory));
                } else {
                    showMessage(mHandler, getString(R.string.unknown_error));
                }
                return;
            }
        }
        if (bitmap != null) {
            mSingleFileImageInfo.setWidth(bitmap.getWidth());
            mSingleFileImageInfo.setHeight(bitmap.getHeight());
            imageView.setImageBitmap(bitmap);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.image_detail, null);
        webviewOrg = view.findViewById(R.id.image_multiple);
        webviewCompressed = view.findViewById(R.id.image_multiple_compressed);
        resTextView = view.findViewById(R.id.text_name_resolution);
        sizeTextView = view.findViewById(R.id.text_name_size);
        resTextViewCompressed = view.findViewById(R.id.text_name_compressed_resolution);
        sizeTextViewCompressed = view.findViewById(R.id.text_name_compressed_size);
        sepTextView = view.findViewById(R.id.seperator);
        parentCompressedView = view.findViewById(R.id.parentCompressed);
        Utils.showFacebookBanner(getContext(), view, R.id.banner_container, "179547122769778_179622146095609");

        return view;
    }


    public void setLoadingIndicator(final boolean b) {
        if (mHandler == null || mProgressBar == null || getContext() == null || isDetached()) {
            return;
        }
        mHandler.post(() -> {
            if (b) {
                mProgressBar.setVisibility(View.VISIBLE);
            } else {
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        appCompatActivity.getSupportActionBar().setDisplayShowCustomEnabled(false);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (v == webviewOrg) {
            if (mImageInfoOrg != null) {
                showImageView(webviewOrg, mImageInfoOrg, mDataApi);
                showFIleSize(mImageInfoOrg, sizeTextView);//here too don't need file://
                sizeTextView.setText(String.format("Before %s ", sizeTextView.getText()));
                setLoadingIndicator(false);
                webviewOrg.removeOnLayoutChangeListener(this);
            }
        } else if (v == webviewCompressed) {
            if (mImageInfoCompressed != null) {
                parentCompressedView.setVisibility(View.VISIBLE);
                showImageView(webviewCompressed, mImageInfoCompressed, mDataApi);
                showFIleSize(mImageInfoCompressed, sizeTextViewCompressed);//here too don't need file://
                sizeTextViewCompressed.setText(String.format("After %s ", sizeTextViewCompressed.getText()));
                setLoadingIndicator(false);
                webviewCompressed.removeOnLayoutChangeListener(this);
            }
        }

    }
}
