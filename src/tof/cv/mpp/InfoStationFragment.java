package tof.cv.mpp;

import tof.cv.mpp.Utils.UtilsWeb;
import tof.cv.mpp.Utils.UtilsWeb.Station;
import tof.cv.mpp.Utils.UtilsWeb.StationStationinfo;
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
import android.widget.Toast;

public class InfoStationFragment extends ListFragment {
	protected static final String TAG = "InfoStationFragment";
	private ProgressDialog progressDialog;
	private Station currentStation;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_info_station, null);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
	}
	
	public void displayInfo(String station){
//		Toast.makeText(getActivity(),"On affiche les infos de: "+station, Toast.LENGTH_LONG).show();
		progressDialog = ProgressDialog.show(getActivity(), "",
				getString(R.string.txt_patient), true);
		searchThread(station);
	}

	private void searchThread(final String station) {
		Runnable search = new Runnable() {
			public void run() {
				currentStation = UtilsWeb.getAPIstation(station, getActivity());
				getActivity().runOnUiThread(dismissProgressDialog);
				getActivity().runOnUiThread(displayResult);
			}
		};
		Thread thread = new Thread(null, search, "stationSearch");
		thread.start();
	}
	
	private Runnable dismissProgressDialog = new Runnable() {
		public void run() {
//			fillData();
			progressDialog.dismiss();
			Toast.makeText(getActivity(),"On affiche les infos", Toast.LENGTH_LONG).show();
		}
	};

	private Runnable displayResult = new Runnable() {
		public void run() {

			String txt = "";
			try {
//				for (VehicleStop aStop : currentVehicle.getVehicleStops()
//						.getVehicleStop()) {
//					txt += aStop.getStation() + " - " + aStop.getTime() + "\n";
//				}
				StationStationinfo stationinfo = currentStation.getStationStationinfo();
				txt += stationinfo.getId() + " - " + stationinfo.getLocationX() + " - " + stationinfo.getLocationY() + "\n";

			} catch (Exception e) {
				txt = getString(R.string.txt_error)+"\n\n"+e.toString();
				e.printStackTrace();
			}
			TextView tv = (TextView) getActivity().findViewById(
					android.R.id.empty);
			tv.setText(txt);
		}
	};

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
