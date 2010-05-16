package com.caspian.android.removal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class AppRemovalMenu extends PreferenceActivity
{   
    /**
     * Key to store advanced options in the preferences
     */
    public static final String PREF_KEY_AUTOMOUNT = "AUTO_MOUNT_SYSTEM";

    /**
     * The name of the shared prefs file
     */
    public static final String PREFS_NAME = "MomentAppRemoval";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // set up the preference layout
        createPreferenceScreen();
    }

    /**
     * Create the preferences screen that will serve as the main window for this
     * application.
     * 
     * @return The preference screen to use as this activity's main window
     */
    private void createPreferenceScreen()
    {
        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        
        // The main preference screen
        PreferenceScreen mainScreen =
            getPreferenceManager().createPreferenceScreen(this);

        Preference deleteSystem = new Preference(this);
        deleteSystem.setTitle("Manage system apps");
        mainScreen.addPreference(deleteSystem);
        
        // add a restore menu item
        Preference restoreSystem = new Preference(this);
        restoreSystem.setTitle("Manage Backups");
        mainScreen.addPreference(restoreSystem);
        
        // Show the 
        boolean autoManageMount = settings.getBoolean(PREF_KEY_AUTOMOUNT, true);
        if (!autoManageMount)
        {
            CheckBoxPreference systemMount = new CheckBoxPreference(this);
            systemMount.setTitle("Mount /system rw");
            systemMount.setChecked(AppRemovalManager.isSystemRw());
            mainScreen.addPreference(systemMount);

            systemMount.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() 
                {
                    @Override
                    public boolean onPreferenceChange(
                        Preference preference, 
                        Object newValue)
                    {
                        boolean checked = ((Boolean)newValue).booleanValue();
                        return remountSystem(checked);
                    }
                });
        }

        // create a sub-screen for managing settings
        PreferenceScreen settingsScreen = 
            getPreferenceManager().createPreferenceScreen(this);
        settingsScreen.setTitle("Settings");
        mainScreen.addPreference(settingsScreen);

        CheckBoxPreference advancedPref = new CheckBoxPreference(this);
        advancedPref.setTitle("Auto Mount System");
        advancedPref.setChecked(autoManageMount);
        advancedPref.setSummary(
            "Let the app remount /system when necessary");
        settingsScreen.addPreference(advancedPref);
        
//      TODO: implement the filter and associate .odex and .apk
        
        
//        EditTextPreference filterPref = new EditTextPreference(this);
//        filterPref.setTitle("Filename filter");
//        settingsScreen.addPreference(filterPref);
//
//        CheckBoxPreference autoPref = new CheckBoxPreference(this);
//        autoPref.setTitle("Auto Handle .odex");
//        autoPref.setSummary(
//            "Backup/delete .odex files associated with .apk");
//        settingsScreen.addPreference(autoPref);

        // show the delete screen
        deleteSystem.setOnPreferenceClickListener(
            new Preference.OnPreferenceClickListener() 
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    Intent i = new Intent(
                        AppRemovalMenu.this, AppRemovalScreen.class);
                    startActivity(i);
                    return false;
                }
            });

        // show the delete screen
        restoreSystem.setOnPreferenceClickListener(
            new Preference.OnPreferenceClickListener() 
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    Intent i = new Intent(
                        AppRemovalMenu.this, AppRestoreScreen.class);
                    startActivity(i);
                    return false;
                }
            });
        
        advancedPref.setOnPreferenceChangeListener(
            new Preference.OnPreferenceChangeListener() 
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    boolean checked = ((Boolean)newValue).booleanValue();
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(
                        PREF_KEY_AUTOMOUNT, 
                        checked);
                    editor.commit();
                    
                    createPreferenceScreen();
                    
                    return true;
                }
            });
        
        setPreferenceScreen(mainScreen);
    }
    
    /**
     * Remount the system writeable or readonly
     * 
     * @param writeable mount the system with rw permission
     * @return Whether the operation was successful
     */
    private boolean remountSystem(boolean writeable)
    {
        boolean operationSucceeded = false;
        
        try
        {
            AppRemovalManager.remountSystemDir(writeable);
            
            operationSucceeded = true;
        }
        catch (Exception exc)
        {
            String message = "Error while remounting /system:\n\n" + 
                exc.getMessage();

            AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setMessage(message);
            dialog.setButton(
                AlertDialog.BUTTON_POSITIVE, 
                "OK", 
                (DialogInterface.OnClickListener)null);
            
            dialog.show();
        }

        return operationSucceeded;
    }
}