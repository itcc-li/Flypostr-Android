package li.itcc.flypostr.commentAdd;

import android.content.Context;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import li.itcc.flypostr.PoiConstants;
import li.itcc.flypostr.model.CommentWrapper;

/**
 * Created by Arthur on 20.08.2016.
 */

public class CommentSaver implements DatabaseReference.CompletionListener {
    private final Context context;
    private String key;

    public CommentSaver(Context context) {
        this.context = context;

    }

    public void save(String postingId, CommentWrapper detail) {
        // upload data
        DatabaseReference commentListRef = FirebaseDatabase.getInstance().getReference(PoiConstants.ROOT_COMMENTS).child(postingId);
        DatabaseReference childRef = commentListRef.push();
        this.key = childRef.getKey();
        childRef.setValue(detail.getBean(), this);
    }

    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        // dataq upload complete
    }

}
