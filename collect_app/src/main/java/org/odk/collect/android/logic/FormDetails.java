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

import java.io.Serializable;

public class FormDetails implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public final String errorStr;

    public final String formName;
    public final String downloadUrl;
    public final String manifestUrl;
    public final String formID;
    public final String formVersion;
    public final String formHash;


    public FormDetails(String error) {
        manifestUrl = null;
        downloadUrl = null;
        formName = null;
        formID = null;
        formVersion = null;
        formHash = null;
        errorStr = error;
    }


    public FormDetails(String name, String url, String manifest, String id, String version) {
        manifestUrl = manifest;
        downloadUrl = url;
        formName = name;
        formID = id;
        formVersion = version;
        formHash = null;
        errorStr = null;
    }

    public FormDetails(String name, String url, String manifest, String id, String version, String hash) {
        manifestUrl = manifest;
        downloadUrl = url;
        formName = name;
        formID = id;
        formVersion = version;
        formHash = hash;
        errorStr = null;
    }

    /**
     * This method removes the hash type (e.g 'md5:') prefix put in the JavaRosa hash for a form
     *
     * @param javaRosaHash
     * @return
     */
    public static String formatHash(String javaRosaHash) {
        if(javaRosaHash != null) {
            javaRosaHash = javaRosaHash.replace("md5:", "");
        }

        return javaRosaHash;
    }

}
