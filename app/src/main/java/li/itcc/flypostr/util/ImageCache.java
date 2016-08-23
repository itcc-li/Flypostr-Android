package li.itcc.flypostr.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by sandro.pedrett on 20.08.2016.
 * TODO: remove, use CachedBitmapLoader instead.
 */
public class ImageCache {
    private final Context context;
    private String rootPath;
    private static HashMap<String, Object> lockHashMap = new HashMap<>();
    private File cachDir;

    /**
     * Create a new file cache thumbnail.
     * @param context of application
     */
    public ImageCache(Context context, String rootPath, int keepFileCounter) {
        this.context = context;
        this.rootPath = rootPath;

        File files = context.getCacheDir();
        this.cachDir = new File(files, rootPath);
        if (!this.cachDir.exists()) {
            this.cachDir.mkdirs();
        } else {
            if (keepFileCounter > 0) {
                cleanup(keepFileCounter);
            }
        }
    }

    public void cleanup(int keepFilesCount) {
        File[] files = cachDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });

        if (files != null && files.length > keepFilesCount) {
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

            for (int i = keepFilesCount; i < files.length; i++) {
                files[i].delete();
            }
        }
    }

    /**
     * Get bitmap in cache if exist, otherwise return null
     */
    public Bitmap getBitmap(String id) {
        // look in memory
        Bitmap result = null;
        File imageFile = getFile(id);

        if (imageFile.exists()) {
            result = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

            if (result == null) {
                imageFile.delete();
            }
        }
        return result;
    }

    @NonNull
    private File getFile(String id) {
        File files = context.getCacheDir();
        File cachDir = new File(files, rootPath);
        return new File(cachDir, id);
    }

    public File createTmpFile() {

        return new File(cachDir, UUID.randomUUID().toString() + ".jpg");
    }

    public File put(String key, File tmpFile) {
        Object lock;
        synchronized (lockHashMap) {
            lock = lockHashMap.get(key);

            if (lock == null) {
                lock = new Object();
                lockHashMap.put(key, lock);
            }
        }

        File destFile = getFile(key);
        synchronized (lock) {
            if (destFile.exists()) {
                destFile.delete();
            }
            tmpFile.renameTo(destFile);
        }
        return destFile;
    }
}
