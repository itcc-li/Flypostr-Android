package li.itcc.flypostr.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import li.itcc.flypostr.PoiConstants;

/**
 * Created by sandro.pedrett on 20.08.2016.
 */
public class ImageLoader implements OnSuccessListener<FileDownloadTask.TaskSnapshot>, OnProgressListener<FileDownloadTask.TaskSnapshot>, OnFailureListener {
    private final String pathToStorageFolder;
    private Context context;
    private File tmpFile;
    private FileDownloadTask task;
    private String filename;
    private ImageLoaderCallback callback;
    private ImageCache cache;

    public enum ImageCacheType {
        THUMBNAILS,
        IMAGES
    }

    public interface ImageLoaderCallback {
        void onImageLoadProgress(String filename, int progressPercent, String progressText);
        void onImageLoaded(String filename, Bitmap bitmap);
        void onError(Throwable e);
    }

    public ImageLoader(Context context, ImageCacheType cacheType) {
        int keepCacheFileCounter;
        switch (cacheType) {
            case IMAGES:
                this.pathToStorageFolder = PoiConstants.ROOT_IMAGES_STORAGE;
                keepCacheFileCounter = PoiConstants.KEEP_IMAGE_CACHE_IMAGES;
                break;
            case THUMBNAILS:
                this.pathToStorageFolder = PoiConstants.ROOT_THUMBNAIL_STORAGE;
                keepCacheFileCounter = PoiConstants.KEEP_IMAGE_CACHE_THUMBNAILS;
                break;
            default:
                throw new RuntimeException();
        }

        this.context = context;
        cache = new ImageCache(context, pathToStorageFolder, keepCacheFileCounter);
    }

    public boolean isInProgress() {
        if (task != null) {
            return task.isInProgress();
        }
        return false;
    }

    public boolean cancel() {
        if (task != null) {
            detach();
            return task.cancel();
        }
        return true;
    }

    public void startProgress(String filename, ImageLoaderCallback callback) {
        this.callback = callback;
        this.filename = filename;

        Bitmap result = cache.getBitmap(filename);

        if (result != null) {
            callback.onImageLoaded(filename, result);
        } else {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference(pathToStorageFolder);

            try {
                // create tmpFile to this path
                tmpFile = cache.createTmpFile();
                task = storageRef.child(filename).getFile(tmpFile);

                task.addOnSuccessListener(this);
                task.addOnProgressListener(this);
                task.addOnFailureListener(this);
            } catch (Exception e) {
                callback.onError(e);
            }
        }

    }

    public void detach() {
        if (task != null) {
            task.removeOnSuccessListener(this);
            task.removeOnProgressListener(this);
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        callback.onError(e);
    }

    @Override
    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
        File destFile = cache.put(filename, tmpFile);
        Bitmap image = BitmapFactory.decodeFile(destFile.getAbsolutePath());
        callback.onImageLoaded(filename, image);
    }

    @Override
    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
        long totalByteCount = taskSnapshot.getTotalByteCount();
        long bytesTransferred = taskSnapshot.getBytesTransferred();
        String progressText;
        int progressPercent;
        if (totalByteCount <= 0) {
            progressPercent = 0;
            progressText = "...";
        }
        else {
            progressPercent = (int)(bytesTransferred * 100L / totalByteCount);
            progressText = Integer.toString(progressPercent) + "%";
        }
        callback.onImageLoadProgress(filename, progressPercent, progressText);
    }
}
