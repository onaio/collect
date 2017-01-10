package io.ona.collect.android.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.FormDownloadList;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;

import io.ona.collect.android.BuildConfig;
import io.ona.collect.android.R;
import io.ona.collect.android.logic.ProjectDetails;
import shadows.org.odk.collect.android.database.ShadowActivityLogger;
import shadows.org.odk.collect.android.utilities.ShadowPRNGFixes;

import static org.junit.Assert.*;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 09/01/2017
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml",
        shadows = {ShadowPRNGFixes.class, ShadowActivityLogger.class})
public class ProjectDownloadListTaskTest {

    @Test
    public void testSplitFormsIntoAccounts() throws Exception {
        Context context = RuntimeEnvironment.application;
        String project1 = "project1";
        ArrayList<String> project1Forms = new ArrayList<>();
        HashMap<String, String> project1FormsMap = new HashMap<>();
        String project1Key = context.getString(R.string.forms_in, project1);
        project1Forms.add("1");
        project1FormsMap.put("1", project1Key);
        project1Forms.add("5");
        project1FormsMap.put("5", project1Key);

        String project2 = "project2";
        ArrayList<String> project2Forms = new ArrayList<>();
        HashMap<String, String> project2FormsMap = new HashMap<>();
        String project2Key = context.getString(R.string.forms_in, project2);
        project2Forms.add("2");
        project2FormsMap.put("2", project2Key);

        String project3 = "project3";
        ArrayList<String> project3Forms = new ArrayList<>();
        HashMap<String, String> project3FormsMap = new HashMap<>();
        String project3Key = context.getString(R.string.forms_in, project3);
        project3Forms.add("3");
        project3FormsMap.put("3", project3Key);
        project3Forms.add("4");
        project3FormsMap.put("4", project3Key);

        ArrayList<ProjectDetails> projects = new ArrayList<>();
        projects.add(new ProjectDetails(project1, null, project1, null, true, true,
                project1Forms, project1FormsMap));
        projects.add(new ProjectDetails(project2, null, project2, null, true, true,
                project2Forms, project2FormsMap));
        projects.add(new ProjectDetails(project3, null, project3, null, true, true,
                project3Forms, project3FormsMap));

        String user1 = "user1";
        String user2 = "user2";
        String user3 = "user3";
        String form1 = "form1";
        String form2 = "form2";
        String form3 = "form3";
        String form4 = "form4";
        String form5 = "form5";
        HashMap<String, FormDetails> allForms = new HashMap<>();
        allForms.put(form1, new FormDetails(form1,
                "http://odk.ona.io/" + user1 + "/forms/1/form.xml", null, form1, null));
        allForms.put(form2, new FormDetails(form2,
                "http://odk.ona.io/" + user2 + "/forms/2/form.xml", null, form2, null));
        allForms.put(form3, new FormDetails(form3,
                "http://odk.ona.io/" + user2 + "/forms/3/form.xml", null, form3, null));
        allForms.put(form4, new FormDetails(form4,
                "http://odk.ona.io/" + user3 + "/forms/4/form.xml", null, form4, null));
        allForms.put(form5, new FormDetails(form5,
                "http://odk.ona.io/" + user1 + "/forms/5/form.xml", null, form5, null));

        // Test a good HashMap
        SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(context);
        sp1.edit().putString(PreferencesActivity.KEY_USERNAME, user1).commit();
        sp1.edit().putBoolean(PreferencesActivity.KEY_SHOW_SHARED_FORMS, true).commit();
        HashMap<String, HashMap<String, FormDetails>> result1 = ProjectDownloadListTask
                .splitFormsIntoAccounts(context, allForms, projects);

        Assert.assertTrue(result1.size() == 4);// Three projects and an extra key for all forms

        Assert.assertTrue(result1.containsKey(project1Key));
        Assert.assertTrue(result1.get(project1Key).size() == 2);
        Assert.assertTrue(result1.get(project1Key).containsKey(form1));
        Assert.assertTrue(result1.get(project1Key).containsKey(form5));

        Assert.assertTrue(result1.containsKey(project2Key));
        Assert.assertTrue(result1.get(project2Key).size() == 1);
        Assert.assertTrue(result1.get(project2Key).containsKey(form2));

        Assert.assertTrue(result1.containsKey(project3Key));
        Assert.assertTrue(result1.get(project3Key).size() == 2);
        Assert.assertTrue(result1.get(project3Key).containsKey(form3));
        Assert.assertTrue(result1.get(project3Key).containsKey(form4));

        // Test when user toggles off shared forms
        SharedPreferences sp2 = PreferenceManager.getDefaultSharedPreferences(context);
        sp2.edit().putString(PreferencesActivity.KEY_USERNAME, user1).commit();
        sp2.edit().putBoolean(PreferencesActivity.KEY_SHOW_SHARED_FORMS, false).commit();

        HashMap<String, HashMap<String, FormDetails>> result2 = ProjectDownloadListTask
                .splitFormsIntoAccounts(context, allForms, projects);

        Assert.assertTrue(result2.size() == 1);// Since all projects owned by logged in user are
        // in only one project, we don't expect there being an extra key holding all projects

        Assert.assertTrue(result2.containsKey(project1Key));
        Assert.assertTrue(result2.get(project1Key).size() == 2);
        Assert.assertTrue(result2.get(project1Key).containsKey(form1));
        Assert.assertTrue(result2.get(project1Key).containsKey(form5));

        // Test a scenario where the user does not have forms
        HashMap<String, FormDetails> otherUsersForms = (HashMap<String, FormDetails>) allForms.clone();
        otherUsersForms.remove(form1);
        otherUsersForms.remove(form5);

        SharedPreferences sp3 = PreferenceManager.getDefaultSharedPreferences(context);
        sp3.edit().putString(PreferencesActivity.KEY_USERNAME, user1).commit();
        sp3.edit().putBoolean(PreferencesActivity.KEY_SHOW_SHARED_FORMS, false).commit();

        HashMap<String, HashMap<String, FormDetails>> result3 = ProjectDownloadListTask
                .splitFormsIntoAccounts(context, otherUsersForms, projects);

        Assert.assertTrue(result3.size() == 0);// Should not show any of the other user's forms if
        // shared forms is toggled off
    }
}