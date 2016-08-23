package li.itcc.flypostr.postingList;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import li.itcc.flypostr.model.PostingWrapper;

/**
 * Created by sandro.pedrett on 21.08.2016.
 */

public class PostingItemWrapper implements Comparable<PostingItemWrapper> {
    public final String id;
    public PostingWrapper posting;
    public LatLng location;
    public float distanceToCurrentLocationInMeter;

    public PostingItemWrapper(String id) {
        this.id = id;
    }

    public void setDistance(LatLng postingLocation, Location myLocation) {
        Location loc = new Location("PostingLoc");
        loc.setLatitude(postingLocation.latitude);
        loc.setLongitude(postingLocation.longitude);

        distanceToCurrentLocationInMeter = loc.distanceTo(myLocation);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PostingItemWrapper)) {
            return false;
        }
        PostingItemWrapper other = (PostingItemWrapper)o;

        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        int result;
        result = id.hashCode();
        return result;
    }

    @Override
    public int compareTo(PostingItemWrapper another) {
        if (distanceToCurrentLocationInMeter < another.distanceToCurrentLocationInMeter) {
            return -1;
        } else if (distanceToCurrentLocationInMeter == another.distanceToCurrentLocationInMeter) {
            return 0;
        } else {
            return 1;
        }
    }
}
