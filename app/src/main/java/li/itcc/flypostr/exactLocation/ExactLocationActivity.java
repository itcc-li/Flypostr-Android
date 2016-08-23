package li.itcc.flypostr.exactLocation;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import li.itcc.flypostr.FlypostrConstants;
import li.itcc.flypostr.R;
import li.itcc.flypostr.postingAdd.PostingAddActivity;

/**
 * Created by Arthur on 12.09.2015.
 */
public class ExactLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String KEY_LOCATION = "KEY_LOCATION";
    private static final String KEY_EXACT_LOCATION = "KEY_EXACT_LOCATION";
    public static final String RESULT_KEY = "RESULT_KEY";
    private Location fGpsLocation;
    private GoogleMap fGoogleMap;
    private Circle fCircle;
    private LatLngBounds fViewArea;
    private LatLng fSouthWest;
    private LatLng fNorthEast;
    private Location fExactLocation;

    public static void start(Activity parent, Location location, Location exactLocation, int requestCode) {
        Intent i = new Intent(parent, ExactLocationActivity.class);
        i.putExtra(KEY_LOCATION, location);
        i.putExtra(KEY_EXACT_LOCATION, exactLocation);
        parent.startActivityForResult(i, requestCode);
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra(RESULT_KEY, fExactLocation);
        setResult(RESULT_OK, data);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fGpsLocation = getIntent().getExtras().getParcelable("KEY_LOCATION");
        fExactLocation = getIntent().getExtras().getParcelable("KEY_EXACT_LOCATION");
        updateViewArea();
        setContentView(R.layout.exact_location_activity);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.smf_map_fragment);
        if (savedInstanceState != null) {
            fExactLocation = savedInstanceState.getParcelable(KEY_EXACT_LOCATION);
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.exact_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_satellite) {
            if (item.isChecked()) {
                item.setChecked(false);
                fGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
            else {
                item.setChecked(true);
                fGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_EXACT_LOCATION, fExactLocation);
        super.onSaveInstanceState(outState);
    }


    private void updateViewArea() {
        Location initialLocation;
        if (fExactLocation != null) {
            initialLocation = fExactLocation;
        }
        else {
            initialLocation = fGpsLocation;
        }
        double latitude = initialLocation.getLatitude();
        double longitude = initialLocation.getLongitude();
        // we add 10%
        double radius = (double) FlypostrConstants.FINE_LOCATION_MAX_RADIUS_IN_METER * 1.1;
        double deltaLatitude = Math.toDegrees(radius / (double) FlypostrConstants.EARTH_RADIUS_IN_METER);
        double cosLat = Math.cos(Math.toRadians(latitude));
        if (cosLat < 0.01) {
            // avoid division by zero at north pole
            cosLat = 0.01;
        }
        double deltaLongitude = deltaLatitude / cosLat;
        double northLatitude = latitude + deltaLatitude;
        double southLatitude = latitude - deltaLatitude;
        double eastLongitude = longitude + deltaLongitude;
        double westLongitude = longitude - deltaLongitude;
        fSouthWest = new LatLng(southLatitude, westLongitude);
        fNorthEast = new LatLng(northLatitude, eastLongitude);
        fViewArea = LatLngBounds.builder().include(fSouthWest).include(fNorthEast).build();
        //fViewArea = new LatLngBounds(fSouthWest, fNorthEast);
    }


    private class CameraPositionCatcher implements GoogleMap.OnCameraIdleListener {

        @Override
        public void onCameraIdle() {
            handleNewCameraPosition();
        }

    }

    private void handleNewCameraPosition() {
        Location selectedLocation = toLocation(fGoogleMap.getCameraPosition().target);
        // check if we are within the circle
        Location correctedLocation = getCorrectedLocation(fGpsLocation, selectedLocation);
        fExactLocation = correctedLocation;
        if (correctedLocation != selectedLocation) {
            // selected location is not ok, move map back
            LatLng newCameraPos = toLatLng(correctedLocation);
            fGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(newCameraPos));
            // show the circle
            if (fCircle == null) {
                CircleOptions circleOptions = new CircleOptions();
                circleOptions.center(toLatLng(fGpsLocation));
                circleOptions.radius(FlypostrConstants.FINE_LOCATION_MAX_RADIUS_IN_METER);
                circleOptions.strokeColor(ContextCompat.getColor(this, R.color.crosshair_circle));
                fCircle = fGoogleMap.addCircle(circleOptions);
            }
        }
    }

    private LatLng toLatLng(Location location) {
        LatLng result = new LatLng(location.getLatitude(), location.getLongitude());
        return result;
    }

    private Location toLocation(LatLng location) {
        Location result = new Location("Exact");
        result.setLatitude(location.latitude);
        result.setLongitude(location.longitude);
        result.setAccuracy(0.5f);
        return result;
    }

    private Location getCorrectedLocation(Location gpsLocation, Location selectedLocation) {
        if (PostingAddActivity.ACCEPT_EVERY_LOCATION) {
            return selectedLocation;
        }
        double distance = selectedLocation.distanceTo(fGpsLocation);
        if (distance > FlypostrConstants.FINE_LOCATION_MAX_RADIUS_IN_METER * 10) {
            // crazy selected position, go back to gps
            return gpsLocation;
        }
        else if (distance > FlypostrConstants.FINE_LOCATION_MAX_RADIUS_IN_METER) {
            // calculate best possible position
            double deltaLatitude = selectedLocation.getLatitude() - gpsLocation.getLatitude();
            double deltaLongitude = selectedLocation.getLongitude() - gpsLocation.getLongitude();
            double factor = (double) FlypostrConstants.FINE_LOCATION_MAX_RADIUS_IN_METER / distance;
            double newLatitude = fGpsLocation.getLatitude() + deltaLatitude * factor;
            double newLongitude = fGpsLocation.getLongitude() + deltaLongitude * factor;
            Location correctedLocation = new Location("Exact");
            correctedLocation.setLatitude(newLatitude);
            correctedLocation.setLongitude(newLongitude);
            return correctedLocation;
        }
        else {
            // selected position is ok
            return selectedLocation;
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        fGoogleMap = map;
        fGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng gpsLoc = toLatLng(fGpsLocation);
        Location initialCameraLocation;
        if (fExactLocation == null) {
            initialCameraLocation = fGpsLocation;
        }
        else {
            initialCameraLocation = fExactLocation;
        }
        LatLng initialLoc = toLatLng(initialCameraLocation);
        fGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLoc, 18.0f));
        CameraPositionCatcher listener = new CameraPositionCatcher();
        fGoogleMap.setOnCameraIdleListener(listener);
        UiSettings settings = fGoogleMap.getUiSettings();
        settings.setMyLocationButtonEnabled(false);
        settings.setMapToolbarEnabled(false);
        settings.setCompassEnabled(false);
        settings.setIndoorLevelPickerEnabled(false);
        settings.setScrollGesturesEnabled(true);
        settings.setZoomGesturesEnabled(true);
        settings.setRotateGesturesEnabled(false);
        settings.setTiltGesturesEnabled(false);
        settings.setZoomControlsEnabled(false);
        LatLng cameraPosition = gpsLoc;
        if (fExactLocation != null) {
            cameraPosition = new LatLng(fExactLocation.getLatitude(), fExactLocation.getLongitude());
        }
        // zoom to the correct level as soon as map is ready
        fGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                onMapLoadedImpl();
            }
        });

    }


    private void onMapLoadedImpl() {
        // zoom
        fGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(fViewArea, 0), new  GoogleMap.CancelableCallback() {

            @Override
            public void onFinish() {
                onZoomFinish();
            }

            @Override
            public void onCancel() {

            }
        });
    }


    private void onZoomFinish() {
        // switch to satellite mode
        fGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }

}
