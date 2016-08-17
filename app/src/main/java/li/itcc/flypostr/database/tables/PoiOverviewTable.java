package li.itcc.flypostr.database.tables;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import li.itcc.flypostr.backend.PoiOverviewBean;
import li.itcc.flypostr.database.DbUtil;

/**
 * Created by Arthur on 26.09.2015.
 */
public class PoiOverviewTable {
    // TODO: make column names private

    public static final String TABLE_POI_OVERVIEW = "PoiOverview";
    public static final String COL_ID = BaseColumns._ID;
    public static final String COL_NAME = "col_name";
    public static final String COL_SHORT_DESCRIPTION = "col_short_desc";
    public static final String COL_LATITUDE = "col_latitude";
    public static final String COL_LONGITUDE = "col_longitude";
    public static final String COL_RATING = "col_rating";


    // table management

    public static void createTable(SQLiteDatabase db) {
        String sql = "create table " + TABLE_POI_OVERVIEW + " (" +
                COL_ID + " string primary key, " +
                COL_NAME + " string not null, " +
                COL_SHORT_DESCRIPTION + " string, " +
                COL_LATITUDE + " real not null, " +
                COL_LONGITUDE + " real not null, " +
                COL_RATING + " real not null" +
                ");";
        db.execSQL(sql);
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL("drop table if exists " + TABLE_POI_OVERVIEW);
    }

    // insert

    public static SQLiteStatement createInsertStatement(SQLiteDatabase db) {
        String sql = "insert into " + TABLE_POI_OVERVIEW + "(" +
                COL_ID + "," +
                COL_NAME + "," +
                COL_SHORT_DESCRIPTION + "," +
                COL_LATITUDE + "," +
                COL_LONGITUDE + "," +
                COL_RATING +
                ") values (?,?,?,?,?,?)";
        return db.compileStatement(sql);
    }

    public static void bindToInsertStatement(SQLiteStatement stmt, PoiOverviewBean bean) {
        stmt.bindString(1, bean.getUuid());
        stmt.bindString(2, bean.getName());
        DbUtil.bindStringOrNull(3, stmt, bean.getShortDescription());
        stmt.bindDouble(4, bean.getLatitude());
        stmt.bindDouble(5, bean.getLongitude());
        stmt.bindDouble(6, bean.getRating());
    }

    public static void insert(SQLiteDatabase db, PoiOverviewBean bean) {
        SQLiteStatement stmt = createInsertStatement(db);
        bindToInsertStatement(stmt, bean);
        stmt.executeInsert();
    }

    //// load overview bean

    public static PoiOverviewBean loadOverviewBean(SQLiteDatabase db, String uuid) {
        Cursor c = null;
        try {
            c = selectOverview(db, uuid);
            PoiOverviewBean result = loadBeanFromCursor(c);
            return result;
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private static Cursor selectOverview(SQLiteDatabase db, String uuid) {
        String sql = "select " +
                COL_NAME + "," +
                COL_SHORT_DESCRIPTION + "," +
                COL_LATITUDE + "," +
                COL_LONGITUDE + "," +
                COL_RATING +
                " from " + TABLE_POI_OVERVIEW +
                " where " + COL_ID + "=?;";
        return db.rawQuery(sql, new String[]{uuid});
    }

    public static PoiOverviewBean loadBeanFromCursor(Cursor c) {
        if (c.moveToNext()) {
            PoiOverviewBean bean = new PoiOverviewBean();
            bean.setName(c.getString(0));
            bean.setShortDescription(DbUtil.getStringOrNull(c, 1));
            bean.setLatitude(c.getDouble(2));
            bean.setLongitude(c.getDouble(3));
            bean.setRating(c.getFloat(4));
            return bean;
        }
        else {
            return null;
        }
    }

    public static int deleteOverview(SQLiteDatabase db, String uuid) {
        return db.delete(TABLE_POI_OVERVIEW, COL_ID + "=?", new String[]{uuid});
    }

    public static void deleteAllEntries(SQLiteDatabase db) {
        db.delete(TABLE_POI_OVERVIEW, null, null);
    }


}
