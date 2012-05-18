package tof.cv.mpp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.Utils.UtilsWeb;
import tof.cv.mpp.adapter.ConnectionAdapter;
import tof.cv.mpp.bo.Connection;
import tof.cv.mpp.bo.Connections;
import tof.cv.mpp.view.ConnectionDialog;
import tof.cv.mpp.view.DateTimePicker;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PlannerFragment extends SherlockListFragment {

	boolean isDebug = false;

	private static final int MENU_DT = 0;
	private static final int MENU_FAV = 1;
	private static final int MENU_PREF = 2;

	public Calendar mDate;

	public static String datePattern = "EEE dd MMM HH:mm";
	public static String abDatePattern = "EEE dd MMM";
	public static String abTimePattern = "HH:mm";

	int positionClicked;

	private static Connections allConnections = new Connections();

	private TextView tvDeparture;
	private TextView tvArrival;

	private ConnectionAdapter connAdapter;

	private String TAG = "BETRAINS";
	private Activity context;

	private static SharedPreferences settings;
	private SharedPreferences.Editor editor;

	private ProgressDialog progressDialog;

	private int style;

	public String fromIntentArrivalStation = null;
	public String fromIntentDepartureStation = null;
	public boolean fromIntent = false;

	// Second part need to be cleaned

	private static final int ACTIVITY_DISPLAY = 0;
	private static final int ACTIVITY_STOP = 1;
	private static final int ACTIVITY_STATION = 2;
	private static final int ACTIVITY_GETSTARTSTATION = 3;
	private static final int ACTIVITY_GETSTOPSTATION = 4;

	private static final int CONNECTION_DIALOG_ID = 0;

	public void onStart() {
		super.onStart();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_planner, null);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		editor = settings.edit();
		context = this.getActivity();
		mDate = Calendar.getInstance();

		updateActionBar();
		setHasOptionsMenu(true);

		style = android.R.style.Theme_Dialog;
		if (Build.VERSION.SDK_INT >= 14)
			style = android.R.style.Theme_DeviceDefault_Dialog;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		tvDeparture = (TextView) getView().findViewById(R.id.tv_start);
		tvArrival = (TextView) getActivity().findViewById(R.id.tv_stop);

		setAllBtnListener();
		
		fillStations(settings.getString("pStart", "MONS"),
				settings.getString("pStop", "TOURNAI"));

	}

	public void fillStations(String departure, String arrival) {
		Log.i("", "fill " + departure + " - " + arrival + " - " + fromIntent);
		tvDeparture = (TextView) getView().findViewById(R.id.tv_start);
		tvArrival = (TextView) getView().findViewById(R.id.tv_stop);

		if (fromIntent) {
			fromIntent = false;
			tvDeparture.setText(fromIntentDepartureStation);
			tvArrival.setText(fromIntentArrivalStation);
			// mySearchThread();
		} else {
			if (departure != null && arrival != null) {
				tvDeparture.setText(departure);
				tvArrival.setText(arrival);
			}
		}

	}

	private Runnable dismissPd = new Runnable() {
		public void run() {
			fillData();
			progressDialog.dismiss();
		}
	};

	private void setAllBtnListener() {
		Button btnInvert = (Button) getActivity().findViewById(
				R.id.mybuttonInvert);
		btnInvert.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				fillStations((String) tvArrival.getText(),
						(String) tvDeparture.getText());
			}
		});

		Button btnSearch = (Button) getView().findViewById(R.id.mybuttonSearch);
		btnSearch.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				mySearchThread();

			}
		});

		tvDeparture.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(getActivity(),
						StationPickerActivity.class);
				startActivityForResult(i, ACTIVITY_GETSTARTSTATION);
			}
		});

		tvArrival.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(getActivity(),
						StationPickerActivity.class);
				startActivityForResult(i, ACTIVITY_GETSTOPSTATION);
			}
		});

		Button btnInfoArrival = (Button) getActivity().findViewById(
				R.id.btn_info_arrival);
		btnInfoArrival.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				String station = tvArrival.getText().toString();
				Intent i = new Intent(getActivity(), InfoStationActivity.class);
				i.putExtra("Name", station);
				i.putExtra("Hour", mDate.get(Calendar.HOUR));
				i.putExtra("Minute", mDate.get(Calendar.MINUTE));
				startActivityForResult(i, ACTIVITY_STATION);

			}
		});
		Button btnInfoDeparture = (Button) getActivity().findViewById(
				R.id.btn_infos_departure);
		btnInfoDeparture.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String station = tvDeparture.getText().toString();
				Intent i = new Intent(getActivity(), InfoStationActivity.class);
				i.putExtra("Name", station);
				i.putExtra("Hour", mDate.get(Calendar.HOUR));
				i.putExtra("Minute", mDate.get(Calendar.MINUTE));
				startActivityForResult(i, ACTIVITY_STATION);

			}
		});

		Button btnAfter = (Button) getActivity().findViewById(
				R.id.mybuttonAfter);
		btnAfter.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				mDate.add(Calendar.HOUR, 1);
				updateActionBar();
				mySearchThread();
			}
		});

		Button btnBefore = (Button) getActivity().findViewById(
				R.id.mybuttonBefore);
		btnBefore.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				mDate.add(Calendar.HOUR, -1);
				updateActionBar();
				mySearchThread();
			}
		});

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		menu.add(Menu.NONE, MENU_DT, Menu.NONE, "Date/Time")
				.setIcon(R.drawable.ic_menu_time)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add(Menu.NONE, MENU_FAV, Menu.NONE, "Add to Fav.")
				.setIcon(R.drawable.ic_menu_star)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add(Menu.NONE, MENU_PREF, Menu.NONE, "Settings")
				.setIcon(R.drawable.ic_menu_preferences)
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
		case (MENU_DT):
			showDateTimeDialog();
			return true;
		case (MENU_FAV):
			Utils.addAsStarred(tvDeparture.getText().toString(), tvArrival
					.getText().toString(), 3, context);
			startActivity(new Intent(getActivity(), StarredActivity.class));
			return true;
		case (MENU_PREF):
			startActivity(new Intent(getActivity(), MyPreferenceActivity.class)
					.putExtra("screen", MyPreferenceActivity.PAGE_PLANNER));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void fillData() {
		if (allConnections != null && allConnections.connection != null) {
			Log.i(TAG, "*** Remplis avec les infos");
			connAdapter = new ConnectionAdapter(this.getActivity()
					.getBaseContext(), R.layout.row_planner,
					allConnections.connection);
			setListAdapter(connAdapter);
			registerForContextMenu(getListView());

		}

		else {
			Log.i(TAG, "*** Remplis avec le Cache");
			allConnections = Utils.getCachedConnections();
			if (allConnections != null) {
				connAdapter = new ConnectionAdapter(this.getActivity()
						.getBaseContext(), R.layout.row_planner,
						allConnections.connection);
				setListAdapter(connAdapter);
				registerForContextMenu(getListView());
			} else {
				Log.i(TAG, "*** Erreur avec le Cache");
				fillWithTips();
			}

		}
	}

	public void fillWithTips() {
		Log.i(TAG, "*** Remplis avec les tips");
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		// fill the map with data
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("tip", getString(R.string.tipa));
		map.put("title", getString(R.string.tipatitle));
		list.add(map);

		map = new HashMap<String, String>();
		map.put("tip", getString(R.string.tipb));
		map.put("title", getString(R.string.tipbtitle));
		list.add(map);

		map = new HashMap<String, String>();
		map.put("tip", getString(R.string.tipc));
		map.put("title", getString(R.string.tipctitle));
		list.add(map);

		map = new HashMap<String, String>();
		map.put("tip", getString(R.string.tipd));
		map.put("title", getString(R.string.tipdtitle));
		list.add(map);

		map = new HashMap<String, String>();
		map.put("tip", getString(R.string.tipe));
		map.put("title", getString(R.string.tipetitle));
		list.add(map);

		// Use a SimpleAdapter to display tips
		String[] from = { "tip", "title" };
		int[] to = { R.id.tiptitle, R.id.tiptext };
		SimpleAdapter adapter = new SimpleAdapter(getActivity(), list,
				R.layout.row_tip, from, to);
		setListAdapter(adapter);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		positionClicked = position;
		getActivity().removeDialog(CONNECTION_DIALOG_ID);

		try {

			Connection currentConnection = allConnections.connection
					.get(positionClicked);

			if (currentConnection.getVias() != null
					&& currentConnection.getVias().via.size() > 0) {

				new ConnectionDialog(getActivity(),
						allConnections.connection.get(positionClicked), style)
						.show();
			} else {
				Intent i = new Intent(getActivity(), InfoTrainActivity.class);
				i.putExtra("Name", currentConnection.getDeparture()
						.getVehicle());
				i.putExtra("fromto", tvDeparture.getText().toString() + " - "
						+ tvArrival.getText().toString());
				i.putExtra("Hour", mDate.get(Calendar.HOUR));
				i.putExtra("Minute", mDate.get(Calendar.MINUTE));
				startActivity(i);
			}

		} catch (Exception e) {
			e.printStackTrace();
			// noDataClick(positionClicked);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		// Log.d(TAG,"requestCode is: "+requestCode);

		switch (requestCode) {
		case ACTIVITY_DISPLAY:
			fillData();
			break;
		case ACTIVITY_STOP:
			fillData();
			break;

		case ACTIVITY_GETSTARTSTATION:
			if (intent != null) {
				String gare = intent.getStringExtra("GARE");
				if (!gare.contentEquals("")) {
					tvDeparture.setText(gare);
					editor.putString("pStart", gare);
					editor.commit();
				}
			}

			break;

		case ACTIVITY_GETSTOPSTATION:
			if (intent != null) {
				String gare = intent.getStringExtra("GARE");
				if (!gare.contentEquals("")) {
					tvArrival.setText(gare);
					editor.putString("pStop", gare);
					editor.commit();
				}
			}
			break;

		default:
			break;

		}

	}

	public void onPause() {
		super.onPause();
		String start = (String) tvDeparture.getText();
		String stop = (String) tvArrival.getText();
		if (!start.contentEquals("") && !start.contentEquals("")) {
			editor.putString("pStart", start);
			editor.putString("pStop", stop);
			editor.commit();
		}

	}

	private void mySearchThread() {
		Runnable trainSearch = new Runnable() {

			public void run() {

				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						progressDialog = ProgressDialog.show(getActivity(), "",
								getString(R.string.txt_patient), true);
					}
				});
				makeApiRequest();
				getActivity().runOnUiThread(dismissPd);
			}
		};

		Thread thread = new Thread(null, trainSearch, "MyThread");
		thread.start();

	}

	public void makeApiRequest() {

		String myStart;
		String myArrival;
		myStart = tvDeparture.getText().toString();
		myArrival = tvArrival.getText().toString();

		String langue = getString(R.string.url_lang);
		// There is a setting to force dutch when Android is in English.
		if (settings.getBoolean("prefnl", false))
			langue = "NL";

		String dA = "depart";
		if (settings.getString(context.getString(R.string.key_planner_da), "1")
				.contentEquals("2"))
			dA = "arrive";

		String trainOnly = "1";
		if (!settings.getBoolean(context.getString(R.string.key_train_only),
				true))
			trainOnly = "train";
		else
			trainOnly = "train;bus";

		allConnections = UtilsWeb.getAPIConnections(
				"" + (mDate.get(Calendar.YEAR) - 2000),
				"" + (mDate.get(Calendar.MONTH) + 1),
				"" + mDate.get(Calendar.DAY_OF_MONTH),
				Utils.formatDate(mDate.getTime(), "HH"),
				Utils.formatDate(mDate.getTime(), "mm"), langue, myStart,
				myArrival, dA, trainOnly, getActivity());

		if (allConnections == null) {
			Log.e(TAG, "API failure!!!");
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getActivity(), R.string.txt_error,
							Toast.LENGTH_LONG).show();
				}
			});

		}
	}

	public void onResume() {
		super.onResume();

		try {
			fillData();
		} catch (Exception e) {
			Log.i(TAG, "Impossible to fill Data:\n" + e.getMessage());
			e.printStackTrace();
		}

	}

	public void noDataClick(int position) {

		// TODO If there were no results, launch browser to check connection.

		/*
		 * Intent i = new Intent(this, InfoTrainActivity.class);
		 * 
		 * i.putExtra("fromto", tvDeparture.getText().toString() + " - " +
		 * tvArrival.getText().toString());
		 * 
		 * i.putExtra(ConnectionDbAdapter.KEY_TRAINS, c.getString(c
		 * .getColumnIndexOrThrow(ConnectionDbAdapter.KEY_TRAINS)));
		 * 
		 * startActivity(i);
		 */
	}

	private void showDateTimeDialog() {

		if (Build.VERSION.SDK_INT >= 14)
			style = android.R.style.Theme_DeviceDefault_Light_Dialog;

		final DateTimePicker mDateTimeDialog = new DateTimePicker(
				(Context) getActivity(), style, this);

		final String timeS = android.provider.Settings.System.getString(
				getActivity().getContentResolver(),
				android.provider.Settings.System.TIME_12_24);
		final boolean is24h = !(timeS == null || timeS.equals("12"));

		mDateTimeDialog.setIs24HourView(is24h);

		mDateTimeDialog.show();
	}

	private void updateActionBar() {
		getSherlockActivity().getSupportActionBar().setTitle(
				Utils.formatDate(mDate.getTime(), abTimePattern));
		getSherlockActivity().getSupportActionBar().setSubtitle(
				Utils.formatDate(mDate.getTime(), abDatePattern));
	}

}
