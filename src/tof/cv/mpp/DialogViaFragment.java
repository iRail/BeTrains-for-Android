package tof.cv.mpp;

import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.adapter.ViaAdapter;
import tof.cv.mpp.bo.Connection;
import tof.cv.mpp.bo.Station;
import tof.cv.mpp.bo.Via;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DialogViaFragment extends DialogFragment {

	private static final String TAG = "BETRAINS";
	private ViaAdapter viaAdapter = null;
	private ListView listview;
	private View departureRow;
	private View arrivalRow;
	private Connection currentConnection;

	public DialogViaFragment() {
		// Empty constructor required for DialogFragment
	}

	public DialogViaFragment(Connection connection) {
		super();
		currentConnection = connection;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		this.setRetainInstance(true);
		if (currentConnection != null) {
			// this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			// Setup the dialog
			View finalView = inflater.inflate(
					R.layout.dialog_connection_detail, null, false);
			departureRow = (View) finalView.findViewById(R.id.view_departure);
			ColorStateList c=fillDetailRow(departureRow, currentConnection.getDeparture(), true);
			
			arrivalRow = (View) finalView.findViewById(R.id.view_arrival);
			fillDetailRow(arrivalRow, currentConnection.getArrival(), false);
			
			listview = (ListView) finalView.findViewById(R.id.listConnections);

			setOnListListener();
			setLlDepartureListener();
			setLlArrivalListener();

			viaAdapter = new ViaAdapter(getActivity(), R.layout.row_via,
					currentConnection.getVias().via,c);
			if (viaAdapter != null) {
				listview.setAdapter(viaAdapter);
			}

			getDialog().setTitle(
					currentConnection.getDeparture().getStation() + " - "
							+ currentConnection.getArrival().getStation());

			return finalView;
		}
		else return null;

	}

	public ColorStateList fillDetailRow(View row, Station station, Boolean isDeparture) {
		TextView tvStation = (TextView) row.findViewById(R.id.tv_station);
		tvStation.setText(station.getStation());

		TextView tvTrain = (TextView) row.findViewById(R.id.tv_train);
		if (isDeparture)
			tvTrain.setText("");

		else
			tvTrain.setText(Utils.getTrainId(station.getVehicle()));

		TextView tvPlatform = (TextView) row.findViewById(R.id.tv_platform);
		tvPlatform.setText(station.getPlatform());

		TextView tvTime = (TextView) row.findViewById(R.id.tv_time);
		tvTime.setText(Utils.formatDate(station.getTime(), false, false));

		TextView tvDelay = (TextView) row.findViewById(R.id.tv_delay);
		if (!station.getDelay().contentEquals("0")) {
			Log.i(TAG, "delay: " + Integer.valueOf(station.getDelay()));
			tvDelay.setText("+"
					+ (int) (Integer.valueOf(station.getDelay()) / 60) + "'");
		}else
			tvDelay.setText("");
		
		return tvPlatform.getTextColors();
	}

	private void setOnListListener() {
		listview.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View aView, int aInt,
					long aLong) {

				showViaDialog(aInt);

			}
		});
	}

	private void setLlDepartureListener() {
		departureRow.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				final CharSequence[] items = { getDeparture() };
				showStationDialog(currentConnection.getDeparture(), items);

			}
		});
	}

	private void setLlArrivalListener() {
		arrivalRow.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				final CharSequence[] items = {
						getArrival(),
						Utils.getTrainId(currentConnection.getArrival()
								.getVehicle()) };
				showStationDialog(currentConnection.getArrival(), items);
			}
		});
	}

	private void showViaDialog(final int position) {

		final String vehicle = Utils.getTrainId(currentConnection.getVias().via
				.get(position).getVehicle());
		final Via currentVia = currentConnection.getVias().via.get(position);
		final CharSequence[] items = { currentVia.getName(), vehicle };

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// builder.setTitle("title");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (item == 1) {
					startTrainInfoActivity(vehicle);
				}

				else if (item == 0) {
					startStationInfoActivity(currentVia.getName(), currentVia
							.getDeparture().getTime());
				}

			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void showStationDialog(final Station station, CharSequence[] items) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// builder.setTitle("title");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (item == 1)
					startTrainInfoActivity(Utils.getTrainId(station
							.getVehicle()));
				if (item == 0)
					startStationInfoActivity(station.getStation(),
							station.getTime());
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private String getDeparture() {
		return currentConnection.getDeparture().getStation();
	}

	private String getArrival() {
		return currentConnection.getArrival().getStation();
	}

	/*
	 * private String getDepartureVehicle() { return
	 * currentConnection.getDeparture().getVehicle(); }
	 * 
	 * private String getArrivalVehicle() { return
	 * currentConnection.getArrival().getVehicle(); }
	 */

	private void startStationInfoActivity(String station, String time) {
		Intent i = new Intent(getActivity(), InfoStationActivity.class);
		i.putExtra("Name", station);
		i.putExtra("Hour", Utils.getHourFromDate(time, false));
		i.putExtra("Minute", Utils.getMinutsFromDate(time, false));
		getActivity().startActivity(i);
	}

	private void startTrainInfoActivity(String vehicle) {
		Intent i = new Intent(getActivity(), InfoTrainActivity.class);
		i.putExtra("fromto", getDeparture() + " - " + getArrival());
		i.putExtra("Name", vehicle);
		getActivity().startActivity(i);
	}

}