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
import greendroid.widget.NormalActionBarItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tof.cv.adapters.TrainInfoAdapter;
import tof.cv.bo.DownloadLastMessageTask;
import tof.cv.bo.DownloadTrainInfoTask;
import tof.cv.bo.Message;
import tof.cv.bo.TrainStop;
import tof.cv.misc.ConnectionDbAdapter;
import tof.cv.misc.ConnectionMaker;
import tof.cv.mpp.R;
import tof.cv.widget.TrainAppWidgetProvider;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
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

public class InfoTrainActivity extends GDListActivity {

	private TextView mTitleText;
	private TextView mMessageText;
	private TextView mEmptyText;
	private String[] GPS={"1","2"};
	private static final int ADD_ID = 1;

	private static final int ACTIVITY_MESSAGE = 1;
	private static final int ACTIVITY_STATION = 2;

	private TrainInfoAdapter trainInfoAdapter = null;

	private String currentTrain = "";
	private int currentPos = 0;

	private String fromto = "";
	private String lang;

	private List<String> mTrainList;

	private ArrayList<ArrayList<TrainStop>> mSaveTrainList = new ArrayList<ArrayList<TrainStop>>();
	private ArrayList<Message> mSaveMessageList = new ArrayList<Message>();

	private Cursor mTrainStopCursor;

	private static SharedPreferences settings;
	private SharedPreferences.Editor editor;

	private String TAG = "Infotrain.java";
	private ConnectionDbAdapter mDbHelper;

	private InfoTrainActivity context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// System.out.println("infotrainactivity started ! ");
		context = this;

		settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());

		if (settings.getBoolean("preffullscreen", false))
			setFullscreen();

		GDActionBar mABar = getGDActionBar();
		 addActionBarItem(getGDActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(R.drawable.ic_title_more),R.id.action_bar_more);
		
		//mABar.addItem(R.drawable.ic_title_more);
		//addActionBarItem(getGDActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(R.drawable.ic_title_more),R.id.action_bar_more);
		mABar.setTitle(getString(R.string.txt_your_train));

		mTitleText = (TextView) findViewById(R.id.title);
		mEmptyText = (TextView) findViewById(android.R.id.empty);

		mMessageText = (TextView) findViewById(R.id.last_message);
		mDbHelper = new ConnectionDbAdapter(context);
		mDbHelper.open();

		Bundle extras = getIntent().getExtras();
		if (extras == null)
			finish();

		fromto = extras.getString("fromto");
		String trainsString = extras.getString(ConnectionDbAdapter.KEY_TRAINS);
		mTrainList = Arrays.asList(trainsString.split(";"));
		int i = 0;
		for (String aTrain : mTrainList) {
			mTrainList.set(i, ConnectionMaker.getTrainId(aTrain));
			i++;
		}
		
		View home = (View) findViewById(R.id.gd_action_bar_home_item);
		home.setOnClickListener((new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(InfoTrainActivity.this,WelcomeActivity.class));
			}
		}));

		lang = this.getString(R.string.url_lang_2);
		if (settings.getBoolean("prefnl", false))
			lang = "nl";
		currentTrain = mTrainList.get(0);
		setTitle();

		System.out.println("current context : " + context);
		new DownloadTrainInfoTask(context, currentPos).execute();

		if (settings.getBoolean("preffirstM", false)) {
			new DownloadLastMessageTask(context).execute(currentTrain);
			setLastMessageText(getString(R.string.txt_load_message));
		} else
			mMessageText.setText(Html.fromHtml(
					"<small>"
					+ getText(R.string.txt_infrabel)
					+ "</small>"));

		registerForContextMenu(this.getListView());

		final ImageButton prevButton = (ImageButton) findViewById(R.id.Button_prev);
		final ImageButton nextButton = (ImageButton) findViewById(R.id.Button_next);
		nextButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				currentPos++;
				currentTrain = mTrainList.get(currentPos);
				setTitle();
				setEmptyText(getString(R.string.txt_loading));

				if (mSaveTrainList.size() <= currentPos) {
					// getListView().setVisibility(View.INVISIBLE);

					trainInfoAdapter = new TrainInfoAdapter(context,
							R.layout.row_info_train, new ArrayList<TrainStop>());

					setListAdapter(trainInfoAdapter);

					new DownloadTrainInfoTask(context, currentPos).execute();

				} else {

					fillTrainStops(currentPos);
				}
				if (settings.getBoolean("preffirstM", false))
					if (mSaveMessageList.size() <= currentPos) {
						setLastMessageText(getString(R.string.txt_load_message));
						new DownloadLastMessageTask(context)
								.execute(currentTrain);

					} else {
						Message lastMessage = mSaveMessageList.get(currentPos);
						if (lastMessage != null)
							setLastMessageText(Html.fromHtml(lastMessage
									.getauteur()
									+ ": "
									+ lastMessage.getbody()
									+ "<br />"
									+ "<small>"
									+ lastMessage.gettime()
									+ "</small>"));
						else
							setLastMessageText(getString(R.string.txt_no_message));
					}

				prevButton.setVisibility(View.VISIBLE);
				if (currentPos == mTrainList.size() - 1) {
					nextButton.setVisibility(View.GONE);
				}

			}
		});

		prevButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				currentPos--;
				currentTrain = mTrainList.get(currentPos);
				setTitle();
				fillTrainStops(currentPos);
				setEmptyText(getString(R.string.txt_loading));

				if (settings.getBoolean("preffirstM", false)) {
					Message lastMessage = mSaveMessageList.get(currentPos);
					if (lastMessage != null)
						setLastMessageText(Html.fromHtml(lastMessage
								.getauteur()
								+ ": "
								+ lastMessage.getbody()
								+ "<br />"
								+ "<small>"
								+ lastMessage.gettime()
								+ "</small>"));
					else
						setLastMessageText(getString(R.string.txt_no_message));
				}

				nextButton.setVisibility(View.VISIBLE);
				if (currentPos == 0) {
					prevButton.setVisibility(View.GONE);
				}
			}
		});

		if (mTrainList.size() > 1) {
			nextButton.setVisibility(View.VISIBLE);
		}

		Bundle bundle = new Bundle();
		Intent mIntent = new Intent();
		mIntent.putExtras(bundle);
		setResult(RESULT_OK, mIntent);
		mDbHelper.close();

		mTitleText.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setQuickAction(v);
			}
		});
		mDbHelper.close();
	}

	@Override
	public int createLayout() {
		return R.layout.activity_info_train;
	}

	public static String getTrainNumber(String train) {

		char c;
		StringBuffer strBuffDigit = new StringBuffer();

		for (int i = train.length() - 1; i > 0; i--) {
			c = train.charAt(i);

			if (Character.isDigit(c)) {
				strBuffDigit.append(c);
			} else
				break;

		}

		return strBuffDigit.reverse().toString();
	}

	public void setFullscreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

		// adds an item to the badge and defines the quick action to be
		// triggered
		// when the item is clicked on
		/*qa.addItem(getResources().getDrawable(
				android.R.drawable.ic_menu_mapmode), context
				.getString(R.string.txt_map), new OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder ad;
				ad = new AlertDialog.Builder(context);
				ad.setTitle(R.string.txt_confirm_map);
				ad.setPositiveButton(android.R.string.ok,
						new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int arg1) {
								String[] GPS = {"A","B"};
								if (!GPS[0].contentEquals("X")
										&& !GPS[1].contentEquals("X")) {
									Intent i = new Intent(
											InfoTrainActivity.this,
											TrainMapActivity.class);
									i.putExtra("lat", GPS[0]);
									i.putExtra("lon", GPS[1]);
									startActivityForResult(i, 0);
								} else
									Toast.makeText(InfoTrainActivity.this,
											"This train is not driving",
											Toast.LENGTH_LONG).show();

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
*/
		qa.addItem(getResources().getDrawable(
				android.R.drawable.ic_menu_sort_alphabetically), context
				.getString(R.string.txt_messages), new OnClickListener() {
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString("ID", mTitleText.getText().toString());
				Intent mIntent = new Intent(InfoTrainActivity.this,
						MessagesActivity.class);
				mIntent.putExtras(bundle);
				startActivityForResult(mIntent, ACTIVITY_MESSAGE);
				qa.dismiss();
			}
		});

		qa.addItem(getResources().getDrawable(android.R.drawable.ic_menu_add),
				"Widget", new OnClickListener() {
					public void onClick(View v) {
						AlertDialog.Builder ad;
						ad = new AlertDialog.Builder(context);
						ad.setTitle(R.string.wid_confirm);
						ad
								.setPositiveButton(
										android.R.string.ok,
										new android.content.DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int arg1) {
												mDbHelper
														.deleteAllWidgetStops();
												mDbHelper.createWidgetStop(
														currentTrain, "1", "",
														fromto);
												for (TrainStop oneStop : mSaveTrainList
														.get(currentPos))
													mDbHelper
															.createWidgetStop(
																	oneStop
																			.getStation(),
																	oneStop
																			.getHour(),
																	oneStop
																			.getDelay()
																			+ " ",
																	oneStop
																			.getstatus());
												Intent intent = new Intent(
														TrainAppWidgetProvider.TRAIN_WIDGET_UPDATE);
												sendBroadcast(intent);
												Toast
														.makeText(
																InfoTrainActivity.this,
																getString(
																		R.string.wid_added,
																		currentTrain),
																Toast.LENGTH_SHORT)
														.show();

											}
										});

						ad
								.setNegativeButton(
										android.R.string.no,
										new android.content.DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int arg1) {

											}
										});
						ad.show();
						qa.dismiss();
					}
				});

		// shows the quick action window on the screen
		qa.show();

	}

	public void setNoTitle() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public void fillTrainStops(int pos) {
		try {
			trainInfoAdapter = new TrainInfoAdapter(this,
					R.layout.row_info_train, mSaveTrainList.get(pos));
			TextView moreInfo = (TextView) findViewById(R.id.More_info);

			moreInfo.setVisibility(View.INVISIBLE);
			setListAdapter(trainInfoAdapter);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void onResume() {
		super.onResume();
		mDbHelper.open();
		// fillTrainStops();
	}

	public void onPause() {
		super.onPause();
		mDbHelper.close();
	}

	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, 1, 0, getString(R.string.txt_add_fav)).setIcon(
				R.drawable.ic_menu_fav);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case 1:

			ConnectionMaker.addAsStarred(currentTrain, "", 2, context);
			return true;
		}

		return false;
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
			TrainStop Sname = trainInfoAdapter.getItem((int) menuInfo.id);
			String gare = removeAccents(Sname.getStation()).toUpperCase();

			Intent i = new Intent(InfoTrainActivity.this,
					InfoStationActivity.class);
			i.putExtra("gare_name", gare);
			int id = PlannerActivity.getStationNumber(gare);
			Log.d(TAG, gare + " - " + id);

			i.putExtra("gare_id", id);
			String[] split = Sname.getHour().split(":");
			i.putExtra("gare_heure", split[0]);
			i.putExtra("gare_minute", split[1]);
			startActivityForResult(i, ACTIVITY_STATION);
			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}

	private static final String PLAIN_ASCII = "AaEeIiOoUu" // grave
			+ "AaEeIiOoUuYy" // acute
			+ "AaEeIiOoUuYy" // circumflex
			+ "AaOoNn" // tilde
			+ "AaEeIiOoUuYy" // umlaut
			+ "Aa" // ring
			+ "Cc" // cedilla
			+ "OoUu" // double acute
	;

	private static final String UNICODE = "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9"
			+ "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD"
			+ "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177"
			+ "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1"
			+ "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF"
			+ "\u00C5\u00E5" + "\u00C7\u00E7" + "\u0150\u0151\u0170\u0171";

	/**
	 * remove accented from a string and replace with ascii equivalent
	 */
	public static String removeAccents(String s) {
		if (s == null)
			return null;
		StringBuilder sb = new StringBuilder(s.length());
		int n = s.length();
		int pos = -1;
		char c;
		boolean found = false;
		for (int i = 0; i < n; i++) {
			pos = -1;
			c = s.charAt(i);
			pos = (c <= 126) ? -1 : UNICODE.indexOf(c);
			if (pos > -1) {
				found = true;
				sb.append(PLAIN_ASCII.charAt(pos));
			} else {
				sb.append(c);
			}
		}
		if (!found) {
			return s;
		} else {
			return sb.toString();
		}
	}

	@Override
	public boolean onHandleActionBarItemClick(GDActionBarItem item, int position) {

		switch (position) {

		case 0:
			setQuickAction(getGDActionBar());
			return true;

		default:
			return super.onHandleActionBarItemClick(item,position);
		}
	}

	public void setTrainStopCursor(Cursor mTrainStopCursor) {
		this.mTrainStopCursor = mTrainStopCursor;
	}

	public void setLastMessageText(String string) {
		mMessageText.setText(string);
	}

	public void setEmptyText(String string) {
		mEmptyText.setText(string);
	}

	public void setLastMessageText(Spanned spanned) {
		mMessageText.setText(spanned);
	}

	public void setTitle() {
		mTitleText.setText(currentTrain);
	}

	public Cursor getTrainStopCursor() {
		return mTrainStopCursor;
	}

	public String getLang() {
		return lang;
	}

	public String getCurrentTrain() {
		return currentTrain;
	}

	public void addStops(ArrayList<TrainStop> arrayListTrainStop, int pos) {
		if (arrayListTrainStop != null)
			this.mSaveTrainList.add(pos, arrayListTrainStop);
		else
			Log.d(TAG, "*** addStops:NULL ***");
	}

	public void addMessage(Message message) {
		this.mSaveMessageList.add(message);
	}

	public static void setSettings(SharedPreferences settings) {
		InfoTrainActivity.settings = settings;
	}

	public static SharedPreferences getSettings() {
		return settings;
	}

	public void setEditor(SharedPreferences.Editor editor) {
		this.editor = editor;
	}

	public SharedPreferences.Editor getEditor() {
		return editor;
	}

}
