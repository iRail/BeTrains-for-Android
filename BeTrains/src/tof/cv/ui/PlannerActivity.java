package tof.cv.ui;

import greendroid.app.GDListActivity;
import greendroid.widget.GDActionBar;
import greendroid.widget.GDActionBarItem;
import greendroid.widget.NormalActionBarItem;

import java.util.ArrayList;
import java.util.Date;

import tof.cv.adapters.ConnectionAdapter;
import tof.cv.bo.Connection;
import tof.cv.bo.Station;
import tof.cv.bo.Via;
import tof.cv.misc.ConnectionDbAdapter;
import tof.cv.misc.ConnectionDialog;
import tof.cv.misc.ConnectionMaker;
import tof.cv.mpp.R;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.devoteam.quickaction.QuickActionWindow;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class PlannerActivity extends GDListActivity implements
		DialogInterface.OnClickListener, Dialog.OnCancelListener {

	boolean isDebug = false;

	private static final String KEY = "UA-2490763-5";

	private static final int MENU_01 = 10;
	private static final int MENU_02 = 11;

	int positionClicked;

	private static ArrayList<Connection> allConnections = new ArrayList<Connection>();

	// ArrayList<Message> listOfMessages;

	// private static final int QUIT_ID = 0;
	private static final int CLICK1_ID = 0;

	private static final int ACTIVITY_DISPLAY = 0;
	private static final int ACTIVITY_STOP = 1;
	private static final int ACTIVITY_STATION = 2;
	private static final int ACTIVITY_GETSTARTSTATION = 3;
	private static final int ACTIVITY_GETSTOPSTATION = 4;
	private static final int ACTIVITY_INFO = 6;

	// private static final int ACTIVITY_PREFS=3;

	private Cursor myConnectionCursor;
	private Cursor myViaCursor;
	private static ConnectionDbAdapter mDbHelper;

	public static String mYear;
	public static String mMonth;
	public static String mDay;

	public static String mHour;
	public static String mMinute;

	GoogleAnalyticsTracker tracker;

	private static final int DATE_DIALOG_ID = 0;
	private static final int TIME_DIALOG_ID = 1;
	private static final int CONNECTION_DIALOG_ID = 2;

	private static SharedPreferences settings;
	private SharedPreferences.Editor editor;

	private TextView tvDeparture;
	private TextView tvArrival;
	private TextView tvHour;
	private TextView tvMinute;
	private TextView tvDay;
	private TextView tvMonth;
	private TextView tvYear;
	private LinearLayout linLayoutDate;
	private LinearLayout linLayoutTime;
	private GDActionBar mActionBar;
	private Button btnSearch;
	private Button btnInvert;
	private Button btnInfoDeparture;
	private Button btnInfoArrival;
	private Button btnAfter;
	private Button btnBefore;
	private TextView txtViewAB;
	private TextView txtViewEmpty;

	private ConnectionAdapter connAdapter;

	private ProgressDialog progressDialog;

	private String TAG = "BETRAINS";
	private PlannerActivity context = this;

	public void onStart() {
		super.onStart();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar = getGDActionBar();

		final Intent intent = getIntent();
		String action = intent.getAction();

		if (Intent.ACTION_MAIN.equals(action)) {
			switch (Integer.valueOf(settings.getString("Activitypref", "1"))) {
			case 2:
				Intent i = new Intent(PlannerActivity.this,
						WelcomeActivity.class);
				finish();
				startActivity(i);
				break;
			case 3:
				i = new Intent(PlannerActivity.this, StarredActivity.class);
				finish();
				startActivity(i);
				break;
			case 4:
				i = new Intent(PlannerActivity.this, TrafficActivity.class);
				finish();
				startActivity(i);
				break;
			case 5:
				i = new Intent(PlannerActivity.this,
						GetClosestStationsActivity.class);
				finish();
				startActivity(i);
				break;
			case 6:
				i = new Intent(PlannerActivity.this, MessagesActivity.class);
				finish();
				startActivity(i);
				break;
			default:
				break;
			}
		}

		editor = settings.edit();

		context = this;


		// Initialization of date and time in ActionBar
		Date date_now = new Date();
		mYear = ConnectionMaker.fillZero("" + (date_now.getYear() - 100));
		mMonth = ConnectionMaker.fillZero("" + (date_now.getMonth() + 1));
		mDay = ConnectionMaker.fillZero("" + date_now.getDate());
		mHour = ConnectionMaker.fillZero("" + date_now.getHours());
		mMinute = ConnectionMaker.fillZero("" + date_now.getMinutes());


		//mActionBar.addItem(R.drawable.ic_title_settings);
		 addActionBarItem(getGDActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(R.drawable.ic_title_settings),R.id.action_bar_settings);
		mActionBar.setTitle(mDay + "/" + mMonth + "/" + mYear + "   " + mHour
				+ ":" + mMinute);

		// Setting action on every button.
		btnSearch = (Button) findViewById(R.id.mybuttonSearch);
		btnInvert = (Button) findViewById(R.id.mybuttonInvert);
		tvDeparture = (TextView) findViewById(R.id.tv_start);
		tvArrival = (TextView) findViewById(R.id.tv_stop);
		btnInfoDeparture = (Button) findViewById(R.id.btn_infos_departure);
		btnInfoArrival = (Button) findViewById(R.id.btn_info_arrival);
		btnBefore = (Button) findViewById(R.id.mybuttonBefore);
		btnAfter = (Button) findViewById(R.id.mybuttonAfter);
		txtViewAB = (TextView) findViewById(R.id.gd_action_bar_title);
		txtViewEmpty = (TextView) findViewById(android.R.id.empty);
		String defaultStart = settings.getString("pStart", "MONS");
		String defaultStop = settings.getString("pStop", "TOURNAI");
		ConnectionMaker.fillStations(context, defaultStart, defaultStop);

		tracker = GoogleAnalyticsTracker.getInstance(); // Start the tracker
		// in manual dispatch mode...
		tracker.start(KEY, this);
		tracker.trackPageView("/Planneractivity");

		// fill the ListView with data in DB.
		mDbHelper = new ConnectionDbAdapter(this);

		registerForContextMenu(getListView());

		setBtnSearchListener();
		setBtnInvertListener();
		setTvDepartureListener();
		setTvArrivalListener();
		setBtnInfoDepartureListener();
		setBtnInfoArrivalListener();
		setBtnBeforeListener();
		setBtnAfterListener();
		setActionBarTitleListener();

		View home = (View) findViewById(R.id.gd_action_bar_home_item);
		home.setOnClickListener((new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(PlannerActivity.this,WelcomeActivity.class));
			}
		}));
		
		if (isDebug)
			debugStuff();

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			tvDeparture.setText(extras.getString("Departure"));
			tvArrival.setText(extras.getString("Arrival"));
			mySearchThread();

		}

	}

	private void debugStuff() {
		ArrayList<Via> vias = new ArrayList<Via>();

		Station departureStation = new Station("IC000", "0", true, "123123123",
				"Start [B]", "00000000 00000000", "+5'", "");

		Station arrivalStation = new Station("IC999", "9", true, "321321321",
				"Stop [B]", "99999999 99999999", "+5'", "");

		vias.add(new Via("1", "123123123", "1", "123123123", "123123123",
				"123123123", "blabla [B]", "IC333", "123123123", ""));
		vias.add(new Via("2", "123123123", "2", "123123123", "123123123",
				"123123123", "babeli [B]", "IC444", "123123123", ""));
		vias.add(new Via("3", "123123123", "3", "123123123", "123123123",
				"123123123", "popopo [B]", "IC555", "123123123", ""));

		allConnections.add(new Connection(departureStation, vias,
				arrivalStation, "10", "22", "33"));

		allConnections.add(new Connection(departureStation, vias,
				arrivalStation, "10", "22", "33"));

		allConnections.add(new Connection(departureStation, vias,
				arrivalStation, "10", "22", "33"));

		allConnections.add(new Connection(departureStation, vias,
				arrivalStation, "10", "22", "33"));

	}

	private Runnable dismissPd = new Runnable() {

		public void run() {
			fillData();
			progressDialog.dismiss();
		}
	};

	private void setActionBarTitleListener() {
		txtViewAB.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				setQuickAction(v);
			}
		});
	}

	private void setBtnInfoDepartureListener() {
		btnInfoDeparture.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				tracker.trackPageView("/InfoStationActivity");
				String station = tvDeparture.getText().toString();
				tracker
						.trackEvent("Click", "ButtonInfoDeparture", "clicked",
								0);
				Intent i = new Intent(PlannerActivity.this,
						InfoStationActivity.class);
				i.putExtra("gare_name", station);
				i.putExtra("gare_id", getStationNumber(station));
				i.putExtra("gare_heure", mHour);
				i.putExtra("gare_minute", mMinute);
				startActivityForResult(i, ACTIVITY_STATION);
			}
		});
	}

	private void setBtnInfoArrivalListener() {
		btnInfoArrival.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				tracker.trackPageView("/InfoStationActivity");
				tracker.trackEvent("Click", "ButtonInfoArrival", "clicked", 0);
				String station = tvArrival.getText().toString();
				Intent i = new Intent(PlannerActivity.this,
						InfoStationActivity.class);
				i.putExtra("gare_name", station);
				i.putExtra("gare_id", getStationNumber(station));
				i.putExtra("gare_heure", mHour);
				i.putExtra("gare_minute", mMinute);
				startActivityForResult(i, ACTIVITY_STATION);
			}
		});

	}

	private void setTvArrivalListener() {

		tvArrival.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				tracker.trackPageView("/BETrainsTabActivity");
				tracker.trackEvent("Click", "TextViewArrival", "clicked", 0);
				Intent i = new Intent(PlannerActivity.this,
						BETrainsTabActivity.class);
				startActivityForResult(i, ACTIVITY_GETSTOPSTATION);
			}
		});

	}

	private void setTvDepartureListener() {
		tvDeparture.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(PlannerActivity.this,
						BETrainsTabActivity.class);
				startActivityForResult(i, ACTIVITY_GETSTARTSTATION);
			}
		});

	}

	private void setBtnInvertListener() {
		btnInvert.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ConnectionMaker.fillStations(context, (String) tvArrival
						.getText(), (String) tvDeparture.getText());
			}
		});

	}

	private void setBtnSearchListener() {
		btnSearch.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// makeApiRequest();
				mySearchThread();
				fillData();
			}
		});
	}

	private void setBtnBeforeListener() {
		btnBefore.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				tracker.trackEvent("Click", "ButtonBefore", "clicked", 0);
				int hour = Integer.parseInt(mHour);
				if (hour > 0)
					mHour = ConnectionMaker.fillZero("" + (hour - 1));
				else
					mHour = "23";
				mActionBar.setTitle(mDay + "/" + mMonth + "/" + mYear + "   "
						+ mHour + ":" + mMinute);
				makeApiRequest();
				fillData();
			}
		});
	}

	private void setBtnAfterListener() {
		btnAfter.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				tracker.trackEvent("Click", "ButtonAfter", "clicked", 0);
				int hour = Integer.parseInt(mHour);
				if (hour < 23)
					mHour = ConnectionMaker.fillZero("" + (hour + 1));
				else
					mHour = "00";
				mActionBar.setTitle(mDay + "/" + mMonth + "/" + mYear + "   "
						+ mHour + ":" + mMinute);
				makeApiRequest();
				fillData();
			}
		});
	}

	@Override
	public int createLayout() {
		settings = PreferenceManager
		.getDefaultSharedPreferences(getBaseContext());
		if (settings.getBoolean("preffullscreen", false))
			ConnectionMaker.setFullscreen(context);
		return R.layout.activity_planner;
	}

	private void fillData() {
		// Get all of the rows from the database and create the item list
		mDbHelper.open();
		myConnectionCursor = mDbHelper.fetchAllConnections();
		// Log.i(TAG,"Cursor size is: "+myConnectionCursor.getCount());
		myViaCursor = mDbHelper.fetchAllVias();
		startManagingCursor(myConnectionCursor);
		startManagingCursor(myViaCursor);

		connAdapter = new ConnectionAdapter(this, R.layout.row_planner,
				new ArrayList<Connection>(), myConnectionCursor);
		setListAdapter(connAdapter);

		if (myConnectionCursor.getCount() > 0)
			fillAllNewConnections(myConnectionCursor, myViaCursor);

		//mDbHelper.close();
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
				allConnections.add(new Connection(
						new Station("", departurePlatform, true, departureTime,
								departureStation, "stationCoordinates", delayDStr,
								"status"),
								vias, 
								new Station(lastTrain,
								arrivalPlatform, true, arrivalTime,
								arrivalStation, "stationCoordinates", delayAStr,
								"status"), duration, delayDStr, delayAStr));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, Integer
					.valueOf(mYear) + 2000, Integer.valueOf(mMonth) - 1,
					Integer.valueOf(mDay));
		case TIME_DIALOG_ID:
			return new TimePickerDialog(this, mTimeSetListener, Integer
					.valueOf(mHour), Integer.valueOf(mMinute), true);

		case CONNECTION_DIALOG_ID:
			// Log.i("BETRAINS","clicked on: "+allConnections
			// .get(positionClicked).getArrivalStation().getVehicle());
			try {
				return new ConnectionDialog(this, allConnections
						.get(positionClicked));
			} catch (Exception e) {
				Toast.makeText(getBaseContext(),
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

	// the callback received when the user "sets" the date in the dialog
	public DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			tracker.trackEvent("Click", "ButtonSetDate", "clicked", 0);
			mYear = ConnectionMaker.fillZero("" + (year - 2000));
			mMonth = ConnectionMaker.fillZero("" + (monthOfYear + 1));
			mDay = ConnectionMaker.fillZero("" + dayOfMonth);
			mActionBar.setTitle(mDay + "/" + mMonth + "/" + mYear + "   "
					+ mHour + ":" + mMinute);
		}
	};

	// the callback received when the user "sets" the time in the dialog
	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			tracker.trackEvent("Click", "ButtonSetTime", "clicked", 0);
			mHour = ConnectionMaker.fillZero("" + hourOfDay);
			mMinute = ConnectionMaker.fillZero("" + minute);
			mActionBar.setTitle(mDay + "/" + mMonth + "/" + mYear + "   "
					+ mHour + ":" + mMinute);
		}
	};

	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, MENU_01, 0, getString(R.string.txt_add_fav)).setIcon(
				R.drawable.ic_menu_fav);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case MENU_01:

			ConnectionMaker.addAsStarred(tvDeparture.getText().toString(),
					tvArrival.getText().toString(), 3, context);
			return true;

		case MENU_02:

			// tvDay = (TextView) findViewById(R.id.tv_day);
			// tvMonth = (TextView) findViewById(R.id.tv_month);
			// tvYear = (TextView) findViewById(R.id.tv_year);

			String date = "20" + tvYear.getText().toString() + "-"
					+ tvMonth.getText().toString() + "-"
					+ tvDay.getText().toString();

			// TO EVALUATE ....
			String url = "http://sefora.b-rail.be/sefora/Sefora/input.do?browserType=2"
					+ "&fromStation="
					+ getStationNumber(tvDeparture.getText().toString())
					+ "&toStation="
					+ getStationNumber(tvArrival.getText().toString())
					+ "&viaStation=&seforaRequest.travelTypeId=1&seforaRequest.travelClassId=2&dateString="
					+ date
					+ "&seforaRequest.travellerAgeId=06&seforaRequest.travelFrequencyId=01&seforaRequest.travelReductionId=00&action=Suivant++%3E";

			url = "http://sefora.b-rail.be/sefora/Sefora/input.do?fromStation=1189&toStation=6&viaStation=&seforaRequest.travelTypeId=1&seforaRequest.travelClassId=2&dateString=2010-03-23&seforaRequest.travellerAgeId=06&seforaRequest.travelFrequencyId=01&seforaRequest.travelReductionId=00&action=Suivant++%3E";

			// String[] PD="";//Codes.affichePrix(url);

			// String prix=PD[0];
			// String dist=PD[1];

			// Toast.makeText(BETrains.this,tv_depart.getText().toString()+" - "+tv_arrivee.getText().toString()+": "+prix+"€"+"\nDistance= "+dist+"km",Toast.LENGTH_LONG).show();
			return true;
		}

		return false;
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		positionClicked = position;
		removeDialog(CONNECTION_DIALOG_ID);
		// Log.v(TAG,"click");
		try {

			Connection currentConnection = allConnections.get(positionClicked);
			// Log.v(TAG,"size: "+currentConnection.getVias().size());
			if (currentConnection.getVias().size() > 0) {
				showDialog(CONNECTION_DIALOG_ID);
			} else
				noDataClick(positionClicked);
		} catch (Exception e) {
			e.printStackTrace();
			noDataClick(positionClicked);

		}

	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
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

	protected void onPause() {
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

				runOnUiThread(new Runnable() {
					public void run() {
						progressDialog = ProgressDialog.show(
								PlannerActivity.this, "",
								getString(R.string.txt_patient), true);
					}
				});

				// TODO
				makeApiRequest();
				runOnUiThread(dismissPd);
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

		Log.d(TAG, settings.getString(context
				.getString(R.string.key_planner_da), "1"));
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
		allConnections = ConnectionMaker.newSearchTrains(mYear, mMonth, mDay,
				mHour, mMinute, langue, myStart, myArrival, dA, trainOnly,
				context);

		if (allConnections == null) {
			Log.e(TAG, "API failure!!!");
			tracker.trackEvent("Error API", myStart + " - " + myArrival, "", 0);
			this.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(PlannerActivity.this, R.string.txt_error,
							Toast.LENGTH_LONG).show();
				}
			});

		}
		tracker.dispatch();

	}

	public void onDestroy() {
		super.onDestroy();
	
		tracker.stop();

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
	
	public void onRestart() {
		super.onRestart();
		//mDbHelper.open();

	}


	public boolean onHandleActionBarItemClick(GDActionBarItem item, int position) {

		switch (position) {
		case 0:
			Intent i = new Intent(PlannerActivity.this,
					PlannerSettingsActivity.class);
			startActivity(i);
			break;

		default:
			return super.onHandleActionBarItemClick(item,position);
		}
		return true;
	}

	public static String getKey() {
		return KEY;
	}

	public static int getClick1Id() {
		return CLICK1_ID;
	}

	public static int getActivityInfo() {
		return ACTIVITY_INFO;
	}

	public void setTvHour(TextView tvHour) {
		this.tvHour = tvHour;
	}

	public TextView getTextViewHour() {
		return tvHour;
	}

	public void setTvMinute(TextView tvMinute) {
		this.tvMinute = tvMinute;
	}

	public TextView getTextViewMinute() {
		return tvMinute;
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

	public void setContext(PlannerActivity context) {
		this.context = context;
	}

	public Activity getContext() {
		return context;
	}

	public void setTAG(String tAG) {
		TAG = tAG;
	}

	public String getTAG() {
		return TAG;
	}

	public void setQuickAction(View v) {
		// array to hold the coordinates of the clicked view
		int[] xy = new int[2];
		// fills the array with the computed coordinates
		v.getLocationInWindow(xy);
		// rectangle holding the clicked view area
		Rect rect = new Rect(xy[0], xy[1], xy[0] + v.getWidth(), xy[1]
				+ v.getHeight());

		// a new QuickActionWindow object
		final QuickActionWindow qa = new QuickActionWindow(context, v, rect);

		// adds an item to the badge and defines the quick action to be
		// triggered
		// when the item is clicked on
		qa.addItem(
				getResources().getDrawable(android.R.drawable.ic_menu_today),
				context.getString(R.string.txt_date), new OnClickListener() {
					public void onClick(View v) {
						showDialog(DATE_DIALOG_ID);
						// qa.dismiss();
					}
				});

		qa.addItem(getResources().getDrawable(R.drawable.ic_menu_time), context
				.getString(R.string.txt_time), new OnClickListener() {
			public void onClick(View v) {
				showDialog(TIME_DIALOG_ID);
				// qa.dismiss();
			}
		});

		// shows the quick action window on the screen
		qa.show();

	}

	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON1) {
			// OK button

		}

		// Remove the dialog, it won't be used again
		removeDialog(CONNECTION_DIALOG_ID);

	}

	public void onCancel(DialogInterface dialog) {
		removeDialog(CONNECTION_DIALOG_ID);

	}

	@Override
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

		Intent i = new Intent(this, InfoTrainActivity.class);

		i.putExtra("fromto", tvDeparture.getText().toString() + " - "
				+ tvArrival.getText().toString());

		i.putExtra(ConnectionDbAdapter.KEY_TRAINS, c.getString(c
				.getColumnIndexOrThrow(ConnectionDbAdapter.KEY_TRAINS)));

		startActivity(i);
	}

}
