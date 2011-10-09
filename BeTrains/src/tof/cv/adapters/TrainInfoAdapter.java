package tof.cv.adapters;

import java.util.ArrayList;

import tof.cv.bo.TrainStop;
import tof.cv.mpp.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TrainInfoAdapter extends AbstractAdapter<TrainStop> {

	public TrainInfoAdapter(Context context, int textViewResourceId,
			ArrayList<TrainStop> items) {
		super(context, textViewResourceId, items);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) super.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row_info_train, null);
		}
		TrainStop o = items.get(position);
		if (o != null) {
			TextView t2 = (TextView) v.findViewById(R.id.arret2);
			TextView t3 = (TextView) v.findViewById(R.id.arret3);
			TextView t4 = (TextView) v.findViewById(R.id.arret4);

			t4.setText(o.getStation());
			t2.setText(o.getHour());
			if (o.getDelay().contentEquals("0"))
				t3.setVisibility(View.GONE);
			else
				t3.setText(o.getDelay());

		}
		return v;
	}
}
