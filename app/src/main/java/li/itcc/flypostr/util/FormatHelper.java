package li.itcc.flypostr.util;

import android.view.View;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import li.itcc.flypostr.R;

/**
 * Created by sandro.pedrett on 20.08.2016.
 */

public final class FormatHelper {
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

    public static String convertToDistance(float distanceInMeter) {
        if (distanceInMeter >= 1000) {
            float distanceInKm = distanceInMeter / 1000;
            if (distanceInKm >=10) {
                int distInKm = (int)distanceInKm;
                return Integer.toString(distInKm) + " km";

            }
            else {
                return String.format("%1$.1f km", distanceInKm);
            }
        }
        else {
            int distInt = (int)distanceInMeter;
            return Integer.toString(distInt) + " m";
        }
    }

    public static void formatAuthor(TextView textView, String author) {
        if (author == null || author.length() == 0) {
            textView.setText(null);
            textView.setVisibility(View.GONE);
        }
        else {
            String text = textView.getResources().getString(R.string.txt_author) + ": " + author;
            textView.setText(text);
            textView.setVisibility(View.VISIBLE);
        }
    }
}
