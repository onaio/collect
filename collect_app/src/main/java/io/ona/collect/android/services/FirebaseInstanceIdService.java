package io.ona.collect.android.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Created by Jason Rogena - jrogena@ona.io on 14/11/2016.
 */

public class FirebaseInstanceIdService extends com.google.firebase.iid.FirebaseInstanceIdService {
    private static final String TAG = "InstanceIdOnaService";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed Firebase token = "+token);
        sendTokenToServer(token);
    }

    private boolean sendTokenToServer(final String token) {
        //send the logged in user with the provided token to the Ona backend
        return false;
    }
}
