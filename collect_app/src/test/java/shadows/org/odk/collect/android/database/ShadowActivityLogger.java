package shadows.org.odk.collect.android.database;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.database.ActivityLogger;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 05/01/2017
 */
@Implements(ActivityLogger.class)
public class ShadowActivityLogger {
    @Implementation
    public void open() {
    }
    @Implementation
    private void log(String object, String context, String action, String instancePath, FormIndex index, String param1, String param2) {
    }
}
