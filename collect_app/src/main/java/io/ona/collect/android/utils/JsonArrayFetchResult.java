package io.ona.collect.android.utils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 06/01/2017
 */
public class JsonArrayFetchResult {
    public final String errorMessage;
    public final int responseCode;
    public final JSONArray array;

    public JsonArrayFetchResult(JSONArray array) {
        this.errorMessage = null;
        this.responseCode = 0;
        this.array = array;
    }

    public JsonArrayFetchResult(String errorMessage, int responseCode) {
        this.array = null;
        this.errorMessage = errorMessage;
        this.responseCode = responseCode;
    }
}
