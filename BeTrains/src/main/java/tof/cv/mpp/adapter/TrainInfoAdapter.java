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
import tof.cv.mpp.bo.Vehicle;

public class TrainInfoAdapter extends ArrayAdapter<Vehicle.VehicleStop> {

    public TrainInfoAdapter(Context context, int textViewResourceId,
                            ArrayList<Vehicle.VehicleStop> items) {
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
        Vehicle.VehicleStop o = getItem(position);
        if (o != null) {
            TextView time = (TextView) v.findViewById(R.id.time);
            TextView delay = (TextView) v.findViewById(R.id.delay);
            TextView station = (TextView) v.findViewById(R.id.station);

            station.setText(Html.fromHtml(o.getStation()));
            time.setText(Utils.formatDate(o.getTime(), false, false));

            if (o.isCancelled())
                time.setText(Html.fromHtml("<font color=\"red\">XXXX</font>"));
            else if (o.getDelay().contentEquals("0"))
                delay.setText("");
            else
                try {
                    delay.setText("+"
                            + (Integer.valueOf(o.getDelay()) / 60)
                            + "'");
                } catch (Exception e) {
                    delay.setText(o.getDelay());
                }
        }
        return v;
    }
}
