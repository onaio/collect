package org.odk.collect.android.broadcasts;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.odk.collect.android.application.Collect;

/**
 * Created by Jason Rogena - jrogena@ona.io on 02/08/2017.
 */

public class Broadcast {
    private static final String PERMISSION_RECEIVE_BROADCASTS
            = "org.odk.collect.android.permission.RECEIVE_BROADCASTS";

    protected final boolean sendBroadcast(Context context, Intent broadcastIntent, String action) {
        if (context != null) {
            broadcastIntent.setAction(constructProperActionName(action));
            context.sendBroadcast(broadcastIntent, PERMISSION_RECEIVE_BROADCASTS);
            return true;
        }

        return false;
    }

    private String constructProperActionName(String action) {
        return String.format("%s.action.%s", Collect.getInstance().getPackageName(), action);
    }
}
