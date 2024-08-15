package apps.sai.com.imageresizer.util;

import android.net.Uri;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import apps.sai.com.imageresizer.BaseFragment;
import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.data.DataApi;
import apps.sai.com.imageresizer.data.ImageInfo;
import apps.sai.com.imageresizer.resize.ImageProcessingTasks;
import apps.sai.com.imageresizer.settings.SettingsManager;

/**
 * Created by sailesh on 14/01/18.
 */

public class MultipleImagesAdaptor extends RecyclerView.Adapter<MultipleImagesAdaptor.MultipleImagesHolder> {
    List<ImageInfo> mImageInfoList;
    BaseFragment mContext;
    RecyclerView mRecyclerView;
    android.os.Handler mHandler;
    DataApi mDataApi;
    private int appearance;
    private GridLayoutManager layoutManager;


    public MultipleImagesAdaptor(BaseFragment context, List<ImageInfo> imageInfoList,

                                 RecyclerView recyclerView, GridLayoutManager layoutManager, DataApi dataApi) {
        mContext = context;
        mRecyclerView = recyclerView;
        mProcessedImageInfoList = new ArrayList<>();
        appearance = SettingsManager.getInstance().getGridAppearnece();
        this.layoutManager = layoutManager;
        mRecyclerView.setLayoutManager(layoutManager);
        this.mImageInfoList = imageInfoList;
        mHandler = new android.os.Handler(Looper.getMainLooper());
        mDataApi = dataApi;
        int spacingInPixels = context.getResources().getDimensionPixelSize(R.dimen.spacing);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
    }
    @NonNull
    @Override
    public MultipleImagesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext.getContext()).inflate(R.layout.multiple_image_row, null);
        return new MultipleImagesHolder(view);
    }

    List<ImageInfo> mProcessedImageInfoList;
    ImageProcessingTasks mImageProcessingTasks;

    public void showProcessedInfoList(List<ImageInfo> processedImageInfoList, ImageProcessingTasks imageProcessingTasks) {
        mProcessedImageInfoList = processedImageInfoList;
        mImageProcessingTasks = imageProcessingTasks;
    }

    public void showProcessedInfo(ImageInfo imageInfo) {
        mProcessedImageInfoList.add(imageInfo);
        this.notifyDataSetChanged();
    }

    public List<ImageInfo> getProcessedImageInfoList() {
        return mProcessedImageInfoList;
    }

    public List<ImageInfo> getCurrentImages() {
        if (mProcessedImageInfoList != null && !mProcessedImageInfoList.isEmpty()) {
            return mProcessedImageInfoList;
        }
        return mImageInfoList;
    }

    @Override
    public void onBindViewHolder(MultipleImagesHolder holder, int position) {

        RelativeLayout.LayoutParams reLayoutParams = new RelativeLayout.LayoutParams(holder.imageView.getLayoutParams().width, ViewGroup.LayoutParams.MATCH_PARENT);
        boolean hideCompressedInfo = false;
        int span = layoutManager.getSpanCount();
        if (appearance == 0) {//large

            if (span == 1 || span == 2) {

                reLayoutParams.height = (int) mContext.getResources().getDimension(R.dimen.large_app);
            } else {
                reLayoutParams.height = (int) mContext.getResources().getDimension(R.dimen.large_app);
                reLayoutParams.height = reLayoutParams.height - reLayoutParams.height / 3;
                hideCompressedInfo = true;
            }
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.imageView.setLayoutParams(reLayoutParams);
        } else if (appearance == 2) { //small
            hideCompressedInfo = true;
            reLayoutParams.height = (int) mContext.getResources().getDimension(R.dimen.small_app);
            holder.imageView.setLayoutParams(reLayoutParams);
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        } else if (appearance == 1) { //normal
            if (span == 1 || span == 2) {

                reLayoutParams.height = (int) mContext.getResources().getDimension(R.dimen.normal_app);
            } else {
                reLayoutParams.height = (int) mContext.getResources().getDimension(R.dimen.normal_app) / 2;
                hideCompressedInfo = true;
            }
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.imageView.setLayoutParams(reLayoutParams);
        }

        final ImageInfo imageInfo = mImageInfoList.get(position);


        if (imageInfo != null) {

            if (imageInfo.getAbsoluteThumbFilePath() != null) {
                File file = new File(imageInfo.getAbsoluteThumbFilePath());

                holder.imageView.setImageURI(Uri.fromFile(file));
            }
            if (!hideCompressedInfo) {
                holder.resTextView.setVisibility(View.VISIBLE);
                holder.sizeTextView.setVisibility(View.VISIBLE);
                holder.resTextView.setText(imageInfo.getWidth() + " x " + imageInfo.getHeight() + String.format("(%s)", imageInfo.getFormatedFileSize()));
                holder.sizeTextView.setVisibility(View.GONE);
//    holder.sizeTextView.setText(String.format("(%s)", imageInfo.getFormatedFileSize()));
            } else {
                holder.resTextView.setVisibility(View.GONE);
                holder.sizeTextView.setVisibility(View.GONE);
            }

            if (mImageInfoList != null && position < mImageInfoList.size()) {
                holder.imageView.setTag(R.id.image_multiple, mImageInfoList.get(position));

            }


            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object org = v.getTag(R.id.image_multiple);
                    Object processed = v.getTag(R.id.text_name_resolution);

                    Uri uriOrg = null, uriProcessed = null;
                    ImageInfo imageInfoOrg = null, imageInfoProcessed = null;
                    if (org instanceof ImageInfo) {
                        imageInfoOrg = (ImageInfo) org;
                        uriOrg = imageInfoOrg.getImageUri();
                    }
                    if (processed instanceof ImageInfo) {

                        imageInfoProcessed = (ImageInfo) processed;
                        uriProcessed = imageInfoProcessed.getImageUri();

                    }
                    final ImageDetailFragment imageDetailFragment = ImageDetailFragment.newInstance(uriOrg, uriProcessed);
                    mHandler.post(() -> Utils.addFragment((AppCompatActivity) mContext.getActivity(), imageDetailFragment, R.id.contentFrame, true));
                }
            });
        }

        if (mProcessedImageInfoList != null) {
            ImageInfo ProcessedInfo = null;

            if (position < mProcessedImageInfoList.size()) {

                ProcessedInfo = mProcessedImageInfoList.get(position);
            }

            if (ProcessedInfo != null) {
                holder.imageView.setTag(R.id.text_name_resolution, ProcessedInfo);
                holder.resTextViewCompressed.setVisibility(View.VISIBLE);
                holder.sizeTextViewCompressed.setVisibility(View.VISIBLE);

                holder.resTextViewCompressed.setText(ProcessedInfo.getWidth() + " x " + ProcessedInfo.getHeight() + String.format("(%s)", ProcessedInfo.getFormatedFileSize()));
                holder.sizeTextViewCompressed.setVisibility(View.GONE);
                holder.sepTextView.setVisibility(View.VISIBLE);
                if (ProcessedInfo.getAbsoluteThumbFilePath() != null) {
                    holder.imageView.setImageURI(Uri.parse(ProcessedInfo.getAbsoluteThumbFilePath()));
                }
            } else {
                holder.resTextViewCompressed.setVisibility(View.GONE);
                holder.sizeTextViewCompressed.setVisibility(View.GONE);
                holder.sepTextView.setVisibility(View.GONE);
                holder.imageView.setTag(R.id.text_name_resolution, null);
            }
        }
    }

    public List<ImageInfo> getImageInfoList() {
        return mImageInfoList;
    }

    public void setmProcessedImageInfoList(List<ImageInfo> mImageInfoList) {
        this.mProcessedImageInfoList = mImageInfoList;
        notifyDataSetChanged();
    }


    public void setLayoutManager(GridLayoutManager layoutManager) {
        if (mRecyclerView != null) {
            this.layoutManager = layoutManager;
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }

    }

    @Override
    public int getItemCount() {
        return mImageInfoList.size();
    }

    public void setImageInfo(ImageInfo imageInfo) {
        int index = mImageInfoList.indexOf(imageInfo);
        if (index != -1) {
            mImageInfoList.set(index, imageInfo);
            notifyItemChanged(index);
        }
    }

    public void remove(int i) {
        if (i >= 0 && i < mImageInfoList.size()) {
            ImageInfo imageInfo = mImageInfoList.get(i);
            if (imageInfo != null && imageInfo.getWidth() <= 1 && imageInfo.getHeight() <= 1) {
                mImageInfoList.remove(imageInfo);
                notifyItemRemoved(i);
            }
        }
    }

    public void remove(ImageInfo imageInfo) {

        if (mImageInfoList != null) {
            int index = mImageInfoList.indexOf(imageInfo);
            if (index != -1) {
                mImageInfoList.remove(index);
                notifyItemRemoved(index);
            }
        }
        if (mProcessedImageInfoList != null) {
            int index = mProcessedImageInfoList.indexOf(imageInfo);

            if (index != -1) {
                mProcessedImageInfoList.remove(index);
                notifyItemRemoved(index);
            }
        }
    }

    public void setAppearance(int val) {
        appearance = val;
        if (mRecyclerView != null) {
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

     static class MultipleImagesHolder extends RecyclerView.ViewHolder {

        View rootView;
        public ImageView imageView;
        public TextView resTextView;
        public TextView sizeTextView;
        public TextView resTextViewCompressed;
        public TextView sizeTextViewCompressed;
        public TextView sepTextView;
        public MultipleImagesHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            imageView = itemView.findViewById(R.id.image_multiple);
            resTextView = itemView.findViewById(R.id.text_name_resolution);
            sizeTextView = itemView.findViewById(R.id.text_name_size);
            resTextViewCompressed = itemView.findViewById(R.id.text_name_compressed_resolution);
            sizeTextViewCompressed = itemView.findViewById(R.id.text_name_compressed_size);
            sepTextView = itemView.findViewById(R.id.seperator);
        }
    }
}
