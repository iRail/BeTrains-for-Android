package tof.cv.mpp.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.InputStream;
import java.util.List;

import tof.cv.mpp.DetailActivity;
import tof.cv.mpp.R;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.bo.Alert;
import tof.cv.mpp.bo.Connection;
import tof.cv.mpp.bo.MaterialType;
import tof.cv.mpp.bo.Occupancy;
import tof.cv.mpp.bo.TrainComposition;

public class ConnectionAdapter extends RecyclerView.Adapter<ConnectionAdapter.ConnectionViewHolder> {
    List<Connection> connection;
    Activity c;

    public ConnectionAdapter(List<Connection> connection, Activity a) {
        this.connection = connection;
        this.c = a;
    }

    @NonNull
    @Override
    public ConnectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_planner, parent, false);
        ConnectionViewHolder vh = new ConnectionViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ConnectionViewHolder holder, int position) {
        final Connection conn = connection.get(position);
        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(c, DetailActivity.class);
                intent.putExtra("connection", new Gson().toJson(conn));
                Pair<View, String> p1 = Pair.create(view.findViewById(R.id.bg), "bg");

                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(c, p1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    c.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(c).toBundle());//, options.toBundle());
                } else
                    c.startActivity(intent);
                c.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        if (conn != null) {
            if (conn.getAlerts() != null && conn.getAlerts().getNumber() > 0) {
                holder.alert.setVisibility(View.VISIBLE);
                holder.alertText.setVisibility(View.VISIBLE);
                String text = "";
                if (conn.getAlerts().getAlertlist() != null)
                    for (Alert anAlert : conn.getAlerts().getAlertlist())
                        text += anAlert.getHeader() + " / ";

                if (text.endsWith(" / "))
                    text = text.substring(0, text.length() - 3);

                holder.alertText.setText(text);
            } else {
                holder.alert.setVisibility(View.GONE);
                holder.alertText.setVisibility(View.GONE);
            }


            String delayStr = " +"
                    + (Integer.valueOf(conn.getDeparture().getDelay()) / 60)
                    + "'";
            if (!conn.getDeparture().getDelay().contentEquals("0"))
                holder.delayD.setText(delayStr);
            else
                holder.delayD.setText("");

            delayStr = " +"
                    + (Integer.valueOf(conn.getArrival().getDelay()) / 60)
                    + "'";
            if (!conn.getArrival().getDelay().contentEquals("0"))
                holder.delayA.setText(delayStr);
            else
                holder.delayA.setText("");


            if (holder.departure != null) {

                holder.departure
                        .setText((conn.getDeparture().getPlatform()
                                .contentEquals("") ? "" : c.getString(R.string.platform) + " " + conn
                                .getDeparture().getPlatform()));

                if (conn.getDeparture().getPlatforminfo() != null && conn.getDeparture().getPlatforminfo().normal == 0)
                    holder.departure
                            .setText("! " + holder.departure.getText() + " !");
            }
            if (holder.arrival != null) {
                holder.arrival.setText((conn.getArrival().getPlatform().contentEquals("") ? ""
                        : c.getString(R.string.platform) + " " + conn.getArrival().getPlatform()));

                if (conn.getArrival().getPlatforminfo() != null && conn.getArrival().getPlatforminfo().normal == 0)
                    holder.arrival
                            .setText("! " + holder.arrival.getText() + " !");
            }

            if (holder.triptime != null) {
                holder.triptime.setText(Html.fromHtml(
                        c.getString(R.string.route_planner_duration)
                                + " <b>"
                                + Utils.formatDate(conn.getDuration(), true, false)
                                + "</b>"));
            }
            if (holder.departtime != null) {
                holder.departtime.setText(conn.getDeparture().isCancelled() ? Html.fromHtml("<font color=\"red\">XXXX</font>") : Utils.formatDate(conn.getDeparture()
                        .getTime(), false, false));
            }
            if (holder.arrivaltime != null) {
                holder.arrivaltime.setText(conn.getArrival().isCancelled() ? Html.fromHtml("<font color=\"red\">XXXX</font>") : Utils.formatDate(conn.getArrival()
                        .getTime(), false, false));
            }
            holder.container.removeAllViews();
            if (holder.numberoftrains != null) { //
                if (conn.getVias() != null) {
                    holder.numberoftrains.setText(Html.fromHtml(
                            c.getString(R.string.route_planner_num_trains)
                                    + " <b>"
                                    + (conn.getVias().getNumberOfVias() + 1)
                                    + "</b>"));


                } else
                    holder.numberoftrains.setText(Html.fromHtml(Utils.getTrainId(conn
                            .getDeparture().getVehicle())));
            }

            holder.trainIconUrl = "https://staging.api.irail.be/composition.php?id=" +
                    conn.getDeparture().getVehicle() + "&format=json";
            //holder.trainIcon.setImageDrawable(null);
            holder.loadicon();


            if (conn.getOccupancy() != null) {
                holder.occupancy.setVisibility(View.VISIBLE);

                switch (conn.getOccupancy().getName()) {
                    case Occupancy.UNKNOWN:
                        //occupancy.setImageResource(R.drawable.ic_occupancy_unknown);
                        holder.occupancy.setVisibility(View.GONE);
                        break;
                    case Occupancy.HIGH:
                        holder.occupancy.setImageResource(R.drawable.ic_occupancy_high);
                        break;
                    case Occupancy.MEDIUM:
                        holder.occupancy.setImageResource(R.drawable.ic_occupancy_medium);
                        break;
                    case Occupancy.LOW:
                        holder.occupancy.setImageResource(R.drawable.ic_occupancy_low);
                        break;
                    default:
                        holder.occupancy.setVisibility(View.GONE);
                }
            } else
                holder.occupancy.setVisibility(View.GONE);

        }
    }

    @Override
    public int getItemCount() {
        return connection.size();
    }

    //AUTHOR: Bertware : https://github.com/hyperrail/hyperrail-for-android
    // Credits to him.


    public MaterialType convert(String parentType, String subType, String orientation, int firstClassSeats) {
        if (parentType.startsWith("HLE")) {
            return convertHle(parentType, subType, orientation);
        } else if (parentType.startsWith("AM") || parentType.startsWith("MR") || parentType.startsWith("AR")) {
            return convertAm(parentType, subType, orientation, firstClassSeats);
        } else if (parentType.startsWith("I") || parentType.startsWith("M")) {
            return convertCarriage(parentType, subType, orientation, firstClassSeats);
        } else {
            return new MaterialType(parentType, subType, orientation);
        }
    }

    public MaterialType convertCarriage(String parentType, String subType, String orientation, int firstClassSeats) {
        String newParentType = parentType;
        String newSubType = subType;

        switch (parentType) {
            case "M4":
                switch (subType) {
                    case "A":
                    case "AU":
                        newSubType = "B_A";
                        break;
                    case "AD":
                    case "AUD":
                        newSubType = "B_AD";
                        break;
                    case "ADX":
                        newSubType = "B_ADX";
                        break;
                    case "B":
                    case "BU":
                    case "BYU":
                        newSubType = "B_B";
                        break;
                    case "BD":
                    case "BDU":
                        newSubType = "B_BD";
                        break;
                }
                break;
            case "M6":
                switch (subType) {
                    case "BXAA":
                    case "BXCT":
                        // 134/117 2nd class, LIKELY steering cabin
                        newSubType = "BDX";
                        break;
                    case "BYU":
                        // BU + Y
                        break;
                    case "BUH":
                        // BU + H
                    case "BDUH":
                        // BUH + D
                    case "BAU":
                        // Mixed 1st/2nd class
                    case "BU":
                        // 140/133 2nd class
                        newSubType = "B";
                        break;
                    case "AU":
                        // 124/133 1st class
                        newSubType = "A";
                        break;
                    case "BDU":
                        // 102/145 2nd class w/ luggage and bike storage
                        newSubType = "BD";
                        break;
                    case "BDAU":
                        // 1st/2nd class w/ luggage and bike storage
                        newSubType = "ABD";
                        break;
                }
                break;
            case "I10":
                if (firstClassSeats > 0) {
                    newSubType = "B_A";
                } else {
                    newSubType = "B_B";
                }
                break;
            case "I11":
                if (subType.contains("X")) {
                    newSubType = "BDX";
                } else if (firstClassSeats > 0) {
                    newSubType = "A";
                } else {
                    newSubType = "B";
                }
                break;
        }
        return new MaterialType(newParentType, newSubType, orientation);
    }

    public MaterialType convertAm(String parentType, String subType, String orientation, int firstClassSeats) {
        String newParentType = parentType;
        String newSubType = subType;

        switch (parentType) {
            case "AM08":
            case "AM08M":
                switch (subType) {
                    case "A":
                    case "C":
                        newSubType = "0_C";
                        break;
                    case "B":
                        newSubType = "0_B";
                        break;
                }
                newParentType = "AM08";
                break;
            case "AM08P":
                newParentType = "AM08";
                switch (subType) {
                    case "A":
                    case "C":
                        newSubType = "5_C";
                        break;
                    case "B":
                        newSubType = "5_B";
                        break;
                }
                break;
            case "AM86":
                if (firstClassSeats > 0) {
                    newSubType = "R_B";
                } else {
                    newSubType = "M_B";
                }
                break;
            case "AR41":
                newParentType = "MW41";
                if (firstClassSeats > 0) {
                    newSubType = "AB";
                } else {
                    newSubType = "B";
                }
                break;
            case "AM75":
                switch (subType) {
                    case "A":
                    case "D":
                        if (firstClassSeats > 0) {
                            newSubType = "RXA_B";
                        } else {
                            newSubType = "RXB_B";
                        }
                        break;
                    case "B":
                        newSubType = "M1_B";
                        break;
                    case "C":
                        newSubType = "M2_B";
                        break;
                }
                break;
            case "AM80":
            case "AM80M":
                newParentType = "AM80";
                switch (subType) {
                    // B, BX, ABDX,
                    case "A":
                    case "C":
                        if (firstClassSeats > 0) {
                            newSubType = "ABDX_B";
                        } else {
                            newSubType = "BX_B";
                        }
                        break;
                    case "B":
                        newSubType = "B_B";
                        break;
                }
                break;
            case "AM62-66":
                newParentType = "AM66";
                if (firstClassSeats > 0) {
                    newSubType = "M2_B";
                } else {
                    newSubType = "M1_B";
                }
                break;
            case "AM96":
            case "AM96M":
            case "AM96P":
                newParentType = "AM96";
                switch (subType) {
                    // B, BX, ABDX,
                    case "A":
                    case "C":
                        if (firstClassSeats > 0) {
                            newSubType = "AX";
                        } else {
                            newSubType = "BX";
                        }
                        break;
                    case "B":
                        newSubType = "BBIC";
                        break;
                }
                break;
        }
        return new MaterialType(newParentType, newSubType, orientation);
    }

    public MaterialType convertHle(String parentType, String subType, String orientation) {
        String newParentType = parentType;
        String newSubType = subType;

        switch (parentType) {
            case "HLE18":
                // NMBS doesn't distinguish between the old and new gen. All the old gen vehicles are out of service.
                newParentType += "II";
                newSubType = "";
                break;
            case "HLE11":
            case "HLE12":
            case "HLE13":
            case "HLE15":
            case "HLE16":
            case "HLE19":
            case "HLE20":
                if (subType.isEmpty()) {
                    newSubType = "B";
                }
                break;
            case "HLE21":

        }

        return new MaterialType(newParentType, newSubType, orientation);
    }


    public class ConnectionViewHolder extends RecyclerView.ViewHolder {
        TextView delayD;
        TextView delayA;
        TextView departure;
        TextView arrival;
        TextView triptime;
        TextView departtime;
        TextView arrivaltime;
        ImageView occupancy;
        TextView numberoftrains;
        ImageView alert;
        TextView alertText;
        ImageView trainIcon;
        LinearLayout container;
        String trainIconUrl;
        View v;

        public ConnectionViewHolder(@NonNull View v) {
            super(v);

            container = v.findViewById(R.id.viacontainer);
            delayD = v.findViewById(R.id.delayD);
            delayA = v.findViewById(R.id.delayA);
            departure = v.findViewById(R.id.departure);
            arrival = v.findViewById(R.id.arrival);
            triptime = v.findViewById(R.id.duration);
            departtime = v.findViewById(R.id.departtime);
            arrivaltime = v.findViewById(R.id.arrivaltime);
            occupancy = v.findViewById(R.id.occupancy);
            numberoftrains = v.findViewById(R.id.numberoftrains);
            alert = v.findViewById(R.id.alert);
            alertText = v.findViewById(R.id.alertText);
            trainIcon = v.findViewById(R.id.trainicon);
            this.v = v;


        }

        public void loadicon() {
            Ion.with(c).load(trainIconUrl)
                    .as(new TypeToken<TrainComposition>() {
                    }).setCallback(new FutureCallback<TrainComposition>() {
                @Override
                public void onCompleted(Exception e, TrainComposition result) {
                    if (result != null && result.composition != null) {
                        if (result.composition.segments.segment.get(0).composition != null) {
                            trainIcon.setVisibility(View.VISIBLE);
                            MaterialType type = result.composition.segments.segment.get(0).composition.units.
                                    unit.get(result.composition.segments.segment.get(0).composition.units.unit.size()>1?1:0).materialType;

                            //Log.e("CVETYPE_ORIGINAL", "" + type.parent_type
                            //       + " - " + type.sub_type
                            //       + " - " + type.orientation);

                            type = convert(type.parent_type, type.sub_type.toUpperCase(), type.orientation, result.composition.segments.segment.get(0).composition.units.unit.get(0).seatsFirstClass);


                            try {
                                String path = "trains/SNCB_" + type.parent_type  + (type.sub_type.length()>0?("_"+type.sub_type):"") + "_R.GIF";
                                Log.e("CVE", path);
                                InputStream ims = c.getAssets().open(path);
                                Drawable d = Drawable.createFromStream(ims, null);
                                trainIcon.setImageDrawable(d);
                                ims.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                return;
                            }


                        } else trainIcon.setVisibility(View.GONE);
                    } else
                        trainIcon.setVisibility(View.GONE);
                }
            });
        }
    }


}
