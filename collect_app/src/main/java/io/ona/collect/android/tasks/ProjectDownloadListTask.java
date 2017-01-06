package io.ona.collect.android.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.utilities.WebUtils;
import org.opendatakit.httpclientandroidlib.Header;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import io.ona.collect.android.R;
import io.ona.collect.android.listeners.ProjectListDownloaderListener;
import io.ona.collect.android.logic.ProjectDetails;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 06/01/2017
 */
public class ProjectDownloadListTask extends AsyncTask<Void, Void, ArrayList<ProjectDetails>> {
    private static final String TAG = "ProjectDownloadListTask";

    public enum HttpError {
        AUTH_REQUIRED,
        GENERIC_ERROR
    }

    private ProjectListDownloaderListener downloaderListener;
    private HttpError downloadError;
    private String errorMessage;
    private final HashMap<String, FormDetails> forms;
    private HashMap<String, HashMap<String, FormDetails>> formsInProjects;

    public ProjectDownloadListTask(ProjectListDownloaderListener downloaderListener) {
        this.downloaderListener = downloaderListener;
        this.forms = null;
    }


    public ProjectDownloadListTask(HashMap<String, FormDetails> forms,
                                   ProjectListDownloaderListener downloaderListener) {
        this.forms = forms;
        this.downloaderListener = downloaderListener;
    }

    @Override
    protected ArrayList<ProjectDetails> doInBackground(Void... params) {
        ArrayList<ProjectDetails> projectDetails = new ArrayList<>();

        Context appContext = Collect.getInstance();
        // Update the webutils user digest to prevent getting 403s from API endpoints
        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(appContext);
        String storedUsername = settings.getString(PreferencesActivity.KEY_USERNAME, null);
        String storedPassword = settings.getString(PreferencesActivity.KEY_PASSWORD, null);
        String server = settings.getString(PreferencesActivity.KEY_API_URL,
                appContext.getString(R.string.default_api_url));

        String downloadListUrl = server + settings.getString(PreferencesActivity.KEY_PROJECTS_URL,
                appContext.getString(R.string.default_ona_projects));

        ArrayList<Header> extraHeaders = new ArrayList<>();
        extraHeaders.add(WebUtils.constructBasicAuthHeader(storedUsername, storedPassword));

        try {
            String result = WebUtils.downloadUrl(new URL(downloadListUrl), extraHeaders);
            JSONArray array = new JSONArray(result);
            for (int i = 0; i < array.length(); i++) {
                try {
                    projectDetails.add(ProjectDetails.construct(array.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (forms != null) {
                formsInProjects = splitFormsIntoAccounts(Collect.getInstance(), forms, projectDetails);
            }
        } catch (MalformedURLException mue) {
            errorMessage = mue.getMessage();
            downloadError = HttpError.GENERIC_ERROR;
        } catch (IOException ioe) {
            errorMessage = ioe.getMessage();
            downloadError = HttpError.GENERIC_ERROR;
        } catch (JSONException e) {
            errorMessage = e.getMessage();
            downloadError = HttpError.GENERIC_ERROR;
        }

        return projectDetails;
    }

    public static HashMap<String, HashMap<String, FormDetails>> splitFormsIntoAccounts(
            Context context, final HashMap<String, FormDetails> forms,
            ArrayList<ProjectDetails> projects) {

        HashMap<String, String> idToKey = new HashMap<>();// Map with formId as key and key on
        // forms map as value
        for (String curKey : forms.keySet()) {
            idToKey.put(forms.get(curKey).formID, curKey);
        }

        HashMap<String, String> onaFormIdProjectMap = new HashMap<>();//map with ona form ids
        // and keys and their corresponding projects as values
        for (ProjectDetails curProject : projects) {
            onaFormIdProjectMap.putAll(curProject.formIdProjectMap);
        }

        if (projects == null) projects = new ArrayList<>();

        String allFormsString = context.getResources().getString(R.string.all_forms);
        HashMap<String, HashMap<String, FormDetails>> result = new HashMap<>();
        result.put(allFormsString, forms);

        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(context);
        boolean showSharedForms = settings.getBoolean(PreferencesActivity.KEY_SHOW_SHARED_FORMS,
                true);
        String username = settings.getString(PreferencesActivity.KEY_USERNAME, null);

        for (FormDetails curForm : forms.values()) {
            String user = FormDetails.getOnaUser(curForm);
            if (user != null) {
                // Check if user wants to see shared forms
                if (username != null && !showSharedForms && !user.equals(username)) {
                    continue;
                }

                String projectKey = null;
                String onaFormId = FormDetails.getOnaFormId(curForm);
                if (onaFormIdProjectMap.containsKey(onaFormId)) {
                    projectKey = onaFormIdProjectMap.get(onaFormId);
                }

                if (projectKey != null) {
                    if (!result.containsKey(projectKey)) {
                        result.put(projectKey,
                                new HashMap<String, FormDetails>());
                    }

                    result.get(projectKey).put(idToKey.get(curForm.formID), curForm);
                }
            } else {
                Log.e(TAG, "Could not extract the user that owns the specified form");
            }
        }

        if (result.size() == 2) {//forms from only one account
            // Remove the all_forms item to avoid redundant items
            result.remove(allFormsString);
        } else if (result.size() == 1 && !showSharedForms) {// Only all_forms in map and shared
            // Forms is not turned on. Mean the user doesn't have any forms
            result.remove(allFormsString);
        }

        return result;
    }

    @Override
    protected void onPostExecute(ArrayList<ProjectDetails> projectDetails) {
        super.onPostExecute(projectDetails);
        if (projectDetails != null) {
            downloaderListener.projectListDownloadingComplete(projectDetails, forms, formsInProjects);
        } else {
            String allFormsString = Collect.getInstance()
                    .getResources().getString(R.string.all_forms);
            HashMap<String, HashMap<String, FormDetails>> formsInProjects = new HashMap<>();
            formsInProjects.put(allFormsString, forms);
            downloaderListener.projectListErrorOccurred(forms, formsInProjects,
                    downloadError, errorMessage);
        }
    }
}
