package tof.cv.mpp.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tof.cv.mpp.R;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.bo.Alert;
import tof.cv.mpp.bo.Connection;
import tof.cv.mpp.bo.Occupancy;

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
            ImageView occupancy = (ImageView) v.findViewById(R.id.occupancy);
            TextView numberoftrains = (TextView) v
                    .findViewById(R.id.numberoftrains);
            ImageView alert = (ImageView) v.findViewById(R.id.alert);
            TextView alertText = (TextView) v.findViewById(R.id.alertText);

            if (conn.getAlerts() != null && conn.getAlerts().getNumber() > 0) {
                alert.setVisibility(View.VISIBLE);
                alertText.setVisibility(View.VISIBLE);
                String text = "";
                if (conn.getAlerts().getAlertlist() != null)
                    for (Alert anAlert : conn.getAlerts().getAlertlist())
                        text += anAlert.getHeader() + " / ";

                if (text.endsWith(" / "))
                    text = text.substring(0, text.length() - 3);

                alertText.setText(text);
            } else {
                alert.setVisibility(View.GONE);
                alertText.setVisibility(View.GONE);
            }


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
                                .contentEquals("") ? "" : getContext().getString(R.string.platform) + " " + conn
                                .getDeparture().getPlatform()));

                if (conn.getDeparture().getPlatforminfo() != null && conn.getDeparture().getPlatforminfo().normal == 0)
                    departure
                            .setText("! " + departure.getText() + " !");
            }
            if (arrival != null) {
                arrival.setText((conn.getArrival().getPlatform().contentEquals("") ? ""
                        : getContext().getString(R.string.platform) + " " + conn.getArrival().getPlatform()));

                if (conn.getArrival().getPlatforminfo() != null && conn.getArrival().getPlatforminfo().normal == 0)
                    arrival
                            .setText("! " + arrival.getText() + " !");
            }

            if (triptime != null) {
                triptime.setText(Html.fromHtml(
                        getContext().getString(R.string.route_planner_duration)
                                + " <b>"
                                + Utils.formatDate(conn.getDuration(), true, false)
                                + "</b>"));
            }
            if (departtime != null) {
                departtime.setText(conn.getDeparture().isCancelled() ? Html.fromHtml("<font color=\"red\">XXXX</font>") : Utils.formatDate(conn.getDeparture()
                        .getTime(), false, false));
            }
            if (arrivaltime != null) {
                arrivaltime.setText(conn.getArrival().isCancelled() ? Html.fromHtml("<font color=\"red\">XXXX</font>") : Utils.formatDate(conn.getArrival()
                        .getTime(), false, false));
            }

            if (numberoftrains != null) { //
                // Log.i("BETRAINS", "number" + conn.getVias()));
                if (conn.getVias() != null)
                    numberoftrains.setText(Html.fromHtml(
                            getContext().getString(R.string.route_planner_num_trains)
                                    + " <b>"
                                    + (conn.getVias().getNumberOfVias() + 1)
                                    + "</b>"));
                else
                    numberoftrains.setText(Html.fromHtml(Utils.getTrainId(conn
                            .getDeparture().getVehicle())));
            }


            if (conn.getOccupancy() != null) {
                occupancy.setVisibility(View.VISIBLE);

                switch (conn.getOccupancy().getName()) {
                    case Occupancy.UNKNOWN:
                        //occupancy.setImageResource(R.drawable.ic_occupancy_unknown);
                        occupancy.setVisibility(View.GONE);
                        break;
                    case Occupancy.HIGH:
                        occupancy.setImageResource(R.drawable.ic_occupancy_high);
                        break;
                    case Occupancy.MEDIUM:
                        occupancy.setImageResource(R.drawable.ic_occupancy_medium);
                        break;
                    case Occupancy.LOW:
                        occupancy.setImageResource(R.drawable.ic_occupancy_low);
                        break;
                    default:
                        occupancy.setVisibility(View.GONE);
                }
            }
            else
                occupancy.setVisibility(View.GONE);

        }
        return v;
    }
}
