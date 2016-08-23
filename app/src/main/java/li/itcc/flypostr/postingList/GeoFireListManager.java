package li.itcc.flypostr.postingList;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import li.itcc.flypostr.FlypostrConstants;

/**
 * Created by sandro.pedrett on 21.08.2016.
 */

public class GeoFireListManager implements GeoQueryEventListener {
    private static final int MAX_RADIUS_FOR_SEARCH_IN_KM = 400; // 400 km radius
    private static final int MAX_DEFAULT_POSTINGS = 4;

    private GeoLocation lastLocation;
    private GeoFireListener listener;
    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private int locationCounter;
    private int bufferRadiusInM;
    private int maxLocationCounter;

    interface GeoFireListener {
        void onLocationFound(String id, LatLng location);
        void onLocationRemoved(String id);
        void onSearchFinish();
        void onConnectionFailed(String errorMsg);
    }

    public GeoFireListManager(GeoFireListener listener) {
        this.listener = listener;
        this.locationCounter = 0;
        this.bufferRadiusInM = 1;
        setMaxLocationCounter(MAX_DEFAULT_POSTINGS);
    }

    public void setMaxLocationCounter(int maxLocationCounter) {
        if (maxLocationCounter < 0) {
            maxLocationCounter = 0;
        }
        this.maxLocationCounter = maxLocationCounter;
    }

    public void setLastLocation(GeoLocation lastLocation) {
        this.lastLocation = lastLocation;
    }

    public void initializeGeoFire() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(FlypostrConstants.ROOT_GEOFIRE);
        geoFire = new GeoFire(ref);
    }

    public void searchNewLocations() {
        if (!isGeoFireConnected()) {
            listener.onConnectionFailed("No connection to server");
            return;
        }

        // only seach for new lastLocation if lastLocation counter not full and buffer radius no reach max radius
        if (locationCounter > maxLocationCounter || (bufferRadiusInM / 1000.0f) > MAX_RADIUS_FOR_SEARCH_IN_KM) {
            listener.onSearchFinish();
            return;
        }

        // radius exponential size
        bufferRadiusInM = bufferRadiusInM * 2;
        updateQuery(lastLocation, bufferRadiusInM / 1000.0f);
    }

    private boolean isGeoFireConnected() {
        return geoFire != null;
    }

    private void updateQuery(GeoLocation location, float radiusInKm) {
        if (location == null) {
            return;
        }
        if (geoQuery == null) {
            geoQuery = geoFire.queryAtLocation(location, (bufferRadiusInM));
            geoQuery.addGeoQueryEventListener(this);
        }
        geoQuery.setRadius(radiusInKm);
        geoQuery.setCenter(location);
    }

    public void detach(){
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
        }
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        locationCounter++;

        LatLng loc = new LatLng(location.latitude, location.longitude);
        listener.onLocationFound(key, loc);
    }

    @Override
    public void onKeyExited(String key) {
        locationCounter--;

        listener.onLocationRemoved(key);
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {

    }

    @Override
    public void onGeoQueryReady() {
        searchNewLocations();
    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        listener.onConnectionFailed(error.getMessage());
    }
}

