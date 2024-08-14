package apps.sai.com.imageresizer.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;

import apps.sai.com.imageresizer.R;
import apps.sai.com.imageresizer.billing.BillingManager;
import apps.sai.com.imageresizer.select.SelectActivity;

public class UpgradeDialog {

    private static final String TAG = "UpgradeDialog";

    private UpgradeDialog() {
        //no instance
    }

    public static MaterialDialog getUpgradeDialog(@NonNull Activity activity) {
        return new MaterialDialog.Builder(activity)
                .title(activity.getResources().getString(R.string.get_pro_title))
                .content(activity.getResources().getString(R.string.upgrade_dialog_message))
                .positiveText(R.string.btn_upgrade)
                .onPositive((dialog, which) -> {

                    purchaseUpgrade(activity);

                })
                .negativeText(R.string.get_pro_button_no)
                .build();
    }

    private static void purchaseUpgrade(@NonNull Activity activity) {
        if (!(activity instanceof SelectActivity)) {
            Log.e(TAG, "Purchase may only be initiated with a BaseActivity");
            return;
        }
       BillingManager billingManager = ((SelectActivity) activity).getBillingManager();
        if (billingManager != null) {
            billingManager.initiatePurchaseFlow();
        }
    }

    public static MaterialDialog getUpgradeSuccessDialog(@NonNull Activity activity) {
        return new MaterialDialog.Builder(activity)
                .title(activity.getResources().getString(R.string.upgraded_title))
                .content(activity.getResources().getString(R.string.upgraded_message))
                .positiveText(R.string.restart_button)
                .onPositive((materialDialog, dialogAction) -> {
                    Intent intent = new Intent(activity, SelectActivity.class);
                    ComponentName componentName = intent.getComponent();
                    Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                    activity.startActivity(mainIntent);
                })
                .build();
    }
}