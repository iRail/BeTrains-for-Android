package tof.cv.mpp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import com.google.android.material.snackbar.Snackbar;

import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import tof.cv.mpp.InfoStationActivity;
import tof.cv.mpp.R;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.bo.Alert;
import tof.cv.mpp.bo.Alerts;
import tof.cv.mpp.bo.Vehicle;

public class TrainInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Vehicle.VehicleStop> list;
    String train;
    Alerts alerts;
    boolean hasAlerts;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public TrainInfoAdapter(ArrayList<Vehicle.VehicleStop> list, Context context, Alerts alerts, String train) {
        this.list = list;
        this.alerts = alerts;
        this.train = train;
        hasAlerts = (alerts != null && alerts.getNumber() > 0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_info_train_alert, parent, false);
            return new VHHeader(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_info_train, parent, false);
            return new TrainInfoAdapter.InfotrainHolder(v);
        }


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holderParam, int position) {
        if (holderParam instanceof InfotrainHolder) {
            InfotrainHolder holder = (InfotrainHolder) holderParam;
            Vehicle.VehicleStop o = list.get(position + (hasAlerts ? -1 : 0));
            if (o != null) {
                holder.item = o;
                holder.station.setText(Html.fromHtml(o.getStation()));

                if (o.getPlatforminfo() != null) {
                    holder.platform.setText(o.getPlatforminfo().name);

                    if (o.getPlatforminfo() != null && o.getPlatforminfo().normal == 0)
                        holder.platform
                                .setText("! " + holder.platform.getText() + " !");
                } else
                    holder.platform.setText("");


                if (o.isCancelled())
                    holder.time.setText(Html.fromHtml("<font color=\"red\">XXXX</font>"));
                else
                    holder.time.setText(Utils.formatDate(o.getTime(), false, false));

                if (o.getDelay().contentEquals("0"))
                    holder.delay.setText("");
                else
                    try {
                        holder.delay.setText("+"
                                + (Integer.valueOf(o.getDelay()) / 60)
                                + "'");
                    } catch (Exception e) {
                        holder.delay.setText(o.getDelay());
                    }

                holder.left.setVisibility(o.hasLeft() ? View.VISIBLE : View.INVISIBLE);

            }
        }
        if (holderParam instanceof VHHeader) {
            VHHeader holder = (VHHeader) holderParam;
            holder.train = train;
            holder.text = "";
            holder.html = "";
            if (alerts.getAlertlist() != null)
                for (Alert anAlert : alerts.getAlertlist()) {
                    holder.text += anAlert.getHeader() + " / ";
                    holder.html += ("<h3>" + anAlert.getHeader() + "</h3>");
                    holder.html += (anAlert.getDescription());
                }


            if (holder.text.endsWith(" / "))
                holder.text = holder.text.substring(0, holder.text.length() - 3);

            holder.alert.setText(Html.fromHtml(holder.text));
        }

    }


    @Override
    public int getItemCount() {
        return list.size() + (hasAlerts ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (hasAlerts && position == 0)
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    public static class InfotrainHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView time;
        TextView delay;
        TextView station;
        View left;
        TextView platform;
        Vehicle.VehicleStop item;

        public InfotrainHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            time = (TextView) v.findViewById(R.id.time);
            delay = (TextView) v.findViewById(R.id.delay);
            station = (TextView) v.findViewById(R.id.station);
            left = (View) v.findViewById(R.id.left);
            platform = (TextView) v.findViewById(R.id.platform);
        }

        @Override
        public void onClick(View view) {
            Intent i = new Intent(view.getContext(), InfoStationActivity.class);
            i.putExtra("Name", item.getStation());
            i.putExtra("ID", item.getStationInfo().getId());
            i.putExtra("timestamp", item.getTime());
            view.getContext().startActivity(i);
        }
    }

    public static class VHHeader extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView alert;
        String html;
        String text;
        String train;

        public VHHeader(View v) {
            super(v);
            v.setOnClickListener(this);
            alert = (TextView) v.findViewById(R.id.alert);
        }

        @Override
        public void onClick(View view) {


            final SpannableString s = new SpannableString(html); // msg should have url to enable clicking
            Linkify.addLinks(s, Linkify.ALL);


            AlertDialog d = new AlertDialog.Builder(view.getContext())
                    .setTitle(train)
                    .setMessage(Html.fromHtml(html)).create();

            d.show();

            ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
            ((TextView) d.findViewById(android.R.id.message)).setLinkTextColor(view.getContext().getResources().getColor(R.color.darkblue));
        }
    }
}
