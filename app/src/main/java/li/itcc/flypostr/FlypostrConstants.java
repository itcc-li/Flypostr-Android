package li.itcc.flypostr;

import li.itcc.flypostr.util.RequiredStringSize;

/**
 * Created by Arthur on 12.09.2015.
 */
public class FlypostrConstants {

    public static final int FINE_LOCATION_MAX_RADIUS_IN_METER = 30;
    public static final int EARTH_RADIUS_IN_METER = 6378100;
    public static final int GEO_QUERY_RADIUS_IN_KILOMETER = 50;

    public static final String ROOT_GEOFIRE = "geofire";
    public static final String ROOT_POSTINGS = "postings";
    public static final String ROOT_IMAGES_STORAGE = "images";
    public static final String ROOT_THUMBNAIL_STORAGE = "thumbnails";
    public static final String ROOT_COMMENTS = "comments";

    public static final int KEEP_IMAGE_CACHE_THUMBNAILS = 500;
    public static final int KEEP_IMAGE_CACHE_IMAGES = 10;


    public static final String INTENT_KEY_POSTING_ID = "postingID";
    public static final String INTENT_KEY_USER_DATA = "userData";



    public static final RequiredStringSize VALIDTAE_POSTING_TITLE = new RequiredStringSize(2, 50);
    public static final RequiredStringSize VALIDTAE_POSTING_TEXT = new RequiredStringSize(0, 500);
    public static final RequiredStringSize VALIDTAE_POSTING_COMMENT = new RequiredStringSize(5, 500);



}
