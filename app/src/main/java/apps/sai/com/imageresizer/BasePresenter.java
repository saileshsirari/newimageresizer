package apps.sai.com.imageresizer;

import android.content.Intent;

/**
 * Created by sailesh on 29/09/17.
 */

public interface BasePresenter<T> {

    /**
     * Binds presenter with a view when resumed. The Presenter will perform initialization here.
     *
     * @param view the view associated with this presenter
     */
    void takeView(T view);

    void launchgalleryExternalApp(boolean singlePhoto); //selected image from gallery or camera

    void onGalleryImageSelected(Intent data);

    /**
     * Drops the reference to the view when destroyed
     */
    void dropView();
}
