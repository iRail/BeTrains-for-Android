package tof.cv.mpp.adapter;

import java.util.ArrayList;

import tof.cv.mpp.R;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.Utils.UtilsWeb.VehicleStop;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TrainInfoAdapter extends ArrayAdapter<VehicleStop> {

	public TrainInfoAdapter(Context context, int textViewResourceId,
			ArrayList<VehicleStop> items) {
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
		VehicleStop o = getItem(position);
		if (o != null) {
			TextView t2 = (TextView) v.findViewById(R.id.arret2);
			TextView t3 = (TextView) v.findViewById(R.id.arret3);
			TextView t4 = (TextView) v.findViewById(R.id.arret4);

			t4.setText(Html.fromHtml(o.getStation()));
			t2.setText(Utils.formatDate(o.getTime(), false, false));
			
			if (o.getDelay().contentEquals("0"))
				t3.setVisibility(View.GONE);
			else
				t3.setText("+"+(Integer.valueOf(o.getDelay())/60)+"'");

		}
		return v;
	}
}
