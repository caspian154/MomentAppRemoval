package com.caspian.android.removal;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore.LoadStoreParameter;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

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
        boolean success = false;
        ArrayList<String> files = getCheckedFiles();

        if (files.size() > 0)
        {
            String fileName = "";

            try
            {
                for (String f : files)
                {
                    fileName = f;
                    mgr.copyFile(appManagementDir + fileName, backupDir, false);
                }

                success = true;
            }
            catch (Exception exc)
            {
                String message = "We're sorry, there was an Exception thrown "
                    + "while backing up " + fileName + ":\n" + exc.getMessage();

                AlertDialog dialog = new AlertDialog.Builder(this).create();
                dialog.setMessage(message);
                dialog.setButton("Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                    }
                });
                
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
            if (!mgr.backupExists(f))
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
            + "you like to Continue without backup, Continue after backup, or "
            + "cancel this operation?\n\n" + filesNotBackedUp;

        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setMessage(message);
        dialog.setButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                deleteCheckedFiles();
            }
        });
        dialog.setButton3("Backup First",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    if (copyCheckedFiles()) performDelete();
                }
            });
        dialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
            }
        });

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
            mgr.remountSystemDir(true);
            
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
            dialog.setButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                }
            });
            
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
                mgr.remountSystemDir(false);
            }
            catch (Exception e)
            {
                // this is bad!
            }
        }

        return success;
    }

}
