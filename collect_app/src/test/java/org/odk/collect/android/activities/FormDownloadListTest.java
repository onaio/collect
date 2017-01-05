package org.odk.collect.android.activities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import io.ona.collect.android.BuildConfig;
import io.ona.collect.android.R;
import shadows.org.odk.collect.android.database.ShadowActivityLogger;
import shadows.org.odk.collect.android.utilities.ShadowPRNGFixes;

/**
 * Tests for FormDownloadList that don't require instrumentation
 *
 * @author Jason Rogena - jrogena@ona.io
 * @since 05/01/2017
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml",
        shadows = {ShadowPRNGFixes.class, ShadowActivityLogger.class})
public class FormDownloadListTest {

    @Test
    public void testGetOnaUser() {
        String username1 = "dfsDFE12_fsd";
        //test a normal url
        String url1 = "https://odk.ona.io/"+username1+"/forms/129132/form.xml";
        Assert.assertTrue(FormDownloadList.getOnaUser(url1).equals(username1));

        //test http url
        String url2 = "http://odk.ona.io/"+username1+"/forms/129132/form.xml";
        Assert.assertTrue(FormDownloadList.getOnaUser(url2).equals(username1));

        //test bad URL
        String url3 = "http://odk.ona.io/"+username1+"/forms/129132/form";
        Assert.assertTrue(FormDownloadList.getOnaUser(url3) == null);

        //test non Ona URL
        String url4 = "http://fa.la.la.la/"+username1+"/forms/129132/form.xml";
        Assert.assertTrue(FormDownloadList.getOnaUser(url4).equals(username1));

        //test a bad username
        String username2 = "dfsDFE12_fsd ";
        String url5 = "http://odk.ona.io/"+username2+"/forms/129132/form.xml";
        Assert.assertTrue(FormDownloadList.getOnaUser(url5) == null);
    }

    @Test
    public void testSplitFormsIntoAccounts() {
        FormDownloadList formDownloadList = Robolectric.setupActivity(FormDownloadList.class);
        String user1 = "user1";
        String user1Key = formDownloadList.getString(R.string.forms_by, user1);
        String user2 = "user2";
        String user2Key = formDownloadList.getString(R.string.forms_by, user2);
        String user3 = "user3";
        String user3Key = formDownloadList.getString(R.string.forms_by, user3);
        String form1 = "form1";
        String form2 = "form2";
        String form3 = "form3";
        String form4 = "form4";
        HashMap<String, FormDetails> allForms = new HashMap<>();
        allForms.put(form1,new FormDetails(form1,
                "http://odk.ona.io/"+user1+"/forms/129132/form.xml", null, form1, null));
        allForms.put(form2,new FormDetails(form2,
                "http://odk.ona.io/"+user1+"/forms/129133/form.xml", null, form2, null));
        allForms.put(form3,new FormDetails(form3,
                "http://odk.ona.io/"+user2+"/forms/129134/form.xml", null, form3, null));
        allForms.put(form4,new FormDetails(form4,
                "http://odk.ona.io/"+user3+"/forms/129135/form.xml", null, form4, null));

        //test a good HashMap
        SharedPreferences sp1 = PreferenceManager.getDefaultSharedPreferences(formDownloadList);
        sp1.edit().putString(PreferencesActivity.KEY_USERNAME, user1).commit();
        sp1.edit().putBoolean(PreferencesActivity.KEY_SHOW_SHARED_FORMS, true).commit();
        HashMap<String, HashMap<String, FormDetails>> result1 = FormDownloadList
                .splitFormsIntoAccounts(formDownloadList, allForms);

        Assert.assertTrue(result1.size() == 4);//three users and an extra key for all forms

        Assert.assertTrue(result1.containsKey(user1Key));
        Assert.assertTrue(result1.get(user1Key).size() == 2);
        Assert.assertTrue(result1.get(user1Key).containsKey(form1));
        Assert.assertTrue(result1.get(user1Key).containsKey(form2));

        Assert.assertTrue(result1.containsKey(user2Key));
        Assert.assertTrue(result1.get(user2Key).size() == 1);
        Assert.assertTrue(result1.get(user2Key).containsKey(form3));

        Assert.assertTrue(result1.containsKey(user3Key));
        Assert.assertTrue(result1.get(user3Key).size() == 1);
        Assert.assertTrue(result1.get(user3Key).containsKey(form4));

        //test when user toggles off shared forms
        SharedPreferences sp2 = PreferenceManager.getDefaultSharedPreferences(formDownloadList);
        sp2.edit().putString(PreferencesActivity.KEY_USERNAME, user1).commit();
        sp2.edit().putBoolean(PreferencesActivity.KEY_SHOW_SHARED_FORMS, false).commit();

        HashMap<String, HashMap<String, FormDetails>> result2 = FormDownloadList
                .splitFormsIntoAccounts(formDownloadList, allForms);

        Assert.assertTrue(result2.size() == 1);//only the user's form list

        Assert.assertTrue(result2.containsKey(user1Key));
        Assert.assertTrue(result2.get(user1Key).size() == 2);
        Assert.assertTrue(result2.get(user1Key).containsKey(form1));
        Assert.assertTrue(result2.get(user1Key).containsKey(form2));

        //test a scenario where the user does not hve forms
        HashMap<String, FormDetails> otherUsersForms = (HashMap<String, FormDetails>)allForms.clone();
        otherUsersForms.remove(form1);
        otherUsersForms.remove(form2);

        SharedPreferences sp3 = PreferenceManager.getDefaultSharedPreferences(formDownloadList);
        sp3.edit().putString(PreferencesActivity.KEY_USERNAME, user1).commit();
        sp3.edit().putBoolean(PreferencesActivity.KEY_SHOW_SHARED_FORMS, false).commit();

        HashMap<String, HashMap<String, FormDetails>> result3 = FormDownloadList
                .splitFormsIntoAccounts(formDownloadList, otherUsersForms);

        Assert.assertTrue(result3.size() == 0);//should not show any of the other user's forms if
        //  shared forms is toggled off
    }
}