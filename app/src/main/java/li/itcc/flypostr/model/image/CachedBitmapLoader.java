package li.itcc.flypostr.model.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by Arthur on 22.08.2016.
 *
 * Has the same API as a BitmapLoader but uses a local file cache to reduce download data size.
 */

public class CachedBitmapLoader {
    private final Context context;
    private final BitmapType cacheType;
    private final BitmapLoader bitmapLoader;
    private final BitmapCacheFileManager fileManager;
    private final BitmapReader bitmapReader;

    public CachedBitmapLoader(Context context, BitmapType cacheType) {
        this.context = context;
        this.cacheType = cacheType;
        this.bitmapLoader = new BitmapLoader(context, cacheType);
        this.bitmapReader = new BitmapReader(context);
        this.fileManager = new BitmapCacheFileManager(context);
    }

    public BitmapLoaderStatus load(@NonNull String imageId, @NonNull BitmapLoaderCallback callback) {
        File cacheFile = fileManager.getFile(imageId, this.cacheType);
        if (cacheFile.exists()) {
            // load from file
            return bitmapReader.load(imageId, cacheFile, callback);
        }
        File tmpFile = fileManager.createTmpFile(this.cacheType);
        CallbackWrapper wrapper = new CallbackWrapper(callback, tmpFile);
        wrapper.downloadStatus = this.bitmapLoader.load(imageId, tmpFile, wrapper);
        return wrapper;
    }


    private class CallbackWrapper implements BitmapLoaderCallback, BitmapLoaderStatus {
        private final BitmapLoaderCallback callback;
        private final File tmpFile;
        public BitmapLoaderStatus downloadStatus;

        public CallbackWrapper(BitmapLoaderCallback callback, File tmpFile) {
            this.callback = callback;
            this.tmpFile = tmpFile;
        }

        @Override
        public void onBitmapProgress(String imageId, int progressPercent, String progressText) {
            callback.onBitmapProgress(imageId, progressPercent, progressText);
        }

        @Override
        public void onBitmapLoaded(String imageId, Bitmap bitmap) {
            // put the temp file in the file cache
            fileManager.put(imageId, cacheType, tmpFile);
            callback.onBitmapLoaded(imageId, bitmap);
        }

        @Override
        public void onError(Throwable e) {
            tmpFile.delete();
            callback.onError(e);
        }

        @Override
        public boolean isInProgress() {
            return downloadStatus.isInProgress();
        }

        @Override
        public boolean cancel() {
            boolean done = downloadStatus.cancel();
            if (done) {
                tmpFile.delete();
            }
            return done;
        }
    }
}
