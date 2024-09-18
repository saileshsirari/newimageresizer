package apps.sai.com.imageresizer;

import android.app.Application;


import apps.sai.com.imageresizer.settings.SettingsManager;

/**
 * Created by sailesh on 11/01/18.
 */

public class ImageResizeApplication extends Application {
    private boolean isUpgraded;
    private static ImageResizeApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance =this;
        //ca-app-pub-3940256099942544/6300978111

        String bannerId = null;

      /*      if(BuildConfig.BUILD_TYPE.equals("debug")){
                bannerId ="ca-app-pub-3940256099942544/6300978111";
        }else if(BuildConfig.BUILD_TYPE.equals("release")){
                bannerId ="ca-app-pub-5260132970861532~3575130198";
        }*/

//        MobileAds.initialize(this, bannerId);

    }
    public void setIsUpgraded(boolean isUpgraded) {
        this.isUpgraded = isUpgraded;
        SettingsManager.getInstance().setLegacyUpgraded(isUpgraded);

    }

    public boolean getIsUpgraded() {
        return isUpgraded /*|| BuildConfig.DEBUG*/;
    }

    public static synchronized ImageResizeApplication getInstance() {
        return instance;
    }

}
