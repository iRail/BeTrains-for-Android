package tof.cv.mpp.Activity;

import java.util.ArrayList;

import tof.cv.mpp.R;
import tof.cv.mpp.Activity.BeTrainsActivity.AbMenu;
import tof.cv.mpp.Activity.BeTrainsActivity.AbMenuAdapter;
import android.content.Context;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class BeTrainsPreferenceActivity extends SherlockPreferenceActivity
		implements ActionBar.OnNavigationListener {
	// The onNavigationItemSelected is called when the view is drawn, I really
	// would like to find something better than this workaround
	private boolean firstCall = true;
	public int index;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Context context = getSupportActionBar().getThemedContext();

		if (findViewById(R.id.istablet) == null) {
			ArrayList<AbMenu> data = new ArrayList<AbMenu>();
			data.add(new AbMenu(R.drawable.ab_planner,
					getString(R.string.btn_home_planner), 0));
			data.add(new AbMenu(R.drawable.ab_twit,
					getString(R.string.btn_home_twitter), 1));
			data.add(new AbMenu(R.drawable.ab_traffic,
					getString(R.string.btn_home_traffic), 2));
			data.add(new AbMenu(R.drawable.ab_chat,
					getString(R.string.btn_home_chat), 3));
			data.add(new AbMenu(R.drawable.ab_starred,
					getString(R.string.btn_home_starred), 4));
			data.add(new AbMenu(R.drawable.ab_closest,
					getString(R.string.btn_closest_stations), 5));
			data.add(new AbMenu(R.drawable.ab_settings,
					getString(R.string.btn_home_settings), 6));
			data.add(new AbMenu(R.drawable.ab_irail, "Liveboards", 7));
			data.add(new AbMenu(R.drawable.ab_map,
					getString(R.string.btn_home_contact), 8));

			AbMenuAdapter adapter = new AbMenuAdapter(context,
					R.layout.row_menu, data);

			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_LIST);
			getSupportActionBar().setListNavigationCallbacks(adapter, this);
			getSupportActionBar().setDisplayShowHomeEnabled(false);
			getSupportActionBar().setDisplayShowTitleEnabled(false);
			getSupportActionBar().setSelectedNavigationItem(index);
		}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (firstCall) {
			firstCall = false;
			return true;
		} else {
			BeTrainsActivity.openActivityFromiD(this, itemId);
			return true;
		}

	}
}
