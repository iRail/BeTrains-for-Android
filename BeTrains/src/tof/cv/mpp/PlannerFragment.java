package tof.cv.mpp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import tof.cv.mpp.Utils.ConnectionMaker;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.adapter.ConnectionAdapter;
import tof.cv.mpp.bo.Connections;
import tof.cv.mpp.view.DateTimePicker;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.SupportActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.Window;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
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

	// First part is cleaned

	boolean isDebug = false;

	private static final int MENU_DT = 0;
	private static final int MENU_FAV = 1;

	Date mDate;

	public static String datePattern = "EEE dd MMM HH:mm";

	int positionClicked;

	private static Connections allConnections = new Connections();

	private TextView tvDeparture;
	private TextView tvArrival;

	private ConnectionAdapter connAdapter;

	private String TAG = "BETRAINS";
	private SupportActivity context;

	// Second part need to be cleaned

	private static final int ACTIVITY_DISPLAY = 0;
	private static final int ACTIVITY_STOP = 1;
	private static final int ACTIVITY_STATION = 2;
	private static final int ACTIVITY_GETSTARTSTATION = 3;
	private static final int ACTIVITY_GETSTOPSTATION = 4;
	private static final int ACTIVITY_INFO = 6;

	private static final int CONNECTION_DIALOG_ID = 0;

	private static SharedPreferences settings;
	private SharedPreferences.Editor editor;

	private ProgressDialog progressDialog;

	public void onStart() {
		super.onStart();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if (settings.getBoolean("preffullscreen", false))
			ConnectionMaker.setFullscreen(getActivity());
		return inflater.inflate(R.layout.fragment_planner, null);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
		editor = settings.edit();
		context = this.getSupportActivity();
		mDate = new Date();

		getSupportActivity().getSupportActionBar().setTitle(
				Utils.formatDate(mDate, datePattern));
		setHasOptionsMenu(true);

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


	}

	public void fillStations(String departure, String arrival) {
		tvDeparture.setText(departure);
		tvArrival.setText(arrival);

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

				// TODO

			}
		});
	}

	private void setBtnInfoArrivalListener() {

		Button btnInfoArrival = (Button) getActivity().findViewById(
				R.id.btn_info_arrival);
		btnInfoArrival.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				String station = tvArrival.getText().toString();
				// Intent i = new Intent(getActivity(),
				// InfoStationActivity.class);
				// i.putExtra("gare_name", station);
				// i.putExtra("gare_id", getStationNumber(station));
				// i.putExtra("gare_heure", mHour);
				// i.putExtra("gare_minute", mMinute);
				// startActivityForResult(i, ACTIVITY_STATION);

			}
		});

	}

	private void setTvArrivalListener() {

		tvArrival = (TextView) getActivity().findViewById(R.id.tv_stop);
		tvArrival.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(getActivity(),
						StationPickerActivity.class);
				startActivityForResult(i, ACTIVITY_GETSTOPSTATION);
			}
		});

	}

	private void setTvDepartureListener() {
		tvDeparture = (TextView) getView().findViewById(R.id.tv_start);
		tvDeparture.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(getActivity(),
						StationPickerActivity.class);
				startActivityForResult(i, ACTIVITY_GETSTARTSTATION);
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

		// TODO: Add settings
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

	private void fillData() {
		if (allConnections != null && allConnections.connection != null) {
			Log.i(TAG, "*** Remplis avec les infos");
			connAdapter = new ConnectionAdapter(this.getActivity()
					.getBaseContext(), R.layout.row_planner,
					allConnections.connection);
			setListAdapter(connAdapter);	
			registerForContextMenu(getListView());
			
		} else{
			allConnections = ConnectionMaker.getCachedConnections();
			connAdapter = new ConnectionAdapter(this.getActivity()
					.getBaseContext(), R.layout.row_planner,
					allConnections.connection);
			setListAdapter(connAdapter);
			registerForContextMenu(getListView());
		}

		if (allConnections == null) {
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
				// Log.i("BETRAINS", allConnections.size() + " - "
				// + positionClicked);
				e.printStackTrace();
			}

		}
		Log.i("BETRAINS", "dialog null");
		return null;

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

			// ConnectionOld currentConnection =
			// allConnections.get(positionClicked);
			// Log.v(TAG,"size: "+currentConnection.getVias().size());
			// if (currentConnection.getVias().size() > 0) {
			// getActivity().showDialog(CONNECTION_DIALOG_ID);
			// } else
			// noDataClick(positionClicked);
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

		// allConnections = new Connections();

		allConnections = ConnectionMaker.getAPIConnections(
				"" + (mDate.getYear() - 100), "" + (mDate.getMonth() + 1), ""
						+ mDate.getDate(), "" + mDate.getHours(),
				"" + mDate.getMinutes(), langue, myStart, myArrival, dA,
				trainOnly, getActivity());

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

	protected void onPrepareDialog(int dialogId, Dialog dialog) {
		switch (dialogId) {
		case CONNECTION_DIALOG_ID: {

		}
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

	@Override
	public void onCancel(DialogInterface arg0) {
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
	}

}
