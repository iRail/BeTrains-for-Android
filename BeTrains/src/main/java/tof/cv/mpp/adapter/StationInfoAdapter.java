package tof.cv.mpp.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tof.cv.mpp.R;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.Utils.UtilsWeb.StationDeparture;

public class StationInfoAdapter extends ArrayAdapter<StationDeparture> {

	public StationInfoAdapter(Context context, int textViewResourceId,
			ArrayList<StationDeparture> items) {
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
		StationDeparture trainstop = getItem(position);
		if (trainstop != null) {
			TextView platform = (TextView) v.findViewById(R.id.tv_platform);
			TextView time = (TextView) v.findViewById(R.id.tv_time);
			TextView delay = (TextView) v.findViewById(R.id.tv_delay);
			TextView station = (TextView) v.findViewById(R.id.tv_station);
			TextView train = (TextView) v.findViewById(R.id.tv_train);

			station.setText(Html.fromHtml(trainstop.getStation().replace(" [NMBS/SNCB]","")));
			time.setText(Utils.formatDate(trainstop.getTime(), false, false));
			if (trainstop.getDelay().contentEquals("0"))
				delay.setText("");
			else try{
				delay.setText("+" + (Integer.valueOf(trainstop.getDelay()) / 60) + "'");
			}catch (Exception e){
				delay.setText(trainstop.getDelay());
			}

			platform.setText(trainstop.getPlatform());
			train.setText(Utils.getTrainId(trainstop.getVehicle()));

		}
		return v;
	}
}
