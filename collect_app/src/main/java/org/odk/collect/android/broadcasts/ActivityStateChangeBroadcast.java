package org.odk.collect.android.broadcasts;

import android.app.Activity;
import android.content.Intent;

/**
 * Broadcasts {@link Activity} state changes
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 02/08/2017.
 */

public class ActivityStateChangeBroadcast extends Broadcast {
    private static final String KEY_ACTIVITY_NAME = "activity_name";
    private static final String KEY_STATE = "state";
    private static final String ACTION_ACTIVITY_STATE_CHANGED = "ACTIVITY_STATE_CHANGED";
    public static final String STATE_CREATED = "created";
    public static final String STATE_STARTED = "started";
    public static final String STATE_RESUMED = "resumed";
    public static final String STATE_PAUSED = "paused";
    public static final String STATE_STOPPED = "stopped";
    private final Activity activity;

    public ActivityStateChangeBroadcast(Activity activity) {
        this.activity = activity;
    }

    public void broadcastState(String state) {
        Intent intent = new Intent();
        intent.putExtra(KEY_ACTIVITY_NAME, activity.getClass().getCanonicalName());
        intent.putExtra(KEY_STATE, state);
        sendBroadcast(activity, intent, ACTION_ACTIVITY_STATE_CHANGED);
    }
}
