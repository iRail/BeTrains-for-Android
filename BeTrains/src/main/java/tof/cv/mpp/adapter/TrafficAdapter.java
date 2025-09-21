package tof.cv.mpp.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tof.cv.mpp.R;
import tof.cv.mpp.bo.Perturbations;

public class TrafficAdapter extends RecyclerView.Adapter<TrafficAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Perturbations.Perturbation> items;
    private final LayoutInflater inflater;

    public TrafficAdapter(Context context, Perturbations list) {
        this.context = context;
        this.items = list.disturbance;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.row_rss, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Perturbations.Perturbation item = items.get(position);

        holder.listTitle.setText(item.title);

        Date d = new Date(item.timestamp * 1000);
        String pubDate = formatDate(d, true);
        holder.listPubdate.setText(pubDate);

        holder.message.setText(Html.fromHtml(item.description));

        holder.itemView.setOnClickListener(v -> {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(item.link));
                context.startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView listTitle;
        TextView listPubdate;
        TextView message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            listTitle = itemView.findViewById(R.id.listtitle);
            listPubdate = itemView.findViewById(R.id.listpubdate);
            message = itemView.findViewById(R.id.message);
        }
    }

    private String formatDate(Date date, boolean withTime) {
        String result = "";
        DateFormat dateFormat;

        if (date != null) {
            try {
                String format = Settings.System.getString(context.getContentResolver(), Settings.System.DATE_FORMAT);
                if (TextUtils.isEmpty(format)) {
                    dateFormat = android.text.format.DateFormat.getDateFormat(context);
                } else {
                    dateFormat = new SimpleDateFormat(format);
                }
                result = dateFormat.format(date);

                if (withTime) {
                    dateFormat = android.text.format.DateFormat.getTimeFormat(context);
                    result += " " + dateFormat.format(date);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
