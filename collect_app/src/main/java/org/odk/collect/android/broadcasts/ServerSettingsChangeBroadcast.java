package org.odk.collect.android.broadcasts;

import android.content.Context;
import android.content.Intent;

import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;

import java.util.HashMap;

/**
 *
 * Created by Jason Rogena - jrogena@ona.io on 03/08/2017.
 */

public class ServerSettingsChangeBroadcast extends Broadcast {
    private static final String KEY_GOOGLE_ACCOUNT = "google_account";
    private static final String KEY_GOOGLE_SHEETS_URL = "google_sheets_url";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_USER = "user";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_FORMLIST_URL = "formlist_url";
    private static final String KEY_SUBMISSION_URL = "submission_url";
    private static final String KEY_PROTOCOL = "protocol";
    private static final String ACTION_SERVER_SETTINGS_CHANGED = "SERVER_SETTINGS_CHANGED";
    private final Context context;
    private static final HashMap<String, String> PREFERENCES_MAP;
    static {
        PREFERENCES_MAP = new HashMap<>();
        PREFERENCES_MAP.put(PreferenceKeys.KEY_PROTOCOL, KEY_PROTOCOL);
        PREFERENCES_MAP.put(PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT, KEY_GOOGLE_ACCOUNT);
        PREFERENCES_MAP.put(PreferenceKeys.KEY_GOOGLE_SHEETS_URL, KEY_GOOGLE_SHEETS_URL);
        PREFERENCES_MAP.put(PreferenceKeys.KEY_USERNAME, KEY_USER);
        PREFERENCES_MAP.put(PreferenceKeys.KEY_PASSWORD, KEY_PASSWORD);
        PREFERENCES_MAP.put(PreferenceKeys.KEY_SERVER_URL, KEY_SERVER_URL);
        PREFERENCES_MAP.put(PreferenceKeys.KEY_FORMLIST_URL, KEY_FORMLIST_URL);
        PREFERENCES_MAP.put(PreferenceKeys.KEY_SUBMISSION_URL, KEY_SUBMISSION_URL);
    }

    public ServerSettingsChangeBroadcast(Context context) {
        this.context = context;
    }

    public void broadcastChange(String preference, Object value) {
        GeneralSharedPreferences settings = GeneralSharedPreferences.getInstance();

        HashMap<String, String> preferences = new HashMap<>();
        for (String curSharedPrefKey : PREFERENCES_MAP.keySet()) {
            preferences.put(curSharedPrefKey, (String) settings.get(curSharedPrefKey));
        }
        if (PREFERENCES_MAP.keySet().contains(preference)) {
            preferences.put(preference, (String) value);
        }

        Intent intent = new Intent();
        for (String curKey : preferences.keySet()) {
            intent.putExtra(PREFERENCES_MAP.get(curKey), preferences.get(curKey));
        }

        sendBroadcast(context, intent, ACTION_SERVER_SETTINGS_CHANGED);
    }
}
