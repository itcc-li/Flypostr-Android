package li.itcc.flypostr.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

/**
 * Created by Arthur on 25.09.2015.
 */
public class DbUtil {

    public static void bindStringOrNull(int pos, SQLiteStatement stmt, String text) {
        if (text == null || text.length() == 0) {
            stmt.bindNull(pos);
        }
        else {
            stmt.bindString(pos, text);
        }
    }

    public static void bindDoubleOrNull(int pos, SQLiteStatement stmt, Double value) {
        if (value == null) {
            stmt.bindNull(pos);
        }
        else {
            stmt.bindDouble(pos, value.doubleValue());
        }
    }

    public static Double getDoubleOrNull(Cursor c, int index) {
        if (c.isNull(index)) {
            return null;
        }
        return Double.valueOf(c.getDouble(index));
    }

    public static String getStringOrNull(Cursor c, int index) {
        if (c.isNull(index)) {
            return null;
        }
        return c.getString(index);
    }
}
