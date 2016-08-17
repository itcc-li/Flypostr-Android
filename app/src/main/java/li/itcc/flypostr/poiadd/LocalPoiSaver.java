package li.itcc.flypostr.poiadd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.util.Log;

import li.itcc.flypostr.backend.PoiCreateBean;
import li.itcc.flypostr.database.PoiDBOpenHelper;
import li.itcc.flypostr.database.tables.UploadTable;
import li.itcc.flypostr.util.StreamUtil;

/**
 * Created by Arthur on 25.09.2015.
 */
public class LocalPoiSaver {

    private final Context fContext;

    public LocalPoiSaver(Context context) {
        fContext = context;
    }

    public void save(PoiCreateBean detail, File localImageFile) {
        new SaveLocalPoiTask(detail, localImageFile).execute((Void)null);
    }

    private class SaveLocalPoiTask extends AsyncTask<Void, Void, Void> {

        private final PoiCreateBean fDetail;
        private final File fLocalImageFile;

        public SaveLocalPoiTask(PoiCreateBean detail, File localImageFile) {
            fDetail = detail;
            fLocalImageFile = localImageFile;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // first insert record into database;
            SQLiteDatabase db = null;
            try {
                PoiDBOpenHelper dbOpenHelper = new PoiDBOpenHelper(fContext);
                db = dbOpenHelper.getWritableDatabase();
                insertRecord(db);
            }
            catch (Exception x) {
                // not cool, we are in charge of the data, they are lost now...
                Log.e("STORE", "Store failed", x);
            }
            finally {
                if (db != null) {
                    db.close();
                }
            }
            return null;
        }

        private void insertRecord(SQLiteDatabase db) throws IOException {
            db.beginTransaction();
            try {
                SQLiteStatement stmt = UploadTable.createInsertStatement(db);
                UploadTable.bindToInsertStatement(stmt, fDetail);
                String uuid = fDetail.getUuid();
                if (stmt.executeInsert() == -1L) {
                    throw new IOException("can not insert");
                }
                stmt.close();
                // now we have an id, copy the image file to private storage until upload finished
                String fileName = null;
                if (fLocalImageFile != null && fLocalImageFile.canRead()) {
                    fileName = getFileName(uuid);
                    OutputStream out = fContext.openFileOutput(fileName, Context.MODE_PRIVATE);
                    InputStream in = new FileInputStream(fLocalImageFile);
                    StreamUtil.pumpAllAndClose(in, out);
                }
                stmt = UploadTable.createUpdateStatement(db);
                UploadTable.bindToUpdateStatement(stmt, fDetail.getUuid(), fileName);
                stmt.executeUpdateDelete();
                stmt.close();
                db.setTransactionSuccessful();
            }
            finally {
                db.endTransaction();
            }
        }

        private String getFileName(String uuid) {
            return "upload_" + uuid;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // immediately start an upload
            UploaderTask task = new UploaderTask(fContext);
            task.execute((Void)null);
        }
    }
}
