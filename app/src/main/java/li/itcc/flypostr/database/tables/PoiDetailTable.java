package li.itcc.flypostr.database.tables;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import li.itcc.flypostr.backend.PoiDetailBean;
import li.itcc.flypostr.database.DbUtil;

/**
 * Created by Arthur on 25.09.2015.
 */
public class PoiDetailTable {
    private static final String TABLE_POI_DETAILS = "PoiDetails";
    private static final String COL_ID = BaseColumns._ID;
    private static final String COL_DESCRIPTION = "col_description";
    private static final String COL_IMAGE_SIZE = "col_image_size";
    private static final String COL_IMAGE_UPDATE_TIME = "col_image_update_time";
    private static final String COL_IMAGE_URL = "col_image_url";

    // table management

    public static void createTable(SQLiteDatabase db) {
        String sql = "create table " + TABLE_POI_DETAILS + " (" +
                COL_ID + " string primary key, " +
                COL_DESCRIPTION + " string, " +
                COL_IMAGE_SIZE + " long not null, " +
                COL_IMAGE_UPDATE_TIME + " long not null, " +
                COL_IMAGE_URL + " string" +
                ");";
        db.execSQL(sql);
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL("drop table if exists " + TABLE_POI_DETAILS);
    }

    // insert

    public static SQLiteStatement createInsertStatement(SQLiteDatabase db) {
        String sql = "insert into " + TABLE_POI_DETAILS + "(" +
                COL_ID + "," +
                COL_DESCRIPTION + "," +
                COL_IMAGE_SIZE +  "," +
                COL_IMAGE_UPDATE_TIME +  "," +
                COL_IMAGE_URL +
                ") values (?,?,?,?,?)";
        return db.compileStatement(sql);
    }

    public static void bindToInsertStatement(SQLiteStatement stmt, PoiDetailBean detail) {
        stmt.bindString(1, detail.getOverview().getUuid());
        DbUtil.bindStringOrNull(2, stmt, detail.getDescription());
        stmt.bindLong(3, detail.getImageSize());
        stmt.bindLong(4, detail.getImageUpdateTime());
        DbUtil.bindStringOrNull(5, stmt, detail.getImageUrl());
    }

    public static void insert(SQLiteDatabase db, PoiDetailBean detail) {
        SQLiteStatement stmt = PoiDetailTable.createInsertStatement(db);
        PoiDetailTable.bindToInsertStatement(stmt, detail);
        stmt.executeInsert();

    }

    //// load a detail

    public static PoiDetailBean loadDetailBean(SQLiteDatabase db, String uuid) {
        Cursor c = null;
        try {
            c = selectDetail(db, uuid);
            PoiDetailBean result = loadBeanFromCursor(c);
            return result;
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private static Cursor selectDetail(SQLiteDatabase db, String uuid) {
        String sql = "select " +
                COL_DESCRIPTION + "," +
                COL_IMAGE_SIZE +  "," +
                COL_IMAGE_UPDATE_TIME +  "," +
                COL_IMAGE_URL +
                " from " + TABLE_POI_DETAILS +
                " where " + COL_ID + "=?;";
        return db.rawQuery(sql, new String[]{uuid});
    }

    public static PoiDetailBean loadBeanFromCursor(Cursor c) {
        if (c.moveToNext()) {
            PoiDetailBean bean = new PoiDetailBean();
            bean.setDescription(DbUtil.getStringOrNull(c, 0));
            bean.setImageSize(c.getLong(1));
            bean.setImageUpdateTime(c.getLong(2));
            bean.setImageUrl(DbUtil.getStringOrNull(c, 3));
            return bean;
        }
        else {
            return null;
        }
    }

    // delete

    public static int deleteDetail(SQLiteDatabase db, String uuid) {
        return db.delete(TABLE_POI_DETAILS, COL_ID + "=?", new String[]{uuid});
    }

}
