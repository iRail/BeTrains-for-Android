package tof.cv.mpp.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tof.cv.mpp.R;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.bo.Connection;
import tof.cv.mpp.bo.Station;
import tof.cv.mpp.bo.Via;

public class ViaAdapter extends BaseAdapter {

	private static final int TYPE_TRAIN = 0;
	private static final int TYPE_STATION = 1;
	private static final int TYPE_MAX_COUNT = 2;
	ArrayList<Via> items;
	Station arrival;

	Via via;
	ColorStateList c;
	private LayoutInflater mInflater;

	public ViaAdapter(Context context, int textViewResourceId,
			Connection _response, ColorStateList c) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.c = c;
		this.items = _response.getVias().via;
		arrival = _response.getArrival();

	}

	@Override
	public int getItemViewType(int position) {
		return (position % 2 == 1) ? TYPE_TRAIN : TYPE_STATION;
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_MAX_COUNT;
	}

	@Override
	public int getCount() {
		Log.i("", "SIZE" + (items.size() * 2));
		return items.size() * 2;
	}

	@Override
	public Via getItem(int position) {
		return items.get(position / 2);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
        int viaPosition=position / 2;

		Log.i("", "***" + viaPosition + "/" + via.getVehicle());
		

		if (convertView == null) {
			holder = new ViewHolder();
			switch (getItemViewType(position)) {
			case TYPE_TRAIN:
				convertView = mInflater.inflate(R.layout.row_via_train, null);
				holder.tvTrain = (TextView) convertView
						.findViewById(R.id.tv_train);
				holder.tvDuration = (TextView) convertView
						.findViewById(R.id.tv_duration);
				break;
			case TYPE_STATION:
				convertView = mInflater.inflate(R.layout.row_via_station, null);
				holder.tvArrivalPlatform = (TextView) convertView
						.findViewById(R.id.tv_arrival_platform);
				holder.tvDeparturePlatform = (TextView) convertView
						.findViewById(R.id.tv_departure_platform);
				holder.tvArrivalTime = (TextView) convertView
						.findViewById(R.id.tv_arrival_time);
				holder.tvDepartureTime = (TextView) convertView
						.findViewById(R.id.tv_departure_time);

				holder.tvStation = (TextView) convertView
						.findViewById(R.id.tv_station);
				holder.tvDuration = (TextView) convertView
						.findViewById(R.id.tv_duration);
				break;
			}
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (via != null) {
			switch (getItemViewType(position)) {
			case TYPE_TRAIN:

				holder.tvTrain.setText(Utils.getTrainId(via.getVehicle()));

				if (viaPosition+ 1 < items.size())
					holder.tvDuration.setText(Utils.formatDate(
							(Long.valueOf(items.get(viaPosition+1).getArrival()
									.getTime()) - Long.valueOf(via.getDeparture()
									.getTime())), true, false));
				else
					holder.tvDuration.setText(Utils.formatDate((Long
							.valueOf(arrival.getTime()) - Long.valueOf(via.getDeparture().getTime())),
							true, false));

				break;
			case TYPE_STATION:

				holder.tvArrivalPlatform
						.setText(via.getArrival().getPlatform());
				holder.tvDeparturePlatform.setText(via.getDeparture()
						.getPlatform());
				holder.tvArrivalTime.setText(Utils.formatDate(via.getArrival()
						.getTime(), false, false));
				holder.tvDepartureTime.setText(Utils.formatDate(via
						.getDeparture().getTime(), false, false));

				// TODO
				/*
				 * if (via.getArrival().getDelay().contentEquals("0"))
				 * tvDelay.setVisibility(View.GONE); else tvDelay.setText("+" +
				 * via.getDelay() + "'");
				 */
				holder.tvStation.setText(via.getName());

				holder.tvDuration.setText("("
						+ Utils.formatDate(via.getTimeBetween(), true, false)
						+ ")");

				holder.tvArrivalPlatform.setTextColor(c);
				holder.tvDeparturePlatform.setTextColor(c);

				break;
			}
			holder.tvDuration.setTextColor(c);

		}
		return convertView;
	}

	public static class ViewHolder {
		public TextView tvArrivalPlatform;
		public TextView tvDeparturePlatform;
		public TextView tvArrivalTime;
		public TextView tvDepartureTime;
		public TextView tvTrain;
		public TextView tvStation;
		public TextView tvDuration;

	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}
