package li.itcc.flypostr.database.tables;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import li.itcc.flypostr.backend.PoiCreateBean;
import li.itcc.flypostr.database.DbUtil;

/**
 * Created by Arthur on 25.09.2015.
 */
public class UploadTable {
    private static final String TABLE_POI_UPLOADS = "PoiUploads";
    private static final String COL_ID = BaseColumns._ID;
    private static final String COL_NAME = "col_name";
    private static final String COL_DESCRIPTION = "col_description";
    private static final String COL_LATITUDE = "col_latitude";
    private static final String COL_LONGITUDE = "col_longitude";
    private static final String COL_EXACT_LATITUDE = "col_exact_latitude";
    private static final String COL_EXACT_LONGITUDE = "col_exact_longitude";
    private static final String COL_LOCAL_IMAGE_FILE = "col_local_image_file";

    public static void createTable(SQLiteDatabase db) {
        String sql = "create table " + TABLE_POI_UPLOADS + " (" +
                COL_ID + " string primary key, " +
                COL_NAME + " string not null, " +
                COL_DESCRIPTION + " string, " +
                COL_LATITUDE + " real not null, " +
                COL_LONGITUDE + " real not null, " +
                COL_EXACT_LATITUDE + " real, " +
                COL_EXACT_LONGITUDE + " real, " +
                COL_LOCAL_IMAGE_FILE + " string);";
        db.execSQL(sql);
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL("drop table if exists " + TABLE_POI_UPLOADS);
    }

    public static SQLiteStatement createInsertStatement(SQLiteDatabase dbWrite) {
        String sql = "insert into " + TABLE_POI_UPLOADS + "(" +
                COL_ID + "," +
                COL_NAME + "," +
                COL_DESCRIPTION + "," +
                COL_LATITUDE + "," +
                COL_LONGITUDE + "," +
                COL_EXACT_LATITUDE + "," +
                COL_EXACT_LONGITUDE +
                ") values (?,?,?,?,?,?,?)";
        return dbWrite.compileStatement(sql);
    }

    public static void bindToInsertStatement(SQLiteStatement stmt, PoiCreateBean detail) {
        stmt.bindString(1, detail.getUuid());
        stmt.bindString(2, detail.getPoiName());
        DbUtil.bindStringOrNull(3, stmt, detail.getPoiDescription());
        stmt.bindDouble(4, detail.getLatitude());
        stmt.bindDouble(5, detail.getLongitude());
        DbUtil.bindDoubleOrNull(6, stmt, detail.getExactLatitude());
        DbUtil.bindDoubleOrNull(7, stmt, detail.getExactLongitude());
    }

    public static SQLiteStatement createUpdateStatement(SQLiteDatabase dbWrite) {
        String sql = "update " + TABLE_POI_UPLOADS + " set " +
                COL_LOCAL_IMAGE_FILE + "=?" +
                " where " + COL_ID + "=?;";
        return dbWrite.compileStatement(sql);
    }

    public static void bindToUpdateStatement(SQLiteStatement stmt, String uuid, String fileName) {
        DbUtil.bindStringOrNull(1, stmt, fileName);
        stmt.bindString(2, uuid);
    }


    public static Cursor executeSelectUploadsStatement(SQLiteDatabase db) {
        String sql = "select " +
                COL_ID + "," +
                COL_NAME + "," +
                COL_DESCRIPTION + "," +
                COL_LATITUDE + "," +
                COL_LONGITUDE + "," +
                COL_EXACT_LATITUDE + "," +
                COL_EXACT_LONGITUDE + "," +
                COL_LOCAL_IMAGE_FILE +
                " from " + TABLE_POI_UPLOADS + ";";
        return db.rawQuery(sql, null);
    }

    public static PoiCreateBean loadBeanFromCursor(Cursor c) {
        PoiCreateBean bean = new PoiCreateBean();
        bean.setUuid(c.getString(0));
        bean.setPoiName(c.getString(1));
        bean.setPoiDescription(DbUtil.getStringOrNull(c, 2));
        bean.setLatitude(c.getDouble(3));
        bean.setLongitude(c.getDouble(4));
        bean.setExactLatitude(DbUtil.getDoubleOrNull(c, 5));
        bean.setExactLongitude(DbUtil.getDoubleOrNull(c, 6));
        return bean;
    }

    public static String loadBlobNameFromCursor(Cursor c) {
        return c.getString(7);
    }

    public static void executeDeleteStatement(SQLiteDatabase db, String id) {
        String sql = "delete from " + TABLE_POI_UPLOADS +
                " where " + COL_ID + "=?;";
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindString(1, id);
        stmt.executeUpdateDelete();
    }

}
