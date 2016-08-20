package li.itcc.flypostr.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;

/**
 * Created by sandro.pedrett on 20.08.2016.
 */
public class ImageLoader implements OnSuccessListener<FileDownloadTask.TaskSnapshot>, OnProgressListener<FileDownloadTask.TaskSnapshot> {
    private final String pathToStorageFolder;
    private Context context;
    private File tmpFile;
    private FileDownloadTask task;
    private String filename;
    private ImageLoaderCallback callback;

    public interface ImageLoaderCallback {
        void onError(String filename, Throwable e);
        void onImageLoaded(String filename, Bitmap bitmap);
        void onUpdateProgressDownload(String filename, long bytesReceived, long totalByteCount);
    }

    public ImageLoader(Context context, String pathToStorageFolder) {
        this.pathToStorageFolder = pathToStorageFolder;
        this.context = context;
    }

    public boolean cancel() {
        detach();
        return task.cancel();
    }

    public void startProgress(String filename, ImageLoaderCallback callback) {
        this.callback = callback;
        this.filename = filename;
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(pathToStorageFolder);

        try {
            // create tmpFile to this path
            tmpFile = new File(context.getCacheDir(), filename);
            task = storageRef.child(filename).getFile(tmpFile);

            task.addOnSuccessListener(this);
            task.addOnProgressListener(this);
        } catch (Exception e) {
            callback.onError(filename, e);
        }
    }

    public void detach() {
        if (task != null) {
            task.removeOnSuccessListener(this);
            task.removeOnProgressListener(this);
        }
    }

    @Override
    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
        Bitmap image = BitmapFactory.decodeFile(tmpFile.getAbsolutePath());

        callback.onImageLoaded(filename, image);
    }

    @Override
    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
        long totalByteCount = taskSnapshot.getTotalByteCount();
        long bytesTransferred = taskSnapshot.getBytesTransferred();

        callback.onUpdateProgressDownload(filename, bytesTransferred, totalByteCount);
    }
}
