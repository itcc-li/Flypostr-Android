package li.itcc.flypostr.postingMap;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by sandro.pedrett on 29.08.2016.
 */

public class PostingClusterRenderer extends DefaultClusterRenderer<MarkerWrapper> {
    public PostingClusterRenderer(Context context, GoogleMap map, ClusterManager<MarkerWrapper> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<MarkerWrapper> cluster) {
        return cluster.getSize() > 1;
    }
}
