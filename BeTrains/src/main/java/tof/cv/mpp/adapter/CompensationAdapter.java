package tof.cv.mpp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import tof.cv.mpp.R;
import tof.cv.mpp.Utils.Utils;

public class CompensationAdapter extends AbstractAdapter<String> {


    public CompensationAdapter(Context context, int textViewResourceId,
                               ArrayList<String> items) {
        super(context, textViewResourceId, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) super.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.row_compensation, null);
        }
        String[] o = items.get(position).split(";");
        if (o != null) {
            TextView title = (TextView) v.findViewById(R.id.title);
            TextView delay = (TextView) v.findViewById(R.id.delay);
            TextView detail = (TextView) v.findViewById(R.id.detail);
            TextView date = (TextView) v.findViewById(R.id.date);

            date.setText(Utils.formatDate(new Date(Long.valueOf(o[0])),"EEE d MMM yy"));

            delay.setText("+"+o[1]+"'");

            try {
                if (!o[2].contentEquals(""))
                    detail.setText(o[2]);
                else
                    detail.setText(getContext().getString(R.string.compensation_empty_detail));

                title.setText(getContext().getString(R.string.train)+" "+o[3]);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        return v;
    }

}