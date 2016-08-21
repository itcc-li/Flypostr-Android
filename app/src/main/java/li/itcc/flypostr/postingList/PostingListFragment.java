package li.itcc.flypostr.postingList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.RuntimeExecutionException;

import li.itcc.flypostr.R;
import li.itcc.flypostr.TitleHolder;
import li.itcc.flypostr.auth.AuthUtil;
import li.itcc.flypostr.auth.AuthenticateClickListener;
import li.itcc.flypostr.postingDetail.PostingDetailActivity;


/**
 * Created by sandro.pedrett on 21.08.2016.
 */
public class PostingListFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,  View.OnClickListener {
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

    @Override
    public void onConnected(Bundle bundle) {
        try {
            // TODO: update lastLocation e.g. periodic timer
            Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            updateLocations(lastKnownLocation);

            updateTableVisibility();
        } catch (SecurityException x) {
            throw new RuntimeExecutionException(x);
        }
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

