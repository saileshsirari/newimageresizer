package apps.sai.com.imageresizer.util;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

public class DialogUtils {

    private static final String TAG = "DialogUtils";

    public static MaterialDialog.Builder getBuilder(Context context) {

        return new MaterialDialog.Builder(context);
    }

    /**
     * Displays the popup dialog recommending the user try the paid version
     */
   /* public static void showUpgradeNagDialog(final Context context, MaterialDialog.SingleButtonCallback listener) {

        //If we're in the free version, the app has been launched more than 15 times,
        //The message hasn't been read before, display the 'upgrade to pro' dialog.
        if (!ShuttleUtils.isUpgraded()
                && SettingsManager.getInstance().getLaunchCount() > 15
                && !SettingsManager.getInstance().getNagMessageRead()) {

            MaterialDialog.Builder builder = getBuilder(context)
                    .title(context.getResources().getString(R.string.get_pro_title))
                    .content(context.getResources().getString(R.string.get_pro_message))
                    .positiveText(R.string.btn_upgrade)
                    .onPositive(listener)
                    .negativeText(R.string.get_pro_button_no);

            builder.show();
            SettingsManager.getInstance().setNagMessageRead();

            AnalyticsManager.logUpgrade(AnalyticsManager.UpgradeType.NAG);
        }
    }*/

   /* public static void showWeekSelectorDialog(final Context context) {

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.weekpicker, null);

        final NumberPicker numberPicker;
        numberPicker = view.findViewById(R.id.weeks);
        numberPicker.setMaxValue(12);
        numberPicker.setMinValue(1);
        numberPicker.setValue(MusicUtils.getIntPref(context, "numweeks", 2));

        getBuilder(context)
                .title(R.string.week_selector)
                .customView(view, false)
                .negativeText(R.string.cancel)
                .positiveText(R.string.button_ok)
                .onPositive((materialDialog, dialogAction) -> {
                    int numweeks;
                    numweeks = numberPicker.getValue();
                    MusicUtils.setIntPref(context, "numweeks", numweeks);
                })
                .show();
    }*/

    /*public static void showRateSnackbar(final Activity activity, final View view) {
        //If the user hasn't dismissed the snackbar in the past, and we haven't already shown it for this session
        if (!SettingsManager.getInstance().getHasRated() && !SettingsManager.getInstance().hasSeenRateSnackbar) {
            //If this is the tenth launch, or a multiple of 50
            if (SettingsManager.getInstance().getLaunchCount() == 10 || (SettingsManager.getInstance().getLaunchCount() != 0 && SettingsManager.getInstance().getLaunchCount() % 50 == 0)) {

                Snackbar snackbar = Snackbar.make(view, R.string.snackbar_rate_text, Snackbar.LENGTH_INDEFINITE)
                        .setDuration(15000)
                        .setAction(R.string.snackbar_rate_action, v -> {
                            ShuttleUtils.openShuttleLink(activity, MusicPlayerApplication.getInstance().getPackageName(), activity.getPackageManager());
                            AnalyticsManager.logRateClicked();
                        })
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);

                                if (event != BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_TIMEOUT) {
                                    // We don't really care whether the user has rated or not. The snackbar was
                                    // dismissed. Never show it again.
                                    SettingsManager.getInstance().setHasRated();
                                }
                            }
                        });
                snackbar.show();

                TextView snackbarText = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                if (snackbarText != null) {
                    snackbarText.setTextColor(Color.WHITE);
                }

                AnalyticsManager.logRateShown();
            }

            SettingsManager.getInstance().hasSeenRateSnackbar = true;
        }
    }*/
}
