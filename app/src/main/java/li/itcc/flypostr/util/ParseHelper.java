package li.itcc.flypostr.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sandro.pedrett on 20.08.2016.
 */

public final class ParseHelper {
    private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    public static Date convertToDate(String date) {
        if (date == null) {
            return null;
        }

        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String convertToString(Date date) {
        if (date == null) {
            return null;
        }

        return dateFormat.format(date);
    }

    public static Integer convertToInteger(String integer) {
        if (integer == null) {
            return null;
        }

        return Integer.parseInt(integer);
    }

    public static String convertToString(int integer) {
        return Integer.toString(integer);
    }

    public static Double convertToDouble(String number) {
        return Double.parseDouble(number);
    }

    public static String convertToString(Double number) {
        return Double.toString(number);
    }
}
