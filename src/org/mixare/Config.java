package org.mixare;

import android.location.Location;

public class Config {
    /** TAG for logging */
    public static final String TAG = "Mixare";
    /** string to name & access the preference file in the internal storage */
    public static final String PREFS_NAME = "MyPrefsFileForMenuItems";
    public static final int DEFAULT_RANGE = 65;
    public final static double DEFAULT_FIX_LAT =51.46184;
    public final static double DEFAULT_FIX_LON =7.01655;
    public final static int DEFAULT_FIX_HEIGHT =300;
    public final static String DEFAULT_FIX_NAME ="defaultFix";
    public static final String MANUAL_FIX_NAME = "manualSet";
    public static final int INTENT_REQUEST_CODE_CENTERMAP = 76;
    public static final int INTENT_REQUEST_CODE_DATASOURCES = 40;
    public static final int INTENT_REQUEST_CODE_PLUGINS = 35;
    public static final int INTENT_REQUEST_CODE_MARKERLIST = 42;
    public static final int INTENT_REQUEST_CODE_MAP = 20;
    public static boolean drawTextBlock = true;

    //currently only for test purposes
    public static boolean useHUD=true;

    public static Location getDefaultFix(){
        Location defaultFix = new Location(DEFAULT_FIX_NAME);

        defaultFix.setLatitude(DEFAULT_FIX_LAT);
        defaultFix.setLongitude(DEFAULT_FIX_LON);
        defaultFix.setAltitude(DEFAULT_FIX_HEIGHT);
        return defaultFix;
    }

    public static Location getManualFix(){
        Location manualFix = new Location(MANUAL_FIX_NAME);
        manualFix.setTime(System.currentTimeMillis());
        return manualFix;
    }
}
