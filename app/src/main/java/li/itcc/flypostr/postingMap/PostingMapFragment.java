package li.itcc.flypostr.postingMap;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.internal.zzf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.google.maps.android.clustering.view.ClusterRenderer;

import java.util.HashMap;
import java.util.Set;

import li.itcc.flypostr.FlypostrConstants;
import li.itcc.flypostr.R;
import li.itcc.flypostr.TitleHolder;
import li.itcc.flypostr.auth.AuthUtil;
import li.itcc.flypostr.auth.AuthenticateClickListener;
import li.itcc.flypostr.model.PostingBean;
import li.itcc.flypostr.model.PostingWrapper;
import li.itcc.flypostr.model.image.BitmapLoaderCallback;
import li.itcc.flypostr.model.image.BitmapType;
import li.itcc.flypostr.model.image.CachedBitmapLoader;
import li.itcc.flypostr.postingDetail.PostingDetailActivity;

import static li.itcc.flypostr.FlypostrConstants.GEO_QUERY_RADIUS_IN_KILOMETER;

/**
 * Created by Arthur on 12.09.2015.
 *
 */
public class PostingMapFragment extends SupportMapFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {
    private static final String KEY_LOCATION_ZOOM_DONE = "KEY_LOCATION_ZOOM_DONE";
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 101;
    private GoogleMap fGoogleMap;
    private ClusterManager<MarkerWrapper> clusterManager;
    private HashMap<String, MarkerWrapper> fIdToMarkerWrapper = new HashMap<>();
    private View fCreateButton;
    private Location fLocation;
    private boolean fLocationZoomDone = false;
    private GoogleApiClient fGoogleApiClient;
    private GeoFire fGeoFire;
    private GeoQuery fGeoQuery;
    private GeoQueryEventListener fGeoQueryEventListener;
    private DatabaseReference postingListRef;
    private PostingDataListener postingListener;
    private boolean fIsStarted;
    private CachedBitmapLoader bitmapLoader;
    private Circle fCircle;
    private Cluster<MarkerWrapper> lastClusterClick;
    private MarkerWrapper lastMarkerWrapperClick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.bitmapLoader = new CachedBitmapLoader(getContext(), BitmapType.THUMBNAIL);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(FlypostrConstants.ROOT_GEOFIRE);

        // TODO: use PostingDetailLoader
        this.postingListRef = FirebaseDatabase.getInstance().getReference("postings");
        postingListener = new PostingDataListener();
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
            Intent createParams = new Intent();
            fCreateButton.setOnClickListener(new AuthenticateClickListener(getActivity(), AuthUtil.REQUEST_CODE_ADD_POSTING, createParams));
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

        clusterManager = new ClusterManager<>(getContext(), fGoogleMap);
        clusterManager.setRenderer(new PostingClusterRenderer(getContext(), fGoogleMap, clusterManager));
        clusterManager.getClusterMarkerCollection().setOnInfoWindowAdapter(new PoiClusterInfoWindowAdapter());
        clusterManager.getMarkerCollection().setOnInfoWindowAdapter(new PoiInfoWindowAdapter());
        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MarkerWrapper>() {
            @Override
            public boolean onClusterClick(Cluster<MarkerWrapper> cluster) {
                lastClusterClick = cluster;
                return true;
            }
        });
        clusterManager.getClusterMarkerCollection().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        });
        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerWrapper>() {
            @Override
            public boolean onClusterItemClick(MarkerWrapper markerWrapper) {
                lastMarkerWrapperClick = markerWrapper;
                return true;
            }
        });

        fGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        fGoogleMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
        fGoogleMap.setOnCameraIdleListener(clusterManager);

        UiSettings setting = fGoogleMap.getUiSettings();
        setting.setMapToolbarEnabled(false);
        setting.setMyLocationButtonEnabled(true);

        updateGeoQuery();
    }

    private LatLng toLatLng(Location location) {
        LatLng result = new LatLng(location.getLatitude(), location.getLongitude());
        return result;
    }

    private void updateGeoQuery() {
        if (fGoogleMap != null && clusterManager != null && fLocation != null && fIsStarted) {
            if (fGeoQuery == null) {
                GeoLocation geoLoc = new GeoLocation(fLocation.getLatitude(), fLocation.getLongitude());
                // creates a new query around fLocation with a maximum radius
                fGeoQuery = fGeoFire.queryAtLocation(geoLoc, GEO_QUERY_RADIUS_IN_KILOMETER);
                // we also add a circle to the map
                CircleOptions circleOptions = new CircleOptions();
                circleOptions.center(toLatLng(fLocation));
                circleOptions.radius(GEO_QUERY_RADIUS_IN_KILOMETER * 1000);
                circleOptions.strokeColor(ContextCompat.getColor(getContext(), R.color.geoquery_circle));
                fCircle = fGoogleMap.addCircle(circleOptions);

                fGeoQueryEventListener = new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        addMarker(location.latitude, location.longitude, key);
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
        if (!this.fIsStarted && fGeoQuery != null) {
            // the fragment stopped, remove listeners
            fGeoQuery.removeGeoQueryEventListener(fGeoQueryEventListener);
            fGeoQuery = null;
            // remove all markers and data listeners
            for (MarkerWrapper markerWrapper : fIdToMarkerWrapper.values()) {
                removeMarker(markerWrapper.postingID);
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
                } else {
                    item.setChecked(true);
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
        fIsStarted = true;
        fGoogleApiClient.connect();
        updateGeoQuery();

    }

    @Override
    public void onStop() {
        super.onStop();
        fIsStarted = false;
        if (fGoogleApiClient.isConnected()) {
            fGoogleApiClient.disconnect();
        }
        updateGeoQuery();
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
/* TODO refactor for cluster
    private boolean onClick(Marker marker) {
        MarkerWrapper wrapper = fMarkerToMakerWrapper.get(marker);
        if (wrapper != null) {
            PostingDetailActivity.start(getActivity(), wrapper.postingID);
        }
        return true;
    }
*/
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

    private void addMarker(double latitude, double longitude, String id) {
        LatLng loc = new LatLng(latitude, longitude);
        MarkerWrapper wrapper = new MarkerWrapper(loc, id);
        clusterManager.addItem(wrapper);
        fIdToMarkerWrapper.put(id, wrapper);

        // add the data to the marker
        DatabaseReference postingRef = postingListRef.child(id);
        postingRef.addValueEventListener(this.postingListener);
        wrapper.postingRef = postingRef;
    }

    private void removeMarker(String key) {
        MarkerWrapper markerWrapper = fIdToMarkerWrapper.get(key);
        if (markerWrapper != null) {
            fIdToMarkerWrapper.remove(key);
            markerWrapper.postingRef.removeEventListener(this.postingListener);
            clusterManager.removeItem(markerWrapper);
        }
    }

    private void moveMarker(String key, double latitude, double longitude) {
        MarkerWrapper markerWrapper = fIdToMarkerWrapper.get(key);
        if (markerWrapper != null) {
            LatLng newLatLng = new LatLng(latitude, longitude);
            markerWrapper.setPosition(newLatLng);
        }
    }


    private class PostingDataListener implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            MarkerWrapper markerWrapper = PostingMapFragment.this.fIdToMarkerWrapper.get(key);
            if (markerWrapper == null) {
                return;
            }
            if (dataSnapshot.exists()) {
                PostingBean bean = dataSnapshot.getValue(PostingBean.class);
                PostingWrapper postingWrapper = new PostingWrapper(bean);
                markerWrapper.title = postingWrapper.getTitle();
                markerWrapper.snippet = postingWrapper.getSnippet();
                // add thumbnail
                // add the image to the marker
                String imageId = bean.imageId;
                if (imageId != null) {
                    ImageDataListener listener = new ImageDataListener(markerWrapper);
                    bitmapLoader.load(imageId, listener);
                }
            }
            else {
                // delete
                removeMarker(key);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    }

    private class ImageDataListener implements BitmapLoaderCallback {
        private final MarkerWrapper markerWrapper;

        public ImageDataListener(MarkerWrapper markerWrapper) {
            this.markerWrapper = markerWrapper;
        }

        @Override
        public void onError(Throwable e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onBitmapProgress(String filename, int progressPercent, String progressText) {
            // we don't show the progress here
        }

        @Override
        public void onBitmapLoaded(String filename, Bitmap bitmap) {
            markerWrapper.bitmap = bitmap;
        }
    }

     public class PoiInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private View fView;
        private ImageView fImage;
        private TextView fName;
        private TextView fDescription;

        public PoiInfoWindowAdapter() {
        }

         @Override
         public View getInfoWindow(Marker marker) {
             if (fView == null) {
                 fView = getLayoutInflater(null).inflate(R.layout.map_info_window, null);
                 fImage = (ImageView)fView.findViewById(R.id.imv_thumbnail);
                 fName = (TextView)fView.findViewById(R.id.txv_poi_name);
                 fDescription = (TextView)fView.findViewById(R.id.txv_description);
             }
             return fView;
         }

         public View getInfoContents(Marker marker) {
            if (lastMarkerWrapperClick == null) {
                return null;
            }
            Bitmap bitmap = lastMarkerWrapperClick.bitmap;

            fImage.setImageBitmap(bitmap);
            fName.setText(lastMarkerWrapperClick.title);
            String snippet = lastMarkerWrapperClick.snippet;
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

    private class PoiClusterInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private View fView;

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            if (lastMarkerWrapperClick == null) {
                return null;
            }

            if (fView == null) {
                fView = getLayoutInflater(null).inflate(R.layout.map_clusterinfo_window, null);
            }
            return fView;
        }
    }
}
