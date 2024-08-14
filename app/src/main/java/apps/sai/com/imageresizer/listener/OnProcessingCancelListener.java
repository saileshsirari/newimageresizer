package apps.sai.com.imageresizer.listener;

import apps.sai.com.imageresizer.data.ImageInfo;

/**
 * Created by sailesh on 24/01/18.
 */

public interface OnProcessingCancelListener {
    void onProcessingCanceled(ImageInfo imageInfo,int pos);

}
