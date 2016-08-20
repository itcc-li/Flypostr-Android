package li.itcc.flypostr.postingAdd;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

import li.itcc.flypostr.PoiConstants;
import li.itcc.flypostr.model.PostingWrapper;

/**
 * Created by Arthur on 20.08.2016.
 */

public class PostingDetailSaver implements DatabaseReference.CompletionListener,  OnSuccessListener<UploadTask.TaskSnapshot>, OnFailureListener {
    private final Context context;
    private String key;

    public PostingDetailSaver(Context context) {
        this.context = context;

    }

    public void save(PostingWrapper detail, File localImageFile) {
        String imageID;
        if (localImageFile != null) {
            imageID = UUID.randomUUID().toString() + ".jpg";
        }
        else {
            imageID = null;
        }
        // upload data
        DatabaseReference postingListRef = FirebaseDatabase.getInstance().getReference(PoiConstants.ROOT_POSTINGS);
        DatabaseReference childRef = postingListRef.push();
        this.key = childRef.getKey();
        detail.setImageId(imageID);
        childRef.setValue(detail.getBean(), this);
        // upload geolocation
        if (detail.getLat() != null && detail.getLng() != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PoiConstants.ROOT_GEOFIRE);
            GeoFire geoFire = new GeoFire(ref);
            GeoLocation geoLoc = new GeoLocation(detail.getLat(), detail.getLng());
            geoFire.setLocation(this.key, geoLoc);
        }
        // upload image
        if (localImageFile != null) {
            // TODO: local caching of the image, e.g. with picasso
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference fileRef = storageRef.child("images").child(imageID);
            Uri fileUrl = Uri.fromFile(localImageFile);
            UploadTask fileUploadTask = fileRef.putFile(fileUrl);
            fileUploadTask.addOnFailureListener(this).addOnSuccessListener(this);
        }
    }

    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        // dataq upload complete
    }

    @Override
    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
        // image uploaded successfully
        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
        // Uri downloadUrl = taskSnapshot.getDownloadUrl();
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        // TODO: Exception handling
        // image upload failed
    }
}
