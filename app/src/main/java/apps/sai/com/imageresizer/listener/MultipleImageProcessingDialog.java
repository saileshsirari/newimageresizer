package apps.sai.com.imageresizer.listener;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.data.ImageInfo;
import apps.sai.com.imageresizer.resize.ImageProcessingTask;

/**
 * Created by sailesh on 17/01/18.
 */
public class MultipleImageProcessingDialog implements OnMultipleImageProcessingListener {
    ImageProcessingTask mImageProcessingTask;
    Context mContext;
    Handler mHandler;
    OnProcessingCancelListener mOnProceesingCancelListener;

    public interface OnDialogDismastedListener {
        void dialogDismissed();
    }

    OnDialogDismastedListener mOnDialogDismastedListener;

    public void setOnDialogDismastedListener(OnDialogDismastedListener mOnDialogDismastedListener) {
        this.mOnDialogDismastedListener = mOnDialogDismastedListener;
    }

    public MultipleImageProcessingDialog(OnProcessingCancelListener onProcessingCancelListener, Context context, ImageProcessingTask imageProcessingTask) {
        mImageProcessingTask = imageProcessingTask;
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mOnProceesingCancelListener = onProcessingCancelListener;
    }

    int currentIndex;

    @Override
    public void onProcessingDone(List<ImageInfo> imageInfoList) {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            if (mOnDialogDismastedListener != null) {
                mOnDialogDismastedListener.dialogDismissed();
            }
        }
    }

    int current;

    private AlertDialog showProgressAlert(Context context) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        current = 1;
        final AlertDialog alertDialog = dialogBuilder.create();
        View view = LayoutInflater.from(context).inflate(R.layout.progress_multi_processing, null);
        progressBar = view.findViewById(R.id.progressMulti);
        textViewProgress = view.findViewById(R.id.textProgress);
        textProgress = view.findViewById(R.id.percenTextview);
        view.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                if (mOnDialogDismastedListener != null) {
                    mOnDialogDismastedListener.dialogDismissed();
                }
                running = false;
                if (mOnProceesingCancelListener != null) {
                    mOnProceesingCancelListener.onProcessingCanceled(currentImageInfo, currentIndex);
                }
            }
        });
        alertDialog.setOnDismissListener(dialog -> running = false);
        alertDialog.show();
        alertDialog.setContentView(view);
        return alertDialog;
    }

    AlertDialog alertDialog;
    ProgressBar progressBar;
    TextView textViewProgress, textProgress;
    volatile boolean running;

    private void incrementProgress(int current, int total) {
        if (progressBar != null) {
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
            if (textProgress != null) {
                float ratio = 0;
                if (progressBar.getProgress() != 0) {
                    ratio = progressBar.getProgress() / (float) progressBar.getMax();
                }
                int p = (int) (ratio * 100);
                textProgress.setText(String.format("%d", p) + " % ");
            }
        }

    }
    @Override
    public void onProcessingStarted(ImageInfo imageInfo, int position, int total) {
        position = current;
        if (alertDialog == null) {
            alertDialog = showProgressAlert(mContext);
        }
        incrementProgress(position, total);
        showProgress(position, total);
    }

    private void showProgress(int position, int total) {
        if (mImageProcessingTask == ImageProcessingTask.COMPRESS) {
            if (alertDialog != null && mContext != null) {
                textViewProgress.setText(String.format(mContext.getString(R.string.processing), position, total));
            }
        } else if (mImageProcessingTask == ImageProcessingTask.SCALE) {
            if (alertDialog != null && mContext != null) {
                textViewProgress.setText(String.format(mContext.getString(R.string.processing), position, total));
            }
        } else {
            if (alertDialog != null && mContext != null) {
                textViewProgress.setText(String.format(mContext.getString(R.string.processing), position, total));
            }
        }
    }

    ImageInfo currentImageInfo;

    @Override
    public void onProcessingFinished(ImageInfo imageInfo, int position, int total) {
        current = current + 1;
        position = current;
        currentIndex = position;
        currentImageInfo = imageInfo;
        showProgress(position, total);
        if (progressBar != null && mContext != null && alertDialog != null) {
            alertDialog.isShowing();
        }
        if (alertDialog != null && mContext != null && (position == total || total == 0)) {
            running = false;
            alertDialog.dismiss();
            if (mOnDialogDismastedListener != null) {
                mOnDialogDismastedListener.dialogDismissed();
            }
        }
        incrementProgress(position, total);
    }
}
