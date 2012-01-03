package tof.cv.mpp;

import java.util.Date;

import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.Utils.UtilsWeb;
import tof.cv.mpp.Utils.UtilsWeb.Vehicle;
import tof.cv.mpp.adapter.TrainInfoAdapter;
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
	private TextView mTitleText;
	
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
			
			if(currentVehicle!=null){
				TrainInfoAdapter trainInfoAdapter = new TrainInfoAdapter(getActivity(),
						R.layout.row_info_train, currentVehicle.getVehicleStops().getVehicleStop());	
				setListAdapter(trainInfoAdapter);
				
			}
			else{
				TextView messagesEmpty = (TextView) getActivity().findViewById(
						android.R.id.empty);
				messagesEmpty.setText(getString(R.string.txt_connection));
				setTitle(Utils.formatDate(new Date(), "dd MMM HH:mm"));
			}	
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
	
	public void setTitle(String txt) {
		mTitleText.setText(txt);
	}

}
