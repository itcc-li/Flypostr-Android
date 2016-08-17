package li.itcc.flypostr.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import li.itcc.flypostr.database.tables.PoiDetailTable;
import li.itcc.flypostr.database.tables.PoiOverviewTable;
import li.itcc.flypostr.database.tables.UploadTable;

public class PoiDBOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PoiDatabase.db";
    private static final int DATABASE_VERSION = 7;

    // SQL Statement to create a new database.

    public PoiDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when no database exists in disk and the helper class needs
    // to create a new one.
    @Override
    public void onCreate(SQLiteDatabase db) {
        PoiOverviewTable.createTable(db);
        PoiDetailTable.createTable(db);
        UploadTable.createTable(db);
    }

    // Called when there is a database version mismatch meaning that
    // the version of the database on disk needs to be upgraded to
    // the current version.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Log the version upgrade.
        Log.w("TaskDBAdapter", "Upgrading from version " +
        oldVersion + " to " +
        newVersion + ", which will destroy all old data");

        // Upgrade the existing database to conform to the new
        // version. Multiple previous versions can be handled by
        // comparing oldVersion and newVersion values.

        // The simplest case is to drop the old table and create a new one.
        PoiOverviewTable.dropTable(db);
        PoiDetailTable.dropTable(db);
        UploadTable.dropTable(db);
        // Create a new one.
        onCreate(db);
    }
}
