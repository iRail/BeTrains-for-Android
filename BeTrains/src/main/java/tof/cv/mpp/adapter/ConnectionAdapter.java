package tof.cv.mpp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import tof.cv.mpp.InfoStationActivity;
import tof.cv.mpp.InfoTrainActivity;
import tof.cv.mpp.R;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.bo.Alert;
import tof.cv.mpp.bo.Connection;
import tof.cv.mpp.bo.MaterialType;
import tof.cv.mpp.bo.Occupancy;
import tof.cv.mpp.bo.TrainComposition;
import tof.cv.mpp.bo.Via;

public class ConnectionAdapter extends RecyclerView.Adapter<ConnectionAdapter.ConnectionViewHolder> {
    List<Connection> connection;
    Activity c;
    ArrayList<Alert> singleAlert;

    public ConnectionAdapter(List<Connection> connection, Activity a, ArrayList<Alert> singleAlert) {
        this.connection = connection;
        this.c = a;
        this.singleAlert = singleAlert;
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
        //if (position != 0) return;
        final Connection conn = connection.get(position);
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(c, DetailActivity.class);
                intent.putExtra("connection", new Gson().toJson(conn));
                Pair<View, String> p1 = Pair.create(view.findViewById(R.id.bg), "bg");

                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(c, p1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    c.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(c).toBundle());//, options.toBundle());
                } else
                    c.startActivity(intent);
                c.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);*/
                holder.card.setVisibility((holder.card.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);
            }
        });

        if (conn != null) {
            holder.co = conn;
            if (conn.getAlerts() != null && conn.getAlerts().getNumber() > 0) {
                holder.alertText.setVisibility(View.VISIBLE);
                String text = "";

                if (conn.getAlerts().getAlertlist() != null)
                    for (Alert anAlert : conn.getAlerts().getAlertlist()) {
                        boolean toDel = false;
                        for (Alert aSingleAlert : singleAlert) {
                            if (aSingleAlert.getHeader().contentEquals(anAlert.getHeader()))
                                toDel = true;
                        }
                        if (!toDel)
                            text += anAlert.getHeader() + "<br/>";
                    }

                if (text.endsWith("<br/>"))
                    text = text.substring(0, text.length() - 5);
                if (text.length() > 0){
                    holder.alertText.setVisibility(View.VISIBLE);
                    holder.alertText.setText(Html.fromHtml(text));
                }else
                    holder.alertText.setVisibility(View.GONE);

            } else {
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

            holder.departureName.setText(conn.getDeparture().getStation());
            holder.departureName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startStationInfoActivity(
                            conn.getDeparture().getStation(), conn.getDeparture().getTime(), conn.getDeparture().getStationInfo().getId());
                }
            });

            holder.arrivalName.setText(conn.getArrival().getStation());
            holder.arrivalName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startStationInfoActivity(
                            conn.getArrival().getStation(), conn.getArrival().getTime(), conn.getArrival().getStationInfo().getId());
                }
            });

            holder.departure
                    .setText(conn.getDeparture().getPlatform());

            if (conn.getDeparture().getPlatforminfo() != null && conn.getDeparture().getPlatforminfo().normal == 0)
                holder.departure.setTypeface(Typeface.DEFAULT_BOLD);


            holder.arrival.setText(conn.getArrival().getPlatform());

            if (conn.getArrival().getPlatforminfo() != null && conn.getArrival().getPlatforminfo().normal == 0)
                holder.arrival.setTypeface(Typeface.DEFAULT_BOLD);

            holder.triptime.setText(Html.fromHtml(
                    " <b>"
                            + Utils.formatDate(conn.getDuration(), true, false)
                            + "</b>"));

            holder.departtime.setText(conn.getDeparture().isCancelled() ? Html.fromHtml("<font color=\"red\">XXXX</font>") : Utils.formatDate(conn.getDeparture()
                    .getTime(), false, false));

            holder.arrivaltime.setText(conn.getArrival().isCancelled() ? Html.fromHtml("<font color=\"red\">XXXX</font>") : Utils.formatDate(conn.getArrival()
                    .getTime(), false, false));

            holder.container.removeAllViews();
            if (holder.numberoftrains != null) { //
                if (conn.getVias() != null && conn.getVias().via != null && conn.getVias().via.size() > 1) {
                    holder.numberoftrainsll.removeAllViews();
                    holder.numberoftrainsll.setVisibility(View.VISIBLE);

                    holder.numberoftrains.setVisibility(View.GONE);

                    LayoutInflater inflater = (LayoutInflater) holder.numberoftrainsll.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    for (Via avia : conn.getVias().via) {
                        View v = inflater.inflate(R.layout.atrain, null);
                        holder.numberoftrainsll.addView(v);

                    }

                } else {
                    holder.numberoftrains.setVisibility(View.VISIBLE);
                    holder.numberoftrainsll.setVisibility(View.GONE);

                    holder.numberoftrains.setText(Html.fromHtml(Utils.getTrainId(conn
                            .getDeparture().getVehicle())));
                }

            }

            int i = 1;
            LayoutInflater inflater = (LayoutInflater) holder.lltrains.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            holder.lltrains.removeAllViews();

            View v = inflater.inflate(R.layout.row_connection_detail, null);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startTrainInfoActivity(conn.getDeparture().getVehicle());
                }
            });
            holder.lltrains.addView(v);

            if (conn.getVias() != null && conn.getVias().via != null)
                for (final Via aVia : conn.getVias().via) {
                    v = inflater.inflate(R.layout.row_via_station, null);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startStationInfoActivity(aVia.getDeparture().getStation(), aVia.getArrival().getTime(), aVia.getStationInfo().getId());
                        }
                    });

                    holder.lltrains.addView(v);


                    v = inflater.inflate(R.layout.row_connection_detail, null);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startTrainInfoActivity(aVia.getDeparture().getVehicle());
                        }
                    });
                    holder.lltrains.addView(v);
                }

            holder.loadicon(Long.valueOf(conn.getDeparture().getTime()));

            if (conn.getOccupancy() != null) {
                holder.occupancy.setVisibility(View.VISIBLE);

                switch (conn.getOccupancy().getName()) {
                    case Occupancy.UNKNOWN:
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


    private MaterialType convert(String parentType, String subType, String orientation, int firstClassSeats) {
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

    private MaterialType convertCarriage(String parentType, String subType, String orientation, int firstClassSeats) {
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

    private MaterialType convertAm(String parentType, String subType, String orientation, int firstClassSeats) {
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

    private MaterialType convertHle(String parentType, String subType, String orientation) {
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
            case "HLE27":
                newSubType = "";//OR U???

        }

        return new MaterialType(newParentType, newSubType, orientation);
    }


    class ConnectionViewHolder extends RecyclerView.ViewHolder {
        TextView delayD;
        TextView delayA;
        TextView departure;
        TextView departureName;
        TextView arrival;
        TextView arrivalName;
        TextView triptime;
        TextView departtime;
        TextView arrivaltime;
        ImageView occupancy;
        TextView numberoftrains;
        LinearLayoutCompat numberoftrainsll;
        TextView alertText;
        LinearLayout container;
        LinearLayout lltrains;
        View parent;
        CardView card;
        Connection co;

        private ConnectionViewHolder(@NonNull View v) {
            super(v);
            container = v.findViewById(R.id.viacontainer);
            delayD = v.findViewById(R.id.delayD);
            delayA = v.findViewById(R.id.delayA);
            departure = v.findViewById(R.id.departure);
            arrival = v.findViewById(R.id.arrival);
            departureName = v.findViewById(R.id.departurename);
            arrivalName = v.findViewById(R.id.arrivalname);
            triptime = v.findViewById(R.id.duration);
            departtime = v.findViewById(R.id.departtime);
            arrivaltime = v.findViewById(R.id.arrivaltime);
            occupancy = v.findViewById(R.id.occupancy);
            numberoftrains = v.findViewById(R.id.numberoftrains);
            numberoftrainsll = v.findViewById(R.id.numberoftrainsll);
            alertText = v.findViewById(R.id.alertText);
            lltrains = v.findViewById(R.id.lltrains);
            card = v.findViewById(R.id.card);
            this.parent = v;


        }

        private void loadicon(long prevTime) {

            int i = 0;
            if (co.getVias() != null && co.getVias().via != null) {
                for (final Via aVia : co.getVias().via) {


                    final TrainComposition.Composition.Segments.Segment.SegmentComposition composition =
                            getCompositionFromCache(aVia.getVehicle());
                    final int position = i;
                    final long prevTimeFinal = prevTime;
                    if (composition == null)
                        Ion.with(c).load("https://api.irail.be/composition.php?id=" + aVia.getVehicle() + "&format=json#")
                                .as(new TypeToken<TrainComposition>() {
                                }).setCallback(new FutureCallback<TrainComposition>() {
                            @Override
                            public void onCompleted(Exception e, TrainComposition result) {
                                // Log.e("CVE", "Ion " + result);
                                if (result != null && result.composition != null) {
                                    if (result.composition.segments.segment.get(0).composition != null) {
                                        //Log.e("CVE", "ADDDD");
                                        cacheComposition(aVia.getVehicle(), result.composition.segments.segment.get(0).composition);
                                        displayComposition(result.composition.segments.segment.get(0).composition,
                                                aVia.getVehicle(),
                                                lltrains.getChildAt(position), aVia, prevTimeFinal);
                                    }
                                } else
                                    displayComposition(null,
                                            aVia.getVehicle(),
                                            lltrains.getChildAt(position), aVia, prevTimeFinal);
                            }
                        });
                    else {
                        // Log.e("CVE", "CACHE " + aMap.getKey());
                        displayComposition(composition,
                                aVia.getVehicle(),
                                lltrains.getChildAt(i), aVia, prevTime);
                    }
                    i++;
                    loadStations(lltrains.getChildAt(i), aVia);
                    i++;
                    prevTime = Long.valueOf(aVia.getDeparture().getTime());
                }

            }

            final TrainComposition.Composition.Segments.Segment.SegmentComposition lastCompo =
                    getCompositionFromCache(co.getArrival().getVehicle());

            long start = co.getArrival().getTimeLong();
            long end = 0;
            end = ((co.getVias() == null || co.getVias().via.size() == 0) ? co.getDeparture().getTimeLong() : co.getVias().via.get(co.getVias().via.size() - 1).getDeparture().getTimeLong());

            final long lastDuration = start - end;

            if (lastCompo == null)
                Ion.with(c).load("https://api.irail.be/composition.php?id=" + co.getArrival().getVehicle() + "&format=json#")
                        .as(new TypeToken<TrainComposition>() {
                        }).setCallback(new FutureCallback<TrainComposition>() {
                    @Override
                    public void onCompleted(Exception e, TrainComposition result) {
                        if (result != null && result.composition != null) {
                            if (result.composition.segments.segment.get(0).composition != null) {
                                cacheComposition(co.getArrival().getVehicle(), result.composition.segments.segment.get(0).composition);
                                displayComposition(result.composition.segments.segment.get(0).composition,
                                        co.getArrival().getVehicle(),
                                        lltrains.getChildAt(lltrains.getChildCount() - 1), null, lastDuration);
                            }
                        } else
                            displayComposition(null,
                                    co.getArrival().getVehicle(),
                                    lltrains.getChildAt(lltrains.getChildCount() - 1), null, lastDuration);
                    }
                });
            else {
                displayComposition(lastCompo,
                        co.getArrival().getVehicle(),
                        lltrains.getChildAt(lltrains.getChildCount() - 1), null, lastDuration);
            }
        }

    }

    private void loadStations(View stationRow, Via aVia) {
        if (stationRow == null)
            return;

        TextView tvArrival = ((TextView) stationRow
                .findViewById(R.id.tv_arrival_platform));
        tvArrival.setText(aVia.getArrival().getPlatform());

        if (aVia.getArrival().getPlatforminfo() != null && aVia.getArrival().getPlatforminfo().normal == 0)
            tvArrival
                    .setText("!" + tvArrival.getText() + "!");

        TextView tvDeparture = ((TextView) stationRow
                .findViewById(R.id.tv_departure_platform));
        tvDeparture.setText(aVia.getDeparture().getPlatform());

        if (aVia.getDeparture().getPlatforminfo() != null && aVia.getDeparture().getPlatforminfo().normal == 0)
            tvDeparture
                    .setText("!" + tvDeparture.getText() + "!");


        ((TextView) stationRow.findViewById(R.id.tv_arrival_time))
                .setText(Utils.formatDate(aVia.getArrival()
                        .getTime(), false, false));
        ((TextView) stationRow.findViewById(R.id.tv_departure_time))
                .setText(Utils.formatDate(aVia.getDeparture()
                        .getTime(), false, false));
        ((TextView) stationRow.findViewById(R.id.tv_station))
                .setText(aVia.getName());

        ((TextView) stationRow.findViewById(R.id.tv_duration))
                .setText("("
                        + Utils.formatDate(aVia.getTimeBetween(),
                        true, false) + ")");

    }

    private void displayComposition(TrainComposition.Composition.Segments.Segment.SegmentComposition composition,
                                    String name, View v, Via aVia, long prevtime) {
        //boolean i1 = false;
        //boolean i2 = false;

        if (v == null || v.findViewById(R.id.train_name) == null) {
            // Log.e("CVE",name);
            return;
        }

        ((TextView) v.findViewById(R.id.train_name)).setText(name.replace("BE.NMBS.", ""));

        if (aVia != null)
            ((TextView) v.findViewById(R.id.tv_duration))
                    .setText(Utils.formatDate(
                            (Long.valueOf(aVia.getArrival().getTime()) - prevtime),
                            true, false));
        else
            ((TextView) v.findViewById(R.id.tv_duration))
                    .setText(Utils.formatDate(
                            (Long.valueOf(prevtime)),
                            true, false));

        if (composition == null) {
            v.findViewById(R.id.trainiconloco).setVisibility(View.GONE);
            v.findViewById(R.id.trainicon).setVisibility(View.GONE);
            return;
        }

        if (composition.units.unit.size() > 1) {
            MaterialType type = composition.units.
                    unit.get(1).materialType;
            type = convert(type.parent_type, type.sub_type.toUpperCase(), type.orientation, composition.units.unit.get(1).seatsFirstClass);
            v.findViewById(R.id.trainicon).setVisibility(View.GONE);
            try {
                String path = "trains/SNCB_" + type.parent_type + (type.sub_type.length() > 0 ? ("_" + type.sub_type) : "") + "_R.GIF";
                // Log.e("CVE", "WAGON: " + path);
                InputStream ims = c.getAssets().open(path);
                Drawable d = Drawable.createFromStream(ims, null);
                ((ImageView) v.findViewById(R.id.trainicon)).setImageDrawable(d);
                ims.close();
                v.findViewById(R.id.trainicon).setVisibility(View.VISIBLE);
                //i1 = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

        }

        MaterialType type = composition.units.
                unit.get(0).materialType;
        type = convert(type.parent_type, type.sub_type.toUpperCase(), type.orientation, composition.units.unit.get(0).seatsFirstClass);
        v.findViewById(R.id.trainiconloco).setVisibility(View.GONE);
        try {
            String path = "trains/SNCB_" + type.parent_type + (type.sub_type.length() > 0 ? ("_" + type.sub_type) : "") + "_R.GIF";
            //Log.e("CVE", "LOCO: " + path);
            InputStream ims = c.getAssets().open(path);
            Drawable d = Drawable.createFromStream(ims, null);
            ((ImageView) v.findViewById(R.id.trainiconloco)).setImageDrawable(d);
            ims.close();
            v.findViewById(R.id.trainiconloco).setVisibility(View.VISIBLE);
            // i2 = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        // v.setVisibility((i1 || i2) ? View.VISIBLE : View.GONE);


    }

    private void startStationInfoActivity(String station, String time, String id) {
        Intent i = new Intent(c, InfoStationActivity.class);
        i.putExtra("Name", station);
        i.putExtra("ID", id);
        i.putExtra("timestamp", Long.valueOf(time));
        c.startActivity(i);
    }

    private void startTrainInfoActivity(String vehicle) {
        Intent i = new Intent(c, InfoTrainActivity.class);
        //i.putExtra("fromto", getDeparture() + " - " + getArrival());
        i.putExtra("Name", vehicle);
        c.startActivity(i);
    }

    private void cacheComposition(String trainId, TrainComposition.Composition.Segments.Segment.SegmentComposition composition) {

        try {
            File file = new File(c.getCacheDir() + File.separator + "composition" + File.separator + trainId + ".cache");

            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();

            if (!file.exists())
                file.createNewFile();

            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(new Gson().toJson(composition).getBytes());
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TrainComposition.Composition.Segments.Segment.SegmentComposition getCompositionFromCache(String trainId) {

        try {
            File file = new File(c.getCacheDir() + File.separator + "composition" + File.separator + trainId + ".cache");

            if (file.exists()) {
                Calendar time = Calendar.getInstance();
                time.add(Calendar.HOUR, -24);
                //I store the required attributes here and delete them
                Date lastModified = new Date(file.lastModified());
                if (lastModified.before(time.getTime())) {
                    //Log.e("CVE", "FILE IS TOO OLD, DELETE IT");
                    file.delete();
                    return null;
                } //else
                //Log.e("CVE", "FILE IS UP TO DATE");
            } else
                return null;

            int length = (int) file.length();

            byte[] bytes = new byte[length];

            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);
            } finally {
                in.close();
            }

            String contents = new String(bytes);
            //Log.e("CVE", "I got:  " + contents);
            return new Gson().fromJson(contents, TrainComposition.Composition.Segments.Segment.SegmentComposition.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;


    }

}
