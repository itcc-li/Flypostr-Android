package li.itcc.flypostr.postingDetail;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import li.itcc.flypostr.PoiConstants;
import li.itcc.flypostr.model.PostingBean;
import li.itcc.flypostr.model.PostingWrapper;

/**
 * Created by sandro.pedrett on 20.08.2016.
 */

public class PostingDetailLoader implements ValueEventListener {
    private Context context;
    private PostingDetailLoaderCallback callback;
    private DatabaseReference postingListRef;

    public interface PostingDetailLoaderCallback {
        void onPostingChanged(PostingWrapper posting);
        void onError(Throwable e);
    }

    public PostingDetailLoader(Context context) {
        this.context = context;

        this.postingListRef = FirebaseDatabase.getInstance().getReference(PoiConstants.ROOT_POSTINGS);
    }

    public void load(String id, PostingDetailLoaderCallback callback) {
        this.callback = callback;

        DatabaseReference postingRef = postingListRef.child(id);
        postingRef.addValueEventListener(this);
    }

    public void detach() {
        postingListRef.removeEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        PostingBean bean = dataSnapshot.getValue(PostingBean.class);
        PostingWrapper postingWrapper = new PostingWrapper(bean);
        callback.onPostingChanged(postingWrapper);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        callback.onError(databaseError.toException());
    }
}
