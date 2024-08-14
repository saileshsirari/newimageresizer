package apps.sai.com.imageresizer.listener;

import java.util.List;

import apps.sai.com.imageresizer.data.ImageInfo;

/**
 * Created by sailesh on 24/01/18.
 */

public interface OnMultipleImageProcessingListener {
    void onProcessingDone(List<ImageInfo> imageInfoList);
    void onProcessingStarted(ImageInfo imageInfo , int position, int total);
    void onProcessingFinished(ImageInfo imageInfo , int position, int total);

}
