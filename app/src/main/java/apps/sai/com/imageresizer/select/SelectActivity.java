package apps.sai.com.imageresizer.select;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.android.billingclient.api.Purchase;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;

import java.io.File;
import java.util.List;

import apps.sai.com.imageresizer.BaseFragment;
import apps.sai.com.imageresizer.ImageResizeApplication;
import apps.sai.com.imageresizer.MyUncaughtExceptionHandler;
import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.billing.BillingManager;
import apps.sai.com.imageresizer.resize.ResizeFragment;
import apps.sai.com.imageresizer.settings.SettingsManager;
import apps.sai.com.imageresizer.util.Config;
import apps.sai.com.imageresizer.util.UiState;
import apps.sai.com.imageresizer.util.UpgradeDialog;
import apps.sai.com.imageresizer.util.Utils;

/**
 * Created by sailesh on 03/01/18.
 */

public class SelectActivity extends AppCompatActivity implements
        FolderChooserDialog.FolderCallback {

    private static final String TAG = SelectActivity.class.getSimpleName();
    public static Bitmap mImage;
    SelectFragment mSelectFragment;

    /** StartAppAd object declaration */
//    private StartAppAd startAppAd = new StartAppAd(this);

    /**
     * StartApp Native Ad declaration
     */
//    private StartAppNativeAd startAppNativeAd = new StartAppNativeAd(this);
//    private NativeAdDetails nativeAd = null;
//    private Banner mBanner;
//
//    private AdView mAdView;

    /**
     * Facebook ads
     */
    private AdView adView;

    private MyUncaughtExceptionHandler mMyUncaughtExceptionHandler;
    private BaseFragment mBaseFragment;


    private BillingManager billingManager;
    private boolean doNotshowAd;

    @Nullable
    public BillingManager getBillingManager() {
        return billingManager;
    }

    public void setCurrentFragment(BaseFragment baseFragment) {
        this.mBaseFragment = baseFragment;
    }


    public void showError(Throwable ex) {


        mBaseFragment.showError(ex);


    }

    public void showError(final int errorId) {

        String s = getString(errorId);
        if (s != null && s.length() > 0) {
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

        }

    }

    public void showFacebookBanner(final AppCompatActivity context, int resid, String id) {
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        AppEventsLogger.activateApp(this);


        // Instantiate an AdView view
        AdView adView = new AdView(context, id, AdSize.BANNER_HEIGHT_50);

        // Find the Ad container
        LinearLayout adContainer = context.findViewById(resid);

        // Add the ad view to container
        adContainer.addView(adView);

        AdListener adListener = new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {

            }

            @Override
            public void onAdLoaded(Ad ad) {
// Ad loaded callback
            }

            @Override
            public void onAdClicked(Ad ad) {
// Ad clicked callback
            }

            @Override
            public void onLoggingImpression(Ad ad) {
// Ad impression logged callback
            }
        };
// Request an ad
        adView.loadAd(adView.buildLoadAdConfig().withAdListener(adListener).build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    boolean showStartAppAd = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (showStartAppAd && Utils.isUpgradedMy() == false) {
//            StartAppSDK.init(this, "201508105", false);
//            StartAppAd.disableSplash();
        }
        doNotshowAd = true;

       /* if (BuildConfig.BUILD_TYPE.equals("release")) {
            setContentView(R.layout.activity_sharing_release);
        } else {
            setContentView(R.layout.activity_sharing);
        }*/

        setContentView(R.layout.activity_sharing);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        BaseFragment.openCvLoaded = false;
        openCvLoaded = false;

        billingManager = new BillingManager(this, new BillingManager.BillingUpdatesListener() {
            @Override
            public void onPurchasesUpdated(List<Purchase> purchases) {


                for (Purchase purchase : purchases) {
                    if (purchase.getSkus().get(0).equals(Config.SKU_PREMIUM)) {
                        ImageResizeApplication.getInstance().setIsUpgraded(true);
                    }
                }
            }

            @Override
            public void onPremiumPurchaseCompleted() {
                ImageResizeApplication.getInstance().setIsUpgraded(true);
                UpgradeDialog.getUpgradeSuccessDialog(SelectActivity.this).show();
            }

            @Override
            public void onPremiumPurchaseRestored() {
                ImageResizeApplication.getInstance().setIsUpgraded(true);
                Toast.makeText(SelectActivity.this, R.string.iab_purchase_restored, Toast.LENGTH_SHORT).show();
            }

        });

//        showFacebookBanner(this,R.id.banner_container,"179547122769778_179622146095609");
        if (Utils.isUpgradedMy() == false) {
            showFacebookBanner(this, R.id.banner_container_top, "179547122769778_189046365153187");

        }



       /* if(Utils.isUpgradedMy() ==false && SettingsManager.getInstance().getLaunchCount()>0) {


            loadInterstitial(this);
        }*/
        SettingsManager.getInstance().incrementLaunchCount();

//        mBanner = findViewById(R.id.startAppBanner);
//        mAdView =findViewById(R.id.adView);
       /* if(BuildConfig.BUILD_TYPE.equals("release")){
            mAdView.setAdUnitId("ca-app-pub-5260132970861532/5707179950");
        }else{
            mAdView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        }*/

        // Instantiate an AdView view
//        mAdView = new AdView(this, "YOUR_PLACEMENT_ID", AdSize.BANNER_HEIGHT_50);

        // Find the Ad Container
//        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);

        // Add the ad view to your activity layout
//        adContainer.addView(mAdView);


//        StartAppAd.disableSplash();


        /**
         * Load Native Ad with the following parameters:
         * 1. Only 1 Ad
         * 2. Download ad image automatically
         * 3. Image size of 150x150px
         */
     /*   try {


        startAppNativeAd.loadAd(
                new NativeAdPreferences()
                        .setAdsNumber(1)
                        .setAutoBitmapDownload(true)

                        .setPrimaryImageSize(2),
                nativeAdListener);

    }catch (Exception e){
            Fabric.getLogger().e("SelectActivity",e.toString());
        }*/


        launchSharingFragment();


//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().show();
    }


    @Override
    protected void onRestart() {
        super.onRestart();


    }


    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
        if (folder != null) {
            if (folder.canWrite() == false) {
                showError(R.string.unable_to_select_folder);
                return;
            }
            SettingsManager.getInstance().setFolderPath(folder.getAbsolutePath());


        }
    }

    @Override
    public void onFolderChooserDismissed(@NonNull FolderChooserDialog dialog) {
        if (dialog != null) {

        }
    }
//    private StartAppAd startAppAd ;

    private InterstitialAd interstitialAd;
    private boolean adShown;

    public void loadInterstitial(final Context context) {
        if(true){
            return;
        }

        interstitialAd = new InterstitialAd(context, "179547122769778_180069089384248");
        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial ad displayed callback
                Log.e(TAG, "Interstitial ad displayed.");
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
                Log.e(TAG, "Interstitial ad dismissed.");
                if (lastScreen) {
                    onBackPressed();
                }
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                Log.e(TAG, "Interstitial ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
                // Show the ad
                try {
                    if (ad instanceof InterstitialAd) {
                        interstitialAd.show();
                        adShown = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                Log.d(TAG, "Interstitial ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
                Log.d(TAG, "Interstitial ad impression logged!");
            }
        };
        interstitialAd.loadAd(
                interstitialAd.buildLoadAdConfig()
                        .withAdListener(interstitialAdListener)
                        .build());

    }

    boolean lastScreen = false;

    @Override
    public void onBackPressed() {
//        if (showExitAd()) return;

        super.onBackPressed();
    }

    private boolean showExitAd() {
        try {


            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                if (lastScreen && Utils.isUpgradedMy() == false) {
                    super.onBackPressed();
                    return true;
                }
                //                if(interstitialAd.isAdLoaded()==false) {

                //                }


                //                startAppAd.onBackPressed();

                if (Utils.isUpgradedMy() == false) {

                    loadInterstitial(this);

                    lastScreen = true;

                    return true;
                }
            }
        } catch (Exception e) {

        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*if(startAppAd!=null){
            startAppAd.onPause();
        }*/

    }

    public void showFullScreenAd() {

        if (true) {


            return;

        }

        try {

            loadInterstitial(this);
        } catch (Exception e) {
            //ignore
        }

//        startAppAd.showAd();
    }


    /**
     * Native Ad Callback
     *//*
    private AdEventListener nativeAdListener = new AdEventListener() {

        @Override
        public void onReceiveAd(Ad ad) {

            try {

                // Get the native ad
                ArrayList<NativeAdDetails> nativeAdsList = startAppNativeAd.getNativeAds();
                if (nativeAdsList.size() > 0) {
                    nativeAd = nativeAdsList.get(0);
                }

                // Verify that an ad was retrieved
                if (nativeAd != null) {

                    // When ad is received and displayed - we MUST send impression
                    nativeAd.sendImpression(SelectActivity.this);



               *//* if (imgFreeApp != null && txtFreeApp != null){

                    // Set button as enabled
                    imgFreeApp.setEnabled(true);
                    txtFreeApp.setEnabled(true);

                    // Set ad's image
                    imgFreeApp.setImageBitmap(nativeAd.getImageBitmap());

                    // Set ad's title
                    txtFreeApp.setText(nativeAd.getTitle());
                }*//*
                }
            }catch (Exception e){
                Fabric.getLogger().e("SelectActivity",e.toString());

            }
        }

        @Override
        public void onFailedToReceiveAd(Ad ad) {


        }
    };
*/

    Handler mHandler = new Handler();

    @Override
    public void onResume() {
        super.onResume();
        if (Utils.isUpgradedMy() == false && doNotshowAd == false) {

            loadInterstitial(this);
            doNotshowAd = true;
        } else {

            if (adShown == false) {

                doNotshowAd = false;
            }
            adShown = false;
        }

     /*   if(startAppAd!=null) {
        startAppAd.onResume();
        }*/


    }

    private boolean openCvLoaded;


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putSerializable("ss", Utils.mUiState);
        outState.putString("imagekey", Utils.mImgeUri.toString());


    }

    @Override
    protected void onDestroy() {

       /* if(adView!=null) {

            adView.destroy();
        }*/


        if (interstitialAd != null) {

            interstitialAd.destroy();
        }

       /* if (mAdView != null) {
            mAdView.destroy();
        }*/
        if (billingManager != null) {
            billingManager.destroy();
        }
        super.onDestroy();

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Utils.mUiState = (UiState) savedInstanceState.getSerializable("ss");
        String imageUrl = savedInstanceState.getString("imagekey");
        if (imageUrl != null) {
            Utils.mImgeUri = Uri.parse(imageUrl);
        }
    }

    public void startResizeViewStatic() {
        Intent resizeIntent = new Intent();


        final Uri imageUri = Utils.mImgeUri;
//        resizeIntent.setData(imageUri);

        Utils.mUiState = UiState.FRAGMENT_RESIZE;

        Utils.mImgeUri = imageUri;
        ResizeFragment resizeFragment = ResizeFragment.newInstance(resizeIntent);
        Utils.addFragment(this, resizeFragment, R.id.contentFrame, true);
    }

    public void startResizeView() {
        Intent resizeIntent = new Intent();


        final Uri imageUri = Utils.mImgeUri;
        resizeIntent.setData(imageUri);

        Utils.mUiState = UiState.FRAGMENT_RESIZE;

        Utils.mImgeUri = imageUri;
        ResizeFragment resizeFragment = ResizeFragment.newInstance(resizeIntent);
        Utils.addFragment(this, resizeFragment, R.id.contentFrame, true);
    }

    private void launchSharingFragment() {


        SelectFragment selectFragment = (SelectFragment) getSupportFragmentManager().findFragmentByTag(SelectFragment.class.getSimpleName());


        if (selectFragment == null) {
            selectFragment = SelectFragment.newInstance();

            Utils.addFragment(this, selectFragment, R.id.contentFrame, false);

        } else {
//            Utils.replaceFragment(this, selectFragment, R.id.contentFrame,false);
        }

        //if it is restored the last state should not be undone

        if (Utils.mUiState != UiState.FRAGMENT_RESIZE) {

            Utils.mUiState = UiState.FRAGMENT_SELECT;
        } else {
            Utils.mUiState = UiState.FRAGMENT_RESIZE;

        }
    }

    public void doNotShowAd(boolean showAd) {
        this.doNotshowAd = showAd;
    }

   /* @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount()-1;

        if(count>=0) {

       FragmentManager.BackStackEntry backStackEntry =  getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1);
       String obj = backStackEntry.getName();
       if(obj.equals(ResizeFragment.class.getSimpleName())){
          Fragment fragment =  getSupportFragmentManager().findFragmentByTag(obj);
          if(fragment instanceof ResizeFragment){
              ResizeFragment resizeFragment = (ResizeFragment) fragment;

             ImageInfo imageInfo =  resizeFragment.getImageInfoAtIndex(0);
             if(imageInfo!=null && imageInfo.isSaved()==false){
                 resizeFragment.showConfirmDiscardAlert(this, new BaseFragment.OnDeleteSelectedListener() {
                     @Override
                     public void onDeleteSelected() {
                         //discard
                         SelectActivity.super.onBackPressed();
                     }
                 });
                 return;
             }

          }
       }


        }
        super.onBackPressed();


    }*/
}
