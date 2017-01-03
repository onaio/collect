package io.ona.collect.android.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ODKSQLiteOpenHelper;

import java.util.HashMap;

/**
 * This provider holds ancillary Ona data on a form
 *
 * @author Jason Rogena - jrogena@ona.io
 * @since 03/01/2017
 */
public class FormAncillaryDataProvider extends ContentProvider {

    private static final String t = "FormAncillaryData";

    private static final String DATABASE_NAME = "form_ancillary_data.db";
    private static final int DATABASE_VERSION = 1;
    private static final String FORM_DATA_TABLE_NAME = "form_ancillary_data";

    private static HashMap<String, String> sFormDataProjectionMap;

    private static final int FORM_DATA = 1;
    private static final int FORM_DATA_ID = 2;

    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends ODKSQLiteOpenHelper {
        // These exist in database versions 2 and 3, but not in 4...
        private static final String TEMP_FORMS_TABLE_NAME = "form_ancillary_data_v1";
        private static final String MODEL_VERSION = "modelVersion";

        DatabaseHelper(String databaseName) {
            super(Collect.METADATA_PATH, databaseName, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            onCreateNamed(db, FORM_DATA_TABLE_NAME);
        }

        private void onCreateNamed(SQLiteDatabase db, String tableName) {
            db.execSQL("CREATE TABLE " + tableName + " ("
                    + FormAncillaryDataAPI.FormDataColumns._ID + " integer primary key, "
                    + FormAncillaryDataAPI.FormDataColumns.NEEDS_UPDATE + " boolean not null );");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
    }

    private DatabaseHelper mDbHelper;

    private DatabaseHelper getDbHelper() {
        // wrapper to test and reset/set the dbHelper based upon the attachment state of the device.
        try {
            Collect.createODKDirs();
        } catch (RuntimeException e) {
            mDbHelper = null;
            return null;
        }

        if (mDbHelper != null) {
            return mDbHelper;
        }
        mDbHelper = new DatabaseHelper(DATABASE_NAME);
        return mDbHelper;
    }

    @Override
    public boolean onCreate() {
        // must be at the beginning of any activity that can be called from an external intent
        DatabaseHelper h = getDbHelper();
        if ( h == null ) {
            return false;
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(FORM_DATA_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
            case FORM_DATA:
                qb.setProjectionMap(sFormDataProjectionMap);
                break;

            case FORM_DATA_ID:
                qb.setProjectionMap(sFormDataProjectionMap);
                qb.appendWhere(FormAncillaryDataAPI.FormDataColumns._ID + "="
                        + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Get the database and run the query
        SQLiteDatabase db = getDbHelper().getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);

        // Tell the cursor what uri to watch, so it knows when its source data
        // changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case FORM_DATA:
                return FormAncillaryDataAPI.FormDataColumns.CONTENT_TYPE;

            case FORM_DATA_ID:
                return FormAncillaryDataAPI.FormDataColumns.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public synchronized Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != FORM_DATA) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = getDbHelper().getWritableDatabase();

        // first try to see if a record with this filename already exists...
        int formId = values.getAsInteger(FormAncillaryDataAPI.FormDataColumns._ID);//not the java
        // rosa formId

        String[] projection = { FormAncillaryDataAPI.FormDataColumns._ID };
        String[] selectionArgs = { String.valueOf(formId) };
        String selection = FormAncillaryDataAPI.FormDataColumns._ID + "=?";
        Cursor c = null;
        try {
            c = db.query(FORM_DATA_TABLE_NAME, projection, selection,
                    selectionArgs, null, null, null);
            if (c.getCount() > 0) {
                // already exists
                throw new SQLException("FAILED Insert into " + uri
                        + " -- row already exists for form with id " + formId);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        long rowId = db.insert(FORM_DATA_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri formDataUri = ContentUris.withAppendedId(FormAncillaryDataAPI.FormDataColumns.CONTENT_URI,
                    rowId);
            getContext().getContentResolver().notifyChange(formDataUri, null);
            return formDataUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    /**
     * This method removes the entry from the content provider, and also removes
     * any associated files. files: form.xml, [formmd5].formdef, formname-media
     * {directory}
     */
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        int count;

        switch (sUriMatcher.match(uri)) {
            case FORM_DATA:
                count = db.delete(FORM_DATA_TABLE_NAME, where, whereArgs);
                break;

            case FORM_DATA_ID:
                String formId = uri.getPathSegments().get(1);

                count = db.delete(
                        FORM_DATA_TABLE_NAME,
                        FormAncillaryDataAPI.FormDataColumns._ID
                                + "="
                                + formId
                                + (!TextUtils.isEmpty(where) ? " AND (" + where
                                + ')' : ""), whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
                      String[] whereArgs) {
        SQLiteDatabase db = getDbHelper().getWritableDatabase();
        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case FORM_DATA:
                count = db.update(FORM_DATA_TABLE_NAME, values, where, whereArgs);
                break;

            case FORM_DATA_ID:
                String formId = uri.getPathSegments().get(1);
                // Whenever file paths are updated, delete the old files.

                Cursor update = null;
                try {
                    update = this.query(uri, null, where, whereArgs, null);

                    // This should only ever return 1 record.
                    if (update.getCount() > 0) {
                        update.moveToFirst();
                        count = db.update(
                                FORM_DATA_TABLE_NAME,
                                values,
                                FormAncillaryDataAPI.FormDataColumns._ID
                                        + "="
                                        + formId
                                        + (!TextUtils.isEmpty(where) ? " AND ("
                                        + where + ')' : ""), whereArgs);
                    } else {
                        Log.e(t, "Attempting to update row that does not exist");
                    }
                } finally {
                    if (update != null) {
                        update.close();
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * This method creates a row in the FormAncillaryDataProvider corresponding to the form
     *
     * @param formId The formId of the form being deleted
     */
    public static void createAncillaryDataRow(long formId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FormAncillaryDataAPI.FormDataColumns._ID, formId);
        contentValues.put(FormAncillaryDataAPI.FormDataColumns.NEEDS_UPDATE, false);

        Uri uri = Collect.getInstance().getContentResolver()
                .insert(FormAncillaryDataAPI.FormDataColumns.CONTENT_URI, contentValues);
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(FormAncillaryDataAPI.AUTHORITY, "formAncillaryData", FORM_DATA);
        sUriMatcher.addURI(FormAncillaryDataAPI.AUTHORITY, "formAncillaryData/#", FORM_DATA_ID);

        sFormDataProjectionMap = new HashMap<String, String>();
        sFormDataProjectionMap.put(FormAncillaryDataAPI.FormDataColumns._ID,
                FormAncillaryDataAPI.FormDataColumns._ID);
        sFormDataProjectionMap.put(FormAncillaryDataAPI.FormDataColumns.NEEDS_UPDATE,
                FormAncillaryDataAPI.FormDataColumns.NEEDS_UPDATE);
    }
}
