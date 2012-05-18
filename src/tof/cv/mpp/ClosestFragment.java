package tof.cv.mpp;

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

import tof.cv.mpp.Utils.DbAdapterLocation;
import tof.cv.mpp.adapter.StationLocationAdapter;
import tof.cv.mpp.bo.StationLocation;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ClosestFragment extends SherlockListFragment {
	protected static final String TAG = "ClosestFragment";
	private MyGPSLocationListener locationGpsListener;
	private MyNetworkLocationListener locationNetworkListener;
	private LocationManager locationManager;
	private Button btnUpdate;
	private Location bestLocationFound;
	private boolean threadLock = false;
	private boolean isFirst = false;
	private MyProgressDialog m_ProgressDialog;
	private Thread thread = null;
	private StationLocationAdapter myLocationAdapter;
	private String strCharacters;
	private DbAdapterLocation mDbHelper;
	ArrayList<StationLocation> stationList = new ArrayList<StationLocation>();
	Cursor locationCursor;
	private TextView tvEmpty;
	private Button btEmpty;
	StationLocation clicked;

	private static final long INT_MINTIME = 3000;
	private static final long INT_MINDISTANCE = 50;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_closest, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setHasOptionsMenu(true);

		m_ProgressDialog = new MyProgressDialog(getActivity());
		mDbHelper = new DbAdapterLocation(getActivity());

		tvEmpty = (TextView) getActivity().findViewById(R.id.empty_tv);

		btEmpty = (Button) getActivity().findViewById(R.id.empty_bt);
		btEmpty.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Intent myIntent = new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(myIntent);
			}
		});

		btnUpdate = (Button) getActivity().findViewById(R.id.btn_update);
		btnUpdate.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if (!threadLock)
					notifyList(true);
			}
		});

		locationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		locationGpsListener = new MyGPSLocationListener();
		locationNetworkListener = new MyNetworkLocationListener();

		myLocationAdapter = new StationLocationAdapter(getActivity(),
				R.layout.row_closest, new ArrayList<StationLocation>());
		setListAdapter(myLocationAdapter);

	}

	public void onListItemClick(ListView l, View v, int position, long id) {
		final CharSequence[] items = { getString(R.string.txt_nav),getString(R.string.txt_map), "StreetView" };
		final StationLocation clicked = (StationLocation) l
				.getItemAtPosition(position);
		AlertDialog.Builder builder = new AlertDialog.Builder(
				this.getActivity());
		builder.setTitle(clicked.getStation());
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				//Toast.makeText(getActivity().getApplicationContext(),
				//		items[item], Toast.LENGTH_SHORT).show();
				switch (item) {
				case 0:
					try {
						Uri uri = Uri.parse("google.navigation:q="
								+ ((double) clicked.getLat() / 1E6) + ","
								+ ((double) clicked.getLon() / 1E6));
						Intent it = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(it);
					} catch (ActivityNotFoundException e) {
						(Toast.makeText(getActivity(),
								"Google Navigation not found",
								Toast.LENGTH_LONG)).show();
					}
					break;
				case 1:
					try {
						
						Intent i = new Intent(android.content.Intent.ACTION_VIEW, 
								Uri.parse("geo:0,0?q="+(clicked.getLat() / 1E6)+","+(clicked.getLon() / 1E6)+" (" + clicked.getStation() + ")"));
						/*Intent i = new Intent(getActivity(),
								MapStationActivity.class);

						i.putExtra("Name", clicked.getStation());
						i.putExtra("lat", (clicked.getLat() / 1E6));

						i.putExtra("lon", (clicked.getLon() / 1E6));
*/
						startActivity(i);
					} catch (ActivityNotFoundException e) {
						(Toast.makeText(getActivity(), "Google Maps not found",
								Toast.LENGTH_LONG)).show();
					}
					break;
				case 2:
					try {
						Intent streetIntent = new Intent(Intent.ACTION_VIEW,
								Uri.parse("google.streetview:cbll=" + clicked.getLat()/ 1E6
										+ "," + clicked.getLon()/ 1E6));
						startActivity(streetIntent);
					} catch (ActivityNotFoundException e) {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle(R.string.svTitle)
								.setMessage(R.string.svMessage)
								.setCancelable(false)
								.setPositiveButton(android.R.string.ok,
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int id) {
												Intent goToMarket = new Intent(
														Intent.ACTION_VIEW,
														Uri.parse("market://details?id=com.google.android.street"));
												try {
													startActivity(goToMarket);
												} catch (Exception e) {
													Toast.makeText(getActivity(),
															R.string.noMarket,
															Toast.LENGTH_LONG).show();

												}
											}
										})
								.setNegativeButton(android.R.string.cancel,
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog,
													int id) {
												dialog.cancel();
											}
										});
						AlertDialog alert = builder.create();
						alert.show();
					}
					break;
				}
			
			}
		});
		builder.create().show();
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		System.out.println("YAHOO DEBUG ++++++");
		switch (item.getItemId()) {
		case 0:
			try {
				Uri uri = Uri.parse("google.navigation:q="
						+ ((double) clicked.getLat() / 1E6) + ","
						+ ((double) clicked.getLon() / 1E6));
				Intent it = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(it);
			} catch (ActivityNotFoundException e) {
				(Toast.makeText(getActivity(), "Navigation not found",
						Toast.LENGTH_LONG)).show();
			}
			return true;
		case 1:
			try {
				Intent i = new Intent(getActivity(), MapStationActivity.class);

				i.putExtra("nom", clicked.getStation());
				i.putExtra("lat", "" + (clicked.getLat() / 1E6));

				i.putExtra("lon", "" + (clicked.getLon() / 1E6));

				startActivity(i);
			} catch (ActivityNotFoundException e) {
				(Toast.makeText(getActivity(), "GoogleMap not found",
						Toast.LENGTH_LONG)).show();
			}

			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(Menu.NONE, 0, Menu.NONE, "Reload")
				.setIcon(R.drawable.ic_menu_refresh)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			downloadStationListFromApi();
			return true;
		case (android.R.id.home):
			// app icon in ActionBar is clicked; Go home
			Intent intent = new Intent(getActivity(), WelcomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class MyGPSLocationListener implements LocationListener

	{

		public void onLocationChanged(final Location loc) {
			Log.v(TAG, "GPS");
			locationManager.removeUpdates(locationNetworkListener);
			if (loc != null) {
				// GPS Location is considered as the best
				// We can of course improve that.
				bestLocationFound = loc;
				btnUpdate.setVisibility(View.VISIBLE);
				btnUpdate.setText(getActivity().getString(
						R.string.update_gps_btn, loc.getAccuracy()));

				if (!threadLock)
					notifyList(false);

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
				bestLocationFound = loc;
				btnUpdate.setVisibility(View.VISIBLE);
				btnUpdate.setText(getActivity().getString(
						R.string.update_gps_btn, loc.getAccuracy()));
				if (!threadLock)
					notifyList(false);
			}
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

	}

	private void updateListToBestLocation() {
		mDbHelper.open();
		locationCursor = mDbHelper.fetchAllLocations();
		if (locationCursor.getCount() == 0) {
			getActivity().runOnUiThread(hideProgressdialog);
			downloadStationListFromApi();
		} else {
			m_ProgressDialog.setMax(locationCursor.getCount());
			stationList.clear();

			for (int i = 0; i < locationCursor.getCount(); i++) {

				compareStationsListToMyLocation(locationCursor, i,
						bestLocationFound.getLatitude(),
						bestLocationFound.getLongitude());
			}
			mDbHelper.close();
			Collections.sort(stationList);
			if (!threadLock)
				notifyList(false);
			getActivity().runOnUiThread(hideProgressdialog);
		}
	}

	private Runnable lockOff = new Runnable() {

		public void run() {
			getActivity().getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	};

	private Runnable lockOn = new Runnable() {

		public void run() {
			getActivity().getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	};

	class MyProgressDialog extends ProgressDialog {
		public MyProgressDialog(Context context) {
			super(context);
		}

		public void onBackPressed() {
			super.onBackPressed();
			getActivity().runOnUiThread(hideProgressdialog);
			thread.interrupt();
			return;
		}
	}

	private Runnable hideProgressdialog = new Runnable() {
		public void run() {
			m_ProgressDialog.dismiss();
		}
	};

	protected void downloadStationListFromApi() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.txt_patient))
				.setMessage(getString(R.string.txt_first_dl))
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Runnable fillDataRunnable = new Runnable() {

									public void run() {
										downloadStationListThread();
									}
								};

								thread = new Thread(null, fillDataRunnable,
										"MagentoBackground");
								thread.start();
								try {
									m_ProgressDialog.hide();
									m_ProgressDialog = new MyProgressDialog(
											getActivity());
								} catch (Exception e) {
									Looper.prepare();
									m_ProgressDialog = new MyProgressDialog(
											getActivity());
								}
								m_ProgressDialog.setCancelable(false);
								m_ProgressDialog
										.setTitle(getString(R.string.txt_patient));
								m_ProgressDialog
										.setMessage(getString(R.string.txt_dl_stations));
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
		getActivity().runOnUiThread(lockOff);
		DownloadAndParseStationList();
		mDbHelper.open();
		final Cursor locationCursor = mDbHelper.fetchAllLocations();
		if (locationCursor.getCount() > 0) {
			// TODO Refresh
			mDbHelper.close();
		} else {
			getActivity().runOnUiThread(hideProgressdialog);
			getActivity().runOnUiThread(noConnexion);
		}
		getActivity().runOnUiThread(lockOn);
		mDbHelper.close();

	}

	/**
	 * Each time I come back in the activity, I listen to GPS
	 */
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "RESUME");
		String txt = "";
		locationManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
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

	public void compareStationsListToMyLocation(Cursor locationCursor, int i,
			double lat, double lon) {
		locationCursor.moveToPosition(i);
		String strName = locationCursor.getString(locationCursor
				.getColumnIndex(DbAdapterLocation.KEY_STATION_NAME));
		m_ProgressDialog.incrementProgressBy(1);

		double iLat = locationCursor.getInt(locationCursor
				.getColumnIndex(DbAdapterLocation.KEY_STATION_LAT));

		double iLon = locationCursor.getInt(locationCursor
				.getColumnIndex(DbAdapterLocation.KEY_STATION_LON));

		double dDis = StationLocationAdapter.distance(lat, lon, iLat / 1E6,
				iLon / 1E6);

		stationList.add(new StationLocation(strName, iLat, iLon, dDis + ""));
	}

	public void DownloadAndParseStationList() {

		try {
			mDbHelper.open();
			URL url = new URL("http://api.irail."
					// URL url = new URL("http://dev.api.irail."
					+ PreferenceManager.getDefaultSharedPreferences(
							getActivity()).getString("countryPref", "be")
					+ "/stations.php");

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

			Looper.prepare();

			getActivity().runOnUiThread(hideProgressdialog);
			Log.v(TAG, "Finish to parse");

		} catch (Exception e) {
			e.printStackTrace();
			Log.v(TAG, "Connexion error");
			try {
				getActivity().runOnUiThread(noConnexion);
				getActivity().runOnUiThread(hideProgressdialog);
			} catch (Exception f) {
				// Si il a quitté l'activité.
			}

		}
	}

	private class StationHandler extends DefaultHandler {

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

				lat = (int) (Float.valueOf(attributes.getValue(attributes
						.getIndex("locationY"))) * 1E6);
				lon = (int) (Float.valueOf(attributes.getValue(attributes
						.getIndex("locationX"))) * 1E6);
			} else {// state = stateUnknown;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			// state = stateUnknown;
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			strCharacters = new String(ch, start, length);
			try {
				// if (state == stateStation && !thread.isInterrupted()) {
				if (!thread.isInterrupted()) {
					m_ProgressDialog.incrementProgressBy(1);
					getActivity().runOnUiThread(changeProgressDialogMessage);
					mDbHelper.createStationLocation(strCharacters, "0", lat,
							lon, 0.0);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	private Runnable changeProgressDialogMessage = new Runnable() {

		public void run() {
			m_ProgressDialog.setMessage(strCharacters);
		}
	};

	private Runnable noConnexion = new Runnable() {

		public void run() {
			getActivity().runOnUiThread(hideProgressdialog);
			tvEmpty.setText(R.string.txt_connection);
			if (locationManager != null) {
				locationManager.removeUpdates(locationGpsListener);
				locationManager.removeUpdates(locationNetworkListener);
			}

			locationManager = null;
		}
	};

	public void notifyList(boolean manual) {
		threadLock = true;
		Log.v(TAG, "notifyList");
		// m_ProgressDialog.hide();
		m_ProgressDialog = new MyProgressDialog(this.getActivity());
		m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		m_ProgressDialog.setCancelable(false);
		m_ProgressDialog.setTitle(getString(R.string.txt_patient));
		m_ProgressDialog.setMessage(getString(R.string.txt_fill_closest));
		m_ProgressDialog.show();
		// I only update automatically first time
		if (!isFirst || manual) {
			Log.v(TAG, "GO!");
			isFirst = true;
			// Upate the list and notify Adapter
			Runnable notifyListRunnable = new Runnable() {
				public void run() {
					updateListToBestLocation();
					myLocationAdapter.clear();
					for (StationLocation object : stationList) {
						myLocationAdapter.add(object);
					}
					myLocationAdapter.notifyDataSetChanged();
					threadLock = false;
					m_ProgressDialog.hide();
				}
			};
			getActivity().runOnUiThread(notifyListRunnable);
		}
		threadLock = false;
		m_ProgressDialog.hide();
	}

}
