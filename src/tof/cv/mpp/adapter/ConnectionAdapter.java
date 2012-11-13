package tof.cv.mpp.adapter;

import java.util.ArrayList;
import java.util.List;

import tof.cv.mpp.R;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.bo.Connection;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ConnectionAdapter extends AbstractAdapter<Connection> {

	public ConnectionAdapter(Context context, int textViewResourceId,
			List<Connection> items) {
		super(context, textViewResourceId, (ArrayList<Connection>) items);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) super.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row_planner, null);
		}
		Connection conn = items.get(position);

		if (conn != null) {
			TextView delayD = (TextView) v.findViewById(R.id.delayD);
			TextView delayA = (TextView) v.findViewById(R.id.delayA);
			TextView departure = (TextView) v.findViewById(R.id.departure);
			TextView arrival = (TextView) v.findViewById(R.id.arrival);
			TextView triptime = (TextView) v.findViewById(R.id.duration);
			TextView departtime = (TextView) v.findViewById(R.id.departtime);
			TextView arrivaltime = (TextView) v.findViewById(R.id.arrivaltime);
			TextView numberoftrains = (TextView) v
					.findViewById(R.id.numberoftrains);

			String delayStr = " +"
					+ (Integer.valueOf(conn.getDeparture().getDelay()) / 60)
					+ "'";
			if (!conn.getDeparture().getDelay().contentEquals("0"))
				delayD.setText(delayStr);
			else
				delayD.setText("");

			delayStr = " +"
					+ (Integer.valueOf(conn.getArrival().getDelay()) / 60)
					+ "'";
			if (!conn.getArrival().getDelay().contentEquals("0"))
				delayA.setText(delayStr);
			else
				delayA.setText("");

			if (departure != null) {

				departure
						.setText((conn.getDeparture().getPlatform()
										.contentEquals("") ? "" : getContext().getString(R.string.txt_quai)+ " "+conn
										.getDeparture().getPlatform()) );
			}
			if (arrival != null) {
				arrival.setText((conn.getArrival().getPlatform().contentEquals("") ? ""
								: getContext().getString(R.string.txt_quai)+" "+conn.getArrival().getPlatform()));
			}

			if (triptime != null) {
				triptime.setText(Html.fromHtml(getContext().getString(
						R.string.txt_duration)
						+ " <b>"
						+ Utils.formatDate(conn.getDuration(), true, false)
						+ "</b>"));
			}
			if (departtime != null) {
				departtime.setText(Utils.formatDate(conn.getDeparture()
						.getTime(), false, false));
			}
			if (arrivaltime != null) {
				arrivaltime.setText(Utils.formatDate(conn.getArrival()
						.getTime(), false, false));
			}

			if (numberoftrains != null) { //
				// Log.i("BETRAINS", "number" + conn.getVias()));
				if (conn.getVias() != null)
					numberoftrains.setText(Html.fromHtml("Trains: <b>"
							+ (conn.getVias().getNumberOfVias() + 1) + "</b>"));
				else
					numberoftrains.setText(Html.fromHtml(Utils.getTrainId(conn
							.getDeparture().getVehicle())));
			}

			int color1 = 0xffffffff;
			int color2 = 0xfff5f5f5;

			if (position % 2 == 0) {
				v.setBackgroundColor(color1);

			} else {
				v.setBackgroundColor(color2);

			}

		}
		return v;
	}
}
