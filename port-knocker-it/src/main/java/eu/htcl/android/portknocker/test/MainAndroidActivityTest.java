package eu.htcl.android.portknocker.test;

import android.test.ActivityInstrumentationTestCase2;
import eu.htcl.android.portknocker.MainAndroidActivity;

public class MainAndroidActivityTest extends ActivityInstrumentationTestCase2<MainAndroidActivity> {

    public MainAndroidActivityTest() {
        super("eu.htcl.android.portknocker", MainAndroidActivity.class); 
    }

    public void testActivity() {
        MainAndroidActivity activity = getActivity();
        assertNotNull(activity);
    }
}
