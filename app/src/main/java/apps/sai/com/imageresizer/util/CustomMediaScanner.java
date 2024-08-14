package apps.sai.com.imageresizer.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.Collections;
import java.util.List;

import apps.sai.com.imageresizer.ImageResizeApplication;
import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.rx.UnsafeConsumer;
import apps.sai.com.imageresizer.select.FileHelper;
import apps.sai.com.imageresizer.select.FolderObject;
import io.reactivex.android.schedulers.AndroidSchedulers;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class CustomMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {

    private static final String TAG = "CustomMediaScanner";

    public interface ScanCompletionListener {
        void onPathScanned(String path);

        void onScanCompleted();
    }

    private final List<String> paths;

    @Nullable
    private final ScanCompletionListener scanCompletionListener;

    private MediaScannerConnection connection;
    private int nextPath;

    private Handler handler;

    private CustomMediaScanner(List<String> paths, @Nullable ScanCompletionListener listener) {
        this.paths = paths;
        scanCompletionListener = listener;
        handler = new Handler(ImageResizeApplication.getInstance().getMainLooper());
    }

    public static void scanFiles(List<String> paths, @Nullable ScanCompletionListener listener) {
        CustomMediaScanner client = new CustomMediaScanner(paths, listener);
        MediaScannerConnection connection = new MediaScannerConnection(ImageResizeApplication.getInstance(), client);
        client.connection = connection;
        connection.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        scanNextPath();
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {

        Log.d(TAG, "Scan complete. Path: " + path);

        scanNextPath();
    }

    private void scanNextPath() {
        if (nextPath >= paths.size()) {
            scanComplete();
            return;
        }
        String path = paths.get(nextPath);

        connection.scanFile(path, null);
        nextPath++;

        if (scanCompletionListener != null) {
            if (handler != null) {
                handler.post(() -> scanCompletionListener.onPathScanned(path));
            }
        }

        Log.d(TAG, "Scanning file: " + path);
    }

    private void scanComplete() {
        connection.disconnect();

        //Notify all media uris of change. This will in turn update any content observers.
        ImageResizeApplication.getInstance().getContentResolver().notifyChange(Uri.parse("content://media"), null);

        if (handler != null) {
            handler.post(() -> {
                if (scanCompletionListener != null) {
                    scanCompletionListener.onScanCompleted();
                }
                cleanup();
            });
        }
    }

    private void cleanup() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    public static void scanFile(Context context, FolderObject folderObject) {

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null);
        TextView pathsTextView = view.findViewById(R.id.paths);
        pathsTextView.setText(folderObject.path);

        MaterialProgressBar indeterminateProgress = view.findViewById(R.id.indeterminateProgress);
        MaterialProgressBar horizontalProgress = view.findViewById(R.id.horizontalProgress);

        MaterialDialog dialog = DialogUtils.getBuilder(context)
                .title(R.string.scanning)
                .customView(view, false)
                .negativeText(R.string.close)
                .show();

        FileHelper.getPathList(new File(folderObject.path), true, false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(paths -> {
//                    ViewUtils.fadeOut(indeterminateProgress, null);
//                    ViewUtils.fadeIn(horizontalProgress, null);
                    horizontalProgress.setMax(paths.size());

                    CustomMediaScanner.scanFiles(paths, new ScanCompletionListener() {
                        @Override
                        public void onPathScanned(String path) {
                            horizontalProgress.setProgress(horizontalProgress.getProgress() + 1);
                            pathsTextView.setText(path);
                        }

                        @Override
                        public void onScanCompleted() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                        }
                    });
                });
    }

    public static void scanFile(String path, UnsafeConsumer<String> message) {
        CustomMediaScanner.scanFiles(Collections.singletonList(path), new CustomMediaScanner.ScanCompletionListener() {
            @Override
            public void onPathScanned(String path) {

            }

            @Override
            public void onScanCompleted() {
                message.accept(ImageResizeApplication.getInstance().getString(R.string.scan_complete));
            }
        });
    }
}