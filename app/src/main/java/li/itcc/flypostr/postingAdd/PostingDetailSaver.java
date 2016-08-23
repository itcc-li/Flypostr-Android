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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import li.itcc.flypostr.FlypostrConstants;
import li.itcc.flypostr.model.PostingWrapper;
import li.itcc.flypostr.util.ImageCache;
import li.itcc.flypostr.util.StreamUtil;

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
        if (localImageFile != null && localImageFile.exists()) {
            imageID = UUID.randomUUID().toString() + ".jpg";
        }
        else {
            imageID = null;
        }
        // upload data
        DatabaseReference postingListRef = FirebaseDatabase.getInstance().getReference(FlypostrConstants.ROOT_POSTINGS);
        DatabaseReference childRef = postingListRef.push();
        this.key = childRef.getKey();
        detail.setImageId(imageID);
        childRef.setValue(detail.getBean(), this);
        // upload geolocation
        if (detail.getLat() != null && detail.getLng() != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(FlypostrConstants.ROOT_GEOFIRE);
            GeoFire geoFire = new GeoFire(ref);
            GeoLocation geoLoc = new GeoLocation(detail.getLat(), detail.getLng());
            geoFire.setLocation(this.key, geoLoc);
        }
        // upload image
        // TODO: set imageId after image and thumbnail are uploaded
        if (imageID != null) {
            ImageCache cacheThumb = new ImageCache(context, FlypostrConstants.ROOT_THUMBNAIL_STORAGE, -1);
            ImageCache cacheImage = new ImageCache(context, FlypostrConstants.ROOT_IMAGES_STORAGE, -1);

            // TODO: local caching of the image, e.g. with picasso
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference imageFileRef = storageRef.child(FlypostrConstants.ROOT_IMAGES_STORAGE).child(imageID);
            Uri fileUrl = Uri.fromFile(localImageFile);
            UploadTask imageUploadTask = imageFileRef.putFile(fileUrl);
            imageUploadTask.addOnFailureListener(this).addOnSuccessListener(this);
            // add the thumbnail
            // TODO: do in dedicated Task, not on UI thread
            File localThumbFile = new File(localImageFile.getParentFile(), "thumb_" + localImageFile.getName());
            SquareImageCropper cropper = new SquareImageCropper(this.context, localThumbFile, 128);
            try {
                cropper.crop(localImageFile);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            StorageReference thumbRef = storageRef.child(FlypostrConstants.ROOT_THUMBNAIL_STORAGE).child(imageID);
            Uri localThumbFileUrl = Uri.fromFile(localThumbFile);
            UploadTask thumbUploadTask = thumbRef.putFile(localThumbFileUrl);
            thumbUploadTask.addOnFailureListener(this).addOnSuccessListener(this);

            File tmpThumb = cacheThumb.createTmpFile();
            File tmpImage = cacheImage.createTmpFile();

            try {
                StreamUtil.pumpAllAndClose(new FileInputStream(localThumbFile), new FileOutputStream(tmpThumb));
                cacheThumb.put(imageID, tmpThumb);

                StreamUtil.pumpAllAndClose(new FileInputStream(localImageFile), new FileOutputStream(tmpImage));
                cacheImage.put(imageID, tmpImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
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