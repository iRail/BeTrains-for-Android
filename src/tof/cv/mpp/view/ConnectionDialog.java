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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tof.cv.mpp.view;

import tof.cv.mpp.InfoStationActivity;
import tof.cv.mpp.InfoTrainActivity;
import tof.cv.mpp.R;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.adapter.ViaAdapter;
import tof.cv.mpp.bo.Connection;
import tof.cv.mpp.bo.Station;
import tof.cv.mpp.bo.Via;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * A dialog that can edit a shortcut intent. For now the icon is displayed, and
 * only the name may be edited. This dialog is created when a user clicks on a
 * connection found in the list of the search results
 */
public class ConnectionDialog extends Dialog implements OnClickListener {

	private static final String TAG = "BETRAINS";
	private ViaAdapter stationAdapter = null;
	private ListView listview;
	private View departureRow;
	private View arrivalRow;
	private Connection currentConnection;

	public ConnectionDialog(Activity plannerActivity, Connection connection) {
		super(plannerActivity);

		currentConnection = connection;
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Setup the dialog
		View finalView = getLayoutInflater().inflate(
				R.layout.dialog_connection_detail, null, false);
		departureRow = (View) finalView.findViewById(R.id.view_departure);
		fillDetailRow(departureRow, connection.getDeparture(), true);
		arrivalRow = (View) finalView.findViewById(R.id.view_arrival);
		fillDetailRow(arrivalRow, connection.getArrival(), false);
		listview = (ListView) finalView.findViewById(R.id.listConnections);

		// setTitle(connection.getDeparture().getStation() + " - "
		// + connection.getArrival().getStation());

		setOnListListener();
		setLlDepartureListener();
		setLlArrivalListener();

		stationAdapter = new ViaAdapter(plannerActivity, R.layout.row_via,
				connection.getVias().via);
		if (stationAdapter != null) {
			listview.setAdapter(stationAdapter);
		}

		setContentView(finalView);

	}

	public void onClick(DialogInterface dialog, int which) {
	}

	public Bundle onSaveInstanceState() {
		Bundle state = super.onSaveInstanceState();
		return state;
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// Do nothing
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// Do nothing
	}

	public void displayStation(Station station) {
		// Do nothing
	}

	public void fillDetailRow(View row, Station station, Boolean isDeparture) {
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
		}

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

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
		Intent i = new Intent(getContext(), InfoStationActivity.class);
		i.putExtra("Name", station);
		i.putExtra("Hour", Utils.getHourFromDate(time, false));
		i.putExtra("Minute", Utils.getMinutsFromDate(time, false));
		getContext().startActivity(i);
	}

	private void startTrainInfoActivity(String vehicle) {
		Intent i = new Intent(getContext(), InfoTrainActivity.class);
		i.putExtra("fromto", getDeparture() + " - " + getArrival());
		i.putExtra("Name", vehicle);
		getContext().startActivity(i);
	}

}
