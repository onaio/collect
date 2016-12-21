package io.ona.collect.android.logic.mqtt;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import org.odk.collect.android.activities.FormDownloadList;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormDetails;

import java.util.HashMap;
import java.util.Random;

import io.ona.collect.android.R;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 20/12/2016
 */
public class FormSchemaUpdateHandler implements MqttMessageHandler {
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
            sendUpdateNotification(formDetails);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This method checks whether the provided form has an update, and sends a notification to the
     * notification centre if so
     *
     * @param formDetails   The details of the form
     */
    public void sendUpdateNotification(FormDetails formDetails) {
        //check if form actually needs an update
        if(FormDownloadList.isLocalFormSuperseded(formDetails.formID, formDetails.formVersion, formDetails.formHash)) {
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
    }

    private int getFormNotificationId(String formId) {
        if(!formNotificationIds.containsKey(formId)) {
            Random random = new Random();
            formNotificationIds.put(formId, random.nextInt(MAX_NOTIFICATION_ID) + MIN_NOTIFICATION_ID);
        }

        return formNotificationIds.get(formId);
    }
}
