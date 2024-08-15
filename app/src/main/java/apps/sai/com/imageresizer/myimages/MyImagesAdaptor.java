package apps.sai.com.imageresizer.myimages;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import apps.sai.com.imageresizer.BaseFragment;
import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.FileApi;
import apps.sai.com.imageresizer.data.ImageInfo;
import apps.sai.com.imageresizer.listener.MultipleImageProcessingDialog;
import apps.sai.com.imageresizer.resize.ResizeFragment;
import apps.sai.com.imageresizer.settings.SettingsManager;
import apps.sai.com.imageresizer.util.ImageDetailFragment;
import apps.sai.com.imageresizer.util.ImageInfoLoadingTask;
import apps.sai.com.imageresizer.util.SpacesItemDecoration;
import apps.sai.com.imageresizer.util.Utils;

/**
 * Created by sailesh on 20/01/18.
 */

public class MyImagesAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ResizeFragment.OnImagedSavedListener {
    List<ImageInfo> mImageInfoList;
    BaseFragment mContext;
    RecyclerView mRecyclerView;
    DataApi mDataApi;
    public final static int HEADER = 0;

    public final static int ITEM = 1;

    private List<ImageInfo> adImageInfoList;

    @Override
    public void onImageSaved(ImageInfo imageInfo) {
        reload();

    }

    public void remove(ImageInfo imageInfo) {

        if (mImageInfoList != null) {
            int index = mImageInfoList.indexOf(imageInfo);
            if (index != -1) {
                mImageInfoList.remove(index);
                notifyItemRemoved(index);
            }
        }
        if (mSelectedImageInfoList != null) {
            int index = mSelectedImageInfoList.indexOf(imageInfo);

            if (index != -1) {
                mSelectedImageInfoList.remove(index);
                notifyItemRemoved(index);
            }
        }


    }

    public void setItems(List<ImageInfo> imageInfoList) {

        setAdImages(imageInfoList, layoutManager);
        this.mImageInfoList = imageInfoList;
        notifyDataSetChanged();

    }

    public interface OnUiUpdateListener {
        void onDataChanged(int newAdaptorSize);

        void onImageDeleted(ImageInfo imageInfo);

        void onAllImagesDeleted(List<ImageInfo> mImageInfoList);

    }

    public interface OnULoadingCancelListener {
        void onLoadingCancel();

    }

    OnDetailedScreenLaunchedListener mOnDetailedScreenLaunchedListener;

    public interface OnDetailedScreenLaunchedListener {
        void onDetailScreenLaunched();
    }

    public void setOnDetailedScreenLaunchedListener(OnDetailedScreenLaunchedListener mOnDetailedScreenLaunchedListener) {
        this.mOnDetailedScreenLaunchedListener = mOnDetailedScreenLaunchedListener;
    }

    OnUiUpdateListener mOnUiUpdateListener;
    OnULoadingCancelListener mOnULoadingCancelListener;

    public void setOnUiUpdateListener(OnUiUpdateListener mOnUiUpdateListener) {
        this.mOnUiUpdateListener = mOnUiUpdateListener;
    }

    private List<ImageInfo> getNonAdList(List<ImageInfo> imageInfoList) {
        List<ImageInfo> nonAdImageInfoList = new ArrayList<>();
        for (ImageInfo imageInfo : imageInfoList) {
            if (imageInfo.isAd() == false) {
                nonAdImageInfoList.add(imageInfo);
            }
        }
        return nonAdImageInfoList;
    }

    public void selectAllItems() {
        mSelectedImageInfoList.clear();
        mSelectedImageInfoList.addAll(getNonAdList(mImageInfoList));

//        mSelectedImageInfoList.addAll(mImageInfoList);
        notifyDataSetChanged();

    }

    public void clearAllItems() {
        mSelectedImageInfoList.clear();
        notifyDataSetChanged();

    }

    public void deleteAllItems() {
        mSelectedImageInfoList.clear();
        mImageInfoList.clear();
        notifyDataSetChanged();

    }

    List<ImageInfoLoadingTask> mImageInfoLoadingTasks;

    public void cancelAllTasks() {
        if (mImageInfoLoadingTasks != null) {
            for (int i = 0; i < mImageInfoLoadingTasks.size(); i++) {
                ImageInfoLoadingTask imageInfoLoadingTask = mImageInfoLoadingTasks.get(i);
                if (imageInfoLoadingTask != null) {
                    imageInfoLoadingTask.cancel(true);
                }
            }
        }
    }

    int mCount;
    boolean shownError;

    public void deleteSelectedImages() {
        mCount = 0;
        //cancel loading
        cancelAllTasks();

        final MultipleImageProcessingDialog multipleImageProcessingDialog = new MultipleImageProcessingDialog(
                (imageInfo, pos) -> cancelAllTasks(), mContext.getContext(), null);

        if (mOnULoadingCancelListener != null) {
            mOnULoadingCancelListener.onLoadingCancel();
        }

        mImageInfoLoadingTasks = new ArrayList<>();
        if (mSelectedImageInfoList.size() > 0) {
            multipleImageProcessingDialog.onProcessingStarted(mSelectedImageInfoList.get(0), 1,
                    mSelectedImageInfoList.size());
        }
        final int size = mSelectedImageInfoList.size();

        shownError = false;
        List<ImageInfo> imageInfoList = new ArrayList<>();

        for (ImageInfo imageInfo : mSelectedImageInfoList) {
            if (imageInfo.isAd()) {
                continue;
            }
            String thumb = imageInfo.getAbsoluteThumbFilePath();
            if (imageInfo.getAbsoluteFilePathUri() == null && imageInfo.getImageUri() != null) {
                imageInfo = Utils.getImageInfo(imageInfo, mContext.getContext(), imageInfo.getImageUri(), mDataApi);
            }
            if (imageInfo != null) {
                if (imageInfo.getAbsoluteFilePathUri() != null) {
                    Uri orgUri = imageInfo.getImageUri();
                    imageInfo.setImageUri(imageInfo.getAbsoluteFilePathUri());
                    imageInfoList.add(imageInfo);
                }
            }
        }
        mDataApi.deleteImageFiles((Activity) mContext.getContext(), imageInfoList);
        if (mOnUiUpdateListener != null) {
            List<ImageInfo> list = new ArrayList<>();
            for (ImageInfo imageInfo : imageInfoList) {
                if (!imageInfo.isDeleted()) {
                    list.add(imageInfo);
                }
            }
            mOnUiUpdateListener.onAllImagesDeleted(list);
        }
        multipleImageProcessingDialog.onProcessingDone(mImageInfoList);
    }

    private void showError(int errorId) {

        try {

            String s = mContext.getActivity().getString(errorId);

            Toast.makeText(mContext.getContext(), s, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {

        }
    }

    public int setImageInfo(ImageInfo imageInfo) {

        int index = mImageInfoList.indexOf(imageInfo);
        if (index != -1) {


            mImageInfoList.set(index, imageInfo);
            notifyItemChanged(index);
        }
        return index;
    }


    public interface OnImagesSelectedListener {
        void onAnyImageSelected();

        void onNoneImageSelected();
    }

    OnImagesSelectedListener mOnImagesSelectedListener;
    MyImagesPresenter myImagesPresenter;

    private int appereance;

    private int repeatAfter;
    private GridLayoutManager layoutManager;

    public MyImagesAdaptor(MyImagesPresenter myImagesPresenter, BaseFragment context, List<ImageInfo> imageInfoList,

                           RecyclerView recyclerView, GridLayoutManager layoutManager,
                           OnUiUpdateListener onUiUpdateListener, OnULoadingCancelListener onULoadingCancelListener) {
        this.mOnUiUpdateListener = onUiUpdateListener;
        mContext = context;
        mRecyclerView = recyclerView;
        mRecyclerView.setLayoutManager(layoutManager);
        this.mImageInfoList = imageInfoList;
        this.myImagesPresenter = myImagesPresenter;
        appereance = SettingsManager.getInstance().getGridAppearnece();

        mOnImagesSelectedListener = (OnImagesSelectedListener) context;
        this.mOnULoadingCancelListener = onULoadingCancelListener;
        mDataApi = new FileApi(context.getContext());
        adImageInfoList = new ArrayList<>();
        this.layoutManager = layoutManager;

        int spacingInPixels = context.getResources().getDimensionPixelSize(R.dimen.spacing);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));


        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (getItemViewType(position)) {
                    case HEADER:
                        return layoutManager.getSpanCount();

                    case ITEM:
                        return 1;

                    default:
                        return 1;
                }
            }
        });


    }

    private void setAdImages(List<ImageInfo> imageInfoList, GridLayoutManager layoutManager) {


        if (appereance == 0) {//large
            repeatAfter = layoutManager.getSpanCount() * 2;//after these rows show ad

        } else if (appereance == 2) { //small
            repeatAfter = layoutManager.getSpanCount() * 5;

        } else if (appereance == 1) { //normal
            repeatAfter = layoutManager.getSpanCount() * 3;

        }
        repeatAfter = repeatAfter * 2;
        //remove old ads if any

        int size = imageInfoList.size();
        List<ImageInfo> nonAdImageInfoList = new ArrayList<>();
        for (int i = 0; i < imageInfoList.size(); i++) {
            if (imageInfoList.get(i).isAd() == false) {
                nonAdImageInfoList.add(imageInfoList.get(i));
            }
        }
        imageInfoList.clear();
        imageInfoList.addAll(nonAdImageInfoList);

        for (int i = repeatAfter; i < size; i += repeatAfter) {
            ImageInfo imageInfo = new ImageInfo();
            imageInfo.setAd(true);
            imageInfoList.add(i, imageInfo);
            ;

        }
        notifyDataSetChanged();
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        if (mRecyclerView != null) {
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        if (viewType == ITEM) {

            View view = LayoutInflater.from(mContext.getContext()).inflate(R.layout.mymultiple_image_row, null);

            return new MultipleImagesHolder(view);
        }

        View view = LayoutInflater.from(mContext.getContext()).inflate(R.layout.header_gridview, null);
        HeaderView headerView = new HeaderView(view);

        return headerView;


    }

    static class HeaderView extends RecyclerView.ViewHolder {
        public View rootView;

        public HeaderView(View itemView) {
            super(itemView);
            this.rootView = itemView;

        }
    }

    List<ImageInfo> mSelectedImageInfoList = new ArrayList<>();

    public void setAppereance(int val) {
        appereance = val;
        if (mRecyclerView != null) {
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder1, int position) {

        ImageInfo imageInfo = mImageInfoList.get(position);


        if (holder1 instanceof MultipleImagesHolder) {
            MultipleImagesHolder holder = (MultipleImagesHolder) holder1;


            RelativeLayout.LayoutParams reLayoutParams = new RelativeLayout.LayoutParams(holder.imageView.getLayoutParams().width, ViewGroup.LayoutParams.MATCH_PARENT);
            boolean hideCompressedInfo = false;

            holder.imageView.setTag(imageInfo);
            int span = layoutManager.getSpanCount();
            if (appereance == 0) {//large

                if (span == 1 || span == 2) {

                    reLayoutParams.height = (int) mContext.getResources().getDimension(R.dimen.large_app);
                } else {
                    reLayoutParams.height = (int) mContext.getResources().getDimension(R.dimen.large_app);
                    reLayoutParams.height = reLayoutParams.height - reLayoutParams.height / 3;
                    hideCompressedInfo = true;
                }
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.imageView.setLayoutParams(reLayoutParams);
            } else if (appereance == 2) { //small
                hideCompressedInfo = true;
                reLayoutParams.height = (int) mContext.getResources().getDimension(R.dimen.small_app);
                holder.imageView.setLayoutParams(reLayoutParams);
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            } else if (appereance == 1) { //normal
                if (span == 1 || span == 2) {

                    reLayoutParams.height = (int) mContext.getResources().getDimension(R.dimen.normal_app);
                } else {
                    reLayoutParams.height = (int) mContext.getResources().getDimension(R.dimen.normal_app) / 2;
                    hideCompressedInfo = true;
                }
                holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.imageView.setLayoutParams(reLayoutParams);
            }


            if (imageInfo.getAbsoluteThumbFilePath() != null) {
                File file = new File(imageInfo.getAbsoluteThumbFilePath());

                holder.imageView.setImageURI(Uri.fromFile(file));
            } else {
//            holder.webviewOrg.setImageURI(imageInfo.getImageUri());
            }
            if (imageInfo.getWidth() > 0 && imageInfo.getHeight() > 0) {
                holder.resTextView.setText(imageInfo.getWidth() + " x " + imageInfo.getHeight());
            }

            if (imageInfo.getFormatedFileSize() != null && imageInfo.getFormatedFileSize().length() > 0) {

                holder.sizeTextView.setText(String.format("(%s)", imageInfo.getFormatedFileSize()));
            }

            holder.checkBox.setTag(mImageInfoList.get(position));
            holder.checkBox.setOnCheckedChangeListener(null);

            if (mSelectedImageInfoList.contains(mImageInfoList.get(position))) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                    ImageInfo selImageInfo = (ImageInfo) buttonView.getTag();
                    if (isChecked) {
                        if (mSelectedImageInfoList.contains(selImageInfo) == false) {
                            mSelectedImageInfoList.add(selImageInfo);
                        }
                    } else {
                        mSelectedImageInfoList.remove(selImageInfo);
                    }
                    if (mSelectedImageInfoList.size() > 0) {
                        mOnImagesSelectedListener.onAnyImageSelected();
                    } else {
                        mOnImagesSelectedListener.onNoneImageSelected();
                    }

                }
            });

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ImageInfo imageInfoIn = (ImageInfo) holder.imageView.getTag();
                    Intent intent = new Intent();
                    Uri uri = imageInfoIn.getImageUri();
//                        if(uri.toString().startsWith("file:/")) {
//                            uri =mDataApi.getImageUriFromCacheWithFileProvider(imageInfoIn.getDataFile().getName());
//                        }

                    intent.setData(uri);

                    if (mOnDetailedScreenLaunchedListener != null) {
                        mOnDetailedScreenLaunchedListener.onDetailScreenLaunched();
                    }


                    final ImageDetailFragment imageDetailFragment = ImageDetailFragment.newInstance(uri, null);
//                final ResizeFragment resizeFragment = ResizeFragment.newInstance(intent);

//                resizeFragment.setOnImagedSavedListener(MyImagesAdaptor.this);


                    Utils.addFragment((AppCompatActivity) mContext.getActivity(), imageDetailFragment, R.id.contentFrame, true);

                    //   Utils.getImageInfo(mContext.getContext(),Uri.parse("file:///data/user/0/apps.sai.com.imageresizer.demo.debug/files/nonShared/IMG-20180120-WA0005.jpg"),mDataApi);
                }
            });
        } else if (holder1 instanceof HeaderView) {
            HeaderView headerView = (HeaderView) holder1;
//           ViewGroup parentAdView =  headerView.rootView.findViewById(R.id.banner_container_top);
//            if(parentAdView.getChildCount()==0) {
            try {
                String nextid = bannerIds[(count++) % (bannerIds.length - 1)];
                Utils.showFacebookBanner(mContext.getContext(), headerView.rootView, R.id.banner_container_top, nextid);

            } catch (Exception e) {
                e.printStackTrace();
                Utils.showFacebookBanner(mContext.getContext(), headerView.rootView, R.id.banner_container_top, bannerIds[0]);

            }
//            }


        }


    }

    int count = 0;

    String bannerIds[] = new String[]{"179547122769778_189237791800711", "179547122769778_179622146095609", "179547122769778_189046365153187", "179547122769778_189237791800711"};


    @Override
    public int getItemViewType(int position) {
        if (mImageInfoList.get(position).isAd() == false) {
            return ITEM;
        }
        return HEADER;
    }

    private void reload() {
        if (myImagesPresenter != null && mContext != null) {
            try {
                this.mImageInfoList = myImagesPresenter.getImages(mContext.getContext());
                this.mSelectedImageInfoList = new ArrayList<>();
                notifyDataSetChanged();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public List<ImageInfo> getSelectedImageInfoList() {
        return mSelectedImageInfoList;
    }


    @Override
    public int getItemCount() {
        return mImageInfoList.size();
    }

    public static class MultipleImagesHolder extends RecyclerView.ViewHolder {

        View rootView;
        public ImageView imageView;
        public TextView resTextView;
        public TextView sizeTextView;
        public CheckBox checkBox;


        public MultipleImagesHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_multiple);
            resTextView = itemView.findViewById(R.id.text_name_resolution);
            sizeTextView = itemView.findViewById(R.id.text_name_size);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}
