package apps.sai.com.imageresizer;

import android.content.Intent;

/**
 * Created by sailesh on 29/09/17.
 */

public interface BaseView <T>{

    void onGalleryImageSelected(Intent data);
    void launchGalleryExternalApp(boolean singlePhoto);

//    void onImageSelected(Intent data); //selected image from gallery or camera


}
