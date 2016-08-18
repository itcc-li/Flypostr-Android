package li.itcc.flypostr.poimap;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import li.itcc.flypostr.R;
import li.itcc.flypostr.TitleHolder;
import li.itcc.flypostr.poiadd.PoiAddOnClickListener;
import li.itcc.flypostr.poidetail.PoiDetailActivity;
import li.itcc.flypostr.util.ThumbnailCache;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiMapFragment extends SupportMapFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {
    private static final String KEY_LOCATION_ZOOM_DONE = "KEY_LOCATION_ZOOM_DONE";
    public static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 8765;
    private GoogleMap fGoogleMap;
    private HashMap<String, Marker> fIdToMarker = new HashMap<>();
    private HashMap<Marker, String> fMarkerToId = new HashMap<>();
    private View fCreateButton;
    private Location fLocation;
    private boolean fLocationZoomDone = false;
    private GoogleApiClient fGoogleApiClient;
    private GeoFire fGeoFire;
    private GeoQuery fGeoQuery;
    private GeoQueryEventListener fGeoQueryEventListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("geoLocation");
        fGeoFire = new GeoFire(ref);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            fLocationZoomDone = savedInstanceState.getBoolean(KEY_LOCATION_ZOOM_DONE);
        }
        View v = super.onCreateView(inflater, container, savedInstanceState);
        super.getMapAsync(this);

        // trick: we have to add a floating button so we add an extra layer
        boolean addButton = true;
        if (addButton) {
            container.removeView(v);
            View rootView = inflater.inflate(R.layout.poi_map_fragment, container, false);
            FrameLayout frame = (FrameLayout) rootView.findViewById(R.id.frame_layout);
            fCreateButton = rootView.findViewById(R.id.viw_add_button);
            fCreateButton.setOnClickListener(new PoiAddOnClickListener(getActivity()));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.removeView(v);
            frame.addView(v, params);
            return rootView;
        }
        return v;
    }

    //// interface OnMapReadyCallback

    @Override
    public void onMapReady(GoogleMap googleMap) {
        fGoogleMap = googleMap;
        fGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                onClick(marker);
            }
        });
        //fGoogleMap.setMyLocationEnabled(false);
        fGoogleMap.setInfoWindowAdapter(new PoiInfoWindowAdapter());
        fGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        UiSettings setting = fGoogleMap.getUiSettings();
        setting.setMapToolbarEnabled(false);
        setting.setMyLocationButtonEnabled(true);
        updateGeoQuery();
    }

    private void updateGeoQuery() {
        if (fGoogleMap != null && fLocation != null) {
            if (fGeoQuery == null) {
                GeoLocation geoLoc = new GeoLocation(fLocation.getLatitude(), fLocation.getLongitude());
                // fGeoFire.setLocation("id654387", geoLoc);
                // creates a new query around fLocation with a radius of 20 kilometers
                fGeoQuery = fGeoFire.queryAtLocation(geoLoc, 20);
                fGeoQueryEventListener = new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        addMarker(location.latitude, location.longitude, "Test", "Descr", key);
                    }

                    @Override
                    public void onKeyExited(String key) {
                        removeMarker(key);
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                        moveMarker(key, location.latitude, location.longitude);
                    }

                    @Override
                    public void onGeoQueryReady() {
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {
                    }
                };
                fGeoQuery.addGeoQueryEventListener(fGeoQueryEventListener);
            }
        }
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TitleHolder) {
            ((TitleHolder) context).setTitleId(R.string.title_overview_map);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.poi_map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_satellite) {
            if (fGoogleMap != null) {
                if (item.isChecked()) {
                    item.setChecked(false);
                    fGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else {
                    item.setChecked(true);
                    fGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
            }
            return true;
        } else if (item.getItemId() == R.id.action_my_location) {
            if (fGoogleMap != null) {
                if (item.isChecked()) {
                    item.setChecked(false);
                    //fGoogleMap.setMyLocationEnabled(false);
                } else {
                    item.setChecked(true);
                    //fGoogleMap.setMyLocationEnabled(true);
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // start loading
        buildGoogleApiClient();
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        fGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (fGoogleApiClient.isConnected()) {
            fGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_LOCATION_ZOOM_DONE, fLocationZoomDone);
    }

    private synchronized void buildGoogleApiClient() {
        Context context = getContext();
        fGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean onClick(Marker marker) {
        String id = fMarkerToId.get(marker);
        if (id != null) {
            PoiDetailActivity.start(getActivity(), id);
        }
        return true;
    }

    // google api client

    @Override
    public void onConnected(Bundle bundle) {
        checkAndGetLocation(true);
    }

    private void checkAndGetLocation(boolean executeRequest) {
        Context context = getContext();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (executeRequest) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
        }
        else {
            Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(fGoogleApiClient);
            setLocation(lastKnownLocation);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkAndGetLocation(false);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public void setLocation(Location location) {
        if (location == null) {
            return;
        }
        if (!isAdded()) {
            return;
        }
        fLocation = location;
        if (fGoogleMap != null) {
            if (!fLocationZoomDone) {
                // only zoom once
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                fGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 11.5f));
                fLocationZoomDone = true;
            }
        }
        updateGeoQuery();
    }


    private void addMarker(double latitude, double longitude, String name, String shortDescr, String id) {
        LatLng loc = new LatLng(latitude, longitude);
        MarkerOptions options = new MarkerOptions();
        options.position(loc).draggable(true).title(name).snippet(shortDescr);
        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_48dp));
        Marker marker = fGoogleMap.addMarker(options);
        fIdToMarker.put(id, marker);
        fMarkerToId.put(marker, id);
    }

    private void removeMarker(String key) {
        Marker marker = fIdToMarker.get(key);
        if (marker != null) {
            fIdToMarker.remove(key);
            fMarkerToId.remove(marker);
            marker.remove();
        }
    }

    private void moveMarker(String key, double latitude, double longitude) {
        Marker marker = fIdToMarker.get(key);
        if (marker != null) {
            LatLng newLatLng = new LatLng(latitude, longitude);
            marker.setPosition(newLatLng);
        }
    }


     public class PoiInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private ThumbnailCache fCache;
        private View fView;
        private ImageView fImage;
        private TextView fName;
        private TextView fDescription;


        public PoiInfoWindowAdapter() {
            fCache = new ThumbnailCache(getContext());
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            String id = fMarkerToId.get(marker);
            if (id == null) {
                return null;
            }
            Bitmap bitmap = fCache.getBitmap(id);
            if (bitmap == null) {
                return null;
            }
            if (fView == null) {
                fView = getLayoutInflater(null).inflate(R.layout.map_info_window, null);
                fImage = (ImageView)fView.findViewById(R.id.imv_thumbnail);
                fName = (TextView)fView.findViewById(R.id.txv_poi_name);
                fDescription = (TextView)fView.findViewById(R.id.txv_description);
            }
            fImage.setImageBitmap(bitmap);
            fName.setText(marker.getTitle());
            String snippet = marker.getSnippet();
            if (snippet == null || snippet.length() == 0) {
                fDescription.setVisibility(View.GONE);
            }
            else {
                fDescription.setText(snippet);
                fDescription.setVisibility(View.VISIBLE);
            }
            return fView;
        }
    }
}
