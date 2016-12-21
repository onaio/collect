package io.ona.collect.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import io.ona.collect.android.utils.MqttUtils;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 21/12/2016
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "boot receiver called", Toast.LENGTH_LONG).show();
        MqttUtils.initMqttAndroidClient();
    }
}
