package li.itcc.flypostr.poidetail;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.os.OperationCanceledException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import li.itcc.flypostr.backend.PoiDetailBean;
import li.itcc.flypostr.backend.PoiOverviewBean;
import li.itcc.flypostr.config.CloudEndpoint;
import li.itcc.flypostr.database.PoiDBOpenHelper;
import li.itcc.flypostr.database.tables.PoiDetailTable;
import li.itcc.flypostr.database.tables.PoiOverviewTable;
import li.itcc.flypostr.services.PoiServices;
import li.itcc.flypostr.util.StreamUtil;
import li.itcc.flypostr.util.ThumbnailCache;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiDetailLoader extends AsyncTaskLoader<PoiDetailBean> {
    private static final String KEY_UUID = "KEY_UUID";
    private final Bundle fArgs;

    private PoiDetailBean fResult;

    public static Bundle createArgs(String id) {
        Bundle result = new Bundle();
        result.putString(KEY_UUID, id);
        return result;
    }


    public PoiDetailLoader(Context context, int id, Bundle args) {
        super(context);
        fArgs = args;
    }

    @Override
    public PoiDetailBean loadInBackground() {
        try {
            Context context = getContext();
            String uuid = fArgs.getString(KEY_UUID);
            // see if we have all data offline available
            PoiDetailBean detail = loadFromLocalDatabase(uuid);
            if (detail == null) {
                PoiServices poiServices = new PoiServices(context, CloudEndpoint.URL);
                detail = poiServices.getPoiDetails(uuid);
                storeToLocalDatabase(detail);
            }
            if (isLoadInBackgroundCanceled()) {
                throw new OperationCanceledException();
            }
            // load image if available
            String imageUrl = detail.getImageUrl();
            if (imageUrl != null) {
                ImageStore store = new ImageStore(context);
                ImageStore.Key key = store.createKey(detail.getOverview().getUuid(), detail.getImageUpdateTime());
                // check if download of image is needed
                if (!store.exists(key)) {
                    // patch host for testing
                    imageUrl = CloudEndpoint.dnsHack(imageUrl);
                    InputStream in = new URL(imageUrl).openStream();
                    OutputStream out = store.createImage(key);
                    StreamUtil.pumpAllAndClose(in, out);
                }
            }
            return detail;
        } catch (Exception x) {
            x.printStackTrace();
        }
        return null;
    }

    private void storeToLocalDatabase(PoiDetailBean detail) {
        PoiDBOpenHelper helper = new PoiDBOpenHelper(getContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            String uuid = detail.getOverview().getUuid();
            PoiDetailTable.deleteDetail(db, uuid);
            PoiOverviewTable.deleteOverview(db, uuid);
            PoiDetailTable.insert(db, detail);
            PoiOverviewTable.insert(db, detail.getOverview());
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
            db.close();
        }
    }

    private PoiDetailBean loadFromLocalDatabase(String uuid) {
        PoiDBOpenHelper helper = new PoiDBOpenHelper(getContext());
        SQLiteDatabase db = helper.getReadableDatabase();
        try {
            PoiDetailBean result = PoiDetailTable.loadDetailBean(db, uuid);
            if (result != null) {
                PoiOverviewBean overviewBean = PoiOverviewTable.loadOverviewBean(db, uuid);
                if (overviewBean != null) {
                    result.setOverview(overviewBean);
                    // check thumb
                    ThumbnailCache cache = new ThumbnailCache(getContext());
                    if (cache.existsBitmap(uuid)) {
                        return result;
                    }
                }
            }
        }
        finally {
            db.close();
        }
        return null;
    }

    @Override
    public void deliverResult(PoiDetailBean data) {
        // called on ui thread
        if (isReset()) {
            return;
        }
        PoiDetailBean oldData = fResult;
        fResult = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (fResult != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(fResult);
        }

        if (takeContentChanged() || fResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        if (fResult != null) {
            fResult = null;
        }
    }



}
