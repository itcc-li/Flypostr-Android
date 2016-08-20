package li.itcc.flypostr.posting;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import li.itcc.flypostr.model.PostingBean;
import li.itcc.flypostr.model.PostingWrapper;

/**
 * Created by sandro.pedrett on 20.08.2016.
 */

public class LoadDetailTask {
    private Context context;
    private LoadDetailCallbackIfc callback;

    // firebase
    private DatabaseReference postingListRef;

    public LoadDetailTask(Context context, LoadDetailCallbackIfc callback) {
        this.context = context;
        this.callback = callback;

        this.postingListRef = FirebaseDatabase.getInstance().getReference("postings");
    }

    public void load(String id) {
        DatabaseReference postingRef = postingListRef.child(id);
        postingRef.addValueEventListener(firebaseListener);
    }

    public void detach() {
        postingListRef.removeEventListener(firebaseListener);
    }

    private ValueEventListener firebaseListener = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            try {
                PostingBean bean = dataSnapshot.getValue(PostingBean.class);
                PostingWrapper postingWrapper = new PostingWrapper(bean);
                callback.onPostingChanged(postingWrapper);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            callback.onError(databaseError.toException());
        }
    };
}
