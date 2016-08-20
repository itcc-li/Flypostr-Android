package li.itcc.flypostr.poiadd;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

import li.itcc.flypostr.model.PostingBean;

/**
 * Created by Arthur on 20.08.2016.
 */

public class PostingDetailSaver {

    private final Context fContext;

    public PostingDetailSaver(Context context) {
        fContext = context;
    }

    public void save(PostingBean detail, File localImageFile) {
        // TODO: local caching of the image, e.g. with picasso
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = storageRef.child("images").child(fileName);
        Uri fileUrl = Uri.fromFile(localImageFile);
        UploadTask fileUploadTask = fileRef.putFile(fileUrl);

        fileUploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                System.out.println("This is an error");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }
        });
    }

}
