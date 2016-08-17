package li.itcc.flypostr.poiadd;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.InputStream;

import li.itcc.flypostr.backend.PoiCreateBean;
import li.itcc.flypostr.backend.PoiOverviewBean;
import li.itcc.flypostr.config.CloudEndpoint;
import li.itcc.flypostr.database.DatabaseContract;
import li.itcc.flypostr.database.PoiDBOpenHelper;
import li.itcc.flypostr.database.tables.PoiOverviewTable;
import li.itcc.flypostr.database.tables.UploadTable;
import li.itcc.flypostr.services.PoiServices;
import li.itcc.flypostr.util.ThumbnailCache;

/**
 * Created by Arthur on 12.09.2015.
 */
public class UploaderTask extends AsyncTask<Void, Void, Void> {
    private final Context fContext;

    public UploaderTask(Context context) {
      fContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        SQLiteDatabase db = null;
        try {
            // check if we have anything to upload
            PoiDBOpenHelper dbOpenHelper = new PoiDBOpenHelper(fContext);
            db = dbOpenHelper.getWritableDatabase();
            PoiServices poiServices = new PoiServices(fContext, CloudEndpoint.URL);
            while (true) {
                if (!executeUpload(db, poiServices)) {
                    break;
                }
            }
        }
        catch (Exception x) {
            Log.e("UPLOAD", "Upload failed", x);
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
        return null;
    }

    private boolean executeUpload(SQLiteDatabase db, PoiServices poiServices) throws Exception {
        db.beginTransaction();
        try {
            Cursor c = UploadTable.executeSelectUploadsStatement(db);
            if (c.getCount() == 0) {
                return false;
            }
            c.moveToFirst();
            PoiCreateBean param = UploadTable.loadBeanFromCursor(c);
            String uuid = param.getUuid();
            String fileName = UploadTable.loadBlobNameFromCursor(c);
            if (fileName != null) {
                InputStream in = fContext.openFileInput(fileName);
                if (in != null) {
                    ImageUploader uploader = new ImageUploader(poiServices);
                    // 1. upload the image file and get blob key
                    String imageBlobKey = uploader.uploadImage(in);
                    param.setImageBlobKey(imageBlobKey);
                }
            }
            // 2. insert bean to cloud
            PoiOverviewBean result = poiServices.insertPoi(param);
            // 3. insert the result into the local database
            PoiOverviewTable.insert(db, result);
            // add thumbnail
            String thumbnail = result.getThumbnailBase64();
            if (thumbnail != null && thumbnail.length() > 0) {
                byte[] thumbnailData = android.util.Base64.decode(thumbnail, Base64.DEFAULT);
                ThumbnailCache cache = new ThumbnailCache(fContext);
                cache.storeBitmap(result.getUuid(), thumbnailData);
            }
            // delete local copy
            UploadTable.executeDeleteStatement(db, uuid);
            if (fileName != null) {
                // delete local file
                fContext.deleteFile(fileName);
            }
            db.setTransactionSuccessful();
            return true;
        }
        finally {
            db.endTransaction();
            fContext.getContentResolver().notifyChange(DatabaseContract.Pois.CONTENT_URI, null);
        }
    }

}
