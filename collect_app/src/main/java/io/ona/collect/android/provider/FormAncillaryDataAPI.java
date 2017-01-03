package io.ona.collect.android.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 03/01/2017
 */
public class FormAncillaryDataAPI {
    public static final String AUTHORITY = "io.ona.collect.android.provider.odk.forms.ancillary";

    public FormAncillaryDataAPI() {}

    public static final class FormDataColumns implements BaseColumns {
        public FormDataColumns() {}

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY +
                "/formAncillaryData");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ona.form.ancillary";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ona.form" +
                ".ancillary";

        public static final String NEEDS_UPDATE = "needsUpdate";
    }
}
