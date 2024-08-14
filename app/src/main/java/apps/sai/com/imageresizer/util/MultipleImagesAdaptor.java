package apps.sai.com.imageresizer.util;

import android.net.Uri;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import apps.sai.com.imageresizer.resize.ResizeFragment;
import apps.sai.com.imageresizer.settings.SettingsManager;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sailesh on 14/01/18.
 */

public   class MultipleImagesAdaptor extends RecyclerView.Adapter<MultipleImagesAdaptor.MutilpleImagesHolder> {
    List<ImageInfo> mImageInfoList;
    BaseFragment mContext;
    RecyclerView mRecyclerView;
    android.os.Handler mHandler;
    DataApi mDataApi;
    private int appereance;
    private GridLayoutManager layoutManager;


    public MultipleImagesAdaptor(BaseFragment context, List<ImageInfo> imageInfoList,

                                 RecyclerView recyclerView,GridLayoutManager layoutManager,DataApi dataApi){
        mContext =context;
        mRecyclerView =recyclerView;
        mProcessedImageInfoList = new ArrayList<>();
        appereance = SettingsManager.getInstance().getGridAppearnece();
        this.layoutManager =layoutManager;
        mRecyclerView.setLayoutManager(layoutManager);

        this.mImageInfoList = imageInfoList;
        mHandler = new android.os.Handler(Looper.getMainLooper());
        mDataApi =dataApi;
        int spacingInPixels = context.getResources().getDimensionPixelSize(R.dimen.spacing);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));








    }
    private int repeatAfter;

    public boolean showAd( ) {


        if (appereance == 0) {//large
            repeatAfter =layoutManager.getSpanCount()*2;//after these rows show ad

        } else if (appereance == 2) { //small
            repeatAfter =layoutManager.getSpanCount()*5;

        } else if (appereance == 1) { //normal
            repeatAfter =layoutManager.getSpanCount()*3;

        }
       if(repeatAfter  * layoutManager.getSpanCount() <mImageInfoList.size()){
            //Utils.showFacebookBanner(mContext,);
           return true;

       }
       return false;
    }


    @Override
    public MutilpleImagesHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext.getContext()).inflate(R.layout.multiple_image_row,null);


        return new MutilpleImagesHolder(view);
    }
    List<ImageInfo> mProcessedImageInfoList;
    ResizeFragment.IMAGE_PROCESSING_TASKS mImage_processing_tasks;

    public void showProcessedInfoList(List<ImageInfo> processedImageInfoList, ResizeFragment.IMAGE_PROCESSING_TASKS image_processing_tasks){
        mProcessedImageInfoList = processedImageInfoList;
        mImage_processing_tasks =image_processing_tasks;
//        this.notifyDataSetChanged();

    }

    public void showProcessedInfo(ImageInfo imageInfo){
        mProcessedImageInfoList.add(imageInfo);
        this.notifyDataSetChanged();

    }

    public List<ImageInfo> getProcessedImageInfoList() {
        return mProcessedImageInfoList;
    }

    public List<ImageInfo> getCurrentImages(){
        if(mProcessedImageInfoList!=null && mProcessedImageInfoList.size()>0){
            return  mProcessedImageInfoList;
        }

        return  mImageInfoList;
    }


    @Override
    public void onBindViewHolder(MutilpleImagesHolder holder, int position) {


        RelativeLayout.LayoutParams reLayoutParams = new RelativeLayout.LayoutParams(holder.imageView.getLayoutParams().width, ViewGroup.LayoutParams.MATCH_PARENT);

        boolean hideCompressedInfo =false;
        int span = layoutManager.getSpanCount();
        if(appereance ==0){//large

            if(span==1 || span ==2) {

                reLayoutParams.height = (int) mContext.getResources().getDimension(R.dimen.large_app);
            }else{
                reLayoutParams.height = (int) mContext.getResources().getDimension(R.dimen.large_app);
                reLayoutParams.height =reLayoutParams.height-reLayoutParams.height/3;
                hideCompressedInfo =true;
            }
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.imageView.setLayoutParams(reLayoutParams);
        }else if(appereance ==2){ //small
            hideCompressedInfo =true;
            reLayoutParams.height =(int) mContext.getResources().getDimension(R.dimen.small_app);
            holder.imageView.setLayoutParams(reLayoutParams);
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        }else if(appereance ==1){ //normal
            if(span==1 || span ==2) {

                reLayoutParams.height = (int) mContext.getResources().getDimension(R.dimen.normal_app);
            }else{
                reLayoutParams.height = (int) mContext.getResources().getDimension(R.dimen.normal_app)/2;
                hideCompressedInfo =true;
            }
            holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.imageView.setLayoutParams(reLayoutParams);
        }

        final ImageInfo imageInfo = mImageInfoList.get(position);



        if(imageInfo!=null) {

             if(imageInfo.getAbsoluteThumbFilePath()!=null) {
                 File file = new File(imageInfo.getAbsoluteThumbFilePath());

                 holder.imageView.setImageURI(Uri.fromFile(file));
             }
            if(hideCompressedInfo ==false) {
                holder.resTextView.setVisibility(View.VISIBLE);
                holder.sizeTextView.setVisibility(View.VISIBLE);
                holder.resTextView.setText(imageInfo.getWidth() + " x " + imageInfo.getHeight() +String.format("(%s)", imageInfo.getFormatedFileSize()));
                holder.sizeTextView.setVisibility(View.GONE);
//    holder.sizeTextView.setText(String.format("(%s)", imageInfo.getFormatedFileSize()));
            }else{
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
//                    Intent intent = new Intent();


//                        imageInfoIn = Utils.getImageInfo(mContext.getContext(),uri,mDataApi);
//                        uri=imageInfoIn.getImageUri();
//                        if(uri.toString().startsWith("file:/")) {
//                            uri =mDataApi.getImageUriFromCacheWithFileProvider(imageInfoIn.getDataFile().getName());
//                        }


                    final ImageDetailFragment imageDetailFragment = ImageDetailFragment.newInstance(uriOrg, uriProcessed);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Utils.addFragment((AppCompatActivity) mContext.getActivity(), imageDetailFragment, R.id.contentFrame, true);

                        }
                    });

                }

            });
        }

        if (mProcessedImageInfoList != null) {
            ImageInfo ProcessedInfo = null;

            if (position < mProcessedImageInfoList.size()) {

                ProcessedInfo = mProcessedImageInfoList.get(position);
            }

            if (ProcessedInfo != null ) {
                holder.imageView.setTag(R.id.text_name_resolution, ProcessedInfo);
                holder.resTextViewCompressed.setVisibility(View.VISIBLE);
                holder.sizeTextViewCompressed.setVisibility(View.VISIBLE);

                holder.resTextViewCompressed.setText(ProcessedInfo.getWidth() + " x " + ProcessedInfo.getHeight()+String.format("(%s)", ProcessedInfo.getFormatedFileSize()));
                holder.sizeTextViewCompressed.setVisibility(View.GONE);
//  holder.sizeTextViewCompressed.setText(String.format("(%s)", ProcessedInfo.getFormatedFileSize()));
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





//            holder.resTextView


        // webviewOrg.setImageURI();


//            holder.textView.setText(mList.get(position));

    }

    public List<ImageInfo> getImageInfoList(){
        return  mImageInfoList;
    }

    public void setmProcessedImageInfoList(List<ImageInfo> mImageInfoList) {
        this.mProcessedImageInfoList = mImageInfoList;
        notifyDataSetChanged();
    }


    public void setLayoutManager(GridLayoutManager layoutManager){
        if(mRecyclerView!=null){
            this.layoutManager =layoutManager;
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }

    }

    @Override
    public int getItemCount() {
        return mImageInfoList.size();
    }

    public void setImageInfo(ImageInfo imageInfo) {
     int index =   mImageInfoList.indexOf(imageInfo);
     if(index!=-1){
//         ImageInfo oldImageInfo =mImageInfoList.get(index);
//         oldImageInfo = imageInfo;
         mImageInfoList.set(index,imageInfo);
         notifyItemChanged(index);
     }

    }

    public void remove(int i) {
        if(i>=0&&i<mImageInfoList.size()){
            ImageInfo imageInfo = mImageInfoList.get(i);
            if(imageInfo!=null && imageInfo.getWidth()<=1 && imageInfo.getHeight()<=1) {
                mImageInfoList.remove(imageInfo);
                notifyItemRemoved(i);
            }
        }
    }

    public void remove(ImageInfo imageInfo) {

        if(mImageInfoList!=null){
            int index =  mImageInfoList.indexOf(imageInfo);
            if(index!=-1){
                mImageInfoList.remove(index);
                notifyItemRemoved(index);
            }
        }
        if(mProcessedImageInfoList!=null) {
            int index =  mProcessedImageInfoList.indexOf(imageInfo);

            if(index!=-1){
                mProcessedImageInfoList.remove(index);
                notifyItemRemoved(index);
            }
        }
    }

    public void setAppereance(int val){
        appereance =val;
        if(mRecyclerView!=null){
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    public static class MutilpleImagesHolder extends RecyclerView.ViewHolder{

        View rootView;
      //  @BindView(R.id.image_multiple)
        public ImageView imageView;
//        @BindView(R.id.text_name_resolution)
        public TextView resTextView;
//        @BindView(R.id.text_name_size)
        public TextView sizeTextView;

//        @BindView(R.id.text_name_compressed_resolution)
        public TextView resTextViewCompressed;
//        @BindView(R.id.text_name_compressed_size)
        public TextView sizeTextViewCompressed;

//        @BindView(R.id.seperator)
        public TextView sepTextView;
        private int appereance;


        public MutilpleImagesHolder(View itemView) {
            super(itemView);
            rootView =itemView;
            imageView = itemView.findViewById(R.id.image_multiple);
            resTextView = itemView.findViewById(R.id.text_name_resolution);
            sizeTextView = itemView.findViewById(R.id.text_name_size);
            resTextViewCompressed = itemView.findViewById(R.id.text_name_compressed_resolution);
            sizeTextViewCompressed = itemView.findViewById(R.id.text_name_compressed_size);
            sepTextView = itemView.findViewById(R.id.seperator);
        }
    }
}
