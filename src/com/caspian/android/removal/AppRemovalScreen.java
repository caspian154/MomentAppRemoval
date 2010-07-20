package com.caspian.android.removal;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

public class AppRemovalScreen extends AppManagementScreen
{
    /**
     * Directory to back up to
     */
    private String backupDir;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        appManagementDir = "/system/app/";
        backupDir = "/sdcard/sdx/backup/app/";
        super.onCreate(savedInstanceState);

        btnCopyFiles.setText("Backup Selected Files");
    }

    /**
     * Copy any checked file back to the system dir to restore it
     */
    @Override
    protected boolean copyCheckedFiles()
    {
        boolean backupDone = backupCheckedFiles(true);
        
        if (backupDone)
        {
            Toast toast = Toast.makeText(
                    getApplicationContext(), 
                    "Successfully backed up files", 
                    Toast.LENGTH_SHORT);
            toast.show();
        }
        
        return backupDone;
    }
    
    /**
     * Move the checked files and overwrite if specified 
     * 
     * @param overWrite
     * @return
     */
    protected boolean backupCheckedFiles(boolean overWrite)
    {
        boolean success = false;
        ArrayList<String> files = getCheckedFiles();

        if (files.size() > 0)
        {
            String fileName = "";

            try
            {
                for (String f : files)
                {
                    fileName = appManagementDir + f;

                    // only backup files that don't exist in the backupdir
                    // if overwrite is true
                    if (!overWrite 
                        || !mgr.backupExists(backupDir + f))
                    {
                        mgr.copyFile(fileName, backupDir, false);
                    }
                }

                success = true;
            }
            catch (Exception exc)
            {
                String message = "We're sorry, there was an Exception thrown "
                    + "while backing up " + fileName + ":\n" + exc.getMessage();

                AlertDialog dialog = new AlertDialog.Builder(this).create();
                dialog.setMessage(message);
                dialog.setButton(
                    AlertDialog.BUTTON_POSITIVE,
                    "Ok", 
                    (DialogInterface.OnClickListener)null);
                
                exc.printStackTrace();

                dialog.show();
                
                createFileList();
            }

        }

        return success;
    }

    /**
     * Delete any files from /system/app
     */
    @Override
    protected boolean deleteCheckedFiles()
    {
        boolean success = false;
        ArrayList<String> files = getCheckedFiles();
        String filesNotBackedUp = "";

        // first check to see if the user wants to back up the files first
        for (String f : files)
        {
            if (!mgr.backupExists(backupDir + f))
            {
                filesNotBackedUp += f + "\n";
            }
        }

        if (!filesNotBackedUp.equals(""))
        {
            backupOrSkip(filesNotBackedUp);
            success = true;
        }
        else
        {
            success = performDelete();
        }
        return success;
    }

    /**
     * Present the user with the option to back up any files that haven't
     * been backed up before deleting them
     * 
     * @param filesNotBackedUp
     */
    private void backupOrSkip(String filesNotBackedUp)
    {
        String message = "The following files have not been backed up. Would "
            + "you like to continue without backup, backup first then continue "
            + ", or cancel this operation?\n\n" + filesNotBackedUp;

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setMessage(message);
        dialog.setButton(
            AlertDialog.BUTTON_POSITIVE,
            "Continue", 
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    deleteCheckedFiles();
                }
            });
        dialog.setButton(
            AlertDialog.BUTTON_NEUTRAL,
            "Backup First",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if (copyCheckedFiles()) performDelete();
                }
            });
        dialog.setButton(
            AlertDialog.BUTTON_NEGATIVE,
            "Cancel", 
            (DialogInterface.OnClickListener)null);

        dialog.show();
    }

    /**
     * Delete the checked files
     */
    private boolean performDelete()
    {
        boolean success = false;
        ArrayList<String> files = getCheckedFiles();

        try 
        {
            // make system writeable
            if (AppSettings.getAutoMount()) AppRemovalManager.remountSystemDir(true);
            
            for (String f : files)
            {
                if (!mgr.backupExists(f))
                {
                    mgr.deleteFile(appManagementDir + f, true);
                }
            }
            
            success = true;
        }
        catch (Exception exc)
        {
            String message = "We're sorry, there was an Exception "
                + "thrown while deleting:\n\n" + exc.getMessage();

            AlertDialog dialog = new AlertDialog.Builder(this).create();
            dialog.setMessage(message);
            dialog.setButton(
                AlertDialog.BUTTON_POSITIVE,
                "Ok", 
                (DialogInterface.OnClickListener)null);
            
            dialog.show();
            
            createFileList();
        }
        finally
        {
            // recreate the file list
            createFileList();

            // make system read only
            try
            {
                if (AppSettings.getAutoMount()) AppRemovalManager.remountSystemDir(false);
            }
            catch (Exception e)
            {
                // this is bad!
            }
        }

        return success;
    }

}
