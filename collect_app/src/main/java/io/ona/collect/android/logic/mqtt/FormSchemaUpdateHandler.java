package io.ona.collect.android.logic.mqtt;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import org.odk.collect.android.activities.FormChooserList;
import org.odk.collect.android.activities.FormDownloadList;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.tasks.DownloadFormsTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import io.ona.collect.android.R;
import io.ona.collect.android.provider.InstanceProviderAPI;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 20/12/2016
 */
public class FormSchemaUpdateHandler implements MqttMessageHandler {
    private static final String TAG = "FormSchemaUpdateHandler";
    private static final String MESSAGE_TYPE_SCHEMA_UPDATE = "odk.form.schema.update";
    private static final String KEY_FORM_ID = "formID";
    private static final String KEY_NAME = "name";
    private static final String KEY_MAJOR_MINOR_VERSION = "majorMinorVersion";
    private static final String KEY_VERSION = "version";
    private static final String KEY_HASH = "hash";
    private static final String KEY_DESCRIPTION_TEXT = "descriptionText";
    private static final String KEY_DOWNLOAD_URL = "downloadUrl";
    private static final String KEY_MANIFEST_URL = "manifestUrl";
    private static final int MIN_NOTIFICATION_ID = 2000;
    private static final int MAX_NOTIFICATION_ID = 4000;

    private final HashMap<String, Integer> formNotificationIds;

    public FormSchemaUpdateHandler() {
        formNotificationIds = new HashMap<>();
    }

    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        try {
            String rawMessage = new String(message.getPayload());
            JSONObject data = new JSONObject(rawMessage);
            if (data.has(KEY_MESSAGE_ID)
                    && data.has(KEY_MESSAGE_TYPE)
                    && data.has(KEY_PAYLOAD)
                    && data.has(KEY_TIME)) {
                String messageType = data.getString(KEY_MESSAGE_TYPE);
                if (messageType.equals(MESSAGE_TYPE_SCHEMA_UPDATE)) {
                    JSONObject payload = data.getJSONObject(KEY_PAYLOAD);
                    if (payload.has(KEY_FORM_ID)
                            && payload.has(KEY_NAME)
                            && payload.has(KEY_MAJOR_MINOR_VERSION)
                            && payload.has(KEY_VERSION)
                            && payload.has(KEY_HASH)
                            && payload.has(KEY_DESCRIPTION_TEXT)
                            && payload.has(KEY_DOWNLOAD_URL)
                            && payload.has(KEY_MANIFEST_URL)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean handle(String topic, MqttMessage message) {
        try {
            String rawMessage = new String(message.getPayload());
            JSONObject data = new JSONObject(rawMessage);
            JSONObject payload = data.getJSONObject(KEY_PAYLOAD);
            FormDetails formDetails = new FormDetails(payload.getString(KEY_NAME),
                    payload.getString(KEY_DOWNLOAD_URL),
                    payload.getString(KEY_MANIFEST_URL),
                    payload.getString(KEY_FORM_ID),
                    payload.getString(KEY_VERSION),
                    FormDetails.formatHash(payload.getString(KEY_HASH)));
            handleForm(formDetails);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void handleForm(FormDetails formDetails) {
        //check if form actually needs an update
        if(FormDownloadList.isLocalFormSuperseded(formDetails.formID, formDetails.formVersion, formDetails.formHash)) {
            FormDownloadList.setFormNeedsUpdate(formDetails.formID, true);
            SharedPreferences settings =
                    PreferenceManager.getDefaultSharedPreferences(Collect.getInstance());
            boolean autoDownload = settings.getBoolean(
                    PreferencesActivity.KEY_AUTODOWNLOAD_FORM_UPDATES, false);
            if(autoDownload) {
                Log.d(TAG, "Autodownload setting set to true");
                autoDownloadForm(formDetails);
            } else {
                Log.d(TAG, "Autodownload setting set to false");
                sendUpdateNotification(formDetails);
            }
        } else {
            FormDownloadList.setFormNeedsUpdate(formDetails.formID, false);
        }
    }

    private void autoDownloadForm(final FormDetails formDetails) {
        //check if there's a non-finalized response for the form on the device
        if(doesFormHaveIncompleteInstances(formDetails.formID)) {
            Log.i(TAG, "Form has incomplete instances");
            //not safe to auto download update
            sendUpdateNotification(formDetails);
        } else {
            Log.i(TAG, "Trying to download form");
            final String formVersion = formDetails.formVersion;

            ArrayList<FormDetails> formList = new ArrayList<>();
            formList.add(formDetails);
            DownloadFormsTask mDownloadFormsTask = new DownloadFormsTask();

            mDownloadFormsTask.setDownloaderListener(new FormDownloaderListener() {
                @Override
                public void formsDownloadingComplete(HashMap<FormDetails, String> result) {
                    boolean formDownloaded = false;
                    if(result != null
                            && result.size() == 1) {
                        for(FormDetails downloadedForm : result.keySet()) {
                            if(downloadedForm.formID.equals(formDetails.formID)) {
                                formDownloaded = true;
                                break;
                            }

                        }
                    }

                    Intent notifyIntent;
                    PendingIntent pendingNotify;
                    String contentText;
                    String contentTitle;

                    if(formDownloaded) {
                        notifyIntent = new Intent(Collect.getInstance(), FormChooserList.class);
                        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        pendingNotify = PendingIntent.getActivity(Collect.getInstance(), 0,
                                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        contentTitle = Collect.getInstance().getResources()
                                .getString(R.string.update_for_form, formDetails.formName);
                        if(formVersion != null && formVersion.length() > 0) {
                            contentText = Collect.getInstance().getResources().getString(
                                    R.string.form_update_downloaded,
                                    Collect.getInstance().getResources().getString(R.string.version_of, formVersion),
                                    formDetails.formName);
                        } else {
                            contentText = Collect.getInstance().getResources().getString(
                                    R.string.form_update_downloaded,
                                    Collect.getInstance().getResources().getString(R.string.a_newer_version_of),
                                    formDetails.formName);
                        }
                    } else {
                        notifyIntent = new Intent(Collect.getInstance(), FormDownloadList.class);
                        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        pendingNotify = PendingIntent.getActivity(Collect.getInstance(), 0,
                                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        contentTitle = Collect.getInstance().getResources()
                                .getString(R.string.update_failed, formDetails.formName);
                        if(formVersion != null && formVersion.length() > 0) {
                            contentText = Collect.getInstance().getResources().getString(
                                    R.string.failed_to_download_form_update,
                                    Collect.getInstance().getResources().getString(R.string.version_of, formVersion),
                                    formDetails.formName);
                        } else {
                            contentText = Collect.getInstance().getResources().getString(
                                    R.string.failed_to_download_form_update,
                                    Collect.getInstance().getResources().getString(R.string.the_new_version_of),
                                    formDetails.formName);
                        }

                    }

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Collect.getInstance())
                            .setSmallIcon(R.drawable.ic_stat_notes)
                            .setContentTitle(contentTitle)
                            .setContentText(contentText)
                            .setContentIntent(pendingNotify)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                            .setAutoCancel(true);

                    NotificationManager mNotificationManager = (NotificationManager) Collect.getInstance()
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(getFormNotificationId(formDetails.formID), mBuilder.build());
                }

                @Override
                public void progressUpdate(String currentFile, int progress, int total) {

                }
            });

            Intent notifyIntent = new Intent(Collect.getInstance(), FormDownloadList.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingNotify = PendingIntent.getActivity(Collect.getInstance(), 0,
                    notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            String contentText;
            if(formVersion != null && formVersion.length() > 0) {
                contentText = Collect.getInstance().getResources().getString(
                        R.string.downloading_form_update,
                        Collect.getInstance().getResources().getString(R.string.version_of, formVersion),
                        formDetails.formName);
            } else {
                contentText = Collect.getInstance().getResources().getString(
                        R.string.downloading_form_update,
                        Collect.getInstance().getResources().getString(R.string.a_newer_version_of),
                        formDetails.formName);
            }

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Collect.getInstance())
                    .setSmallIcon(R.drawable.ic_stat_notes)
                    .setContentTitle(Collect.getInstance().getResources().getString(R.string.updating_form, formDetails.formName))
                    .setContentText(contentText)
                    //.setContentIntent(pendingNotify)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                    .setAutoCancel(true);

            NotificationManager mNotificationManager = (NotificationManager) Collect.getInstance()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(getFormNotificationId(formDetails.formID), mBuilder.build());

            mDownloadFormsTask.execute(formList);
        }
    }

    private boolean doesFormHaveIncompleteInstances(String jrFormId) {
        String[] selectionArgs = { jrFormId, InstanceProviderAPI.STATUS_INCOMPLETE };
        String selection = InstanceProviderAPI.InstanceColumns.JR_FORM_ID
                + "=? and "
                + InstanceProviderAPI.InstanceColumns.STATUS +"=?";
        String[] fields = { InstanceProviderAPI.InstanceColumns.DISPLAY_NAME,
                InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT };

        Cursor instanceCursor = null;
        try {
            instanceCursor = Collect.getInstance().getContentResolver().query(
                    InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                    fields, selection, selectionArgs, null);
            if ( instanceCursor.getCount() > 0 ) {
                return true;
            }
        } finally {
            if (instanceCursor != null) {
                instanceCursor.close();
            }
        }

        return false;
    }

    /**
     * This method checks whether the provided form has an update, and sends a notification to the
     * notification centre if so
     *
     * @param formDetails   The details of the form
     */
    private void sendUpdateNotification(FormDetails formDetails) {
        Intent notifyIntent = new Intent(Collect.getInstance(), FormDownloadList.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingNotify = PendingIntent.getActivity(Collect.getInstance(), 0,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String formVersion = formDetails.formVersion;
        String contentText;
        if(formVersion != null && formVersion.length() > 0) {
            contentText = Collect.getInstance().getResources().getString(
                    R.string.form_update_ready_for_download,
                    Collect.getInstance().getResources().getString(R.string.version_of, formVersion),
                    formDetails.formName);
        } else {
            contentText = Collect.getInstance().getResources().getString(
                    R.string.form_update_ready_for_download,
                    Collect.getInstance().getResources().getString(R.string.a_newer_version_of),
                    formDetails.formName);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Collect.getInstance())
                .setSmallIcon(R.drawable.ic_stat_notes)
                .setContentTitle(Collect.getInstance().getResources().getString(R.string.update_for_form, formDetails.formName))
                .setContentText(contentText)
                .setContentIntent(pendingNotify)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) Collect.getInstance()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(getFormNotificationId(formDetails.formID), mBuilder.build());
    }

    private int getFormNotificationId(String formId) {
        if(!formNotificationIds.containsKey(formId)) {
            Random random = new Random();
            formNotificationIds.put(formId, random.nextInt(MAX_NOTIFICATION_ID) + MIN_NOTIFICATION_ID);
        }

        return formNotificationIds.get(formId);
    }
}
