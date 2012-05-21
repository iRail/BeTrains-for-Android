package tof.cv.mpp.Activity;

import java.util.ArrayList;

import tof.cv.mpp.ChatActivity;
import tof.cv.mpp.ClosestActivity;
import tof.cv.mpp.MyPreferenceActivity;
import tof.cv.mpp.PlannerActivity;
import tof.cv.mpp.R;
import tof.cv.mpp.StarredActivity;
import tof.cv.mpp.TrafficActivity;
import tof.cv.mpp.TwitterActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class BeTrainsActivity extends SherlockFragmentActivity implements
		ActionBar.OnNavigationListener {

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
		} else {
			//Do all the tablet stuff
		}		

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (firstCall) {
			firstCall = false;
			return true;
		} else {
			openActivityFromiD(this, itemId);
			return true;
		}

	}

	public static void openActivityFromiD(Activity a, long itemId) {
		switch ((int) itemId) {
		case 0:
			a.startActivity(new Intent(a, PlannerActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_CLEAR_TASK
							| Intent.FLAG_ACTIVITY_NEW_TASK));
			break;
		case 1:
			a.startActivity(new Intent(a, TwitterActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_CLEAR_TASK
							| Intent.FLAG_ACTIVITY_NEW_TASK));
			break;
		case 2:
			a.startActivity(new Intent(a, TrafficActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_CLEAR_TASK
							| Intent.FLAG_ACTIVITY_NEW_TASK));
			break;
		case 3:
			a.startActivity(new Intent(a, ChatActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_CLEAR_TASK
							| Intent.FLAG_ACTIVITY_NEW_TASK));
			break;
		case 4:
			a.startActivity(new Intent(a, StarredActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_CLEAR_TASK
							| Intent.FLAG_ACTIVITY_NEW_TASK));
			break;
		case 5:
			a.startActivity(new Intent(a, ClosestActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_CLEAR_TASK
							| Intent.FLAG_ACTIVITY_NEW_TASK));
			break;
		case 6:
			a.startActivity(new Intent(a, MyPreferenceActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_CLEAR_TASK
							| Intent.FLAG_ACTIVITY_NEW_TASK));
			break;
		case 7:
			// TODO: Open Google Play
			break;
		case 9:
			// TODO: Open Alert Dialog
			break;
		}
	}

	public static class AbMenu {
		public int icon;
		public String title;
		public int id;

		public AbMenu() {
			super();
		}

		public AbMenu(int icon, String title, int id) {
			super();
			this.icon = icon;
			this.title = title;
			this.id = id;
		}
	}

	public static class AbMenuAdapter extends BaseAdapter {

		Context context;
		int layoutResourceId;
		ArrayList<AbMenu> data;
		LayoutInflater inflater;

		public AbMenuAdapter(Context a, int textViewResourceId,
				ArrayList<AbMenu> data) {
			super();
			this.data = data;
			inflater = (LayoutInflater) a
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.context = a;
			this.layoutResourceId = textViewResourceId;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.row_menu, null);
			}
			TextView tvActivityname = (TextView) v
					.findViewById(R.id.activityname);
			ImageView ivImage = (ImageView) v.findViewById(R.id.avatar);
			final AbMenu item = data.get(position);

			if (item != null) {
				tvActivityname.setText(item.title);
				ivImage.setImageResource(item.icon);
			}
			return v;
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return data.get(position).id;
		}
	}

}
