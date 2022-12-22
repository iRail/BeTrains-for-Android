package tof.cv.mpp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.drm.DrmStore;
import android.net.Uri;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tof.cv.mpp.R;
import tof.cv.mpp.bo.Perturbations;


public class TrafficAdapter extends ArrayAdapter<Perturbations.Perturbation> {

    private LayoutInflater myLayoutInflater;
    ArrayList<Perturbations.Perturbation> items;
    Activity c;

    public TrafficAdapter(Activity context, int textViewResourceId, Perturbations list, LayoutInflater layoutInflater) {
        super(context, textViewResourceId, list.disturbance);
        this.myLayoutInflater = layoutInflater;
        this.items = list.disturbance;
        this.c = context;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        // return super.getView(position, convertView, parent);

        Perturbations.Perturbation item = items.get(position);

        View row = convertView;

        if (row == null) {
            row = myLayoutInflater.inflate(R.layout.row_rss, parent, false);
        }

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(items.get(position).link));
                    c.startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        TextView listTitle = row.findViewById(R.id.listtitle);
        listTitle.setText(item.title);

        TextView listPubdate = row
                .findViewById(R.id.listpubdate);

        Date d = new Date();
        d.setTime(item.timestamp*1000);
        String pubDate = formatDate(d, true);
        //Todo: Parse the text date to display in user locale.
        listPubdate.setText(pubDate);

        TextView message = (TextView) row
                .findViewById(R.id.message);
        message.setText(Html.fromHtml(item.description));
        return row;
    }

    public String formatDate(Date date, boolean withTime) {
        String result = "";
        DateFormat dateFormat;

        if (date != null) {
            try {
                String format = Settings.System.getString(c.getContentResolver(), Settings.System.DATE_FORMAT);
                if (TextUtils.isEmpty(format)) {
                    dateFormat = android.text.format.DateFormat.getDateFormat(c);
                } else {
                    dateFormat = new SimpleDateFormat(format);
                }
                result = dateFormat.format(date);

                if (withTime) {
                    dateFormat = android.text.format.DateFormat.getTimeFormat(c);
                    result += " " + dateFormat.format(date);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
	

