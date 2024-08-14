package apps.sai.com.imageresizer.rx;

import io.reactivex.functions.Action;

/**
 * An Action which does not throw on error.
 */
public interface UnsafeAction extends Action {

    @Override
    void run();
}