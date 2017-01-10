package io.ona.collect.android.logic;

import android.content.Context;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.FormDownloadList;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import io.ona.collect.android.BuildConfig;
import io.ona.collect.android.R;
import shadows.org.odk.collect.android.database.ShadowActivityLogger;
import shadows.org.odk.collect.android.utilities.ShadowPRNGFixes;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 09/01/2017
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml",
        shadows = {ShadowPRNGFixes.class, ShadowActivityLogger.class})
public class ProjectDetailsTest {

    @Test
    public void testConstruct() throws Exception {
        // Test OK JSON String
        JSONArray array1 = new JSONArray(getStringResource("ok_ona_api_projects_string" +
                ".json"));
        for (int i = 0; i < array1.length(); i++) {
            ProjectDetails curProject = ProjectDetails.construct(array1.getJSONObject(i));

            if (i == 0) {
                Assert.assertTrue(curProject.id.equals("17444"));
                Assert.assertTrue(curProject.isPublic == true);
                Assert.assertTrue(curProject.isStarred == false);
                Assert.assertTrue(curProject.name.equals("overlimit"));
                Assert.assertTrue(curProject.url.equals("https://api.ona.io/api/v1/projects/1"));

                // Test forms in project
                Assert.assertTrue(curProject.formIds.size() == 4);
                Assert.assertTrue(curProject.formIds.contains("129132"));
                Assert.assertTrue(curProject.formIds.contains("148128"));
                Assert.assertTrue(curProject.formIds.contains("129133"));
                Assert.assertTrue(curProject.formIds.contains("150167"));
            }
        }


        // Test a badly formed JSON String
        JSONArray array2 = new JSONArray(getStringResource("bad_ona_api_projects_string.json"));
        try {
            ProjectDetails.construct(array2.getJSONObject(0));
            Assert.assertTrue(false);
        } catch (JSONException e) {
            Assert.assertTrue("JSONException thrown while trying to initialize Project " +
                    "details using badly formed JSON", true);
        }
    }

    @Test
    public void testFormIdProjectMap() throws Exception {
        Context context = RuntimeEnvironment.application;

        JSONArray array1 = new JSONArray(getStringResource("ok_ona_api_projects_string.json"));
        ProjectDetails project1 = ProjectDetails.construct(array1.getJSONObject(0));

        HashMap formIdProjectMap = project1.formIdProjectMap;
        for (String curValue : (Collection<String>) formIdProjectMap.values()) {
            Assert.assertTrue(curValue.equals(context.getString(R.string.forms_in,
                    project1.name)));
        }
    }


    private String getStringResource(String pathToResource) {
        BufferedInputStream result = (BufferedInputStream)
                Config.class.getClassLoader().getResourceAsStream(pathToResource);
        byte[] b = new byte[256];
        int val = 0;
        String txt = "";
        do {
            try {
                val = result.read(b);
                if (val > 0) {
                    txt += new String(b, 0, val);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (val > -1);

        return txt;
    }
}