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

    @Override
    public boolean canHandle(String topic, MqttMessage message) {
        try {
            String rawMessage = new String(message.getPayload());
            JSONObject data = new JSONObject(rawMessage);
            if(data.has(KEY_MESSAGE_ID)
                    && data.has(KEY_MESSAGE_TYPE)
                    && data.has(KEY_PAYLOAD)
                    && data.has(KEY_TIME)) {
                String messageType = data.getString(KEY_MESSAGE_TYPE);
                if(messageType.equals(MESSAGE_TYPE_SCHEMA_UPDATE)) {
                    JSONObject payload = data.getJSONObject(KEY_PAYLOAD);
                    if(payload.has(KEY_FORM_ID)
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
                    payload.getString(KEY_VERSION));

            Intent notifyIntent = new Intent(Collect.getInstance(), FormDownloadList.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingNotify = PendingIntent.getActivity(Collect.getInstance(), 0,
                    notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Collect.getInstance())
                    .setSmallIcon(R.drawable.notes)
                    .setContentTitle(Collect.getInstance().getResources().getString(R.string.update_for_form, formDetails.formName))
                    .setContentIntent(pendingNotify)
                    .setContentText(Collect.getInstance().getResources().getString(R.string.form_update_ready_for_download, formDetails.formName))
                    .setAutoCancel(false)
                    .setLargeIcon(
                            BitmapFactory.decodeResource(Collect.getInstance().getResources(),
                                    android.R.drawable.ic_dialog_info));

            NotificationManager mNotificationManager = (NotificationManager)Collect.getInstance()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(data.getInt(KEY_MESSAGE_ID), mBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
