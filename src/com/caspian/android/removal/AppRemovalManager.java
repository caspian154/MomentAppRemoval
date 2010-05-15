package com.caspian.android.removal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.widget.ListView;
import android.widget.Toast;

public class AppRemovalManager
{
    private String backupDir = "/sdcard/sdx/backup/app";
    private String appDir = "/system/app/";
    
    /**
     * Remount /system 
     * 
     * @param writeable mount ro (false) or rw (true)
     * @throws IOException
     * @throws InterruptedException
     */
    public void remountSystemDir(boolean writeable) throws IOException, InterruptedException
    {
        remountDir("/system", "/dev/stl5", writeable);
    }

    /**
     * Remount the specified dir 
     * 
     * @param dirName
     * @param deviceName
     * @param writeable mount ro (false) or rw (true)
     * @throws IOException
     * @throws InterruptedException
     */
    public void remountDir(
        String dirName, 
        String deviceName,
        boolean writeable) throws IOException, InterruptedException
    {
        String cmd[] = new String[3];
        cmd[0] = "su";
        cmd[1] = "-c";
        cmd[2]= "mount -t rfs -o remount,";
        cmd[2] += (writeable ? "rw" : "ro");
        cmd[2] += " " + deviceName + " " + dirName;

        // get the runtime object
        Runtime r = Runtime.getRuntime();
        Process p = r.exec(cmd);     
        
        if (p.waitFor() != 0)
        {
            throw new IOException("Error could not mount " + dirName + ": \n" + 
                getProcessError(p));
        }
    }

    /**
     * Check if this file has been backed up.
     * 
     * @param fileName
     * @return true if there's a backup
     */
    public boolean backupExists(String fileName)
    {
        File testFile = new File(fileName);
        return testFile.exists();
    }

    /**
     * Copy the file to the destination. Destination dir will be created if it 
     * does not exist.
     * @param fileName Fully qualified filename
     * @param destination Fully qualified destination
     * @throws Exception
     */
    public void copyFile(
        String fileName, 
        String destination,
        boolean requiresRoot) throws Exception 
    {
        // make sure destination exists
        File destinationFile = new File(destination);
        if (!destinationFile.exists())
        {
            destinationFile.mkdirs();
        }
        
        // get the runtime object
        Runtime r = Runtime.getRuntime();

        Process p;
        if (requiresRoot)
        {
            String cmd[] = new String[3];
            cmd[0] = "su";
            cmd[1] = "-c";
            cmd[2]= "busybox cp " + fileName + " " + destination;
            
            p = r.exec(cmd);
        }
        else
        {
            String cmdString = "busybox cp " + fileName + " " + destination;
            p = r.exec(cmdString);
        }

        if (p.waitFor() != 0)
        {
            throw new Exception("Error could not copy file " + 
                fileName + ": \n" + getProcessError(p));
        }
    }

    /**
     * 
     * @param fileName The fully qualified file name
     * @throws Exception 
     */
    public void deleteFile(
        String fileName, 
        boolean requiresRoot) throws Exception 
    {
        // get the runtime object
        Runtime r = Runtime.getRuntime();

        String cmd[];
        if (requiresRoot)
        {
            cmd = new String[3];
            cmd[0] = "su";
            cmd[1] = "-c";
            cmd[2]= "rm " + fileName;
        }
        else
        {
            cmd = new String[2];
            cmd[0] = "rm";
            cmd[1] = fileName;
        }
        Process p = r.exec(cmd);

        if (p.waitFor() != 0)
        {
            throw new Exception("Error could not delete file " + 
                fileName + ": \n" + getProcessError(p));
        }
    }

    /**
     * Gets the output from a process
     * 
     * @param p
     * @return
     * @throws IOException
     */
    public static String getProcessOutput(Process p) throws IOException
    {
        BufferedReader br = new BufferedReader(
            new InputStreamReader(p.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null)
        {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    /**
     * Gets error output from a process
     * 
     * @param p
     * @return
     * @throws IOException
     */
    public static String getProcessError(Process p) throws IOException
    {
        BufferedReader br = new BufferedReader(
            new InputStreamReader(p.getErrorStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null)
        {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
