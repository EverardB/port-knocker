package eu.htcl.android.portknocker;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

/**
 *
 * @author everard
 */
public class Settings extends TabActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TabHost thost = getTabHost();
        thost.setup();

        //TabHost.TabSpec startspec = thost.newTabSpec("start");
        //Intent startintent = new Intent(this, HostList.class);
        //startintent.putExtra(DBAdapter.KEY_SELECTED, DBAdapter.START_MODE);
        //startspec.setContent(startintent);
        //startspec.setIndicator("Start", getResources().getDrawable(R.drawable.start));
        //thost.addTab(startspec);

        //TabHost.TabSpec stopspec = thost.newTabSpec("stop");
        //Intent stopintent = new Intent(this, HostList.class);
        //stopintent.putExtra(DBAdapter.KEY_SELECTED, DBAdapter.STOP_MODE);
        //stopspec.setContent(stopintent);
        //stopspec.setIndicator("Stop", getResources().getDrawable(R.drawable.stop));
        //thost.addTab(stopspec);
    }
}
