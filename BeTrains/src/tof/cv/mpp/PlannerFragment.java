package tof.cv.mpp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import tof.cv.mpp.Utils.ConnectionDbAdapter;
import tof.cv.mpp.Utils.ConnectionMaker;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.bo.Connection;
import tof.cv.mpp.bo.Station;
import tof.cv.mpp.bo.Via;
import tof.cv.mpp.view.DateTimePicker;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.SupportActivity;
import android.support.v4.view.Menu;
import android.view.MenuInflater;
import android.support.v4.view.MenuItem;
import android.support.v4.view.Window;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PlannerFragment extends ListFragment implements
		DialogInterface.OnClickListener, Dialog.OnCancelListener {

	boolean isDebug = false;

	private static final int MENU_DT = 0;
	private static final int MENU_FAV = 1;

	Date mDate;

	String datePattern = "EEE ddMMM HH:mm";

	int positionClicked;

	private static ArrayList<Connection> allConnections = new ArrayList<Connection>();
	// ArrayList<Message> listOfMessages;

	private static final int ACTIVITY_DISPLAY = 0;
	private static final int ACTIVITY_STOP = 1;
	private static final int ACTIVITY_STATION = 2;
	private static final int ACTIVITY_GETSTARTSTATION = 3;
	private static final int ACTIVITY_GETSTOPSTATION = 4;
	private static final int ACTIVITY_INFO = 6;

	private Cursor myConnectionCursor;
	private Cursor myViaCursor;
	private static ConnectionDbAdapter mDbHelper;

	private static final int CONNECTION_DIALOG_ID = 0;

	private static SharedPreferences settings;
	private SharedPreferences.Editor editor;

	private TextView tvDeparture;
	private TextView tvArrival;

	private LinearLayout linLayoutDate;
	private LinearLayout linLayoutTime;
	private TextView txtViewAB;

	private ConnectionDbAdapter connAdapter;

	private ProgressDialog progressDialog;

	private String TAG = "BETRAINS";
	private SupportActivity context;

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
		context = this.getSupportActivity();
		Date mDate = new Date();

		getSupportActivity().getSupportActionBar().setTitle(
				Utils.formatDate(mDate, datePattern));
		setHasOptionsMenu(true);

		// mActionBar.addItem(R.drawable.ic_title_settings);
		// addActionBarItem(getGDActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(R.drawable.ic_title_settings),R.id.action_bar_settings);

		mDbHelper = new ConnectionDbAdapter(getActivity());

		Bundle extras = getActivity().getIntent().getExtras();
		if (extras != null) {
			tvDeparture.setText(extras.getString("Departure"));
			tvArrival.setText(extras.getString("Arrival"));
			mySearchThread();
		}

		setHasOptionsMenu(true);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setBtnSearchListener();
		setActionBarListener();
		setBtnInvertListener();
		setTvArrivalListener();
		setTvDepartureListener();
		setBtnInfoArrivalListener();
		setBtnInfoDepartureListener();
		setBtnAfterListener();
		setBtnBeforeListener();

		String defaultStart = settings.getString("pStart", "MONS");
		String defaultStop = settings.getString("pStop", "TOURNAI");
		fillStations(defaultStart, defaultStop);

		fillData();

	}

	public void fillStations(String departure, String arrival) {
		final TextView textStart = (TextView) context
				.findViewById(R.id.tv_start);
		final TextView textStop = (TextView) context.findViewById(R.id.tv_stop);
		textStart.setText(departure);
		textStop.setText(arrival);

	}

	private Runnable dismissPd = new Runnable() {
		public void run() {
			fillData();
			progressDialog.dismiss();
		}
	};

	private void setBtnInfoDepartureListener() {
		Button btnInfoDeparture = (Button) getActivity().findViewById(
				R.id.btn_infos_departure);
		btnInfoDeparture.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String station = tvDeparture.getText().toString();
				// 0);
				/*
				 * Intent i = new Intent(PlannerActivity.this,
				 * InfoStationActivity.class); i.putExtra("gare_name", station);
				 * i.putExtra("gare_id", getStationNumber(station));
				 * i.putExtra("gare_heure", mHour); i.putExtra("gare_minute",
				 * mMinute); startActivityForResult(i, ACTIVITY_STATION);
				 */
			}
		});
	}

	private void setBtnInfoArrivalListener() {

		Button btnInfoArrival = (Button) getActivity().findViewById(
				R.id.btn_info_arrival);
		btnInfoArrival.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				String station = tvArrival.getText().toString();
				//Intent i = new Intent(getActivity(),
				//		InfoStationActivity.class);
				//i.putExtra("gare_name", station);
				//i.putExtra("gare_id", getStationNumber(station));
				//i.putExtra("gare_heure", mHour);
				//i.putExtra("gare_minute", mMinute);
				//startActivityForResult(i, ACTIVITY_STATION);

			}
		});

	}

	private void setTvArrivalListener() {

		tvArrival = (TextView) getActivity().findViewById(R.id.tv_stop);
		tvArrival.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// Intent i = new Intent(PlannerActivity.this,
				// BETrainsTabActivity.class);
				// startActivityForResult(i, ACTIVITY_GETSTOPSTATION);
			}
		});

	}

	private void setTvDepartureListener() {
		tvDeparture = (TextView) getActivity().findViewById(R.id.tv_start);
		tvDeparture.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// Intent i = new Intent(PlannerActivity.this,
				// BETrainsTabActivity.class);
				// startActivityForResult(i, ACTIVITY_GETSTARTSTATION);
			}
		});

	}

	private void setBtnInvertListener() {
		Button btnInvert = (Button) getActivity().findViewById(
				R.id.mybuttonInvert);
		btnInvert.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				fillStations((String) tvArrival.getText(),
						(String) tvDeparture.getText());
			}
		});

	}

	private void setBtnSearchListener() {
		Button btnSearch = (Button) getView().findViewById(R.id.mybuttonSearch);
		btnSearch.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				mySearchThread();
				fillData();

			}
		});
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(Menu.NONE, MENU_DT, Menu.NONE, "Date/Time")
				.setIcon(R.drawable.icon)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menu.add(Menu.NONE, MENU_FAV, Menu.NONE, "Add to Fav.")
				.setIcon(R.drawable.icon)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void setActionBarListener() {
		txtViewAB = (TextView) context.findViewById(R.id.abs__action_bar_title);

		// txtViewAB.setOnClickListener(new Button.OnClickListener() {
		// public void onClick(View v) {
		// showDateTimeDialog();
		// }
		// });
	}

	private void setBtnBeforeListener() {
		Button btnBefore = (Button) getActivity().findViewById(
				R.id.mybuttonBefore);
		btnBefore.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// tracker.trackEvent("Click", "ButtonBefore", "clicked", 0);
				// int hour = Integer.parseInt(mHour);
				// if (hour > 0)
				// mHour = ConnectionMaker.fillZero("" + (hour - 1));
				// else
				// mHour = "23";
				// mActionBar.setTitle(mDay + "/" + mMonth + "/" + mYear + "   "
				// + mHour + ":" + mMinute);
				makeApiRequest();
				fillData();
			}
		});
	}

	private void setBtnAfterListener() {
		Button btnAfter = (Button) getActivity().findViewById(
				R.id.mybuttonAfter);
		btnAfter.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// tracker.trackEvent("Click", "ButtonAfter", "clicked", 0);
				// int hour = Integer.parseInt(mHour);
				// if (hour < 23)
				// mHour = ConnectionMaker.fillZero("" + (hour + 1));
				// else
				// mHour = "00";
				// mActionBar.setTitle(mDay + "/" + mMonth + "/" + mYear + "   "
				// + mHour + ":" + mMinute);
				makeApiRequest();
				fillData();
			}
		});
	}

	public int createLayout() {
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if (settings.getBoolean("preffullscreen", false))
			ConnectionMaker.setFullscreen(getActivity());
		return R.layout.activity_planner;
	}

	private void fillData() {
		// Get all of the rows from the database and create the item list
		mDbHelper.open();
		myConnectionCursor = mDbHelper.fetchAllConnections();
		// Log.i(TAG,"Cursor size is: "+myConnectionCursor.getCount());
		myViaCursor = mDbHelper.fetchAllVias();
		getActivity().startManagingCursor(myConnectionCursor);
		getActivity().startManagingCursor(myViaCursor);

		// connAdapter = new ConnectionAdapter(this, R.layout.row_planner,
		// new ArrayList<Connection>(), myConnectionCursor);
		// setListAdapter(connAdapter);

		if (myConnectionCursor.getCount() > 0) {
			registerForContextMenu(getListView());
			fillAllNewConnections(myConnectionCursor, myViaCursor);
		}

		else {
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

		// mDbHelper.close();
	}

	/*
	 * database -> overloaded
	 */
	private void fillAllNewConnections(Cursor myConnectionCursor,
			Cursor myViaCursor) {
		/*
		 * we have to take each cursor and look it it has any vias , normally
		 * the rowId will be the same as the index of the cursor
		 */
		allConnections = new ArrayList<Connection>();
		myConnectionCursor.moveToFirst();

		// Log.i(TAG,"Cursor size is: "+myConnectionCursor.getCount());

		int firstRowId = myConnectionCursor.getInt(myConnectionCursor
				.getColumnIndex(ConnectionDbAdapter.KEY_ROWID));

		for (int i = 0; i < myConnectionCursor.getCount(); i++) {
			myConnectionCursor.moveToPosition(i);

			ArrayList<String> trains = new ArrayList<String>();
			String departureStation = myConnectionCursor
					.getString(myConnectionCursor
							.getColumnIndex(ConnectionDbAdapter.KEY_DEPARTURE));
			String arrivalStation = myConnectionCursor
					.getString(myConnectionCursor
							.getColumnIndex(ConnectionDbAdapter.KEY_ARRIVAL));

			String departureTime = myConnectionCursor
					.getString(myConnectionCursor
							.getColumnIndex(ConnectionDbAdapter.KEY_DEPARTTIME));
			String arrivalTime = myConnectionCursor
					.getString(myConnectionCursor
							.getColumnIndex(ConnectionDbAdapter.KEY_ARRIVALTIME));

			String departurePlatform = myConnectionCursor
					.getString(myConnectionCursor
							.getColumnIndex(ConnectionDbAdapter.KEY_DEPARTURE_PLATFORM));
			String arrivalPlatform = myConnectionCursor
					.getString(myConnectionCursor
							.getColumnIndex(ConnectionDbAdapter.KEY_ARRIVAL_PLATFORM));

			String duration = myConnectionCursor.getString(myConnectionCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_TRIPTIME));
			String delayDStr = myConnectionCursor.getString(myConnectionCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_DELAY_DEPARTURE));
			String delayAStr = myConnectionCursor.getString(myConnectionCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_DELAY_ARRIVAL));

			/*
			 * parse the trains from the TRAINS field, and add them , after
			 * splitting , to the trainslist of connection
			 */
			String trainsConcatenated = myConnectionCursor
					.getString(myConnectionCursor
							.getColumnIndex(ConnectionDbAdapter.KEY_TRAINS));
			String[] trainsParsed = trainsConcatenated.split(";");
			String lastTrain = "";
			for (int j = 0; j < trainsParsed.length; j++) {
				lastTrain = trainsParsed[j];
				trains.add(lastTrain);

			}
			/*
			 * String vehicle, String platform, boolean platformNormal, String
			 * time, String station, String stationCoordinates, boolean
			 * delay,String strDelay,String status
			 */

			/*
			 * now the via's we have to go through all the vias, and put the
			 * cursor back in its begin position afterwards
			 */
			ArrayList<Via> vias = new ArrayList<Via>();

			for (int j = 0; j < myViaCursor.getCount(); j++) {
				if (myViaCursor.moveToPosition(j)) {

					int index = myViaCursor
							.getColumnIndex(ConnectionDbAdapter.KEY_VIA_ROWIDOFCONNECTION);
					int rowId = myViaCursor.getInt(index);

					if (rowId == i + firstRowId) {

						/*
						 * String arrivalPlatform, String arrivalTime, String
						 * departurePlatform, String departureTime, String
						 * timeBetween, String coordinates, String stationName,
						 * String vehicle, String duration
						 */
						String viaArrivalPlatform;
						String viaArrivalTime;
						String viaDeparturePlatform;
						String viaDepartureTime;
						String timeBetween;
						String viaCoordinates;
						String viaDelay;
						String viaStationName;
						String viaVehicle;
						String viaDuration;
						viaArrivalPlatform = myViaCursor
								.getString(myViaCursor
										.getColumnIndex(ConnectionDbAdapter.KEY_VIA_ARRIVALPLATFORM));
						viaArrivalTime = myViaCursor
								.getString(myViaCursor
										.getColumnIndex(ConnectionDbAdapter.KEY_VIA_ARRIVALTIME));
						viaDeparturePlatform = myViaCursor
								.getString(myViaCursor
										.getColumnIndex(ConnectionDbAdapter.KEY_VIA_DEPARTUREPLATFORM));
						viaDepartureTime = myViaCursor
								.getString(myViaCursor
										.getColumnIndex(ConnectionDbAdapter.KEY_VIA_DEPARTURETIME));
						timeBetween = myViaCursor
								.getString(myViaCursor
										.getColumnIndex(ConnectionDbAdapter.KEY_VIA_TIMEBETWEEN));
						viaStationName = myViaCursor
								.getString(myViaCursor
										.getColumnIndex(ConnectionDbAdapter.KEY_VIA_STATIONNAME));
						viaVehicle = myViaCursor
								.getString(myViaCursor
										.getColumnIndex(ConnectionDbAdapter.KEY_VIA_VEHICLE));
						viaDelay = myViaCursor
								.getString(myViaCursor
										.getColumnIndex(ConnectionDbAdapter.KEY_VIA_DELAY));
						viaCoordinates = myViaCursor
								.getString(myViaCursor
										.getColumnIndex(ConnectionDbAdapter.KEY_VIA_COORDINATES));
						viaDuration = myViaCursor
								.getString(myViaCursor
										.getColumnIndex(ConnectionDbAdapter.KEY_VIA_DURATION));
						vias.add(new Via(viaArrivalPlatform, viaArrivalTime,
								viaDeparturePlatform, viaDepartureTime,
								timeBetween, viaCoordinates, viaStationName,
								viaVehicle, viaDuration, viaDelay));
					}
				}
			}
			try {
				allConnections.add(new Connection(new Station("",
						departurePlatform, true, departureTime,
						departureStation, "stationCoordinates", delayDStr,
						"status"), vias, new Station(lastTrain,
						arrivalPlatform, true, arrivalTime, arrivalStation,
						"stationCoordinates", delayAStr, "status"), duration,
						delayDStr, delayAStr));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {

		case CONNECTION_DIALOG_ID:
			// Log.i("BETRAINS","clicked on: "+allConnections
			// .get(positionClicked).getArrivalStation().getVehicle());
			try {
				// return new ConnectionDialog(this, allConnections
				// .get(positionClicked));
			} catch (Exception e) {
				Toast.makeText(getActivity(),
						getString(R.string.txt_create_connections),
						Toast.LENGTH_LONG).show();
				Log.i("BETRAINS", allConnections.size() + " - "
						+ positionClicked);
				e.printStackTrace();
			}

		}
		Log.i("BETRAINS", "dialog null");
		return null;

	}

	public static int getStationNumber(String stationToFind) {
		stationToFind = stationToFind.replace("-", " ");
		boolean found = false;
		int z = 0;

		stationToFind = stationToFind.replace(" [B]", "");
		stationToFind = stationToFind.replace(" [b]", "");

		for (String x : ConnectionMaker.LIST_OF_STATIONS) {
			if (x.compareToIgnoreCase(stationToFind) == 0) {
				found = true;
				break;
			}
			z++;
		}

		if (found) {
			Log.i("BETRAINS", stationToFind + " = "
					+ ConnectionMaker.LIST_ID[z]);
			return Integer.valueOf(ConnectionMaker.LIST_ID[z]);
		}

		else {
			Log.i("BETRAINS", stationToFind + " not found");
			return -1;
		}

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
		// Log.v(TAG,"click");
		try {

			Connection currentConnection = allConnections.get(positionClicked);
			// Log.v(TAG,"size: "+currentConnection.getVias().size());
			if (currentConnection.getVias().size() > 0) {
				getActivity().showDialog(CONNECTION_DIALOG_ID);
			} else
				noDataClick(positionClicked);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				noDataClick(positionClicked);
			} catch (Exception f) {
				f.printStackTrace();
			}

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
			String gare = intent.getStringExtra("GARE");
			if (!gare.contentEquals("")) {
				tvDeparture.setText(gare);
				editor.putString("pStart", gare);
				editor.commit();
			}
			break;

		case ACTIVITY_GETSTOPSTATION:
			gare = intent.getStringExtra("GARE");
			if (!gare.contentEquals("")) {
				tvArrival.setText(gare);
				editor.putString("pStop", gare);
				editor.commit();
			}
			break;

		default:
			break;

		}

	}

	public void onPause() {
		super.onPause();
		// String start = (String) tvDeparture.getText();
		// String stop = (String) tvArrival.getText();
		// if (!start.contentEquals("") && !start.contentEquals("")) {
		// editor.putString("pStart", start);
		// editor.putString("pStop", stop);
		// editor.commit();
		// }

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

				// TODO
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

		int item = 0;
		for (String x : ConnectionMaker.LIST_OF_STATIONS) {
			if (x.compareToIgnoreCase(myStart) == 0) {
				myStart = ConnectionMaker.LIST_OF_STATIONS[item];
				break;
			}
			item++;
		}

		item = 0;

		for (String x : ConnectionMaker.LIST_OF_STATIONS) {
			if (x.compareToIgnoreCase(myArrival) == 0) {
				myArrival = ConnectionMaker.LIST_OF_STATIONS[item];
				break;
			}
			item++;
		}

		if (myStart.contains("/")) {
			String[] foreign = myStart.split("/");
			myStart = foreign[1] + "%20(" + foreign[0] + ")";
		}

		if (myArrival.contains("/")) {
			String[] etranger = myArrival.split("/");
			myArrival = etranger[1] + "%20(" + etranger[0] + ")";
		}

		String langue = getString(R.string.url_lang);
		if (settings.getBoolean("prefnl", false))
			langue = "NL";

		Log.d(TAG, settings.getString(
				context.getString(R.string.key_planner_da), "1"));
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

		allConnections = new ArrayList<Connection>();
		// allConnections = ConnectionMaker.newSearchTrains(mYear, mMonth, mDay,
		// mHour, mMinute, langue, myStart, myArrival, dA, trainOnly,
		// getActivity());

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

	public void onDestroy() {
		super.onDestroy();

		// tracker.stop();

	}

	public void onResume() {
		super.onResume();
		try {
			fillData();
		} catch (Exception e) {
			Log.i(TAG, "Impossible to fecth Database:\n" + e.getMessage());
			e.printStackTrace();
		}

	}

	/*
	 * public boolean onHandleActionBarItemClick(GDActionBarItem item, int
	 * position) {
	 * 
	 * switch (position) { case 0: Intent i = new Intent(PlannerActivity.this,
	 * PlannerSettingsActivity.class); startActivity(i); break;
	 * 
	 * default: return super.onHandleActionBarItemClick(item,position); } return
	 * true; }
	 */

	public static int getActivityInfo() {
		return ACTIVITY_INFO;
	}

	public void setLinearLayoutDate(LinearLayout linLayoutDate) {
		this.linLayoutDate = linLayoutDate;
	}

	public LinearLayout getLinearLayoutDate() {
		return linLayoutDate;
	}

	public void setLinearLayoutTime(LinearLayout linLayoutTime) {
		this.linLayoutTime = linLayoutTime;
	}

	public LinearLayout getLinearLayoutTime() {
		return linLayoutTime;
	}

	public Activity getContext() {
		return getActivity();
	}

	public void setTAG(String tAG) {
		TAG = tAG;
	}

	public String getTAG() {
		return TAG;
	}

	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON1) {
			// OK button

		}

		// Remove the dialog, it won't be used again
		getActivity().removeDialog(CONNECTION_DIALOG_ID);

	}

	public void onCancel(DialogInterface dialog) {
		getActivity().removeDialog(CONNECTION_DIALOG_ID);

	}

	protected void onPrepareDialog(int dialogId, Dialog dialog) {
		switch (dialogId) {
		case CONNECTION_DIALOG_ID: {

		}
		}
	}

	public void noDataClick(int position) {
		Cursor c = myConnectionCursor;
		c.moveToPosition(position);
		// If there were no results, launch browser to check connection.

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
		// Create the dialog
		final Dialog mDateTimeDialog = new Dialog(
				(Context) getSupportActivity());
		// Inflate the root layout
		final RelativeLayout mDateTimeDialogView = (RelativeLayout) getSupportActivity()
				.getLayoutInflater().inflate(R.layout.dtp_date_time_dialog,
						null);
		// Grab widget instance
		final DateTimePicker mDateTimePicker = (DateTimePicker) mDateTimeDialogView
				.findViewById(R.id.DateTimePicker);

		// Check is system is set to use 24h time (this doesn't seem to work as
		// expected though)
		final String timeS = android.provider.Settings.System.getString(
				getSupportActivity().getContentResolver(),
				android.provider.Settings.System.TIME_12_24);
		final boolean is24h = !(timeS == null || timeS.equals("12"));

		// Update demo TextViews when the "OK" button is clicked
		((Button) mDateTimeDialogView.findViewById(R.id.SetDateTime))
				.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						getSupportActivity().getSupportActionBar().setTitle(
								mDateTimePicker.getFormatedDate(datePattern));
						mDateTimeDialog.cancel();

					}
				});

		// Cancel the dialog when the "Cancel" button is clicked
		((Button) mDateTimeDialogView.findViewById(R.id.CancelDialog))
				.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						mDateTimeDialog.cancel();
					}
				});

		// Reset Date and Time pickers when the "Reset" button is clicked
		((Button) mDateTimeDialogView.findViewById(R.id.ResetDateTime))
				.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						mDateTimePicker.reset();
					}
				});

		mDateTimePicker.setIs24HourView(is24h);
		mDateTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDateTimeDialog.setContentView(mDateTimeDialogView);
		mDateTimeDialog.show();
	}

}
