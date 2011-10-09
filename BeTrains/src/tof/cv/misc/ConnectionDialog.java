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

package tof.cv.misc;

import tof.cv.adapters.ViaAdapter;
import tof.cv.bo.Connection;
import tof.cv.bo.Station;
import tof.cv.bo.Via;
import tof.cv.mpp.R;
import tof.cv.ui.InfoStationActivity;
import tof.cv.ui.InfoTrainActivity;
import tof.cv.ui.PlannerActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


/**
 * A dialog that can edit a shortcut intent. For now the icon is displayed, and
 * only the name may be edited. 
 * This dialog is created when a user clicks on a connection found in the list of the search results
 */
public class ConnectionDialog extends Dialog implements OnClickListener {

	private static final String TAG = "BETRAINS";
	private ViaAdapter stationAdapter = null;
	private ListView listview;
	private View departureRow;
	private View arrivalRow;
	private Connection currentConnection;

	public ConnectionDialog(PlannerActivity plannerActivity,
			Connection connection) {
		super(plannerActivity);
		
		currentConnection = connection;

		// Setup the dialog
		View finalView = getLayoutInflater().inflate(
				R.layout.dialog_connection_detail, null, false);
		departureRow = (View) finalView.findViewById(R.id.view_departure);
		fillDetailRow(departureRow, connection.getDepartureStation());
		arrivalRow = (View) finalView.findViewById(R.id.view_arrival);
		fillDetailRow(arrivalRow, connection.getArrivalStation());
		listview = (ListView) finalView.findViewById(R.id.listConnections);

		setTitle(connection.getDepartureStation().getStation() + " - "
				+ connection.getArrivalStation().getStation());

		setOnListListener();
		setLlDepartureListener();
		setLlArrivalListener();

		stationAdapter = new ViaAdapter(plannerActivity, R.layout.row_via,
				connection.getVias());
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

	public void fillDetailRow(View row, Station station) {
		TextView tvStation = (TextView) row.findViewById(R.id.tv_station);
		tvStation.setText(station.getStation());

		TextView tvTrain = (TextView) row.findViewById(R.id.tv_train);
		tvTrain.setText(ConnectionMaker.getTrainId(station.getVehicle()));

		TextView tvPlatform = (TextView) row.findViewById(R.id.tv_platform);
		tvPlatform.setText(station.getPlatform());

		TextView tvTime = (TextView) row.findViewById(R.id.tv_time);
		tvTime.setText(ConnectionMaker.formatDate(station.getTime(), false,false));

		TextView tvDelay = (TextView) row.findViewById(R.id.tv_delay);
		if(!station.getDelayValue().contentEquals("0")){
			Log.i(TAG,"delay: "+Integer.valueOf(station.getDelayValue()));
			tvDelay.setText("+"+(int)(Integer.valueOf(station.getDelayValue())/60)+"'");
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
				final CharSequence[] items = { getDepartureStation() };
				showStationDialog(currentConnection.getDepartureStation(),
						items);

			}
		});
	}

	private void setLlArrivalListener() {
		arrivalRow.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				final CharSequence[] items = {
						getArrivalStation(),
						ConnectionMaker.getTrainId(currentConnection
								.getArrivalStation().getVehicle()) };
				showStationDialog(currentConnection.getArrivalStation(), items);
			}
		});
	}

	private void showViaDialog(final int position) {

		final Via currentVia=currentConnection.getVias().get(position);
		final String vehicle=ConnectionMaker.getTrainId(currentConnection.getVias().get(position).getVehicle());
		final CharSequence[] items = {
				currentVia.getStationName(),
				vehicle };
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		// builder.setTitle("title");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (item == 1){

					startTrainInfoActivity(vehicle);
					
				}

				else if (item == 0){
					startStationInfoActivity(currentVia.getStationName(),currentVia.getDepartureTime());
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
					startTrainInfoActivity(ConnectionMaker.getTrainId(station.getVehicle()));
				if (item == 0)
					startStationInfoActivity(station.getStation(),station.getTime());
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private String getDepartureStation() {
		return currentConnection.getDepartureStation().getStation();
	}
	
	private String getArrivalStation() {
		return currentConnection.getArrivalStation().getStation();
	}
	
	private String getDepartureVehicle() {
		return currentConnection.getDepartureStation().getVehicle();
	}
	
	private String getArrivalVehicle() {
		return currentConnection.getArrivalStation().getVehicle();
	}
	
	private void startStationInfoActivity(String station,String time ) {
		Intent i = new Intent(getContext(),
				InfoStationActivity.class);
		i.putExtra("gare_name", station);
		int id = PlannerActivity.getStationNumber(station);
		Log.d(TAG, station + " - " + id);
		i.putExtra("gare_id", id);
		i.putExtra("gare_heure", ConnectionMaker.getHourFromDate(time,false));
		i.putExtra("gare_minute",ConnectionMaker.getMinutsFromDate(time,false));
		getContext().startActivity(i);
	}
	
	private void startTrainInfoActivity(String vehicle) {
		Intent i = new Intent(getContext(),
				InfoTrainActivity.class);
		i.putExtra("fromto",getDepartureStation()+" - "+getArrivalStation());
		try{
			
			i.putExtra(ConnectionDbAdapter.KEY_TRAINS,vehicle );
			getContext().startActivity(i);	
		}catch(Exception e){
			e.printStackTrace();
			Toast.makeText(getContext(), "Error",
					Toast.LENGTH_SHORT).show();
		}
	}

}
