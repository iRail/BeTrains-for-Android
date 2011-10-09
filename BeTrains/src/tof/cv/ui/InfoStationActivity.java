package tof.cv.ui;

/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.savedInstanceState
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import greendroid.app.GDListActivity;
import greendroid.widget.GDActionBar;
import greendroid.widget.GDActionBarItem;

import java.util.ArrayList;
import java.util.Date;

import tof.cv.adapters.StationInfoAdapter;
import tof.cv.bo.DownloadStationInfoTask;
import tof.cv.bo.Station;
import tof.cv.misc.ConnectionDbAdapter;
import tof.cv.misc.ConnectionMaker;
import tof.cv.mpp.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.devoteam.quickaction.QuickActionWindow;

public class InfoStationActivity extends GDListActivity {

	private TextView mBodyText;
	private long timestamp;
	private Long rowId;
	private Context context;
	private StationInfoAdapter trainstops = null;
	private static final int ADD_ID = 1;
	private static final int ACTIVITY_RETARD = 1;
	private static final String TAG = "InfoStation";
	private String stationsName;
	private int hour;
	private int minute;
	private String title;
	private String lang;
	private ProgressDialog progDialog = null;
	private String[] GPS={"1","2"};
	private SharedPreferences settings;
	private ArrayList<Station> stationStops;
	private ImageButton nextButton;
	private ImageButton prevButton;
	private String choice;
	GDActionBar mABar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		stationStops = new ArrayList<Station>();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		context = this;

		if (settings.getBoolean("preffullscreen", false))
			ConnectionMaker.setFullscreen(this);

		settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		mABar = getGDActionBar();
		// mABar.addItem(R.drawable.ic_title_more);
	/*	addActionBarItem(getGDActionBar().newActionBarItem(
				NormalActionBarItem.class)
				.setDrawable(R.drawable.ic_title_more), R.id.action_bar_more);
	*/
		mABar.setTitle(getString(R.string.txt_station));

		mBodyText = (TextView) findViewById(R.id.hours);
		nextButton = (ImageButton) findViewById(R.id.Button_next);
		prevButton = (ImageButton) findViewById(R.id.Button_prev);

		registerForContextMenu(this.getListView());

		choice = "DEP";

		setRowId(null);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {

			stationsName = extras.getString("gare_name");
			String strHour = extras.getString("gare_heure");
			String strMinute = extras.getString("gare_minute");

			Date date_now = new Date();

			if (strHour == null)
				strHour = ConnectionMaker.fillZero("" + date_now.getHours());
			if (strMinute == null)
				strMinute = ConnectionMaker
						.fillZero("" + date_now.getMinutes());

			hour = Integer.valueOf(strHour);
			minute = Integer.valueOf(strMinute);
			lang = this.getString(R.string.url_lang_2);

			if (settings.getBoolean("prefnl", false))
				lang = "nl";

			new DownloadStationInfoTask(this, stationsName, lang, "" + hour, ""
					+ minute, choice).execute();

			Bundle bundle = new Bundle();
			Intent mIntent = new Intent();
			mIntent.putExtras(bundle);
			setResult(RESULT_OK, mIntent);
		}

		View home = (View) findViewById(R.id.gd_action_bar_home_item);
		home.setOnClickListener((new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(InfoStationActivity.this,
						WelcomeActivity.class));
			}
		}));

		setNextButtonListener();
		setPrevButtonListener();

	}
	
	public void setGPS(String lat, String lon) {
		GPS[0]=lat;
		GPS[1]=lon;
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
		final AlertDialog.Builder ad = new AlertDialog.Builder(context);

		// adds an item to the badge and defines the quick action to be
		// triggered
		// when the item is clicked on
		qa.addItem(getResources().getDrawable(
				android.R.drawable.ic_menu_directions), context
				.getString(R.string.txt_nav), new OnClickListener() {
			public void onClick(View v) {

				ad.setTitle(R.string.txt_confirm_nav);
				ad.setPositiveButton(android.R.string.ok,
						new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {
								Runnable getStationNAV = new Runnable() {

									public void run() {

										runOnUiThread(returnNav);
									}
								};

								Thread thread = new Thread(null, getStationNAV,
										"MagentoBackground");
								thread.start();
								progDialog = ProgressDialog
										.show(InfoStationActivity.this,
												"please wait",
												"Loading position", true);

							}
						});

				ad.setNegativeButton(android.R.string.no,
						new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {

							}
						});
				ad.show();
				qa.dismiss();
			}
		});

		qa.addItem(getResources().getDrawable(
				android.R.drawable.ic_menu_mapmode), context
				.getString(R.string.txt_map), new OnClickListener() {
			public void onClick(View v) {
				ad.setTitle(R.string.txt_confirm_map);
				ad.setPositiveButton(android.R.string.ok,
						new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {

								Runnable getStationGPS = new Runnable() {

									public void run() {

										runOnUiThread(returnGPS);
									}
								};

								Thread threadGps = new Thread(null,
										getStationGPS, "MagentoBackground");
								threadGps.start();
								progDialog = ProgressDialog
										.show(InfoStationActivity.this,
												"please wait",
												"Loading position", true);

							}
						});

				ad.setNegativeButton(android.R.string.no,
						new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {

							}
						});
				ad.show();
				qa.dismiss();
			}
		});

		// shows the quick action window on the screen
		qa.show();

	}

	private void setNextButtonListener() {
		nextButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Log.i("","before: "+new Date(timestamp).toString());
				timestamp += 3600000;
				Log.i("","after: "+new Date(timestamp).toString());
				searchTrains();

			}
		});

	}

	private void setPrevButtonListener() {
		prevButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				timestamp -= 3600000;
				searchTrains();
			}
		});

	}

	private Runnable returnGPS = new Runnable() {

		public void run() {

			if (!GPS[0].contentEquals("X") && !GPS[1].contentEquals("X")) {

				try {
					Intent i = new Intent(InfoStationActivity.this,
							StationMapActivity.class);
					i.putExtra("nom", title);
					i.putExtra("lat", GPS[0]);
					i.putExtra("lon", GPS[1]);
					startActivityForResult(i, 0);
				} catch (ActivityNotFoundException e) {
					(Toast.makeText(context, "GoogleMap not found",
							Toast.LENGTH_LONG)).show();
				}

			} else
				Toast.makeText(InfoStationActivity.this,
						"Sorry, an error happened", Toast.LENGTH_LONG).show();

			progDialog.dismiss();
			// m_adapter.notifyDataSetChanged();
		}
	};

	@Override
	public int createLayout() {
		return R.layout.activity_info_station;
	}

	private Runnable returnNav = new Runnable() {

		public void run() {

			if (!GPS[0].contentEquals("X") && !GPS[1].contentEquals("X")) {

				try {
					Uri uri = Uri.parse("google.navigation:q=" + GPS[0] + ","
							+ GPS[1]);
					Intent it = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(it);
				} catch (ActivityNotFoundException e) {
					(Toast.makeText(context, "Navigation not found",
							Toast.LENGTH_LONG)).show();
				}

			} else
				Toast.makeText(InfoStationActivity.this,
						"Sorry, an error happened", Toast.LENGTH_LONG).show();

			progDialog.dismiss();

		}
	};

	public void searchTrains() {

		String url = "http://api.irail.be/liveboard/?station="
				+ ConnectionMaker.getTrainId(stationsName).replace(" ", "%20")
				+ "&lang=" + lang + "&time="
				+ ConnectionMaker.NewgetHourFromDate(timestamp) + ""
				+ ConnectionMaker.NewgetMinutsFromDate(timestamp)
				+ "&date=" +ConnectionMaker.formatDate(timestamp)
				+ "&arrdep=" + choice;

		try {
			stationStops = ConnectionMaker.afficheGareL(url, this);
		} catch (StringIndexOutOfBoundsException e) {
			Log.e("InfoGare.java", e.toString());
		}
		if (stationStops != null)
			if (stationStops.size() > 0) {
				title = stationStops.get(0).getStation();
				stationStops.remove(0);
			} else {
				title = "Error (unknown)";
			}

		setAllText(title);
		
		fillTrainStops();

	}

	public void setAllText(String text) {
		mABar.setTitle(text);
		mBodyText.setText(ConnectionMaker.getDate(timestamp));
	}

	public void setTimestamp(long l) {
		this.timestamp = l;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public void setConnectionTitleText() {
		this.mBodyText.setText(getString(R.string.txt_connection));
	}

	public void setStationStops(ArrayList<Station> stationStops) {
		this.stationStops = stationStops;
	}

	public void setFullscreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public void setNoTitle() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public void fillTrainStops() {

		trainstops = new StationInfoAdapter(this, R.layout.row_info_station,
				stationStops);
		if (stationStops != null) {
			setListAdapter(trainstops);
		}
	}

	public void onResume() {
		super.onResume();

	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, ADD_ID, 0, "INFO");
	}

	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ADD_ID:
			AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();
			Station trainstop = trainstops.getItem((int) menuInfo.id);

			String id = trainstop.getVehicle();

			StringBuffer strBuff = new StringBuffer();
			StringBuffer strBuffid = new StringBuffer();
			char c;

			for (int i = 0; i < id.length(); i++) {
				c = id.charAt(i);

				if (Character.isDigit(c)) {
					strBuff.append(c);
				} else
					strBuffid.append(c);
			}
			String num = strBuff.toString();
			String mid = strBuffid.toString();

			Intent i = new Intent(InfoStationActivity.this,
					InfoTrainActivity.class);

			i.putExtra("fromto", "");
			i.putExtra(ConnectionDbAdapter.KEY_ROWID, 69);

			i.putExtra(ConnectionDbAdapter.KEY_TRAINS, mid + " " + num);
			startActivityForResult(i, ACTIVITY_RETARD);

			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}

	@Override
	public boolean onHandleActionBarItemClick(GDActionBarItem item, int position) {

		switch (position) {

		case 0:
			setQuickAction(getGDActionBar());
			return true;

		default:
			return super.onHandleActionBarItemClick(item, position);
		}
	}

	public static String getTag() {
		return TAG;
	}

	public String getLang() {
		return lang;
	}

	public void setSettings(SharedPreferences settings) {
		this.settings = settings;
	}

	public SharedPreferences getSettings() {
		return settings;
	}

	public void setRowId(Long rowId) {
		this.rowId = rowId;
	}

	public Long getRowId() {
		return rowId;
	}

}
