package org.odk.collect.android.logic;

import org.junit.Assert;
import org.junit.Test;
import org.odk.collect.android.activities.FormDownloadList;

import static org.junit.Assert.*;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 09/01/2017
 */
public class FormDetailsTest {

    @Test
    public void testGetOnaFormId() throws Exception {
        String formId1 = "221321";
        // Test a normal url
        String url1 = "https://odk.ona.io/user1/forms/" + formId1 + "/form.xml";
        Assert.assertTrue(FormDetails.getOnaFormId(new FormDetails(null, url1, null, null, null))
                .equals(formId1));

        // Test http url
        String url2 = "http://odk.ona.io/user1/forms/" + formId1 + "/form.xml";
        Assert.assertTrue(FormDetails.getOnaFormId(new FormDetails(null, url2, null, null, null))
                .equals(formId1));

        // Test other ona URL
        String url3 = "http://ona.io/user1/forms/" + formId1 + "/form.xml";
        Assert.assertTrue(FormDetails.getOnaFormId(new FormDetails(null, url3, null, null, null))
                .equals(formId1));

        // Test bad URL
        String url4 = "http://odk.ona.io/user1/forms/" + formId1 + "/form";
        Assert.assertTrue(FormDetails.getOnaFormId(new FormDetails(null, url4, null, null, null))
                == null);

        // Test non Ona URL
        String url5 = "http://fa.la.la.la/user1/forms/" + formId1 + "/form.xml";
        Assert.assertTrue(FormDetails.getOnaFormId(new FormDetails(null, url5, null, null, null))
                .equals(formId1));

        // Test a bad form id
        String formId2 = "231123d";
        String url6 = "http://odk.ona.io/user1/forms/" + formId2 + "/form.xml";
        Assert.assertTrue(FormDetails.getOnaFormId(new FormDetails(null, url6, null, null, null))
                == null);
    }

    @Test
    public void testGetOnaUser() throws Exception {
        String username1 = "dfsDFE12_fsd";
        // Test a normal url
        String url1 = "https://odk.ona.io/" + username1 + "/forms/129132/form.xml";
        Assert.assertTrue(FormDetails.getOnaUser(new FormDetails(null, url1, null, null, null))
                .equals(username1));

        // Test http url
        String url2 = "http://odk.ona.io/" + username1 + "/forms/129132/form.xml";
        Assert.assertTrue(FormDetails.getOnaUser(new FormDetails(null, url2, null, null, null))
                .equals(username1));

        // Test other ona URL
        String url3 = "http://ona.io/" + username1 + "/forms/129132/form.xml";
        Assert.assertTrue(FormDetails.getOnaUser(new FormDetails(null, url3, null, null, null))
                .equals(username1));

        // Test bad URL
        String url4 = "http://odk.ona.io/" + username1 + "/forms/129132/form";
        Assert.assertTrue(FormDetails.getOnaUser(new FormDetails(null, url4, null, null, null))
                == null);

        // Test non Ona URL
        String url5 = "http://fa.la.la.la/" + username1 + "/forms/129132/form.xml";
        Assert.assertTrue(FormDetails.getOnaUser(new FormDetails(null, url5, null, null, null))
                .equals(username1));

        // Test a bad username
        String username2 = "dfsDFE12_fsd ";
        String url6 = "http://odk.ona.io/" + username2 + "/forms/129132/form.xml";
        Assert.assertTrue(FormDetails.getOnaUser(new FormDetails(null, url6, null, null, null))
                == null);
    }

    @Test
    public void testCompareTo() throws Exception {
        // Test alphanumerics in same case
        FormDetails form1 = new FormDetails("a", null, null, null, null);
        FormDetails form2 = new FormDetails("b", null, null, null, null);
        Assert.assertTrue(form1.compareTo(form2) < 0);

        FormDetails form3 = new FormDetails("9", null, null, null, null);
        Assert.assertTrue(form1.compareTo(form3) > 0);

        // Test alphanumerics in different cases
        FormDetails form4 = new FormDetails("A", null, null, null, null);
        Assert.assertTrue(form1.compareTo(form4) > 0);

        // Test null instances
        Assert.assertTrue(form1.compareTo(null) > 0);

        FormDetails form5 = new FormDetails(null, null, null, null, null);
        Assert.assertTrue(form1.compareTo(form5) > 0);
    }

    @Test
    public void testCompare() throws Exception {
        // Test normal scenario
        FormDetails lhs1 = new FormDetails("a", null, null, null, null);
        FormDetails rhs1 = new FormDetails("b", null, null, null, null);
        Assert.assertTrue(lhs1.compare(lhs1, rhs1) < 1);

        // Test null left hand side
        Assert.assertTrue(rhs1.compare(null, rhs1) < 0);

        // Test null right hand side
        Assert.assertTrue(lhs1.compare(lhs1, null) > 0);
    }
}