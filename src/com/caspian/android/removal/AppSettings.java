package com.caspian.android.removal;

import android.content.SharedPreferences;

public class AppSettings
{

    /**
     * The name of the shared prefs file
     */
    public static final String PREFS_NAME = "MomentAppRemoval";

    /**
     * Key to store advanced options in the preferences
     */
    public static final String PREF_KEY_AUTOMOUNT = "AUTO_MOUNT_SYSTEM";

    /**
     * Key to store advanced options in the preferences
     */
    public static final String PREF_KEY_FILTER = "FILTER_TEXT";

    /**
     * Key to store advanced options in the preferences
     */
    public static final String PREF_KEY_ODEX = "ASSOCIATE_ODEX_W_APK";
    
    /**
     * the shared preferences object
     */
    protected static SharedPreferences settings;
    
    /**
     * Set the shared preferences object
     * 
     * @param settings
     */
    public static void CreateSettings(SharedPreferences settings)
    {
        AppSettings.settings = settings;
    }
    
    public static String getFilter()
    {
        return settings.getString(PREF_KEY_FILTER, "*.apk");
    }
    
    public static void setFilter(String filter)
    {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_KEY_FILTER, filter);
        editor.commit();
    }
    
    public static boolean getAutoMount()
    {
        return settings.getBoolean(PREF_KEY_AUTOMOUNT, true);
    }
    
    public static void setAutoMount(boolean autoMount)
    {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_KEY_AUTOMOUNT, autoMount);
        editor.commit();
    }
    
    public static boolean getAssociateOdex()
    {
        return settings.getBoolean(PREF_KEY_ODEX, true);
    }
    
    public static void setAssociateOdex(boolean associateOdex)
    {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_KEY_ODEX, associateOdex);
        editor.commit();
    }
}
