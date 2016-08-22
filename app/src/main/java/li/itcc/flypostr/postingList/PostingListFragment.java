package li.itcc.flypostr.postingList;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import li.itcc.flypostr.R;
import li.itcc.flypostr.TitleHolder;
import li.itcc.flypostr.auth.AuthUtil;
import li.itcc.flypostr.auth.AuthenticateClickListener;
import li.itcc.flypostr.postingDetail.PostingDetailActivity;


/**
 * Created by sandro.pedrett on 21.08.2016.
 */
public class PostingListFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,  View.OnClickListener {
    private static final int PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 101;
    private PostingListAdapter dataAdapter;
    private RecyclerView listView;
    private TextView emptyText;
    private View createButton;
    private GoogleApiClient googleApiClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();
        View rootView = inflater.inflate(R.layout.posting_list_fragment, container, false);

        listView = (RecyclerView) rootView.findViewById(android.R.id.list);
        emptyText = (TextView) rootView.findViewById(android.R.id.empty);
        createButton = rootView.findViewById(R.id.viw_add_button);
        Intent createParams = new Intent();
        createButton.setOnClickListener(new AuthenticateClickListener(activity, AuthUtil.REQUEST_CODE_ADD_POSTING, createParams));

        // list adapter
        dataAdapter = new PostingListAdapter(activity, this);
        listView.setLayoutManager(new LinearLayoutManager(activity));
        listView.setAdapter(dataAdapter);

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
    public void onStart() {
        super.onStart();
        buildGoogleApiClient();
        googleApiClient.connect();
    }

    private synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }

        dataAdapter.disconnect();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
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
            if (googleApiClient.isConnected()) {
                Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                updateLocations(lastKnownLocation);
                updateTableVisibility();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkAndGetLocation(false);
    }


    private void updateLocations(Location location) {
        // update locations if connected
        if (googleApiClient != null && googleApiClient.isConnected()) {
            dataAdapter.setLocation(location);
            dataAdapter.connect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        updateTableVisibility();
    }

    private void updateTableVisibility() {
        if (!googleApiClient.isConnected()) {
            emptyText.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // nothing do to
    }

    @Override
    public void onClick(View view) {
        int position = listView.getChildLayoutPosition(view);
        PostingItemWrapper item = dataAdapter.getItem(position);
        PostingDetailActivity.start(getActivity(), item.id);
    }
}

