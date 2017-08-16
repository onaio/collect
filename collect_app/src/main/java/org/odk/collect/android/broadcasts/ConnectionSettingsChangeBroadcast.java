package org.odk.collect.android.broadcasts;

import android.content.Context;
import android.content.Intent;

import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * Created by Jason Rogena - jrogena@ona.io on 03/08/2017.
 */

public class ConnectionSettingsChangeBroadcast extends Broadcast {
    private static final String ACTION_SERVER_SETTINGS_CHANGED = "CONNECTION_SETTINGS_CHANGED";
    private final Context context;
    private static final ArrayList<String> CONNECTION_PREFERENCES;
    static {
        CONNECTION_PREFERENCES = new ArrayList<>();
        CONNECTION_PREFERENCES.add(PreferenceKeys.KEY_PROTOCOL);
        CONNECTION_PREFERENCES.add(PreferenceKeys.KEY_SELECTED_GOOGLE_ACCOUNT);
        CONNECTION_PREFERENCES.add(PreferenceKeys.KEY_GOOGLE_SHEETS_URL);
        CONNECTION_PREFERENCES.add(PreferenceKeys.KEY_USERNAME);
        CONNECTION_PREFERENCES.add(PreferenceKeys.KEY_PASSWORD);
        CONNECTION_PREFERENCES.add(PreferenceKeys.KEY_SERVER_URL);
        CONNECTION_PREFERENCES.add(PreferenceKeys.KEY_FORMLIST_URL);
        CONNECTION_PREFERENCES.add(PreferenceKeys.KEY_SUBMISSION_URL);
    }

    public ConnectionSettingsChangeBroadcast(Context context) {
        this.context = context;
    }

    /**
     * Broadcasts connection settings
     *
     * @param preference    The preference that just changed. Set to {@code null} if none
     * @param value         The new value of the preference that was just set. Set to {@code null}
     *                      if none
     */
    public void sendBroadcast(String preference, Object value) {
        GeneralSharedPreferences settings = GeneralSharedPreferences.getInstance();

        HashMap<String, String> preferences = new HashMap<>();
        for (String curSharedPrefKey : CONNECTION_PREFERENCES) {
            preferences.put(curSharedPrefKey, (String) settings.get(curSharedPrefKey));
        }
        if (preference != null && CONNECTION_PREFERENCES.contains(preference)) {
            preferences.put(preference, (String) value);
        }

        Intent intent = new Intent();
        for (String curKey : preferences.keySet()) {
            intent.putExtra(curKey, preferences.get(curKey));
        }

        sendBroadcast(context, intent, ACTION_SERVER_SETTINGS_CHANGED);
    }
}
