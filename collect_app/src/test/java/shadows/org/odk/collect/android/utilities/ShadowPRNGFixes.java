package shadows.org.odk.collect.android.utilities;

import org.odk.collect.android.utilities.PRNGFixes;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 05/01/2017
 */
@Implements(PRNGFixes.class)
public class ShadowPRNGFixes {
    @Implementation
    public static void apply() {
        //override whatever code this method executes
    }
}
