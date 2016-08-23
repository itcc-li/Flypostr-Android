package li.itcc.flypostr.model.image;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;
import java.util.WeakHashMap;

import li.itcc.flypostr.FlypostrConstants;

/**
 * Created by Arthur on 22.08.2016.
 */

public class BitmapCacheFileManager {
    private static WeakHashMap<String, Object> lockHashMap = new WeakHashMap<>();
    private final Context context;
    private final File thumbCacheDir;
    private final File imageCacheDir;

    public BitmapCacheFileManager(Context context) {
        this.context = context;
        File cacheDir = new File(context.getCacheDir(), "bitmapcache");
        this.thumbCacheDir = new File(cacheDir, FlypostrConstants.ROOT_THUMBNAIL_STORAGE);
        if (!this.thumbCacheDir.exists()) {
            this.thumbCacheDir.mkdirs();
        }
        this.imageCacheDir = new File(cacheDir, FlypostrConstants.ROOT_IMAGES_STORAGE);
        if (!this.imageCacheDir.exists()) {
            this.imageCacheDir.mkdirs();
        }
    }

    @NonNull
    public File getFile(String imageId, BitmapType type) {
        File cacheDir = getCacheDir(type);
        return new File(cacheDir, imageId);
    }

    private File getCacheDir(BitmapType type) {
        if (type == BitmapType.IMAGE) {
            return this.imageCacheDir;
        }
        else if (type == BitmapType.THUMBNAIL) {
            return this.thumbCacheDir;
        }
        throw new RuntimeException();
    }

    public File createTmpFile(BitmapType type) {
        return new File(getCacheDir(type), UUID.randomUUID().toString() + ".jpg");
    }

    public File put(String imageId, BitmapType type, File tmpFile) {
        Object lock;
        synchronized (lockHashMap) {
            lock = lockHashMap.get(imageId);
            if (lock == null) {
                lock = new Object();
                lockHashMap.put(imageId, lock);
            }
        }
        File destFile = getFile(imageId, type);
        synchronized (lock) {
            if (destFile.exists()) {
                destFile.delete();
            }
            tmpFile.renameTo(destFile);
        }
        return destFile;
    }

    /**
     * Execute when app closes
     */
    public void cleanup() {
        // cleanup in background
        new CleanupTask().execute();
    }

    private class CleanupTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            executeCleanup();
            return null;
        }
    }

    private void executeCleanup() {
        try {
            cleanup(this.thumbCacheDir, FlypostrConstants.KEEP_IMAGE_CACHE_THUMBNAILS);
            cleanup(this.imageCacheDir, FlypostrConstants.KEEP_IMAGE_CACHE_IMAGES);
        }
        catch (Exception x) {
            // ignor
        }
    }


    private void cleanup(File dir, int keepCount) {
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });

        if (files != null && files.length > keepCount) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    long delta = rhs.lastModified() - lhs.lastModified();
                    if (delta < 0) {
                        return -1;
                    } else if (delta == 0) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });

            for (int i = keepCount; i < files.length; i++) {
                files[i].delete();
            }
        }
    }
}
