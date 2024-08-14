package apps.sai.com.imageresizer.data;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;

/**
 * Created by sailesh on 01/03/18.
 */

public class DataManager {
    private static final String TAG = "DataManager";
    private Disposable songsSubscription;
//    private BehaviorRelay<List<Song>> songsRelay = BehaviorRelay.create();

    private static DataManager instance;
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private DataManager() {

    }
    /**
     * Returns an {@link Observable < List >} from the songs relay, filtered by the passed in predicate.
     */
    public Observable<List<Song>> getSongsObservable(Predicate<Song> predicate) {
        return null;
        /*return getSongsRelay()
                .map(songs -> Stream.of(songs)
                        .filter(predicate)
                        .toList());*/
    }
}
