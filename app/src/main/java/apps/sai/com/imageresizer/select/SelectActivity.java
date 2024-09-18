package apps.sai.com.imageresizer.select;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
        if (!s.isEmpty()) {
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doNotshowAd = true;
        setContentView(R.layout.activity_select);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        billingManager = new BillingManager(this, new BillingManager.BillingUpdatesListener() {
            @Override
            public void onPurchasesUpdated(List<Purchase> purchases) {
                for (Purchase purchase : purchases) {
                    for (String product : purchase.getProducts()) {
                        if (product.equals(Config.SKU_PREMIUM)) {
                            ImageResizeApplication.getInstance().setIsUpgraded(true);
                        }
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

        if (!Utils.isUpgradedMy()) {
            showFacebookBanner(this, R.id.banner_container_top, "179547122769778_189046365153187");
        }
        SettingsManager.getInstance().incrementLaunchCount();
        launchSharingFragment();
    }


    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
        if (!folder.canWrite()) {
            showError(R.string.unable_to_select_folder);
            return;
        }
        SettingsManager.getInstance().setFolderPath(folder.getAbsolutePath());


    }

    @Override
    public void onFolderChooserDismissed(@NonNull FolderChooserDialog dialog) {

    }
//    private StartAppAd startAppAd ;

    private InterstitialAd interstitialAd;
    private boolean adShown;

    public void loadInterstitial(final Context context) {
        if (true) {
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
                   Log.e(TAG,e.toString());
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


    @Override
    public void onResume() {
        super.onResume();
        if (!Utils.isUpgradedMy() && !doNotshowAd) {

            loadInterstitial(this);
            doNotshowAd = true;
        } else {

            if (!adShown) {

                doNotshowAd = false;
            }
            adShown = false;
        }

     /*   if(startAppAd!=null) {
        startAppAd.onResume();
        }*/


    }


    @Override
    public void onSaveInstanceState(Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putSerializable("ss", Utils.mUiState);
        outState.putString("imagekey", Utils.mImgeUri.toString());


    }

    @Override
    protected void onDestroy() {

        try {


            if (interstitialAd != null) {

                interstitialAd.destroy();
            }

       /* if (mAdView != null) {
            mAdView.destroy();
        }*/
            if (billingManager != null) {
                billingManager.destroy();
            }
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }
        super.onDestroy();

    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Utils.mUiState = (UiState) savedInstanceState.getSerializable("ss");
        String imageUrl = savedInstanceState.getString("imagekey");
        if (imageUrl != null) {
            Utils.mImgeUri = Uri.parse(imageUrl);
        }
    }

    private void launchSharingFragment() {


        SelectFragment selectFragment = (SelectFragment) getSupportFragmentManager().findFragmentByTag(SelectFragment.class.getSimpleName());


        if (selectFragment == null) {
            selectFragment = SelectFragment.newInstance();

            Utils.addFragment(this, selectFragment, R.id.contentFrame, false);

        }
//            Utils.replaceFragment(this, selectFragment, R.id.contentFrame,false);

        //if it is restored the last state should not be undone

        if (Utils.mUiState != UiState.FRAGMENT_RESIZE) {

            Utils.mUiState = UiState.FRAGMENT_SELECT;
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
