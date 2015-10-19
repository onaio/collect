package io.ona.collect.android.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.RenderScript;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import io.ona.collect.android.R;
import io.ona.collect.android.listeners.FormListDownloaderListener;
import io.ona.collect.android.logic.FormDetails;
import io.ona.collect.android.tasks.DownloadFormListTask;
import io.ona.collect.android.tasks.DownloadFormsTask;

/**
 * Created by onamacuser on 10/19/15.
 */
public class UpdatesCheckService extends Service implements FormListDownloaderListener {
    // constant
    public static final long NOTIFY_INTERVAL = 10 * 1000; // 10 Minutes.
    NotificationCompat.Builder mBuilder;
    // Sets an ID for the notification
    int mNotificationId = 001;
    // Gets an instance of the NotificationManager service
    NotificationManager mNotifyMgr;

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    private DownloadFormListTask mDownloadFormListTask;
    private HashMap<String, FormDetails> mFormNamesAndURLs = new HashMap<String, FormDetails>();

    Intent resultIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.common_signin_btn_icon_dark)
                        .setContentTitle("New Form")
                        .setContentText("New or updated form(s) are available.")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setWhen(0);
        mNotifyMgr =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // cancel if already existed
        if (mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }

    @Override
    public void formListDownloadingComplete(HashMap<String, FormDetails> result) {
        mDownloadFormListTask.setDownloaderListener(null);
        mDownloadFormListTask = null;

        if (result == null) {
            Log.e("Err", "Formlist Downloading returned null.  That shouldn't happen");
            return;
        }

        if (result.containsKey(DownloadFormListTask.DL_ERROR_MSG)) {
            // Download failed
            Log.e("Err", "Download failed.");
        } else {
            // Everything worked. Clear the list and add the results.
            mFormNamesAndURLs = result;

            ArrayList<String> ids = new ArrayList<String>(mFormNamesAndURLs.keySet());
            for (int i = 0; i < result.size(); i++) {
                String formDetailsKey = ids.get(i);
                FormDetails details = mFormNamesAndURLs.get(formDetailsKey);
                if (UpdatedFormDownloadList.isLocalFormSuperseded(details.formID, details.formVersion)) {
                    resultIntent = new Intent(this, UpdatedFormDownloadList.class);
                    resultIntent.putExtra("FORMNAMESANDURLS", mFormNamesAndURLs);
                    // Because clicking the notification opens a new ("special") activity, there's
                    // no need to create an artificial back stack.
                    PendingIntent resultPendingIntent =
                            PendingIntent.getActivity(
                                    this,
                                    0,
                                    resultIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);
                    // Builds the notification and issues it.
                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                }
            }
        }
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    downloadFormList();
                }

            });
        }
    }

    /**
     * Starts the download task.
     */
    private void downloadFormList() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

        if (ni == null || !ni.isConnected()) {
            Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
        } else {

            mFormNamesAndURLs = new HashMap<String, FormDetails>();

            if (mDownloadFormListTask != null &&
                    mDownloadFormListTask.getStatus() != AsyncTask.Status.FINISHED) {
                return; // we are already doing the download!!!
            } else if (mDownloadFormListTask != null) {
                mDownloadFormListTask.setDownloaderListener(null);
                mDownloadFormListTask.cancel(true);
                mDownloadFormListTask = null;
            }

            mDownloadFormListTask = new DownloadFormListTask();
            mDownloadFormListTask.setDownloaderListener(this);
            mDownloadFormListTask.execute();
        }
    }
}