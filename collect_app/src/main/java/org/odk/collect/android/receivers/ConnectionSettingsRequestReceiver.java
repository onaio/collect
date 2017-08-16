package org.odk.collect.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.odk.collect.android.broadcasts.ConnectionSettingsChangeBroadcast;

/**
 * Created by Jason Rogena - jrogena@ona.io on 16/08/2017.
 */

public class ConnectionSettingsRequestReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectionSettingsChangeBroadcast broadcast = new ConnectionSettingsChangeBroadcast(context);
        broadcast.sendBroadcast(null, null);
    }
}
