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


    public interface ImageLoaderCallback {
        void onError(Throwable e);
        void onImageLoaded(String filename, Bitmap bitmap);
        void onUpdateProgressDownload(String filename, long bytesReceived, long totalByteCount);
    }

    public ImageLoader(Context context, String pathToStorageFolder) {
        this.pathToStorageFolder = pathToStorageFolder;
        this.context = context;
        cache = new ImageCache(context, pathToStorageFolder);
    }

    public boolean cancel() {
        detach();
        return task.cancel();
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

        callback.onUpdateProgressDownload(filename, bytesTransferred, totalByteCount);
    }
}
