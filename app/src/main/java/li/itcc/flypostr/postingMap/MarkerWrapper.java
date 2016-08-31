package li.itcc.flypostr.postingMap;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by sandro.pedrett on 29.08.2016.
 */
public class MarkerWrapper implements ClusterItem {
    private LatLng position;
    public String postingID;
    public String title;
    public String snippet;
    public Bitmap image;
    public DatabaseReference postingRef;
    public Bitmap bitmap;

    public MarkerWrapper(LatLng position, String postingID) {
        this.position = position;
        this.postingID = postingID;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng latLng) {
        position = latLng;
    }
}
