package apps.sai.com.imageresizer.billing;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

import apps.sai.com.imageresizer.rx.UnsafeAction;
import apps.sai.com.imageresizer.util.Config;

public class BillingManager implements PurchasesUpdatedListener, PurchasesResponseListener, ConsumeResponseListener {

    private static final String TAG = "BillingManager";

    public static final int BILLING_MANAGER_NOT_INITIALIZED = -1;

    public interface BillingUpdatesListener {
        void onPurchasesUpdated(List<Purchase> purchases);

        void onPremiumPurchaseCompleted();

        void onPremiumPurchaseRestored();
    }

    private final Activity activity;

    private BillingUpdatesListener updatesListener;

    private BillingClient billingClient;

    boolean serviceConnected = false;

    int billingClientResponseCode = BILLING_MANAGER_NOT_INITIALIZED;

    private boolean purchaseFlowInitiated = false;
    private boolean restorePurchasesInitiated = false;

    public BillingManager(Activity activity, final BillingUpdatesListener updatesListener) {

        this.activity = activity;
        this.updatesListener = updatesListener;

        billingClient = BillingClient.newBuilder(activity)
                .enablePendingPurchases()
                .setListener(this)
                .build();
        startServiceConnection(() -> queryPurchases(false));
    }


    private void startServiceConnection(UnsafeAction executeOnSuccess) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                // Logic from ServiceConnection.onServiceConnected should be moved here.
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    serviceConnected = true;
                    executeOnSuccess.run();
                }
                billingClientResponseCode = billingResult.getResponseCode();
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Logic from ServiceConnection.onServiceDisconnected should be moved here.
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                serviceConnected = false;
            }
        });
    }

    @Override
    public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            // Handle the success of the consume operation.
        }
    }

    public void consumePurchase(String purchaseToken) {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchaseToken)
                        .build();
        billingClient.consumeAsync(consumeParams, this);
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
            Purchase premiumPurchase = null;
            for (Purchase purchase : list) {
                if (purchase.getSkus().contains(Config.SKU_PREMIUM)) {
                    premiumPurchase = purchase;
                    consumePurchase(premiumPurchase.getPurchaseToken());
                }
            }
            if (purchaseFlowInitiated || restorePurchasesInitiated) {
                if (premiumPurchase != null) {
                    if (purchaseFlowInitiated) {
                        updatesListener.onPremiumPurchaseCompleted();
                        purchaseFlowInitiated = false;
                    }
                    if (restorePurchasesInitiated) {
                        updatesListener.onPremiumPurchaseRestored();
                        restorePurchasesInitiated = false;
                    }
                }
            } else {
                updatesListener.onPurchasesUpdated(list);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "onPurchasesUpdated() - user cancelled the purchase flow - skipping");
        } else {
            Log.e(TAG, String.format("onPurchasesUpdated() got unknown resultCode: %d", billingResult.getResponseCode()), null);
        }
    }

    @Override
    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
        int code = billingResult.getResponseCode();
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

        }
    }


    public void queryPurchases(boolean initiateBilling) {
        if (billingClient != null && activity != null) {

            UnsafeAction queryAction = () -> {
                //  billingClient.queryPurchasesAsync(this);
                // Query Skus
                String skuToSell = Config.SKU_PREMIUM;
                List<String> skuList = new ArrayList<>();
                skuList.add(skuToSell);
                SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                billingClient.querySkuDetailsAsync(params.build(),
                        new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(@NonNull BillingResult billingResult,
                                                             List<SkuDetails> skuDetailsList) {
                                // Process the result.
                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                    for (SkuDetails sku : skuDetailsList) {
                                        if (sku.getSku().equals(Config.SKU_PREMIUM)) {
                                            BillingFlowParams purchaseParams =
                                                    BillingFlowParams.newBuilder()
                                                            .setSkuDetails(sku)
                                                            .build();
                                            if(initiateBilling) {
                                                billingClient.launchBillingFlow(activity, purchaseParams);
                                            }

                                        }
                                    }
                                } else {
                                    Log.e(TAG, "Query purchases() got an unknown response code: " + billingResult.getResponseCode(), null);
                                }
                            }
                        });
                /*Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                if (purchasesResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    onPurchasesUpdated(BillingClient.BillingResponseCode.OK, purchasesResult.getPurchasesList());
                } else {
                    LogUtils.logException(TAG, "Query purchases() got an unknown response code: " + purchasesResult.getResponseCode(), null);
                }*/
            };

            if (serviceConnected) {
                queryAction.run();
            } else {
                startServiceConnection(queryAction);
            }
        }
    }

    /**
     * Start a purchase or subscription replace flow
     */
    public void initiatePurchaseFlow() {
        UnsafeAction purchaseFlowRequest = new UnsafeAction() {
            @Override
            public void run() {
                queryPurchases(true);
            }
        };

        if (serviceConnected) {
            purchaseFlowRequest.run();
        } else {
            startServiceConnection(purchaseFlowRequest);
        }
    }

    public void restorePurchases() {
        restorePurchasesInitiated = true;
        queryPurchases(false);
    }

    /**
     * Returns the value Billing client response code or BILLING_MANAGER_NOT_INITIALIZED if the
     * client connection response was not received yet.
     */
    public int getBillingClientResponseCode() {
        return billingClientResponseCode;
    }

    public void destroy() {
        Log.d(TAG, "Destroying the manager.");

        if (billingClient != null && billingClient.isReady()) {
            billingClient.endConnection();
            billingClient = null;
        }
    }
}
