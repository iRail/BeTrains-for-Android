package tof.cv.mpp;

import java.util.ArrayList;
import java.util.Date;

import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.DownloadLastMessageTask;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.Utils.UtilsWeb;
import tof.cv.mpp.Utils.UtilsWeb.Vehicle;
import tof.cv.mpp.Utils.UtilsWeb.VehicleStop;
import tof.cv.mpp.adapter.TrainInfoAdapter;
import tof.cv.mpp.bo.Message;
import tof.cv.widget.TrainAppWidgetProvider;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class InfoTrainFragment extends ListFragment {
	protected static final String TAG = "ChatFragment";
	private Vehicle currentVehicle;
	private TextView mTitleText;
	private TextView mMessageText;
	private String fromTo;
	private long timestamp;
	private ArrayList<Message> mSaveMessageList = new ArrayList<Message>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_info_train, null);
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
		mMessageText = (TextView) getActivity().findViewById(R.id.last_message);
		
		mMessageText.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putString(DbAdapterConnection.KEY_NAME,
						currentVehicle.getId());
				Intent mIntent = new Intent(v.getContext(),
						ChatActivity.class);
				mIntent.putExtras(bundle);
				startActivityForResult(mIntent, 0);

			}
		});
		
		Log.i("'",""+mMessageText);
		registerForContextMenu(getListView());
	}

	public void displayInfo(String vehicle, String fromTo,long timestamp) {
		this.fromTo = fromTo;
		if(timestamp!=0)
			this.timestamp=timestamp;
		else
			this.timestamp=System.currentTimeMillis();
		mMessageText = (TextView) getActivity().findViewById(R.id.last_message);
		if (PreferenceManager
				.getDefaultSharedPreferences(this.getActivity()).getBoolean("preffirstM", false)) {
			new DownloadLastMessageTask(this).execute(vehicle);
			setLastMessageText(getString(R.string.txt_load_message));
		} else
			mMessageText.setText(Html.fromHtml(
					"<small>"
					+ getText(R.string.txt_infrabel)
					+ "</small>"));

		myTrainSearchThread(vehicle,timestamp);
	}

	private void myTrainSearchThread(final String vehicle,final long timestamp) {
		Runnable trainSearch = new Runnable() {
			public void run() {
				currentVehicle = UtilsWeb.getAPIvehicle(vehicle, getActivity(),timestamp);
				if(getActivity()!=null)
					getActivity().runOnUiThread(displayResult);
			}
		};
		Thread thread = new Thread(null, trainSearch, "MyThread");
		thread.start();
	}


	private Runnable displayResult = new Runnable() {
		public void run() {

			if (currentVehicle != null
					&& currentVehicle.getVehicleStops() != null) {
				TrainInfoAdapter trainInfoAdapter = new TrainInfoAdapter(
						getActivity(), R.layout.row_info_train, currentVehicle
								.getVehicleStops().getVehicleStop());
				setListAdapter(trainInfoAdapter);
				setTitle(Utils
						.formatDate(
								new Date(timestamp),
								"dd MMM HH:mm"));
			} else {
				Toast.makeText(getActivity(), R.string.txt_connection, Toast.LENGTH_LONG).show();
				getActivity().finish();
			}
		}
	};

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(Menu.NONE, 0, Menu.NONE, "Widget")
				.setIcon(R.drawable.ic_menu_save)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add(Menu.NONE, 1, Menu.NONE, "Fav")
				.setIcon(R.drawable.ic_menu_star)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add(Menu.NONE, 2, Menu.NONE, "Map")
				.setIcon(R.drawable.ic_menu_mapmode)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (android.R.id.home):
			getActivity().finish();
			return true;
		case 0:
			widget();
			return true;
		case 1:
			if (currentVehicle != null) {
				Utils.addAsStarred(currentVehicle.getId(), "", 2, getActivity());
				startActivity(new Intent(getActivity(), StarredActivity.class));
			}
			return true;
		case 2:
			if (currentVehicle != null) {
				Intent i = new Intent(getActivity(), MapVehicleActivity.class);
				i.putExtra("Name", currentVehicle.getId());
				startActivity(i);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void widget() {

		AlertDialog.Builder ad;
		ad = new AlertDialog.Builder(getActivity());
		ad.setTitle(R.string.wid_confirm);
		ad.setPositiveButton(android.R.string.ok,
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int arg1) {
						final DbAdapterConnection mDbHelper = new DbAdapterConnection(
								getActivity());
						mDbHelper.open();
						mDbHelper.deleteAllWidgetStops();
						mDbHelper.createWidgetStop(currentVehicle.getId()
								.replace("BE.NMBS.", ""), "1", "", fromTo);
						for (VehicleStop oneStop : currentVehicle
								.getVehicleStops().getVehicleStop())
							mDbHelper.createWidgetStop(oneStop.getStation(),
									""+oneStop.getTime(), oneStop.getDelay(),
									oneStop.getStatus());
						Intent intent = new Intent(
								TrainAppWidgetProvider.TRAIN_WIDGET_UPDATE);
						getActivity().sendBroadcast(intent);
						mDbHelper.close();
						Toast.makeText(getActivity(),
								getString(R.string.wid_added, ""),
								Toast.LENGTH_SHORT).show();

					}
				});

		ad.setNegativeButton(android.R.string.no,
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int arg1) {

					}
				});
		if (currentVehicle != null)
			ad.show();

	}

	public void setTitle(String txt) {
		mTitleText.setText(txt);
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterView.AdapterContextMenuInfo info =
	            (AdapterView.AdapterContextMenuInfo) menuInfo;
		
		VehicleStop clicked = (VehicleStop) getListAdapter().getItem(
				(int) info.id);
		
		menu.add(0, 0, 0, clicked.getStation());
	}

	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();
			VehicleStop stop = (VehicleStop) getListAdapter().getItem(
					(int) menuInfo.id);
			Intent i = new Intent(getActivity(), InfoStationActivity.class);
			i.putExtra("Name", stop.getStation());
			i.putExtra("timestamp", stop.getTime());
			startActivity(i);

			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}
	
	public void setLastMessageText(Spanned spanned) {
		mMessageText.setText(spanned);
	}
	
	public void setLastMessageText(String text) {
		mMessageText.setText(text);
	}
	
	public void addMessage(Message message) {
		this.mSaveMessageList.add(message);
	}

}
