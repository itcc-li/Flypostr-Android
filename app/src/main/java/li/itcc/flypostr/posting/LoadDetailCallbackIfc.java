package li.itcc.flypostr.posting;

import li.itcc.flypostr.model.PostingWrapper;

/**
 * Created by sandro.pedrett on 20.08.2016.
 */

public interface LoadDetailCallbackIfc {
    void onPostingChanged(PostingWrapper posting);
    void onError(Throwable e);
}
