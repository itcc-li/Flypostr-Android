package li.itcc.flypostr.poimap;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.util.HashMap;

import li.itcc.flypostr.R;
import li.itcc.flypostr.TitleHolder;
import li.itcc.flypostr.database.DatabaseContract;
import li.itcc.flypostr.database.tables.PoiOverviewTable;
import li.itcc.flypostr.poiadd.PoiAddOnClickListener;
import li.itcc.flypostr.poidetail.PoiDetailActivity;
import li.itcc.flypostr.util.ThumbnailCache;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiMapFragment extends SupportMapFragment implements LoaderManager.LoaderCallbacks<Cursor>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {
    private static final String KEY_LOCATION_ZOOM_DONE = "KEY_LOCATION_ZOOM_DONE";
    private GoogleMap fGoogleMap;
    private HashMap<Marker, String> fMarkers = new HashMap<>();
    private View fCreateButton;
    private Location fLocation;
    private boolean fLocationZoomDone = false;
    private GoogleApiClient fGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
            FrameLayout frame = (FrameLayout)rootView.findViewById(R.id.frame_layout);
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
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TitleHolder) {
            ((TitleHolder)context).setTitleId(R.string.title_overview_map);
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
        }
        else if (item.getItemId() == R.id.action_my_location) {
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
        getLoaderManager().initLoader(0, null, this);
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
        String id = fMarkers.get(marker);
        if (id != null) {
            PoiDetailActivity.start(getActivity(), id);
        }
        return true;
    }

    // google api client

    @Override
    public void onConnected(Bundle bundle) {
        try {
            Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(fGoogleApiClient);
            setLocation(lastKnownLocation);
        }
        catch (SecurityException x) {
        }
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
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = null;
        String where = null;
        String[] whereArgs = null;
        String sortOrder = null;
        Uri queryUri = DatabaseContract.Pois.CONTENT_URI;
        CursorLoader loader = new CursorLoader(getActivity(), queryUri, projection, where, whereArgs, sortOrder);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        clearAllMarkers();
        if (data == null || fGoogleMap == null) {
            return;
        }
        int longitudeCol = data.getColumnIndex(PoiOverviewTable.COL_LONGITUDE);
        int latitudeCol = data.getColumnIndex(PoiOverviewTable.COL_LATITUDE);
        int nameCol = data.getColumnIndex(PoiOverviewTable.COL_NAME);
        int descrCol = data.getColumnIndex(PoiOverviewTable.COL_SHORT_DESCRIPTION);
        int idCol = data.getColumnIndex(PoiOverviewTable.COL_ID);
        if (data.moveToFirst()) {
            do {
                double longitude = data.getDouble(longitudeCol);
                double latitude = data.getDouble(latitudeCol);
                String name = data.getString(nameCol);
                String shortDescr = data.getString(descrCol);
                String id = data.getString(idCol);
                LatLng loc = new LatLng(latitude, longitude);
                MarkerOptions options = new MarkerOptions();
                options.position(loc).draggable(true).title(name).snippet(shortDescr);
                //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_48dp));
                Marker marker = fGoogleMap.addMarker(options);
                fMarkers.put(marker, id);
            } while (data.moveToNext());

        }
    }

    private void clearAllMarkers() {
        //for (Marker marker : fMarkers.keySet()) {
        //    marker.remove();
        //}
        if (fGoogleMap != null) {
            fGoogleMap.clear();
        }
        fMarkers.clear();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
            String id = fMarkers.get(marker);
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
