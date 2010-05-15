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

public class AppRemovalScreen extends Activity
{
    private ListView fileList;
    
    private AppRemovalManager mgr;
    private LinearLayout panel;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mgr = new AppRemovalManager();
        
        // layout the page
        layoutPage();
        
        // create the list of files
        createFileList();
    }
    
    private void layoutPage()
    {
        // Create a panel and set it as the content view
        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        setContentView(panel);

        // create a couple buttons
        LinearLayout buttonPanel = new LinearLayout(this);
        buttonPanel.setOrientation(LinearLayout.HORIZONTAL);
        panel.addView(buttonPanel);

        Button btnDeleteFiles = new Button(this);
        btnDeleteFiles.setText("Delete Selected Files");
        buttonPanel.addView(btnDeleteFiles);

        Button btnBackupFiles = new Button(this);
        btnBackupFiles.setText("Backup Selected Files");
        buttonPanel.addView(btnBackupFiles);

        fileList = new ListView(this);
        fileList.setFastScrollEnabled(true);
        fileList.setItemsCanFocus(false);
        fileList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        fileList.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_multiple_choice));
        panel.addView(fileList);
        

        // create callback for the buttons
        btnBackupFiles.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                backupCheckedFiles();
            }
        });

        btnDeleteFiles.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                deleteCheckedFilesPrompt();
            }
        });
    }

    private void createFileList()
    {
        File f = new File("/system/app/");
        String[] files = f.list();
        Arrays.sort(files);

        ArrayAdapter<String> a = (ArrayAdapter<String>)fileList.getAdapter();
        a.clear();
        
        for (String fileName : files)
        {
            a.add(fileName);
        }
        
        //a.notifyDataSetChanged();
        
        fileList.clearChoices();
        fileList.invalidate();
//        fileList.setAdapter(a);
//
//        panel.removeView(fileList);
//        panel.addView(fileList);
//        
//        fileList.requestLayout();
//        panel.requestLayout();
        
    }


    /**
     * Backup the checked files
     */
    private boolean backupCheckedFiles()
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
                    mgr.backupFile(fileName);
                }

                success = true;
            }
            catch (Exception exc)
            {
                String message = "We're sorry, there was an Exception thrown "
                    + "while backing up " + fileName + ":\n" + exc.getMessage();

                AlertDialog dialog = new AlertDialog.Builder(this).create();
                dialog.setMessage(message);
                dialog.setButton("Yes", new DialogInterface.OnClickListener() {

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
     * First check to see if the files
     * 
     * @return
     */
    private boolean deleteCheckedFilesPrompt()
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
            success = deleteCheckedFiles();
        }
        return success;
    }

    /**
     * Get a list of strings that contain the file names of the selected items
     * 
     * @return
     */
    private ArrayList<String> getCheckedFiles()
    {
        SparseBooleanArray items = fileList.getCheckedItemPositions();
        ArrayList<String> files = new ArrayList<String>();

        for (int i = 0; i < items.size(); i++)
        {
            if (items.valueAt(i))
            {
                files
                    .add(fileList.getItemAtPosition(items.keyAt(i)).toString());
            }
        }

        return files;
    }


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
                    if (backupCheckedFiles()) deleteCheckedFiles();
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
    private boolean deleteCheckedFiles()
    {
        boolean success = false;
        ArrayList<String> files = getCheckedFiles();

        try
        {
            mgr.deleteFiles(files);

            createFileList();
            
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

        return success;
    }

}
