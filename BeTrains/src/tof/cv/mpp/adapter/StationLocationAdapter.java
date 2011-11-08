package tof.cv.mpp.adapter;

import java.util.ArrayList;

import tof.cv.mpp.R;
import tof.cv.mpp.bo.StationLocation;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StationLocationAdapter extends AbstractAdapter<StationLocation> {
	public StationLocationAdapter(Context context, int rowResourceId,
			ArrayList<StationLocation> items) {
		super(context, rowResourceId, items);	
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) super.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row_closest, null);
		}
		StationLocation station = items.get(position);
		if (station != null) {
			TextView tvName = (TextView) v.findViewById(R.id.tv_name);
			TextView tvGps = (TextView) v.findViewById(R.id.tv_gps);
			TextView tvDistance = (TextView) v.findViewById(R.id.tv_dis);
			tvName.setText(station.getStation());
			tvGps.setText(station.getLat()/1E6+" - "+station.getLon()/1E6);
			int iDistance=(int)(Double.valueOf(station.getDistance())/100);
			tvDistance.setText((double)iDistance/10+"km");

		}
		return v;
	}
	
    public static double distance(double sLat,double sLon,double eLat,double eLon){
        double d2r = (Math.PI / 180);

        try{
            double dlong = (eLon - sLon) * d2r;
            double dlat = (eLat - sLat) * d2r;
            double a =
                Math.pow(Math.sin(dlat / 2.0), 2)
                    + Math.cos(sLat * d2r)
                    * Math.cos(eLat * d2r)
                    * Math.pow(Math.sin(dlong / 2.0), 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            return 6367 * c * 1000;


        } catch(Exception e){
            e.printStackTrace();
        }
        return 0;
}
}
