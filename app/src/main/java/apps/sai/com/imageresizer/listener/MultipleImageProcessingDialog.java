package apps.sai.com.imageresizer.listener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.data.ImageInfo;
import apps.sai.com.imageresizer.resize.ImageProcessingTasks;

/**
 * Created by sailesh on 17/01/18.
 */
public class MultipleImageProcessingDialog implements OnMultipleImageProcessingListener {
    ImageProcessingTasks mImageProcessingTasks;
//    ResizeFragment resizeFragment;
    Context mContext;
//    MultipleImagesAdaptor mMultipleImagesAdaptor;
    Handler mHandler;
    OnProcessingCancelListener mOnProceesingCancelListener;

    public static interface OnDialogDismmistedListener{
        void ondialogDismissed();
    }
    OnDialogDismmistedListener mOnDialogDismmistedListener;

    public void setmOnDialogDismmistedListener(OnDialogDismmistedListener mOnDialogDismmistedListener) {
        this.mOnDialogDismmistedListener = mOnDialogDismmistedListener;
    }

    public MultipleImageProcessingDialog(OnProcessingCancelListener onProceesingCancelListener, Context context, ImageProcessingTasks imageProcessingTasks) {
        mImageProcessingTasks = imageProcessingTasks;
//        this.resizeFragment=resizeFragment;
        mContext =context;
        mHandler = new Handler(Looper.getMainLooper());
        mOnProceesingCancelListener=onProceesingCancelListener;
    }
    int currentIndex;

    @Override
    public void onProcessingDone(List<ImageInfo> imageInfoList) {
//            mImageInfoListCached =imageInfoList;

//        mMultipleImagesAdaptor.showProcessedInfoList(imageInfoList, mImageProcessingTasks);

        if(alertDialog!=null && alertDialog.isShowing()){
            alertDialog.dismiss();

            if(mOnDialogDismmistedListener!=null){
                mOnDialogDismmistedListener.ondialogDismissed();
            }

        }

    }
     int current;
    private AlertDialog showProgressAlert(Context context){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
//        dialogBuilder.setMessage("");

        current =1;

        final AlertDialog alertDialog = dialogBuilder.create();

//        alertDialog.setCancelable(false);
        View view = LayoutInflater.from(context).inflate(R.layout.progress_multi_processing,null);
          progressBar = view.findViewById(R.id.progressMulti);

        textViewProgress = view.findViewById(R.id.textProgress);

        textProgress = view.findViewById(R.id.percenTextview);

        view.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                if(mOnDialogDismmistedListener!=null){
                    mOnDialogDismmistedListener.ondialogDismissed();
                }
                running=false;
                if(mOnProceesingCancelListener!=null) {
                    mOnProceesingCancelListener.onProcessingCanceled(currentImageInfo,currentIndex);
                }
            }
        });

      /*  view.findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                running=false;
            }
        });*/
//    listView.setAdapter();




        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                running =false;
            }
        });

        alertDialog.show();
        alertDialog.setContentView(view);


        return alertDialog;
    }
AlertDialog alertDialog;
    ProgressBar progressBar;
    TextView textViewProgress,textProgress;
    volatile boolean running ;

    private void incrementProgress(int current , int total){


      /*  mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(running==false){
                    return;
                }
             if(progressBar!=null){
                int p= progressBar.getProgress();
                progressBar.setProgress(p+1);
                 incrementProgress();
             }
            }
        },incrementms);*/
     if(progressBar!=null) {
         if (current < total) {

             float ratio = progressBar.getMax() / (float) total;
             int currentProgress = (int) ratio * current;
             int p = progressBar.getProgress();
             if (p < currentProgress) {
                 progressBar.setProgress(currentProgress);
             }
         } else {
             progressBar.setVisibility(View.GONE);
         }

         if(textProgress!=null ){
             float ratio =0;
             if(progressBar.getProgress()!=0) {

                 ratio =   progressBar.getProgress()/(float)progressBar.getMax();
             }
             int p = (int) (ratio *100);

             textProgress.setText(String.format("%d",p)+" % ");
         }
     }

    }

    volatile int incrementms= 100;
    long timeStart;
    @Override
    public void onProcessingStarted(ImageInfo imageInfo, int position, int total) {
         position =current;
        if (alertDialog ==null) {
            alertDialog = showProgressAlert(mContext);
        }
       /* if(imageInfo!=null){
            imageInfo = Utils.getImageInfo()
        }*/

      /*  if (position == 1) {
            alertDialog = showProgressAlert(mContext);
            if(progressBar!=null){
                running =true;
                progressBar.setProgress(0);
                timeStart =System.currentTimeMillis();
                incrementProgress(position,total);

            }

        }*/

        incrementProgress(position,total);
        showProgress(position, total);


    }

    private void showProgress(int position, int total) {
        if (mImageProcessingTasks == ImageProcessingTasks.COMPRESS) {
            if(alertDialog!=null && mContext!=null){
//                alertDialog.setMessage(String.format(mContext.getString(R.string.processing),position,total));
                textViewProgress.setText(String.format(mContext.getString(R.string.processing),position,total));
            }

//            Toast.makeText(mContext, "Compressing " + position + " out of " + total + " images", Toast.LENGTH_LONG).show();
        } else if (mImageProcessingTasks == ImageProcessingTasks.SCALE) {

//            Toast.makeText(mContext, "Scaling " + position + " out of " + total + " images", Toast.LENGTH_LONG).show();
            if(alertDialog!=null && mContext!=null){
//                alertDialog.setMessage(String.format(mContext.getString(R.string.processing),position,total));
                textViewProgress.setText(String.format(mContext.getString(R.string.processing),position,total));

            }
        }else{
            if(alertDialog!=null && mContext!=null){
//                alertDialog.setMessage(String.format(mContext.getString(R.string.processing),position,total));
                textViewProgress.setText(String.format(mContext.getString(R.string.processing),position,total));

            }
        }
    }

    ImageInfo currentImageInfo;
    @Override
    public void onProcessingFinished(ImageInfo imageInfo, int position, int total) {
        /*if(position>1) {
            onProcessingStarted(imageInfo, position, total);
        }*/
        current =current+1;
        position =current;

        currentIndex = position;
        currentImageInfo =imageInfo;
      /*  if(position==1){
            long diff =System.currentTimeMillis()-timeStart;
//            diff =diff*1000;//seconds
//            if(diff>0){

            if(total!=0) {

                int divisions = progressBar.getMax() / total;//10
                if (diff > 0 && divisions > 0) {
                    incrementms = (int) (diff / divisions);
                    incrementms = incrementms == 0 ? 100 : incrementms;
                }
                progressBar.setProgress(divisions);
            }

//            }

        }*/
        showProgress(position, total);
//            if(mImageProcessingTasks==ImageProcessingTasks.COMPRESS) {
        if(progressBar!=null &&mContext!=null && alertDialog!=null && alertDialog.isShowing()) {

           /* if(total!=0) {
                int divisions = progressBar.getMax() / total;//10
                int currentProgress = divisions * position;

                progressBar.setProgress(currentProgress);
            }*/
        }
        if(alertDialog!=null && mContext!=null &&   position==total || total ==0) {

            running =false;
            alertDialog.dismiss();
            if(mOnDialogDismmistedListener!=null){
                mOnDialogDismmistedListener.ondialogDismissed();
            }


        }

        incrementProgress(position,total);

//        mMultipleImagesAdaptor.showProcessedInfo(imageInfo);
//            }
    }
}
