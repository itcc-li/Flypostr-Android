package li.itcc.flypostr.database;

import android.net.Uri;

/**
 * Created by Arthur on 12.12.2014.
 */
public class DatabaseContract {

    public static final String AUTHORITY = "li.itcc.provider.hackathon15";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static class Pois {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/pois");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.li.itcc.hackathon15.poi";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.li.itcc.hackathon15.poi";

    }

}
