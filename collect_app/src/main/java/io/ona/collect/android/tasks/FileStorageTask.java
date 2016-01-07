package io.ona.collect.android.tasks;

import android.app.Activity;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import io.ona.collect.android.logic.FormDetails;

/**
 * Created by onamacuser on 28/11/2015.
 */
public class FileStorageTask {
    Activity activity;
    public static String dismissedFormsFile;

    public FileStorageTask(Activity activity) {
        this.activity = activity;
        dismissedFormsFile = activity.getFilesDir()+"/dismissedfile";
    }

    public HashSet<HashMap<String, String>> getDismissedForms() {
        try{
            FileInputStream fin = new FileInputStream(dismissedFormsFile);
            ObjectInputStream ois = new ObjectInputStream(fin);
            HashSet<HashMap<String, String>> dismissedForms = (HashSet<HashMap<String, String>>) ois.readObject();
            ois.close();
            return dismissedForms;
        }catch(Exception ex){
            ex.printStackTrace();
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
            ex.printStackTrace();
            return false;
        }
    }
}
