/*
 * Copyright (C) 2011 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.logic;

import android.util.Log;

import java.io.Serializable;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormDetails implements Serializable, Comparator<FormDetails>, Comparable<FormDetails> {
    /**
     * 
     */
    private static final String TAG = "FormDetails";
    private static final long serialVersionUID = 1L;

    public final String errorStr;

    public final String formName;
    public final String downloadUrl;
    public final String manifestUrl;
    public final String formID;
    public final String formVersion;


    public FormDetails(String error) {
        manifestUrl = null;
        downloadUrl = null;
        formName = null;
        formID = null;
        formVersion = null;
        errorStr = error;
    }


    public FormDetails(String name, String url, String manifest, String id, String version) {
        manifestUrl = manifest;
        downloadUrl = url;
        formName = name;
        formID = id;
        formVersion = version;
        errorStr = null;
    }

    public static String getOnaFormId(FormDetails formDetails) {
        String formDownloadUrl = formDetails.downloadUrl;

        if (formDownloadUrl != null) {
            Pattern pattern = Pattern.compile("http[s]?://[\\w\\.]+/[\\w]+/forms/([\\d]+)/form\\.xml");
            Matcher matcher = pattern.matcher(formDownloadUrl);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } else {
            Log.d(TAG, "Download url is null for " + formDetails.formID);
        }

        return null;
    }

    /**
     * This method extracts the Ona user that owns a form from the form's Download URL
     *
     * @param formDetails The FormDetails to obtain the user from
     * @return The Ona User that owns the form or null if something goes wrong
     */
    public static String getOnaUser(FormDetails formDetails) {
        String formDownloadUrl = formDetails.downloadUrl;

        if (formDownloadUrl != null) {
            Pattern pattern = Pattern.compile("http[s]?://[\\w\\.]+/([\\w]+)/forms/[\\d]+/form\\.xml");
            Matcher matcher = pattern.matcher(formDownloadUrl);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } else {
            Log.d(TAG, "Download url is null for " + formDetails.formID);
        }

        return null;
    }

    /**
     * Comparison using the form name for sorting
     *
     * @param another The object to be compared with this FormDetails
     * @return
     */
    @Override
    public int compareTo(FormDetails another) {
        if (formName != null && another != null && another.formName != null) {
            return formName.compareTo(another.formName);
        } else if (formName == null) {
            return -1;// This object is considered less than another
        } else {// Either another, or another's formName is null
            return 1;
        }
    }

    /**
     * Comparison based on the two FormDetails' form name for sorting
     *
     * @param lhs The first FormDetails
     * @param rhs The second FormDetails
     * @return
     */
    @Override
    public int compare(FormDetails lhs, FormDetails rhs) {
        if (lhs != null) {
            return lhs.compareTo(rhs);
        } else {
            return -1;// lhs is considered less than rhs
        }
    }
}
