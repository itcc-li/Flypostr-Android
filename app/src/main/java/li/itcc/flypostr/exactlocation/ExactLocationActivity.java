package li.itcc.flypostr.exactlocation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import li.itcc.flypostr.PoiConstants;
import li.itcc.flypostr.R;

/**
 * Created by Arthur on 12.09.2015.
 */
public class ExactLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String KEY_LOCATION = "KEY_LOCATION";
    private static final String KEY_EXACT_LOCATION = "KEY_EXACT_LOCATION";
    public static final String RESULT_KEY = "RESULT_KEY";
    private Location fLocation;
    private Marker fMarker;
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
        fLocation = getIntent().getExtras().getParcelable("KEY_LOCATION");
        fExactLocation = getIntent().getExtras().getParcelable("KEY_EXACT_LOCATION");
        updateViewArea();
        setContentView(R.layout.fine_poistion_activity);
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
        double latitude = fLocation.getLatitude();
        double longitude = fLocation.getLongitude();
        // we add 10%
        double radius = (double) PoiConstants.FINE_LOCATION_MAX_RADIUS * 1.1;
        double deltaLatitude = Math.toDegrees(radius / (double)PoiConstants.EARTH_RADIUS_IN_METER);
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


    @Override
    public void onMapReady(GoogleMap map) {
        fGoogleMap = map;
        LatLng position = new LatLng(fLocation.getLatitude(), fLocation.getLongitude());
        fGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        fGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18.0f));
        UiSettings settings = fGoogleMap.getUiSettings();
        settings.setMyLocationButtonEnabled(false);
        settings.setMapToolbarEnabled(false);
        settings.setCompassEnabled(false);
        settings.setIndoorLevelPickerEnabled(false);
        settings.setScrollGesturesEnabled(false);
        settings.setZoomGesturesEnabled(false);
        settings.setRotateGesturesEnabled(false);
        settings.setTiltGesturesEnabled(false);
        settings.setZoomControlsEnabled(false);
        LatLng markerPosition = position;
        if (fExactLocation != null) {
            markerPosition = new LatLng(fExactLocation.getLatitude(), fExactLocation.getLongitude());
        }
        String title = getString(R.string.txt_drag_me);
        String snippet = getString(R.string.txt_drag_me_snippet);
        MarkerOptions options = new MarkerOptions();
        options.position(markerPosition).draggable(true).title(title).snippet(snippet);
        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_48dp));
        fMarker = fGoogleMap.addMarker(options);
        CircleOptions circleOptions = new CircleOptions().center(position).radius(PoiConstants.FINE_LOCATION_MAX_RADIUS);
        fCircle = fGoogleMap.addCircle(circleOptions);
        // zoom to the correct level as soon as map is ready
        fGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                onMapLoadedImpl();
            }
        });
        fGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // no default behavior
                return true;
            }
        });
        fGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng newPos = marker.getPosition();
                Location loc = new Location("Exact");
                loc.setLatitude(newPos.latitude);
                loc.setLongitude(newPos.longitude);
                loc.setAccuracy(1f);
                double distance = loc.distanceTo(fLocation);
                if (distance > PoiConstants.FINE_LOCATION_MAX_RADIUS) {
                    // jump back
                    double deltaLatitude = newPos.latitude - fLocation.getLatitude();
                    double deltaLongitude = newPos.longitude - fLocation.getLongitude();
                    double factor = (double)PoiConstants.FINE_LOCATION_MAX_RADIUS / distance;
                    double newLatitude = fLocation.getLatitude() + deltaLatitude * factor;
                    double newLongitude = fLocation.getLongitude() + deltaLongitude * factor;
                    marker.setPosition(new LatLng(newLatitude, newLongitude));
                    loc.setLatitude(newLatitude);
                    loc.setLongitude(newLongitude);
                }
                fExactLocation = loc;
                // remember that the user has done a successful drag
                SharedPreferences settings = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("DragDone", true);
                editor.commit();
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

        SharedPreferences settings = getSharedPreferences("PREF_NAME", Context.MODE_PRIVATE);
        boolean dragDone = settings.getBoolean("DragDone", false);
        if (dragDone) {
            // don't show drag tip
            return;
        }
        fMarker.showInfoWindow();
        // hide info window after 3 secs
        new Handler(this.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (fMarker.isInfoWindowShown()) {
                    fMarker.hideInfoWindow();
                }
            }
            {}},3000);
    }

}
