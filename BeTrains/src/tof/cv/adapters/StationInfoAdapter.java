package tof.cv.adapters;

import java.util.ArrayList;

import tof.cv.bo.Station;
import tof.cv.mpp.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StationInfoAdapter extends AbstractAdapter<Station> {

	public StationInfoAdapter(Context context, int textViewResourceId,
			ArrayList<Station> items) {
		super(context, textViewResourceId, items);		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) super.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row_info_station, null);
		}
		Station trainstop = items.get(position);
		if (trainstop != null) {
			TextView t1 = (TextView) v.findViewById(R.id.tv_platform);
			TextView t2 = (TextView) v.findViewById(R.id.tv_time);
			TextView t3 = (TextView) v.findViewById(R.id.tv_delay);
			TextView t4 = (TextView) v.findViewById(R.id.tv_station);
			TextView t5 = (TextView) v.findViewById(R.id.tv_train);

			t4.setText(trainstop.getStation());
			t2.setText(trainstop.getTime());
			t3.setText(trainstop.getDelayValue());
			t1.setText(trainstop.getPlatform());
			t5.setText(trainstop.getVehicle());

		}
		return v;
	}
}
