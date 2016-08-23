package li.itcc.flypostr.postingList;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;

import li.itcc.flypostr.R;
import li.itcc.flypostr.model.PostingWrapper;
import li.itcc.flypostr.model.image.BitmapLoaderCallback;
import li.itcc.flypostr.postingDetail.PostingDetailLoader;
import li.itcc.flypostr.util.FormatHelper;
import li.itcc.flypostr.util.ImageLoader;
import li.itcc.flypostr.util.SquareImageView;


/**
 * Created by sandro.pedrett on 21.08.2016.
 */

// TODO: remove ImageLoader, use CachedBitmapLoader instead, it uses asynchronous file reading

public class PostingListAdapter extends RecyclerView.Adapter<PostingListAdapter.ViewHolder> implements GeoFireListManager.GeoFireListener {
    private ArrayList<PostingItemWrapper> dataList;
    private Context context;
    private GeoFireListManager geoFireManager;
    private View.OnClickListener viewListener;
    private Location lastMyLocation;


    public PostingListAdapter(Context context, View.OnClickListener viewListener) {
        this.context = context;
        this.geoFireManager = new GeoFireListManager(this);
        this.dataList = new ArrayList<>();
        this.viewListener = viewListener;
    }

    public void connect() {
        geoFireManager.initializeGeoFire();
        geoFireManager.searchNewLocations();
    }

    public void disconnect() {
        geoFireManager.detach();
    }

    public void setLocation(Location location) {
        lastMyLocation = location;
        if (location != null) {
            GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
            geoFireManager.setLastLocation(geoLocation);
        }
    }

    @Override
    public void onLocationFound(String id, LatLng location) {
        PostingItemWrapper item = new PostingItemWrapper(id);
        item.location = location;
        item.setDistance(location, lastMyLocation);

        dataList.add(item);

        Collections.sort(dataList);

        // update ui
        notifyDataSetChanged();
    }

    @Override
    public void onLocationRemoved(String id) {
        PostingItemWrapper item = new PostingItemWrapper(id);
        dataList.remove(item);
    }

    @Override
    public void onSearchFinish() {
        // TODO implement a progressbar in actionbar -> stop this here
    }

    @Override
    public void onConnectionFailed(String resultMsg) {
        Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show();
    }

    public PostingItemWrapper getItem(int position) {
        return dataList.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements PostingDetailLoader.PostingDetailLoaderCallback, BitmapLoaderCallback {
        public TextView name;
        public TextView description;
        public TextView distance;
        public SquareImageView image;
        public String currentId;
        public Bitmap bitmap;
        public ProgressBar progressBar;
        public TextView progressText;

        private PostingDetailLoader loader;
        private ImageLoader imageLoader;
        private int position;
        private boolean isLoadingImage;

        public ViewHolder(View view) {
            super(view);
            loader = new PostingDetailLoader(context);
            imageLoader = new ImageLoader(context, ImageLoader.ImageCacheType.IMAGES);
            image = (SquareImageView)view.findViewById(R.id.imv_thumbnail);
            name = (TextView) view.findViewById(R.id.txv_posting_name);
            description = (TextView) view.findViewById(R.id.txv_posting_description);
            distance = (TextView) view.findViewById(R.id.txv_posting_distance);
            progressBar = (ProgressBar)view.findViewById(R.id.prg_list_item_progressLoading);
            progressBar.setMax(100);
            progressBar.setVisibility(View.GONE);
            progressText = (TextView)view.findViewById(R.id.txv_list_item_progressText);
            progressText.setVisibility(View.GONE);
        }

        public void load(String id, int position) {
            this.position = position;
            if (id != currentId) {
                currentId = id;
                bitmap = null;
                loader.detach();
                imageLoader.cancel();
                loader.load(id, this);
            }
        }

        @Override
        public void onPostingChanged(PostingWrapper posting) {
            PostingItemWrapper item = dataList.get(position);
            item.posting = posting;

            if (imageLoader.isInProgress()) {
                this.imageLoader.cancel();
                this.isLoadingImage = false;
            }
            String imageId = posting.getImageId();
            if (imageId != null) {
                this.isLoadingImage = true;
                this.imageLoader.startProgress(imageId, this);
            }
            // notify ui update
            notifyDataSetChanged();
        }

        @Override
        public void onPostingDeleted(String id) {
            PostingListAdapter.this.onLocationRemoved(id);
        }


        @Override
        public void onBitmapLoaded(String filename, Bitmap bitmap) {
            this.bitmap = bitmap;
            this.isLoadingImage = false;
            notifyDataSetChanged();
        }

        @Override
        public void onBitmapProgress(String filename, int progressPercent, String progressText) {
            this.progressText.setText(progressText);
            this.progressBar.setProgress(progressPercent);
        }

        @Override
        public void onError(Throwable e) {
            // TODO: implement error callback
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.posting_list_row, parent, false);
        view.setOnClickListener(viewListener);

        PostingListAdapter.ViewHolder vh = new PostingListAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(PostingListAdapter.ViewHolder holder, int position) {
        PostingItemWrapper item = dataList.get(position);

        if (item == null) {
            Log.w("FlyPostr", "Item at position " + position + " are not in datalist");
            return;
        }

        // load posting
        holder.load(item.id, position);
        if (item.posting != null) {
            // bind data
            holder.name.setText(item.posting.getTitle());
            holder.description.setText(item.posting.getText());
            holder.distance.setText(FormatHelper.convertToDistance(item.distanceToCurrentLocationInMeter));
        }
        // update progress visibility
        if (holder.isLoadingImage) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressText.setVisibility(View.VISIBLE);
        }
        else {
            holder.progressBar.setProgress(0);
            holder.progressText.setText("");
            holder.progressBar.setVisibility(View.GONE);
            holder.progressText.setVisibility(View.GONE);
        }


        holder.image.setImageBitmap(holder.bitmap);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
