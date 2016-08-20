package li.itcc.flypostr.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;

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

    public static Integer convertToInteger(Long number) {
        if (number == null) {
            return null;
        }

        return number.intValue();
    }

    public static String convertToString(Integer integer) {
        if (integer == null) {
            return null;
        }

        return Integer.toString(integer);
    }

    public static Double convertToDouble(String number) {
        return Double.parseDouble(number);
    }

    public static String convertToString(Double number) {
        return Double.toString(number);
    }

    public static Long convertToLong(Integer number) {
        if (number == null) {
            return null;
        }

        return Long.valueOf(number);
    }

    public static String convertToString(Long number) {
        if (number == null) {
            return null;
        }

        return number.toString();
    }
}
