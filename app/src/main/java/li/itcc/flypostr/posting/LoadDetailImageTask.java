package li.itcc.flypostr.posting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

/**
 * Created by sandro.pedrett on 20.08.2016.
 */

public class LoadDetailImageTask implements OnSuccessListener<FileDownloadTask.TaskSnapshot> {
    private Context context;
    private LoadDetailImageCallbackIfc callback;
    private File file;

    // firebase
    private StorageReference storageRef;

    public LoadDetailImageTask(Context context, LoadDetailImageCallbackIfc callback) {
        this.context = context;
        this.callback = callback;

        this.storageRef = FirebaseStorage.getInstance().getReference();
    }

    public void loadImage(String filename) {
        try {
            file = new File(context.getCacheDir(), filename );
            FileDownloadTask task = storageRef.child("images").child(filename).getFile(file);
            task.addOnSuccessListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
        Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
        callback.onImageReceived(image);
    }
}
