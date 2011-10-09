package tof.cv.adapters;

import java.util.ArrayList;

import tof.cv.bo.Connection;
import tof.cv.bo.Station;
import tof.cv.bo.Via;
import tof.cv.misc.ConnectionDbAdapter;
import tof.cv.misc.ConnectionMaker;
import tof.cv.mpp.R;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.devoteam.quickaction.QuickActionWindow;

public class ConnectionAdapter extends AbstractAdapter<Connection> {

	private Cursor connCursor;

	public ConnectionAdapter(Context context, int textViewResourceId,
			ArrayList<Connection> items, Cursor connCursor) {
		super(context, textViewResourceId, items);
		this.connCursor = connCursor;
		parseConnectionsFromDb();
	}

	private void parseConnectionsFromDb() {
		/*
		 * here we extract the connection data from the database using the
		 * cursor to each row ( row = 1 connection ) and put it in a collection,
		 * which is already given in the constructor
		 */
		
		for (int i = 0; i < connCursor.getCount(); i++) {
			
			connCursor.moveToPosition(i);
			
			String departure = connCursor.getString(connCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_DEPARTURE));
			String arrival = connCursor.getString(connCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_ARRIVAL));
			String departtime = connCursor.getString(connCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_DEPARTTIME));
			String arrivaltime = connCursor.getString(connCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_ARRIVALTIME));
			String triptime = connCursor.getString(connCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_TRIPTIME));
			String delayDStr = connCursor.getString(connCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_DELAY_DEPARTURE));
			String delayAStr = connCursor.getString(connCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_DELAY_ARRIVAL));
			String platAStr = connCursor.getString(connCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_ARRIVAL_PLATFORM));
			String platDStr = connCursor.getString(connCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_DEPARTURE_PLATFORM));

			ArrayList <Via> viaList=new ArrayList <Via>();
			
			String trainsConcatenated = connCursor.getString(connCursor
					.getColumnIndex(ConnectionDbAdapter.KEY_TRAINS));
			String[] trainsParsed = trainsConcatenated.split(";");
			for (int j = 0; j < trainsParsed.length; j++) {
				viaList.add(new Via("", "", "", "", "", "", "", trainsParsed[j], "",""));
			}

			items.add(new Connection(
					new Station("", "("+platDStr+")", false, departtime, departure, "0000 0000", delayDStr, ""),
					viaList,
					new Station("", "("+platAStr+")", false, arrivaltime, arrival, "0000 0000", delayAStr, ""),
					triptime,
					delayDStr,
					delayAStr));

		}
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
				
				String delayStr="+"+(Integer.valueOf(conn.getDDelay())/60)+"'";
				if(conn.isDDelay())
					delayD.setText(delayStr);
				else
					//delayD.setText(delayStr);
					delayD.setVisibility(View.GONE);
			}
			
			if (delayA != null) {
				
				String delayStr="+"+(Integer.valueOf(conn.getADelay())/60)+"'";
				if(conn.isADelay())
					delayA.setText(delayStr);
				else
					//delayA.setText(delayStr);
					delayA.setVisibility(View.GONE);
			}
			
			if (departure != null) {
				departure.setText(conn.getDepartureStation().getStation());
			}
			if (arrival != null) {
				arrival.setText(conn.getArrivalStation().getStation());
			}
			
			if (quai1 != null) {
				quai1.setText(conn.getDepartureStation().getPlatform());
			}
			if (quai2 != null) {
				quai2.setText(conn.getArrivalStation().getPlatform());
			}
			
			if (triptime != null) {
				//System.out.println("triptime : " + conn.getTripTime());
				triptime.setText(ConnectionMaker.formatDate(conn.getDuration(),true,false));
			}
			if (departtime != null) {
				departtime.setText(ConnectionMaker.formatDate(conn.getDepartureStation().getTime(),false,false));
			}
			if (arrivaltime != null) {
				arrivaltime.setText(ConnectionMaker.formatDate(conn.getArrivalStation().getTime(),false,false));
			}
			if (numberoftrains != null) {	
				//Log.i("BETRAINS","number"+conn.getTrains());
				if(conn.getVias().size()>1)
					numberoftrains.setText(Html.fromHtml("Trains: <b>"+conn.getVias().size() +"</b>"));
				else
					numberoftrains.setText(Html.fromHtml(ConnectionMaker.getTrainId(conn.getVias().get(0).getVehicle())));
			}
			
			int color1=0x00101010;
			int color2=0xfff5f5f5;
			
			if (position % 2 == 0) {
				v.setBackgroundColor(color1);

			} else {
				v.setBackgroundColor(color2);

			}
		}
		return v;
	}
	
	public void setQuickAction(View v) {
		// array to hold the coordinates of the clicked view
		int[] xy = new int[2];
		// fills the array with the computed coordinates
		v.getLocationInWindow(xy);
		// rectangle holding the clicked view area
		Rect rect = new Rect(xy[0], xy[1], xy[0] + v.getWidth(), xy[1]
				+ v.getHeight());


		final QuickActionWindow qa = new QuickActionWindow(getContext(), v, rect);

		qa.addItem(
				getContext().getResources().getDrawable(android.R.drawable.ic_menu_today),
				getContext().getString(R.string.txt_date), new OnClickListener() {
					public void onClick(View v) {
						qa.dismiss();
					}
				});

		qa.addItem(getContext().getResources().getDrawable(R.drawable.ic_menu_time), getContext()
				.getString(R.string.txt_time), new OnClickListener() {
			public void onClick(View v) {
				qa.dismiss();
			}
		});
		qa.show();

	}


}
