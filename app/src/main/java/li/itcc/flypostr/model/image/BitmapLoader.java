package li.itcc.flypostr.model.image;

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
public class BitmapLoader {
    private final BitmapType cacheType;
    private final StorageReference storageRef;
    private Context context;

    public BitmapLoader(Context context, BitmapType cacheType) {
        this.context = context;
        this.cacheType = cacheType;
        String pathToStorageFolder;
        switch (cacheType) {
            case IMAGE:
                pathToStorageFolder = PoiConstants.ROOT_IMAGES_STORAGE;
                break;
            case THUMBNAIL:
                pathToStorageFolder = PoiConstants.ROOT_THUMBNAIL_STORAGE;
                break;
            default:
                throw new RuntimeException();
        }
        this.storageRef = FirebaseStorage.getInstance().getReference(pathToStorageFolder);
    }

    public BitmapLoaderStatus load(@NonNull String imageId, @NonNull File localFile, @NonNull BitmapLoaderCallback callback) {
        // check the file
        File destDir = localFile.getParentFile();
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        // we support multiple calls in parallel, so we allocate an executor
        BitmapLoaderExecutor executor = new BitmapLoaderExecutor(imageId, localFile, callback);
        executor.start();
        return executor;
    }

    private class BitmapLoaderExecutor implements BitmapLoaderStatus, OnSuccessListener<FileDownloadTask.TaskSnapshot>, OnProgressListener<FileDownloadTask.TaskSnapshot>, OnFailureListener {
        private final String imageId;
        private final BitmapLoaderCallback callback;
        private final File localFile;
        private FileDownloadTask task;

        private BitmapLoaderExecutor(String imageId, File localFile, BitmapLoaderCallback callback) {
            this.imageId = imageId;
            this.localFile = localFile;
            this.callback = callback;
        }

        private void start() {
            task = storageRef.child(imageId).getFile(localFile);
            task.addOnSuccessListener(this);
            task.addOnProgressListener(this);
            task.addOnFailureListener(this);
        }

        @Override
        public void onFailure(@NonNull Exception e) {
            callback.onError(e);
        }

        @Override
        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
            callback.onBitmapLoaded(imageId, bitmap);
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
            callback.onBitmapProgress(imageId, progressPercent, progressText);
        }


        public boolean isInProgress() {
            return task.isInProgress();
        }

        public boolean cancel() {
            return task.cancel();
        }
    }
}
