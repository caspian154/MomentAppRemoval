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
    /**
     * Get whether /system is mounted rw
     * @return
     */
    public static boolean isSystemRw()
    {
        boolean isRw = false;
        String cmd[] = new String[3];
        cmd[0] = "sh";
        cmd[1] = "-c";
        cmd[2]= "mount | busybox grep stl5";

        try
        {
            // get the runtime object
            Runtime r = Runtime.getRuntime();
            Process p = r.exec(cmd);     
            
            if (p.waitFor() == 0)
            {
                String output = getProcessOutput(p);
                
                // A typical mount shouldn't contain rw unless the system
                // dir is mounted rw - I hope this is a valid assumption
                isRw = output.contains("rw"); 
            }
            else
            {
                String output = getProcessError(p);
                System.err.println(output);
            }
        }
        catch (Exception e)
        {
            // eat the exception
            e.printStackTrace();
        }
        
        return isRw;
    }
    
    /**
     * Remount /system 
     * 
     * @param writeable mount ro (false) or rw (true)
     * @throws IOException
     * @throws InterruptedException
     */
    public static void remountSystemDir(boolean writeable) 
    throws IOException, InterruptedException
    {
        remountDir("/system", "/dev/stl5", writeable);
    }

    /**
     * Remount the specified dir 
     * 
     * su -c "mount -t rfs -o remount,rw /dev/stl5 /system"
     * or
     * su -c "mount -t rfs -o remount,ro /dev/stl5 /system"
     * 
     * @param dirName
     * @param deviceName
     * @param writeable mount ro (false) or rw (true)
     * @throws IOException
     * @throws InterruptedException
     */
    public static void remountDir(
        String dirName, 
        String deviceName,
        boolean writeable) throws IOException, InterruptedException
    {
        String cmd[] = new String[3];
        cmd[0] = "su";
        cmd[1] = "-c";
        cmd[2]= "mount -o remount,";
        cmd[2] += (writeable ? "rw" : "ro");
        cmd[2] += " " + dirName;

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
    public boolean fileExists(String fileName)
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
        String source,
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
        String cmdString = "cat " + source + fileName + " > " + 
            destination + fileName;
        
        if (requiresRoot)
        {
            String cmd[] = new String[3];
            cmd[0] = "su";
            cmd[1] = "-c";
            cmd[2]= cmdString;
            
            p = r.exec(cmd);
        }
        else
        {   
            String cmd[] = new String[3];
            cmd[0] = "sh";
            cmd[1] = "-c";
            cmd[2]= cmdString;
            p = r.exec(cmd);
            //cmd[2]= cmdString;
            //p = r.exec(cmdString);
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
