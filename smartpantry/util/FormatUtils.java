package com.example.smartpantry.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Small formatting / date helpers shared by several screens. */
public final class FormatUtils {

    private FormatUtils() { }

    /** 3.0 -> "3", 0.5 -> "0.5", 1.25 -> "1.25". */
    public static String formatQuantity(double q) {
        if (Math.abs(q - Math.rint(q)) < 1e-9) {
            return String.valueOf((long) Math.rint(q));
        }
        return String.format(Locale.US, "%.2f", q).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    /**
     * Whole days from today until the given ISO date (yyyy-MM-dd).
     * Negative = already expired. Integer.MAX_VALUE = no / invalid date.
     */
    public static int daysUntil(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            Date date = format.parse(isoDate);
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            double diff = date.getTime() - today.getTimeInMillis();
            return (int) Math.round(diff / 86400000.0);
        } catch (ParseException e) {
            return Integer.MAX_VALUE;
        }
    }
}
