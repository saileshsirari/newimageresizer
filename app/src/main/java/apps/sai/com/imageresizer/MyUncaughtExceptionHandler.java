package apps.sai.com.imageresizer;

import apps.sai.com.imageresizer.select.SelectActivity;

/**
 * Created by sailesh on 11/01/18.
 */

public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private SelectActivity activity;
    private Thread.UncaughtExceptionHandler defaultUEH;

    public MyUncaughtExceptionHandler(SelectActivity activity) {
        this.activity = activity;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void setActivity(SelectActivity activity) {
        this.activity = activity;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        activity.showError(ex);

//        defaultUEH.uncaughtException(thread, ex);

    }
}
