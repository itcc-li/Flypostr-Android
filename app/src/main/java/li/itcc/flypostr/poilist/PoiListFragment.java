package li.itcc.flypostr.poilist;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.RuntimeExecutionException;

import java.text.DecimalFormat;

import li.itcc.flypostr.R;
import li.itcc.flypostr.TitleHolder;
import li.itcc.flypostr.auth.AuthUtil;
import li.itcc.flypostr.auth.AuthenticateClickListener;
import li.itcc.flypostr.postingDetail.PostingDetailActivity;
import li.itcc.flypostr.util.ThumbnailCache;


/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiListFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private DecimalFormat FORMAT_0 = new DecimalFormat("#,##0");
    private DecimalFormat FORMAT_1 = new DecimalFormat("#,##0.0");
    private PoiCursorAdapter fDataAdapter;
    private ListView fListView;
    private TextView fEmptyText;
    private View fCreateButton;
    private Location fLocation;
    private ThumbnailCache fThumbnailCache;
    private ProgressBar fProgressBar;
    private GoogleApiClient fGoogleApiClient;

    public PoiListFragment() {
    }

    //@Override
    //public void onAttach(Activity activity) {
    //    super.onAttach(activity);
    //    setHasOptionsMenu(true);
    //}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();
        View rootView = inflater.inflate(R.layout.poi_list_fragment, container, false);
        fThumbnailCache = new ThumbnailCache(getContext());
        fListView = (ListView) rootView.findViewById(android.R.id.list);
        fEmptyText = (TextView) rootView.findViewById(android.R.id.empty);
        fCreateButton = rootView.findViewById(R.id.viw_add_button);
        fCreateButton.setOnClickListener(new AuthenticateClickListener(activity, AuthUtil.REQUEST_CODE_ADD_POSTING));
        fProgressBar = (ProgressBar) rootView.findViewById(R.id.prb_progress);
        fProgressBar.setMax(100);
        fProgressBar.setVisibility(View.GONE);
        fDataAdapter = new PoiCursorAdapter(activity);
        fListView.setAdapter(fDataAdapter);
        fListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(position, id);
            }
        });
        updateTableVisibility();
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TitleHolder) {
            ((TitleHolder) context).setTitleId(R.string.title_poi_list);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
        // start loading
        buildGoogleApiClient();

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

    private void onListItemClick(int position, long id) {
        Cursor c = (Cursor) fDataAdapter.getItem(position);
        String poiId = ""; // TODO
        PostingDetailActivity.start(getActivity(), poiId);
    }

    private synchronized void buildGoogleApiClient() {
        Context context = getContext();
        fGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.poi_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void updateTableVisibility() {
        if (fDataAdapter.getCount() == 0) {
            fEmptyText.setVisibility(View.VISIBLE);
            fListView.setVisibility(View.GONE);
        } else {
            fEmptyText.setVisibility(View.GONE);
            fListView.setVisibility(View.VISIBLE);
        }
    }


    public void setLocation(Location location) {
        if (location == null) {
            return;
        }
        fLocation = location;
        fDataAdapter.notifyDataSetChanged();
    }

    // google api callbacks

    @Override
    public void onConnected(Bundle bundle) {
        try {
            Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(fGoogleApiClient);
            setLocation(lastKnownLocation);
        }
        catch (SecurityException x) {
            throw new RuntimeExecutionException(x);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private class PoiCursorAdapter extends ResourceCursorAdapter {
        private int POI_NAME_INDEX;
        private int POI_SHORT_DESCRIPTION;
        private int POI_LATITUDE_INDEX;
        private int POI_LONGITUDE_INDEX;
        private int POI_ID_INDEX;

        public PoiCursorAdapter(Context context) {
            super(context, R.layout.poi_list_row, null, FLAG_REGISTER_CONTENT_OBSERVER);
            int count = 0;
            POI_NAME_INDEX = count++;
            POI_SHORT_DESCRIPTION = count++;
            POI_LATITUDE_INDEX = count++;
            POI_LONGITUDE_INDEX = count++;
            POI_ID_INDEX = count++;
        }


        // TODO

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // name
            TextView name = (TextView) view.findViewById(R.id.txv_poi_name);
            name.setText("");
            // short description
            TextView description = (TextView) view.findViewById(R.id.txv_description);
            description.setText("");
            // distance
            TextView distance = (TextView) view.findViewById(R.id.txv_poi_distance);
            if (fLocation != null) {
                double latitude = 10;
                double longitude = 10;
                Location pointLocation = new Location("");
                pointLocation.setLatitude(latitude);
                pointLocation.setLongitude(longitude);
                float meters = fLocation.distanceTo(pointLocation);
                String distanceText;
                if (meters < 1000.0f) {
                    distanceText = FORMAT_0.format(meters) + " m";
                } else {
                    float km = meters / 1000f;
                    distanceText = FORMAT_1.format(km) + " km";
                }
                distance.setText(distanceText);
            } else {
                distance.setText("");
            }
            // thumb
            ImageView imageView = (ImageView) view.findViewById(R.id.imv_thumbnail);
            imageView.setImageBitmap(null); // TODO
        }
    }
}

