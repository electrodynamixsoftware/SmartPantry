package com.example.smartpantry.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Thin wrapper around SharedPreferences for the Settings screen. */
public final class AppPrefs {
    private static final String FILE = "smart_pantry_prefs";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    private static final String KEY_ALMOST_THERE = "almost_there";

    private AppPrefs() { }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static boolean isExpiryAlertsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static void setExpiryAlertsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    public static boolean isAlmostThereEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ALMOST_THERE, false);
    }

    public static void setAlmostThereEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_ALMOST_THERE, enabled).apply();
    }
}
