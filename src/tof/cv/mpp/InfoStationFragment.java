package tof.cv.mpp;

import java.util.Date;

import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.Utils.UtilsWeb;
import tof.cv.mpp.Utils.UtilsWeb.Station;
import tof.cv.mpp.Utils.UtilsWeb.StationDeparture;
import tof.cv.mpp.adapter.StationInfoAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class InfoStationFragment extends ListFragment {
	protected static final String TAG = "InfoStationFragment";
	private Station currentStation;
	private TextView mTitleText;
	private long timestamp;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_info_station, null);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mTitleText = (TextView) getActivity().findViewById(R.id.title);
		registerForContextMenu(getListView());
	}

	public void displayInfo(String station, long timestamp) {
		if(timestamp!=0)
			this.timestamp=timestamp;
		else
			this.timestamp=System.currentTimeMillis();
		searchThread(station, timestamp);
	}

	private void searchThread(final String station, final long timestamp) {
		Runnable search = new Runnable() {
			public void run() {
				currentStation = UtilsWeb.getAPIstation(station, timestamp,
						getActivity());
				if (getActivity() != null)
					getActivity().runOnUiThread(displayResult);
			}
		};
		Thread thread = new Thread(null, search, "stationSearch");
		thread.start();
	}

	private Runnable displayResult = new Runnable() {
		public void run() {
			if (currentStation != null
					&& currentStation.getStationDepartures() != null) {
				StationInfoAdapter StationInfoAdapter = new StationInfoAdapter(
						getActivity(), R.layout.row_info_station,
						currentStation.getStationDepartures()
								.getStationDeparture());
				setListAdapter(StationInfoAdapter);
				setTitle(Utils.formatDate(
						new Date(timestamp),
						"dd MMM HH:mm"));
			} else {
				setTitle(Utils.formatDate(new Date(), "dd MMM HH:mm") + "\n\n"
						+ getString(R.string.txt_connection));
			}
		}
	};

	public void setTitle(String txt) {
		mTitleText.setText(txt);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(Menu.NONE, 0, Menu.NONE, "Fav")
				.setIcon(R.drawable.ic_menu_star)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add(Menu.NONE, 1, Menu.NONE, "Map")
				.setIcon(R.drawable.ic_menu_mapmode)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (android.R.id.home):
			// app icon in ActionBar is clicked; Go home
			Intent intent = new Intent(getActivity(), WelcomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		case 0:
			if (currentStation != null) {
				Utils.addAsStarred(currentStation.getStation(), "", 1,
						getActivity());
				startActivity(new Intent(getActivity(), StarredActivity.class));
			}
			return true;
		case 1:
			if (currentStation != null) {
				Intent i = new Intent(getActivity(), MapStationActivity.class);
				i.putExtra("Name", currentStation.getStation());
				startActivity(i);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		StationDeparture clicked = (StationDeparture) getListAdapter().getItem(
				(int) info.id);

		menu.add(0, 0, 0, clicked.getVehicle());
	}

	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();
			StationDeparture stop = (StationDeparture) getListAdapter()
					.getItem((int) menuInfo.id);
			Intent i = new Intent(getActivity(), InfoTrainActivity.class);
			i.putExtra("Name", stop.getVehicle());
			i.putExtra("timestamp", stop.getTime());
			startActivity(i);
			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}

}
