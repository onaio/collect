package io.ona.collect.android.services;

import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

/**
 * This service is what receives messages from FCM
 *
 * Created by Jason Rogena - jrogena@ona.io on 14/11/2016.
 */

public class FirebaseCloudMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "CloudMessagingService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived called");
    }
}
