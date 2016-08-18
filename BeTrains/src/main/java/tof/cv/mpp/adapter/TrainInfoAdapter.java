package tof.cv.mpp.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tof.cv.mpp.InfoStationActivity;
import tof.cv.mpp.R;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.bo.Vehicle;

public class TrainInfoAdapter extends RecyclerView.Adapter<TrainInfoAdapter.InfotrainHolder> {

    ArrayList<Vehicle.VehicleStop> list;

    public TrainInfoAdapter(ArrayList<Vehicle.VehicleStop> list, Context context) {
        this.list = list;
    }

    @Override
    public TrainInfoAdapter.InfotrainHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_info_train, parent, false);
        TrainInfoAdapter.InfotrainHolder viewHolder = new TrainInfoAdapter.InfotrainHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(InfotrainHolder holder, int position) {
        Vehicle.VehicleStop o = list.get(position);
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
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class InfotrainHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView time;
        TextView delay;
        TextView station;
        TextView platform;
        Vehicle.VehicleStop item;

        public InfotrainHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            time = (TextView) v.findViewById(R.id.time);
            delay = (TextView) v.findViewById(R.id.delay);
            station = (TextView) v.findViewById(R.id.station);
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
}
