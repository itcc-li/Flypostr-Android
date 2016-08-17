package li.itcc.flypostr.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Base64;

import java.util.List;

import li.itcc.flypostr.backend.PoiOverviewBean;
import li.itcc.flypostr.backend.PoiOverviewListBean;
import li.itcc.flypostr.database.tables.PoiOverviewTable;
import li.itcc.flypostr.util.ThumbnailCache;
import li.itcc.flypostr.util.loading.TaskProgressListener;

/**
 * Created by patrik on 12/09/2015.
 */
public class PoiTableUpdater  {
    private final Context fContext;
    private final ThumbnailCache fCache;

    public PoiTableUpdater(Context context) {
        fContext = context;
        fCache = new ThumbnailCache(context);
    }

    public void updatePoiTable(TaskProgressListener listener, PoiOverviewListBean listBean) throws Exception {
        PoiDBOpenHelper helper = new PoiDBOpenHelper(fContext);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            PoiOverviewTable.deleteAllEntries(db);
            fCache.deleteAllThumbnails();
            listener.onTaskProgress(80);
            List<PoiOverviewBean> list = listBean.getList();
            if (list != null) {
                SQLiteStatement stmt = PoiOverviewTable.createInsertStatement(db);
                int insertCount = listBean.getList().size();
                int doneInserts = 0;
                for (PoiOverviewBean poi : listBean.getList()) {
                    PoiOverviewTable.bindToInsertStatement(stmt, poi);
                    stmt.executeInsert();
                    String thumbnail = poi.getThumbnailBase64();
                    if (thumbnail != null && thumbnail.length() > 0) {
                        byte[] thumbnailData = android.util.Base64.decode(thumbnail, Base64.DEFAULT);
                        fCache.storeBitmap(poi.getUuid(), thumbnailData);
                    }
                    doneInserts++;
                    int progress = 80 * (20 * doneInserts / insertCount);
                    listener.onTaskProgress(progress);
                }
                stmt.close();
            }
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
            db.close();
            fContext.getContentResolver().notifyChange(DatabaseContract.Pois.CONTENT_URI, null);
        }
    }

}
