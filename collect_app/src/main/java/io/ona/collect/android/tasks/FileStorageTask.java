package io.ona.collect.android.tasks;

import android.app.Activity;
import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by onamacuser on 28/11/2015.
 */
public class FileStorageTask {
    Context context;
    public static String dismissedFormsFile;
    public static String datesOfDownloadFile;

    public FileStorageTask(Context context1) {
        this.context = context1;
        dismissedFormsFile = context1.getFilesDir()+"/dismissedfile";
        datesOfDownloadFile = context1.getFilesDir()+"/datesOfDownload";
    }

    public HashSet<HashMap<String, String>> getDismissedForms() {
        try{
            FileInputStream fin = new FileInputStream(dismissedFormsFile);
            ObjectInputStream ois = new ObjectInputStream(fin);
            HashSet<HashMap<String, String>> dismissedForms = (HashSet<HashMap<String, String>>) ois.readObject();
            ois.close();
            return dismissedForms;
        }catch(Exception ex){
            //ex.printStackTrace();
            return new HashSet<HashMap<String, String>>();
        }
    }

    public boolean saveDismissedForms(HashSet<HashMap<String, String>> list) {
        try{
            FileOutputStream fout = new FileOutputStream(dismissedFormsFile);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(list);
            oos.close();
            return true;
        }catch(Exception ex){
            //ex.printStackTrace();
            return false;
        }
    }

    public HashMap<HashMap<String, String>, Date> getDatesOfDownload() {
        try{
            FileInputStream fin = new FileInputStream(datesOfDownloadFile);
            ObjectInputStream ois = new ObjectInputStream(fin);
            HashMap<HashMap<String, String>, Date> datesOfDownloadMap = (HashMap<HashMap<String, String>, Date>) ois.readObject();
            ois.close();
            return datesOfDownloadMap;
        }catch(Exception ex){
            //ex.printStackTrace();
            return new HashMap<HashMap<String, String>, Date>();
        }
    }

    public boolean saveDatesOfDownload(HashMap<HashMap<String, String>, Date> list) {
        try{
            FileOutputStream fout = new FileOutputStream(datesOfDownloadFile);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(list);
            oos.close();
            return true;
        }catch(Exception ex){
            //ex.printStackTrace();
            return false;
        }
    }
}
