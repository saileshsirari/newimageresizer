package apps.sai.com.imageresizer.billing;

import static com.android.billingclient.api.BillingClient.BillingResponseCode.OK;
import static com.android.billingclient.api.BillingClient.BillingResponseCode.USER_CANCELED;
import static com.android.billingclient.api.BillingClient.ProductType.*;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.ProductType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

import apps.sai.com.imageresizer.data.BitmapResult;
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

    private final BillingUpdatesListener updatesListener;

    private BillingClient billingClient;

    private boolean serviceConnected = false;

    int billingClientResponseCode = BILLING_MANAGER_NOT_INITIALIZED;

    private boolean purchaseFlowInitiated = false;
    private boolean restorePurchasesInitiated = false;

    public BillingManager(Activity activity, final BillingUpdatesListener updatesListener) {

        this.activity = activity;
        this.updatesListener = updatesListener;
        PendingPurchasesParams pendingPurchasesParams = PendingPurchasesParams.newBuilder().enableOneTimeProducts().build();

        billingClient = BillingClient.newBuilder(activity)
                .setListener(this)
                .enablePendingPurchases(pendingPurchasesParams)
                .build();
        startServiceConnection(() -> queryPurchases(false));
    }


    private void startServiceConnection(UnsafeAction executeOnSuccess) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                // Logic from ServiceConnection.onServiceConnected should be moved here.
                if (billingResult.getResponseCode() == OK) {
                    Log.d(TAG, "Billing response OK");
                    serviceConnected = true;
                    executeOnSuccess.run();
                }else{
                    Toast.makeText(activity,  billingResult.getDebugMessage()+" Billing response not OK ", Toast.LENGTH_LONG).show();
                    Log.e(TAG, billingResult.getDebugMessage());
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
        if (billingResult.getResponseCode() == OK && list != null) {
            Purchase premiumPurchase = null;
            for (Purchase purchase : list) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    premiumPurchase = purchase;
                    if (!purchase.isAcknowledged()) {
                        AcknowledgePurchaseParams acknowledgePurchaseParams =
                                AcknowledgePurchaseParams.newBuilder()
                                        .setPurchaseToken(purchase.getPurchaseToken())
                                        .build();
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult1 -> {
                            Log.i(TAG, "acknowledgePurchase() -"+billingResult1.getResponseCode()+" , "+billingResult.getDebugMessage());


                        });
                    }
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
        } else if (billingResult.getResponseCode() == USER_CANCELED) {
            Log.i(TAG, "onPurchasesUpdated() - user cancelled the purchase flow - skipping");
        } else {
            Log.e(TAG, String.format("onPurchasesUpdated() got unknown resultCode: %d", billingResult.getResponseCode()), null);
        }
    }

    @Override
    public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
        int code = billingResult.getResponseCode();
        if (billingResult.getResponseCode() == OK) {

        }
    }


    public void queryPurchases(boolean initiateBilling) {
        if (billingClient != null && activity != null) {

            UnsafeAction queryAction = () -> {
                //  billingClient.queryPurchasesAsync(this);
                // Query Skus
                List<QueryProductDetailsParams.Product> list = new ArrayList<>();
                list.add(QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(Config.SKU_PREMIUM)
                        .setProductType(INAPP)
                        .build());
                QueryProductDetailsParams queryProductDetailsParams =
                        QueryProductDetailsParams.newBuilder()
                                .setProductList(list)
                                .build();

                billingClient.queryProductDetailsAsync(
                        queryProductDetailsParams,
                        (billingResult, productDetailsList) -> {
                            // check billingResult
                            // process returned productDetailsList
                            if (billingResult.getResponseCode() == OK) {
                                Log.e(TAG, "1. response ok "+productDetailsList.size());
                                for (ProductDetails productDetails : productDetailsList) {
                                    if (productDetails.getProductId().equals(Config.SKU_PREMIUM)) {
                                        Log.e(TAG, "2. response ok "+productDetails);
                                        List<ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
                                        ProductDetailsParams pd = ProductDetailsParams.newBuilder()
                                                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                                .setProductDetails(productDetails)
                                                .build();
                                        productDetailsParamsList.add(pd);

                                        if (initiateBilling) {
                                            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                                    .setProductDetailsParamsList(productDetailsParamsList)
                                                    .build();
                                           BillingResult billingResultFLow =  billingClient.launchBillingFlow(activity, billingFlowParams);
                                            if (billingResultFLow.getResponseCode() == OK) {
                                                Log.e(TAG, "3. response ok ");
                                            }else{
                                                Log.e(TAG, "3. response not ok "+billingResult.getDebugMessage() +". "+billingResult.getResponseCode());
                                            }

                                        }
                                    }
                                }
                            } else {
                                Log.e(TAG, "Query purchases() got an unknown response code: " + billingResult.getResponseCode(), null);
                            }
                        }
                );
                if (false) {
                    /*
                    billingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(@NonNull BillingResult billingResult,
                                                                 List<SkuDetails> skuDetailsList) {
                                    // Process the result.
                                    if (billingResult.getResponseCode() == OK) {
                                        for (SkuDetails sku : skuDetailsList) {
                                            if (sku.getSku().equals(Config.SKU_PREMIUM)) {
                                                BillingFlowParams purchaseParams =
                                                        BillingFlowParams.newBuilder()
                                                                .setSkuDetails(sku)
                                                                .build();
                                                if (initiateBilling) {
                                                    billingClient.launchBillingFlow(activity, purchaseParams);
                                                }

                                            }
                                        }
                                    } else {
                                        Log.e(TAG, "Query purchases() got an unknown response code: " + billingResult.getResponseCode(), null);
                                    }
                                }
                            });*/
                }
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
        UnsafeAction purchaseFlowRequest = () -> queryPurchases(true);

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
