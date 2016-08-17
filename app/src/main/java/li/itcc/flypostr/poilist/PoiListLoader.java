package li.itcc.flypostr.poilist;

import android.content.Context;

import li.itcc.flypostr.config.CloudEndpoint;
import li.itcc.flypostr.backend.PoiOverviewListBean;
import li.itcc.flypostr.database.PoiTableUpdater;
import li.itcc.flypostr.services.PoiOverviewQuery;
import li.itcc.flypostr.services.PoiServices;
import li.itcc.flypostr.util.loading.GenericTask;
import li.itcc.flypostr.util.loading.TaskExecutionListener;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiListLoader {
    private final Context fContext;
    private final PoiListLoaderListener fListener;

    public interface PoiListLoaderListener extends TaskExecutionListener<PoiOverviewListBean> {
    }

    public PoiListLoader(Context context, PoiListLoaderListener listener) {
        fContext = context;
        fListener = listener;
    }

    public void refresh() {
        new RefreshTask(fListener).execute();
    }

    private class RefreshTask extends GenericTask<Void, PoiOverviewListBean> {
        private Throwable fException;

        public RefreshTask(PoiListLoaderListener listener) {
            super(listener);
        }

        @Override
        protected PoiOverviewListBean doInBackgroundOrThrow(Void... params) throws Exception {
            PoiServices poiServices = new PoiServices(fContext, CloudEndpoint.URL);
            PoiOverviewQuery q = new PoiOverviewQuery();
            PoiOverviewListBean listBean = poiServices.getPoiList(q);
            onTaskProgress(70);
            PoiTableUpdater poiTableUpdater = new PoiTableUpdater(fContext);
            poiTableUpdater.updatePoiTable(this, listBean);
            return listBean;
        }

    }
}
