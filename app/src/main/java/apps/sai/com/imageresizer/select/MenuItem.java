package apps.sai.com.imageresizer.select;

import apps.sai.com.imageresizer.R;

/**
 * Created by sailesh on 03/01/18.
 */

public class MenuItem {

    public static final int TAKE_PHOTO_ID = 1;
    public static final int RESIZED_PHOTO_ID = 3;
    public static final String TAKE_PHOTO_TEXT = "Take Photo";
    public static final int TAKE_PHOTO_RES_ID =R.drawable.ic_action_crop ;
    public static final int SELECT_PHOTO_RES_ID = R.drawable.ic_action_compress;
    public static final int SELECT_PHOTO_ID = 2;
    public static final int RATE_APP_ID = 5;
    public static final String SELECT_PHOTO_TEXT ="Select Photo" ;

    public static final int SETTINGS_ID = 7;
    public static final int MORE_APPS = 8;
    public static final int REMOVE_ADS = 9;

    public MenuItem(int id , String name , int imageResourcePath){
        this.id =id;
        this.imageResourcePath = imageResourcePath;
        this.name =name;
    }

    private final String name;

    private final int id ;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public int getImageResourcePath() {
        return imageResourcePath;
    }



    private final int imageResourcePath;

    public static MenuItem newInstance(int takePhotoId, String takePhotoText, int takePhotoResId) {

        return  new MenuItem(takePhotoId,takePhotoText,takePhotoResId);
    }
}
