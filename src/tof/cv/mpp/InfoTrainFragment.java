package tof.cv.mpp;

import java.util.Date;

import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.Utils.UtilsWeb;
import tof.cv.mpp.Utils.UtilsWeb.Vehicle;
import tof.cv.mpp.Utils.UtilsWeb.VehicleStop;
import tof.cv.mpp.adapter.TrainInfoAdapter;
import tof.cv.widget.TrainAppWidgetProvider;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class InfoTrainFragment extends ListFragment {
	protected static final String TAG = "ChatFragment";
	private ProgressDialog progressDialog;
	private Vehicle currentVehicle;
	private TextView mTitleText;
	private String fromTo;

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

	}

	public void displayInfo(String vehicle, String fromTo) {
		progressDialog = ProgressDialog.show(getActivity(), "",
				getString(R.string.txt_patient), true);
		this.fromTo=fromTo;
		myTrainSearchThread(vehicle);
	}

	private void myTrainSearchThread(final String vehicle) {
		Runnable trainSearch = new Runnable() {
			public void run() {
				currentVehicle = UtilsWeb.getAPIvehicle(vehicle, getActivity());
				getActivity().runOnUiThread(dismissPd);
				getActivity().runOnUiThread(displayResult);
			}
		};
		Thread thread = new Thread(null, trainSearch, "MyThread");
		thread.start();
	}

	private Runnable dismissPd = new Runnable() {
		public void run() {
			progressDialog.dismiss();
		}
	};

	private Runnable displayResult = new Runnable() {
		public void run() {

			if (currentVehicle != null && currentVehicle.getVehicleStops() != null) {
				TrainInfoAdapter trainInfoAdapter = new TrainInfoAdapter(
						getActivity(), R.layout.row_info_train, currentVehicle
								.getVehicleStops().getVehicleStop());
				setListAdapter(trainInfoAdapter);
				setTitle(Utils.formatDate(new Date(Long.valueOf(currentVehicle.getTimestamp())*1000), "dd MMM HH:mm"));

			} else {
				TextView messagesEmpty = (TextView) getActivity().findViewById(
						android.R.id.empty);
				messagesEmpty.setText(getString(R.string.txt_connection));
				setTitle(Utils.formatDate(new Date(), "dd MMM HH:mm"));
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
			widget();
			return true;
		case 1:
			Utils.addAsStarred(currentVehicle.getId(), "", 2, getActivity());
			startActivity(new Intent(getActivity(), StarredActivity.class));
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
						mDbHelper.createWidgetStop(currentVehicle.getId().replace("BE.NMBS.", ""), "1", "",
								fromTo);
						for (VehicleStop oneStop : currentVehicle
								.getVehicleStops().getVehicleStop())
							mDbHelper.createWidgetStop(oneStop.getStation(),
									oneStop.getTime(),
									oneStop.getDelay(), oneStop.getStatus());
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

}
