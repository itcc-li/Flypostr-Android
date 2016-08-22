package li.itcc.flypostr.model.image;

import android.graphics.Bitmap;

/**
 * Created by Arthur on 22.08.2016.
 */
public interface BitmapLoaderCallback {

    void onBitmapProgress(String imageId, int progressPercent, String progressText);

    /**
     * @param imageId
     * @param bitmap might be null if the image is not available.
     */
    void onBitmapLoaded(String imageId, Bitmap bitmap);

    void onError(Throwable e);
}
