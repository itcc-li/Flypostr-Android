package li.itcc.flypostr.postingList;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.RecoverySystem;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;

import li.itcc.flypostr.R;
import li.itcc.flypostr.guiutil.SquareImageView;
import li.itcc.flypostr.model.PostingWrapper;
import li.itcc.flypostr.postingDetail.PostingDetailLoader;
import li.itcc.flypostr.util.ImageLoader;

/**
 * Created by sandro.pedrett on 21.08.2016.
 */
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

    public class ViewHolder extends RecyclerView.ViewHolder implements PostingDetailLoader.PostingDetailLoaderCallback, ImageLoader.ImageLoaderCallback {
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

        public ViewHolder(View view) {
            super(view);
            loader = new PostingDetailLoader(context);
            imageLoader = new ImageLoader(context, ImageLoader.ImageCacheType.IMAGES);
            image = (SquareImageView)view.findViewById(R.id.imv_thumbnail);
            name = (TextView) view.findViewById(R.id.txv_posting_name);
            description = (TextView) view.findViewById(R.id.txv_posting_description);
            distance = (TextView) view.findViewById(R.id.txv_posting_distance);
            progressBar = (ProgressBar)view.findViewById(R.id.prg_list_item_progressLoading);
            progressText = (TextView)view.findViewById(R.id.txv_list_item_progressText);
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
                imageLoader.cancel();
            }
            imageLoader.startProgress(posting.getImageId(), this);

            // notify ui update
            notifyDataSetChanged();
        }

        @Override
        public void onImageLoaded(String filename, Bitmap bitmap) {
            this.bitmap = bitmap;
            notifyDataSetChanged();
        }

        @Override
        public void onUpdateProgressDownload(String filename, long bytesReceived, long totalByteCount) {
            if (totalByteCount >= 0) {
                float progress = ((bytesReceived / (float)totalByteCount) * 10.0f);
                try {
                    progressText.setText(String.format(context.getResources().getConfiguration().locale, "%1$.0f%%", progress)); // Example: 10.215 -> "10%"
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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

        holder.progressBar.setVisibility(View.VISIBLE);

        if (item.posting != null) {
            holder.name.setText(item.posting.getTitle());
            holder.description.setText(item.posting.getText());
            holder.distance.setText(String.format(context.getResources().getConfiguration().locale, "Distance: %1$.1fm", item.distanceToCurrentLocatoin));
        }

        // disable/enable progressbar
        if (holder.bitmap != null) {
            holder.progressBar.setVisibility(View.GONE);
        } else {
            holder.progressBar.setVisibility(View.VISIBLE);
        }

        holder.image.setImageBitmap(holder.bitmap);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}