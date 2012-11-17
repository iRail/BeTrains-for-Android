package tof.cv.mpp.adapter;

import java.util.ArrayList;

import tof.cv.mpp.R;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.bo.Via;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ViaAdapter extends AbstractAdapter<Via> {

	Via via;
	ColorStateList c;

	public ViaAdapter(Context context, int textViewResourceId,
			ArrayList<Via> items, ColorStateList c) {
		super(context, textViewResourceId, items);
		this.c = c;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) super.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row_via, null);
		}

		via = items.get(position);
		/*
		 * final String currentTrain = via.getVehicle();
		 * 
		 * v.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * Toast.makeText(getContext(),currentTrain, Toast.LENGTH_SHORT).show();
		 * }
		 * 
		 * });
		 */

		if (via != null) {

			TextView tvArrivalPlatform = (TextView) v
					.findViewById(R.id.tv_arrival_platform);
			tvArrivalPlatform.setText(via.getArrival().getPlatform());

			TextView tvDelay = (TextView) v.findViewById(R.id.tv_delay);

			tvDelay.setVisibility(View.GONE);
			/*
			 * if(via.getDelay().contentEquals("0"))
			 * tvDelay.setVisibility(View.GONE); else
			 * tvDelay.setText("+"+via.getDelay()+"'");
			 */

			TextView tvDeparturePlatform = (TextView) v
					.findViewById(R.id.tv_departure_platform);
			tvDeparturePlatform.setText(via.getDeparture().getPlatform());

			TextView tvArrivalTime = (TextView) v
					.findViewById(R.id.tv_arrival_time);
			tvArrivalTime.setText(Utils.formatDate(via.getArrival().getTime(),
					false, false));

			TextView tvDepartureTime = (TextView) v
					.findViewById(R.id.tv_departure_time);
			tvDepartureTime.setText(Utils.formatDate(via.getDeparture()
					.getTime(), false, false));

			TextView tvTrain = (TextView) v.findViewById(R.id.tv_train);
			tvTrain.setText(Utils.getTrainId(via.getVehicle()));

			TextView tvStation = (TextView) v.findViewById(R.id.tv_station);
			tvStation.setText(via.getName());

			TextView tvDuration = (TextView) v.findViewById(R.id.tv_duration);
			tvDuration.setText(Utils.formatDate(via.getTimeBetween(), true,
					false));
			
			tvArrivalPlatform.setTextColor(c);
			tvDeparturePlatform.setTextColor(c);
			tvDuration.setTextColor(c);

		}
		return v;
	}
}
