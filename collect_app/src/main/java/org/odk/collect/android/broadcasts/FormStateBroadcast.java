package org.odk.collect.android.broadcasts;

import android.content.Context;
import android.content.Intent;

import org.odk.collect.android.dto.Form;
import org.odk.collect.android.logic.FormDetails;

/**
 * Broadcasts form state events
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 03/08/2017.
 */

public class FormStateBroadcast extends Broadcast {
    private final Context context;
    private static final String ACTION_FORM_STATE_CHANGED = "FORM_STATE_CHANGED";
    public static final String STATE_FORM_DOWNLOADED = "form_downloaded";
    public static final String STATE_FORM_DELETED = "form_deleted";
    public static final String STATE_SUBMISSION_CREATED = "submission_created";
    public static final String STATE_SUBMISSION_EDITED = "submission_edited";
    public static final String STATE_SUBMISSIONS_SENT = "submissions_sent";
    public static final String STATE_SUBMISSIONS_DELETED = "submissions_deleted";
    public static final String KEY_STATE = "state";
    public static final String KEY_FORM_NAME = "form_name";
    public static final String KEY_DOWNLOAD_URL = "download_url";
    public static final String KEY_MANIFEST_URL = "manifest_url";
    public static final String KEY_FORM_ID = "form_id";
    public static final String KEY_FORM_VERSION = "form_version";

    public FormStateBroadcast(Context context) {
        this.context = context;
    }

    public void broadcastState(FormDetails formDetails, String state) {
        Intent intent = new Intent();
        intent.putExtra(KEY_STATE, state);
        addFormDetails(intent, formDetails);
        sendBroadcast(context, intent, ACTION_FORM_STATE_CHANGED);
    }

    public void broadcastState(Form form, String state) {
        Intent intent = new Intent();
        intent.putExtra(KEY_STATE, state);
        addForm(intent, form);
        sendBroadcast(context, intent, ACTION_FORM_STATE_CHANGED);
    }

    private void addFormDetails(Intent intent, FormDetails formDetails) {
        intent.putExtra(KEY_FORM_NAME, formDetails.formName);
        intent.putExtra(KEY_DOWNLOAD_URL, formDetails.downloadUrl);
        intent.putExtra(KEY_MANIFEST_URL, formDetails.manifestUrl);
        intent.putExtra(KEY_FORM_ID, formDetails.formID);
        intent.putExtra(KEY_FORM_VERSION, formDetails.formVersion);
    }

    private void addForm(Intent intent, Form form) {
        intent.putExtra(KEY_FORM_NAME, form.getDisplayName());
        intent.putExtra(KEY_DOWNLOAD_URL, form.getJrDownloadUrl());
        intent.putExtra(KEY_MANIFEST_URL, form.getJrManifestUrl());
        intent.putExtra(KEY_FORM_ID, form.getJrFormId());
        intent.putExtra(KEY_FORM_VERSION, form.getJrVersion());
    }
}
