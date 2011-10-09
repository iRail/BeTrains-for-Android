package tof.cv.ui;

import greendroid.app.GDListActivity;
import greendroid.widget.GDActionBar;
import greendroid.widget.GDActionBarItem;
import greendroid.widget.NormalActionBarItem;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import tof.cv.adapters.StationLocationAdapter;
import tof.cv.bo.StationLocation;
import tof.cv.misc.ConnectionMaker;
import tof.cv.misc.LocationDbHelper;
import tof.cv.mpp.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.devoteam.quickaction.QuickActionWindow;

public class GetClosestStationsActivity extends GDListActivity {

	private static final String TAG = "BETRAINS";
	private static final long INT_MINTIME = 0;
	private static final long INT_MINDISTANCE = 0;
	private static SharedPreferences settings;
	private LocationManager locationManager;
	private MyGPSLocationListener locationGpsListener;
	private MyNetworkLocationListener locationNetworkListener;
	private GetClosestStationsActivity context = this;
	private static LocationDbHelper mDbHelper;
	private boolean isFirst = false;
	private TextView tvTitle;
	private MyProgressDialog m_ProgressDialog;
	private Button btnUpdate;
	private Location lastLocation;
	private StationLocation clickedItem;
	private StationLocationAdapter myLocationAdapter;
	private Thread thread = null;
	private TextView tvEmpty;
	private Button btEmpty;
	private String strCharacters;

	ArrayList<StationLocation> stationList = new ArrayList<StationLocation>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialization of ACtionBar, buttons, etc...
		GDActionBar mActionBar = getGDActionBar();
		mActionBar.setTitle(getString(R.string.btn_closest_stations));
		//mActionBar.addItem(android.R.drawable.ic_menu_help);
		 addActionBarItem(getGDActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(android.R.drawable.ic_menu_help),R.id.action_bar_help);
		// Request the location update with Network (faster )
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationGpsListener = new MyGPSLocationListener();
		locationNetworkListener = new MyNetworkLocationListener();

		View home = (View) findViewById(R.id.gd_action_bar_home_item);
		home.setOnClickListener((new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(GetClosestStationsActivity.this,WelcomeActivity.class));
			}
		}));
		
		context = this;
		m_ProgressDialog = new MyProgressDialog(this);
		mDbHelper = new LocationDbHelper(this);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvEmpty = (TextView) findViewById(R.id.empty_tv);
		btEmpty = (Button) findViewById(R.id.empty_bt);
		btEmpty.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				Intent myIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
				startActivity(myIntent);
			}
		});
		btnUpdate = (Button) findViewById(R.id.btn_update);
		btnUpdate.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				updateListToLocation(lastLocation);
			}
		});

		// fill the list with the infos.

	}
	

	@Override
	public int createLayout() {
		settings = PreferenceManager
		.getDefaultSharedPreferences(getBaseContext());
		if (settings.getBoolean("preffullscreen", false))
			ConnectionMaker.setFullscreen(context);
		return R.layout.activity_closest;
	}

	/**
	 * Each time I come back in the activity, I listen to GPS
	 */
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG,"RESUME");
		isFirst = false;
		checkIfDbIsEmpty();

		String txt = "";
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		for (String aProvider : locationManager.getAllProviders())
			txt += ("<br>"
					+ aProvider
					+ ": <b>"
					+ (locationManager.isProviderEnabled(aProvider) ? "ON"
							: "OFF") + "</b>");
		txt += "<br><br>" + getString(R.string.txt_location);
		tvEmpty.setText(Html.fromHtml(txt));

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				INT_MINTIME, INT_MINDISTANCE, locationGpsListener);

		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, INT_MINTIME, INT_MINDISTANCE,
				locationNetworkListener);

	}

	/**
	 * Each time I leave the activity, I stop listening to GPS (battery)
	 */
	@Override
	public void onPause() {
		super.onPause();
		if (locationManager != null) {
			locationManager.removeUpdates(locationGpsListener);
			locationManager.removeUpdates(locationNetworkListener);
		}

		locationManager = null;
	}

	/**
	 * Fill the list at the activity creation
	 */
	private void checkIfDbIsEmpty() {
		mDbHelper.open();
		final Cursor locationCursor = mDbHelper.fetchAllLocations();
		if (locationCursor.getCount() == 0) {
			isFirst = true;
			downloadStationListFromApi();
		}
	}

	/**
	 * The handler and the parsing process
	 */
	private class StationHandler extends DefaultHandler {

		final int stateUnknown = 0;
		//final int stateStation = 1;
		int state = stateUnknown;
		int lat = 0;
		int lon = 0;

		@Override
		public void startDocument() throws SAXException {
		}

		@Override
		public void endDocument() throws SAXException {
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (localName.equalsIgnoreCase("station")) {
				
				lat = (int) (Float.valueOf( attributes.getValue(attributes.getIndex("locationY"))) * 1E6);
				lon = (int) (Float.valueOf( attributes.getValue(attributes.getIndex("locationX"))) * 1E6);


			}			
			
			else {
				state = stateUnknown;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			state = stateUnknown;
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			strCharacters = new String(ch, start, length);
			try {
				//if (state == stateStation && !thread.isInterrupted()) {
				if (!thread.isInterrupted()) {
					m_ProgressDialog.incrementProgressBy(1);
					runOnUiThread(changeProgressDialogMessage);
					mDbHelper.createStationLocation(strCharacters, "0", lat,
							lon, 0.0);

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * The locationListener that is called everytime I get a new position from
	 * system
	 */
	private class MyGPSLocationListener implements LocationListener

	{

		
		public void onLocationChanged(final Location loc) {

			if (loc != null) {
				// Change the title to the current accuracy and show button tu
				// update.
				// Because we have now a location.
				lastLocation = loc;
				btnUpdate.setVisibility(View.VISIBLE);
				btnUpdate.setText(context.getString(R.string.update_gps_btn,
						loc.getAccuracy()));
				// I only update automatically first time. After, user will do
				// that via the button.
				if (!isFirst) {
					Log.v(TAG, "MyGPSLocationListener");
					isFirst = true;
					updateListToLocation(loc);
				}

			}

		}

		
		public void onProviderDisabled(String provider) {
		}

		
		public void onProviderEnabled(String provider) {
		}

		
		public void onStatusChanged(String provider, int status,

		Bundle extras) {
		}

	}

	private class MyNetworkLocationListener implements LocationListener

	{

		
		public void onLocationChanged(final Location loc) {

			if (loc != null) {
				// Change the title to the current accuracy and show button tu
				// update.
				// Because we have now a location.
				lastLocation = loc;
				btnUpdate.setVisibility(View.VISIBLE);
				btnUpdate.setText(context.getString(R.string.update_gps_btn,
						loc.getAccuracy()));

				// I only update automatically first time. After, user will do
				// that via the button.
				if (!isFirst) {
					Log.v(TAG, "MyNetworkLocationListener");
					isFirst = true;
					updateListToLocation(loc);
				}
			}

		}

		
		public void onProviderDisabled(String provider) {
		}

	
		public void onProviderEnabled(String provider) {
		}

		
		public void onStatusChanged(String provider, int status,

		Bundle extras) {
		}

	}

	/**
	 * I have a new location and I update the list
	 */
	private void updateListToLocation(final Location loc) {
		Runnable updateListRunnable = new Runnable() {
			
			public void run() {
				updateListToLocationThread(loc.getLatitude(), loc
						.getLongitude());
			}
		};
		Log.v(TAG, "updateListToLocation");
		tvTitle.setText(getBaseContext().getString(R.string.txt_accuracy,
				loc.getAccuracy()));
		thread = new Thread(null, updateListRunnable, "MagentoBackground");
		thread.start();
		m_ProgressDialog.hide();
		m_ProgressDialog = new MyProgressDialog(this);
		m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		m_ProgressDialog.setCancelable(false);
		m_ProgressDialog.setTitle(getString(R.string.txt_patient));
		m_ProgressDialog.setMessage(getString(R.string.txt_fill_closest));
		m_ProgressDialog.show();

	}

	/**
	 * The thread that is launched to read the database and compare each Station
	 * location to my current location.
	 */
	@SuppressWarnings("unchecked")
	private void updateListToLocationThread(double lat, double lon) {

		mDbHelper.open();
		Cursor locationCursor = mDbHelper.fetchAllLocations();
		Log.i(TAG, "size in updateListToLocationThread: "
				+ locationCursor.getCount());
		if (locationCursor.getCount() == 0) {
			runOnUiThread(hideProgressdialog);
			downloadStationListFromApi();

		} else {
			runOnUiThread(hideProgressdialog);
			m_ProgressDialog.setMax(locationCursor.getCount());
			stationList.clear();

			for (int i = 0; i < locationCursor.getCount(); i++) {
				if (thread.isInterrupted()) {
					break;
				}
				compareStationsListToMyLocation(locationCursor, i, lat, lon);
			}
			Collections.sort(stationList);
			Looper.prepare();
			StationLocationAdapter locationAdapter = new StationLocationAdapter(
					context, R.layout.row_closest, stationList);
			myLocationAdapter = locationAdapter;
			runOnUiThread(hideProgressdialog);

		}

	}

	public void compareStationsListToMyLocation(Cursor locationCursor, int i,
			double lat, double lon) {
		locationCursor.moveToPosition(i);
		String strName = locationCursor.getString(locationCursor
				.getColumnIndex(LocationDbHelper.KEY_STATION_NAME));
		m_ProgressDialog.incrementProgressBy(1);
		
		double iLat = locationCursor.getInt(locationCursor
				.getColumnIndex(LocationDbHelper.KEY_STATION_LAT));

		double iLon = locationCursor.getInt(locationCursor
				.getColumnIndex(LocationDbHelper.KEY_STATION_LON));

		double dDis = StationLocationAdapter.distance(lat, lon, iLat / 1E6,
				iLon / 1E6);

		stationList.add(new StationLocation(strName, iLat, iLon, dDis + ""));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		StationLocationAdapter adapter = (StationLocationAdapter) l
				.getAdapter();
		try {
			clickedItem = (StationLocation) adapter.getItem(position);
			setQuickAction(v);
		} catch (Exception e) {

		}

	}

	public void setQuickAction(View v) {
		int[] xy = new int[2];
		v.getLocationInWindow(xy);
		Rect rect = new Rect(xy[0], xy[1], xy[0] + v.getWidth(), xy[1]
				+ v.getHeight());
		final QuickActionWindow qa = new QuickActionWindow(this, v, rect);

		qa.addItem(getResources().getDrawable(
				android.R.drawable.ic_menu_directions), this
				.getString(R.string.txt_nav), new OnClickListener() {
			public void onClick(View v) {
				try {
					Uri uri = Uri.parse("google.navigation:q="
							+ ((double) clickedItem.getLat() / 1E6) + ","
							+ ((double) clickedItem.getLon() / 1E6));
					Intent it = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(it);
				} catch (ActivityNotFoundException e) {
					(Toast.makeText(context, "Navigation not found",
							Toast.LENGTH_LONG)).show();
				}
				qa.dismiss();
			}
		});

		qa.addItem(getResources().getDrawable(
				android.R.drawable.ic_menu_mapmode), this
				.getString(R.string.txt_map), new OnClickListener() {
			public void onClick(View v) {
				try {
					Intent i = new Intent(GetClosestStationsActivity.this,
							StationMapActivity.class);

					i.putExtra("nom", clickedItem.getStation());
					i.putExtra("lat", "" + (clickedItem.getLat() / 1E6));

					i.putExtra("lon", "" + (clickedItem.getLon() / 1E6));

					startActivity(i);
				} catch (ActivityNotFoundException e) {
					(Toast.makeText(context, "GoogleMap not found",
							Toast.LENGTH_LONG)).show();
				}

				qa.dismiss();
			}
		});

		qa.addItem(getResources().getDrawable(
				android.R.drawable.ic_menu_myplaces), this
				.getString(R.string.txt_info_station), new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(context, clickedItem.getStation(),
						Toast.LENGTH_SHORT).show();
				qa.dismiss();
			}
		});
		qa.show();

	}

	
	
	@Override
	public boolean onHandleActionBarItemClick(GDActionBarItem item, int position) {

		switch (position) {

		case 0:
			downloadStationListFromApi();
			return true;

		default:
			return super.onHandleActionBarItemClick(item,position);
		}
	}
	
	protected void downloadStationListFromApi() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.txt_patient))
		.setMessage(getString(R.string.txt_first_dl))	
		.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								Runnable fillDataRunnable = new Runnable() {
									
									public void run() {
										downloadStationListThread();
									}
								};

								thread = new Thread(null, fillDataRunnable, "MagentoBackground");
								thread.start();
								try {
									m_ProgressDialog.hide();
									m_ProgressDialog = new MyProgressDialog(GetClosestStationsActivity.this);
								} catch (Exception e) {
									Looper.prepare();
									m_ProgressDialog = new MyProgressDialog(GetClosestStationsActivity.this);
								}
								m_ProgressDialog.setCancelable(false);
								m_ProgressDialog.setTitle(getString(R.string.txt_patient));
								m_ProgressDialog.setMessage(getString(R.string.txt_dl_stations));
								m_ProgressDialog.setMax(660);
								m_ProgressDialog.show();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();


	}

	/**
	 * Fill the list at the activity creation
	 */
	public void downloadStationListThread() {
		mDbHelper.open();
		runOnUiThread(lockOff);
		DownloadAndParseStationList();
		mDbHelper.open();
		final Cursor locationCursor = mDbHelper.fetchAllLocations();
		if (locationCursor.getCount() > 0) {
			mDbHelper.close();
			/*startActivity(new Intent(GetClosestStationsActivity.this,
					GetClosestStationsActivity.class));

			finish();*/
		} else {
			runOnUiThread(hideProgressdialog);
			runOnUiThread(noConnexion);
		}
		runOnUiThread(lockOn);
		mDbHelper.close();

	}

	public void DownloadAndParseStationList() {

		try {
			URL url = new URL("http://api.irail."
			//URL url = new URL("http://dev.api.irail."
					+ PreferenceManager.getDefaultSharedPreferences(
							getBaseContext()).getString("countryPref", "be")
					+ "/stations.php");
			isFirst = true;
			
			Log.v(TAG, "Begin to parse " + url);
			SAXParserFactory mySAXParserFactory = SAXParserFactory
					.newInstance();
			SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
			XMLReader myXMLReader = mySAXParser.getXMLReader();
			StationHandler myRSSHandler = new StationHandler();
			myXMLReader.setContentHandler(myRSSHandler);
			mDbHelper.deleteAllLocations();
			InputSource myInputSource = new InputSource(url.openStream());
			myXMLReader.parse(myInputSource);
			Log.v(TAG, "Finish to parse");
			isFirst = false;
			
		} catch (Exception e) {
			e.printStackTrace();
			Log.v(TAG, "Connexion error");
			runOnUiThread(noConnexion);
			runOnUiThread(hideProgressdialog);

		}
	}

	private Runnable changeProgressDialogMessage = new Runnable() {
		
		public void run() {
			m_ProgressDialog.setMessage(strCharacters);
		}
	};

	private Runnable hideProgressdialog = new Runnable() {
		
		public void run() {
			m_ProgressDialog.dismiss();
			setListAdapter(myLocationAdapter);
		}
	};

	private Runnable noConnexion = new Runnable() {
		
		public void run() {
			runOnUiThread(hideProgressdialog);
			tvEmpty.setText(R.string.txt_connection);
			if (locationManager != null) {
				locationManager.removeUpdates(locationGpsListener);
				locationManager.removeUpdates(locationNetworkListener);
			}

			locationManager = null;
		}
	};
	private Runnable lockOff = new Runnable() {
		
		public void run() {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	};
	
	private Runnable lockOn = new Runnable() {
	
		public void run() {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	};

	/**
	 * ProgressDialog that stop thread when back key is pressed
	 */
	class MyProgressDialog extends ProgressDialog {
		public MyProgressDialog(Context context) {
			super(context);
		}

		@Override
		public void onBackPressed() {
			super.onBackPressed();
			mDbHelper.close();
			runOnUiThread(hideProgressdialog);
			thread.interrupt();
			return;
		}
	}

}