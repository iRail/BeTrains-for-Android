package tof.cv.mpp.Activity;

import java.util.ArrayList;

import tof.cv.mpp.R;
import tof.cv.mpp.adapter.TweetItemAdapter.ViewHolder;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class BeTrainsActivity extends SherlockFragmentActivity implements
		ActionBar.OnNavigationListener {
	// private CustomSpinnerdapter list;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Context context = getSupportActionBar().getThemedContext();

		ArrayList<AbMenu> data = new ArrayList<AbMenu>();
		data.add(new AbMenu(R.drawable.ab_planner,
				getString(R.string.btn_home_planner), 1));
		data.add(new AbMenu(R.drawable.ab_twit,
				getString(R.string.btn_home_twitter), 2));
		data.add(new AbMenu(R.drawable.ab_traffic,
				getString(R.string.btn_home_traffic), 3));
		data.add(new AbMenu(R.drawable.ab_chat,
				getString(R.string.btn_home_chat), 4));
		data.add(new AbMenu(R.drawable.ab_starred,
				getString(R.string.btn_home_starred), 5));
		data.add(new AbMenu(R.drawable.ab_closest,
				getString(R.string.btn_closest_stations), 6));
		data.add(new AbMenu(R.drawable.ab_settings,
				getString(R.string.btn_home_settings), 7));
		data.add(new AbMenu(R.drawable.ab_irail, "Liveboards", 8));
		data.add(new AbMenu(R.drawable.ab_map,
				getString(R.string.btn_home_contact), 9));

		AbMenuAdapter adapter = new AbMenuAdapter(context, R.layout.row_menu,
				data);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(adapter, this);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		Toast.makeText(this, itemPosition + " - " + itemId, Toast.LENGTH_LONG)
				.show();
		return true;
	}

	public class AbMenu {
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

	public class AbMenuAdapter extends BaseAdapter {

		Context context;
		int layoutResourceId;
		ArrayList<AbMenu> data;
		LayoutInflater inflater;

		public AbMenuAdapter(Context a, int textViewResourceId,
				ArrayList<AbMenu> data) {
			// super(a, textViewResourceId, data);
			this.data = data;
			inflater = (LayoutInflater) a
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.context = a;
			this.layoutResourceId = textViewResourceId;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			ViewHolder holder;
			if (v == null) {
				// LayoutInflater vi =
				// (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.row_menu, null);
				holder = new ViewHolder();
				holder.username = (TextView) v.findViewById(R.id.username);
				holder.image = (ImageView) v.findViewById(R.id.avatar);
				v.setTag(holder);
			} else
				holder = (ViewHolder) v.getTag();

			final AbMenu item = data.get(position);
			if (item != null) {
				holder.username.setText(item.title);
				holder.image.setImageResource(item.icon);

			}
			return v;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
	}

}
