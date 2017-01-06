package io.ona.collect.android.listeners;

import org.odk.collect.android.logic.FormDetails;

import java.util.ArrayList;
import java.util.HashMap;

import io.ona.collect.android.logic.ProjectDetails;
import io.ona.collect.android.tasks.ProjectDownloadListTask;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 06/01/2017
 */
public interface ProjectListDownloaderListener {
    void projectListDownloadingComplete(ArrayList<ProjectDetails> value,
                                        HashMap<String, FormDetails> forms,
                                        HashMap<String, HashMap<String, FormDetails>> projectForms);

    void projectListErrorOccurred(HashMap<String, FormDetails> forms,
                                  HashMap<String, HashMap<String, FormDetails>> formsInProjects,
                                  ProjectDownloadListTask.HttpError error, String errorMessage);
}
