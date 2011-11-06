package tof.cv.mpp.adapter;

import java.util.ArrayList;
import java.util.List;

import tof.cv.mpp.R;
import tof.cv.mpp.Utils.ConnectionMaker;
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
			TextView departure = (TextView) v.findViewById(R.id.text1);
			TextView arrival = (TextView) v.findViewById(R.id.text2);
			TextView triptime = (TextView) v.findViewById(R.id.text3);
			TextView departtime = (TextView) v.findViewById(R.id.text4);
			TextView arrivaltime = (TextView) v.findViewById(R.id.text5);
			TextView quai1 = (TextView) v.findViewById(R.id.quai1);
			TextView quai2 = (TextView) v.findViewById(R.id.quai2);
			TextView numberoftrains = (TextView) v
					.findViewById(R.id.numberoftrains);

			if (delayD != null) {

				String delayStr = "+"
						+ (Integer.valueOf(conn.getDeparture().getDelay()) / 60) + "'";
				if (!conn.getDeparture().getDelay().contentEquals("0"))
					delayD.setText(delayStr);
				else
					// delayD.setText(delayStr);
					delayD.setVisibility(View.GONE);
			}

			if (delayA != null) {

				String delayStr = "+"
						+ (Integer.valueOf(conn.getArrival().getDelay()) / 60) + "'";
				if (conn.getArrival().getDelay().contentEquals("0"))
					delayA.setText(delayStr);
				else
					// delayA.setText(delayStr);
					delayA.setVisibility(View.GONE);
			}

			if (departure != null) {
				departure.setText(conn.getDeparture().getStation());
			}
			if (arrival != null) {
				arrival.setText(conn.getArrival().getStation());
			}

			if (quai1 != null) {
				quai1.setText(conn.getDeparture().getPlatform());
			}
			if (quai2 != null) {
				quai2.setText(conn.getArrival().getPlatform());
			}

			if (triptime != null) {
				// System.out.println("triptime : " + conn.getTripTime());
				triptime.setText(ConnectionMaker.formatDate(conn.getDuration(),
						true, false));
			}
			if (departtime != null) {
				departtime.setText(ConnectionMaker.formatDate(conn
						.getDeparture().getTime(), false, false));
			}
			if (arrivaltime != null) {
				arrivaltime.setText(ConnectionMaker.formatDate(conn
						.getArrival().getTime(), false, false));
			}
			
			//TODO: Vias
			/*if (numberoftrains != null) {
				// Log.i("BETRAINS","number"+conn.getTrains());
				if (conn.getVias().size() > 1)
					numberoftrains.setText(Html.fromHtml("Trains: <b>"
							+ conn.getVias().size() + "</b>"));
				else
					numberoftrains.setText(Html.fromHtml(ConnectionMaker
							.getTrainId(conn.getVias().get(0).getVehicle())));
			}*/

			int color1 = 0x00101010;
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
