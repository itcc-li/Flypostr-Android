package li.itcc.flypostr.postingDetail;

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
public class PostingImageLoader implements OnSuccessListener<FileDownloadTask.TaskSnapshot>, OnProgressListener<FileDownloadTask.TaskSnapshot> {
    private final String pathToStorageFolder;
    private Context context;
    private PostingImageLoaderCallback callback;
    private File file;
    private FileDownloadTask task;

    public interface PostingImageLoaderCallback {
        void onImageReceived(Bitmap bitmap);
        void onUpdateProgressDownload(long bytesReceived, long totalByteCount);
        void onError(Throwable e);
    }

    public PostingImageLoader(Context context, String pathToStorageFolder) {
        this.pathToStorageFolder = pathToStorageFolder;
        this.context = context;
    }

    public void loadImage(String filename, PostingImageLoaderCallback callback) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference(pathToStorageFolder);
        this.callback = callback;

        try {
            // TODO implement
            file = new File(context.getCacheDir(), filename);
            task = storageRef.child(filename).getFile(file);

            task.addOnSuccessListener(this);
            task.addOnProgressListener(this);
        } catch (Exception e) {
            callback.onError(e);
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
        Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
        callback.onImageReceived(image);
    }

    @Override
    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
        long totalByteCount = taskSnapshot.getTotalByteCount();
        long bytesTransferred = taskSnapshot.getBytesTransferred();
        callback.onUpdateProgressDownload(bytesTransferred, totalByteCount);
    }
}
