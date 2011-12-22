package tof.cv.mpp;

import tof.cv.mpp.Utils.UtilsWeb;
import tof.cv.mpp.Utils.UtilsWeb.Vehicle;
import tof.cv.mpp.Utils.UtilsWeb.VehicleStop;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class InfoTrainFragment extends ListFragment {
	protected static final String TAG = "ChatFragment";
	private ProgressDialog progressDialog;
	private Vehicle currentVehicle;

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

	public void displayInfo(String vehicle) {
		progressDialog = ProgressDialog.show(getActivity(), "",
				getString(R.string.txt_patient), true);
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
			fillData();
			progressDialog.dismiss();
		}
	};

	private Runnable displayResult = new Runnable() {
		public void run() {

			String txt = "";
			try {
				for (VehicleStop aStop : currentVehicle.getVehicleStops()
						.getVehicleStop()) {
					txt += aStop.getStation() + " - " + aStop.getTime() + "\n";
				}

			} catch (Exception e) {
				txt = getString(R.string.txt_error)+"\n\n"+e.toString();
				e.printStackTrace();
			}
			TextView tv = (TextView) getActivity().findViewById(
					android.R.id.empty);
			tv.setText(txt);
		}
	};

	private void fillData() {
		/*
		 * if (allConnections != null && allConnections.connection != null) {
		 * Log.i(TAG, "*** Remplis avec les infos"); connAdapter = new
		 * ConnectionAdapter(this.getActivity() .getBaseContext(),
		 * R.layout.row_planner, allConnections.connection);
		 * setListAdapter(connAdapter); registerForContextMenu(getListView());
		 * 
		 * }
		 */
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
