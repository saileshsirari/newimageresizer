package apps.sai.com.imageresizer.util;

import apps.sai.com.imageresizer.data.BitmapResult;
import apps.sai.com.imageresizer.data.ImageInfo;

public interface OnImageProcessedListener {
    void onImageLoaded(BitmapResult bitmapResult, ImageInfo imageInfoOrg);
}
