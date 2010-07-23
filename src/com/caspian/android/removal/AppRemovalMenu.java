package com.caspian.android.removal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class AppRemovalMenu extends PreferenceActivity
{   
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // Create the AppSettings object
        AppSettings.CreateSettings(
            getSharedPreferences(AppSettings.PREFS_NAME, 0));
        
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
        
        // show the mount preference if automount is false
        boolean autoManageMount = AppSettings.getAutoMount();
        if (!autoManageMount)
        {
            CheckBoxPreference systemMount = new CheckBoxPreference(this);
            systemMount.setTitle("Mount /system rw");
            systemMount.setChecked(
                AppRemovalManager.isMountPointRw(AppRemovalManager.SYSTEM_DIR));
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

        CheckBoxPreference autoMountPref = new CheckBoxPreference(this);
        autoMountPref.setTitle("Auto Mount System");
        autoMountPref.setChecked(autoManageMount);
        autoMountPref.setSummary(
            "Let the app remount /system when necessary");
        settingsScreen.addPreference(autoMountPref);

        EditTextPreference filterPref = new EditTextPreference(this);
        filterPref.setDialogTitle("Filename filter for the management pages.");
        filterPref.setTitle("Filename filter");
        filterPref.setText(AppSettings.getFilter());
        settingsScreen.addPreference(filterPref);

        CheckBoxPreference odexPref = new CheckBoxPreference(this);
        odexPref.setTitle("Auto Handle .odex");
        odexPref.setSummary(
            "Backup/delete .odex files associated with .apk");
        odexPref.setChecked(AppSettings.getAssociateOdex());
        settingsScreen.addPreference(odexPref);

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

        autoMountPref.setOnPreferenceChangeListener(
            new Preference.OnPreferenceChangeListener() 
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    AppSettings.setAutoMount(
                        ((Boolean)newValue).booleanValue());
                    createPreferenceScreen();
                    
                    return true;
                }
            });

        filterPref.setOnPreferenceChangeListener(
            new Preference.OnPreferenceChangeListener() 
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    AppSettings.setFilter(newValue.toString());
                    
                    return true;
                }
            });

        odexPref.setOnPreferenceChangeListener(
            new Preference.OnPreferenceChangeListener() 
            {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue)
                {
                    AppSettings.setAssociateOdex(
                        ((Boolean)newValue).booleanValue());

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