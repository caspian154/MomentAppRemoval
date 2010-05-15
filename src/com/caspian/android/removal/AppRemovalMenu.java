package com.caspian.android.removal;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class AppRemovalMenu extends PreferenceActivity
{   
    /**
     * The main preference screen used to show all the options
     */
    private PreferenceScreen mainScreen;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // set up the preference layout
        setPreferenceScreen(createPreferenceScreen());
    }

    /**
     * Create the preferences screen that will serve as the main window for this
     * application.
     * 
     * @return The preference screen to use as this activity's main window
     */
    private PreferenceScreen createPreferenceScreen()
    {
        // The main preference screen
        mainScreen = getPreferenceManager().createPreferenceScreen(this);

        Preference systemFiles = new Preference(this);
        systemFiles.setTitle("Manage /system/apps/");
        mainScreen.addPreference(systemFiles);
        
        // show the delete
        systemFiles.setOnPreferenceClickListener(
            new Preference.OnPreferenceClickListener() 
            {
                
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    startNewAcitvity();
                    
                    return false;
                }
            });

        // create a sub-screen for managing settings
        PreferenceScreen settingsScreen = 
            getPreferenceManager().createPreferenceScreen(this);
        settingsScreen.setTitle("Settings");
        //mainScreen.addPreference(settingsScreen);

        CheckBoxPreference advancedPref = new CheckBoxPreference(this);
        advancedPref.setTitle("Advanced Mode");
        advancedPref.setSummary(
            "Enabling opens up a few advanced features.");
        settingsScreen.addPreference(advancedPref);
        
        EditTextPreference filterPref = new EditTextPreference(this);
        filterPref.setTitle("Filename filter");
        settingsScreen.addPreference(filterPref);

        CheckBoxPreference autoPref = new CheckBoxPreference(this);
        autoPref.setTitle("Auto Handle .odex");
        autoPref.setSummary(
            "Backup/delete .odex files associated with .apk");
        settingsScreen.addPreference(autoPref);
        
        return mainScreen;

    }
    
    private void startNewAcitvity()
    {
        Intent i = new Intent(this, AppRemovalScreen.class);
        startActivity(i);
    }
}