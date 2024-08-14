package apps.sai.com.imageresizer.data;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by sailesh on 15/01/18.
 */

public class BitmapResult {

    private Bitmap bitmap;
    private Uri contentUri;
    private Uri absolutePathUri;
    private Throwable error;

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Uri getContentUri() {
        return contentUri;
    }

    public void setContentUri(Uri contentUri) {
        this.contentUri = contentUri;
    }

    public Uri getAbsolutePathUri() {
        return absolutePathUri;
    }

    public void setAbsolutePathUri(Uri absolutePathUri) {
        this.absolutePathUri = absolutePathUri;
    }
}
