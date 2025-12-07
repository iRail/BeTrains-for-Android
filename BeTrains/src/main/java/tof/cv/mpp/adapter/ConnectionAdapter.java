package tof.cv.mpp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    CompositionRequestCallback callback;

    private Map<String, TrainComposition.Composition.Segments.Segment.SegmentComposition> compositions = new HashMap<>();

    public interface CompositionRequestCallback {
        void onCompositionNeeded(String vehicleId);
    }

    public ConnectionAdapter(List<Connection> connection, Activity a, ArrayList<Alert> singleAlert,
            CompositionRequestCallback callback) {
        this.connection = connection;
        this.c = a;
        this.singleAlert = singleAlert;
        this.callback = callback;
    }

    public void updateCompositions(
            Map<String, TrainComposition.Composition.Segments.Segment.SegmentComposition> newCompositions) {
        this.compositions.putAll(newCompositions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConnectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_planner, parent, false);
        return new ConnectionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ConnectionViewHolder holder, int position) {
        final Connection conn = connection.get(position);

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isExpanded = holder.card.getVisibility() == View.VISIBLE;
                holder.card.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
                if (!isExpanded) {
                    requestCompositionsForConnection(conn);
                }
            }
        });

        if (conn != null) {
            bindBasicInfo(holder, conn);
            bindAlerts(holder, conn);
            bindOccupancy(holder, conn);
            bindExpandedDetails(holder, conn);
        }
    }

    private void requestCompositionsForConnection(Connection conn) {
        if (conn.getVias() != null && conn.getVias().via != null) {
            for (Via via : conn.getVias().via) {
                if (!compositions.containsKey(via.getVehicle())) {
                    callback.onCompositionNeeded(via.getVehicle());
                }
            }
        }

        if (conn.getDeparture() != null && !compositions.containsKey(conn.getDeparture().getVehicle())) {
            // Not usually needed if no vias, but logic handles it
        }

        if (conn.getArrival() != null && !compositions.containsKey(conn.getArrival().getVehicle())) {
            callback.onCompositionNeeded(conn.getArrival().getVehicle());
        }
    }

    private void bindBasicInfo(ConnectionViewHolder holder, Connection conn) {
        String delayStr = " +" + Math.abs((Integer.valueOf(conn.getDeparture().getDelay()) / 60)) + "'";
        holder.delayD.setText(!conn.getDeparture().getDelay().contentEquals("0") ? delayStr : "");

        delayStr = " +" + Math.abs((Integer.valueOf(conn.getArrival().getDelay()) / 60)) + "'";
        holder.delayA.setText(!conn.getArrival().getDelay().contentEquals("0") ? delayStr : "");

        holder.departureName.setText(conn.getDeparture().getStation());
        holder.departureName.setOnClickListener(v -> startStationInfoActivity(
                conn.getDeparture().getStation(), conn.getDeparture().getTime(),
                conn.getDeparture().getStationInfo().getId()));

        holder.arrivalName.setText(conn.getArrival().getStation());
        holder.arrivalName.setOnClickListener(v -> startStationInfoActivity(
                conn.getArrival().getStation(), conn.getArrival().getTime(),
                conn.getArrival().getStationInfo().getId()));

        holder.departure.setText(conn.getDeparture().getPlatform());
        holder.departure.setTypeface(Typeface.DEFAULT_BOLD);

        holder.arrival.setText(conn.getArrival().getPlatform());
        holder.arrival.setTypeface(Typeface.DEFAULT_BOLD);

        holder.triptime.setText(Html.fromHtml(" <b>" + Utils.formatDate(conn.getDuration(), true, false) + "</b>"));

        holder.departtime.setText(conn.getDeparture().isCancelled() ? Html.fromHtml("<font color=\"red\">XXXX</font>")
                : Utils.formatDate(conn.getDeparture().getTime(), false, false));
        holder.arrivaltime.setText(conn.getArrival().isCancelled() ? Html.fromHtml("<font color=\"red\">XXXX</font>")
                : Utils.formatDate(conn.getArrival().getTime(), false, false));

        holder.numberoftrainsll.removeAllViews();
        if (conn.getVias() != null && conn.getVias().via != null && !conn.getVias().via.isEmpty()) {
            holder.numberoftrainsll.setVisibility(View.VISIBLE);
            holder.numberoftrains.setVisibility(View.GONE);

            LayoutInflater inflater = LayoutInflater.from(holder.numberoftrainsll.getContext());
            holder.numberoftrainsll.addView(inflater.inflate(R.layout.atrain, holder.numberoftrainsll, false));
            for (Via avia : conn.getVias().via) {
                holder.numberoftrainsll.addView(inflater.inflate(R.layout.atrain, holder.numberoftrainsll, false));
            }
        } else {
            holder.numberoftrains.setVisibility(View.VISIBLE);
            holder.numberoftrainsll.setVisibility(View.GONE);
            holder.numberoftrains.setText(Html.fromHtml(Utils.getTrainId(conn.getDeparture().getVehicle())));
        }
    }

    private void bindAlerts(ConnectionViewHolder holder, Connection conn) {
        if (conn.getAlerts() != null && conn.getAlerts().getNumber() > 0) {
            String text = "";
            if (conn.getAlerts().getAlertlist() != null)
                for (Alert anAlert : conn.getAlerts().getAlertlist()) {
                    boolean toDel = false;
                    if (singleAlert != null)
                        for (Alert aSingleAlert : singleAlert) {
                            if (aSingleAlert.getHeader().contentEquals(anAlert.getHeader()))
                                toDel = true;
                        }
                    if (!toDel)
                        text += anAlert.getHeader() + "<br/>";
                }

            if (text.endsWith("<br/>"))
                text = text.substring(0, text.length() - 5);

            if (text.length() > 0) {
                holder.alertText.setVisibility(View.VISIBLE);
                holder.alertText.setText(Html.fromHtml(text));
            } else
                holder.alertText.setVisibility(View.GONE);

        } else {
            holder.alertText.setVisibility(View.GONE);
        }
    }

    private void bindOccupancy(ConnectionViewHolder holder, Connection conn) {
        if (conn.getOccupancy() != null) {
            holder.occupancy.setVisibility(View.VISIBLE);
            switch (conn.getOccupancy().getName()) {
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
        } else {
            holder.occupancy.setVisibility(View.GONE);
        }
    }

    private void bindExpandedDetails(ConnectionViewHolder holder, Connection conn) {
        holder.lltrains.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(holder.lltrains.getContext());

        long startTime = Long.valueOf(conn.getDeparture().getTime());

        // First Train View
        View firstTrainView = inflater.inflate(R.layout.row_connection_detail, holder.lltrains, false);
        firstTrainView.setOnClickListener(view -> startTrainInfoActivity(conn.getDeparture().getVehicle()));
        holder.lltrains.addView(firstTrainView);

        if (conn.getVias() != null && conn.getVias().via != null) {

            // Train 1: Depart -> first Via
            String firstVehicleId = conn.getDeparture().getVehicle();
            // Original logic used "aVia.getVehicle()" for EACH loop iteration.
            // It seems "aVia.getVehicle()" represents the train ARRIVING at the via?
            // "aVia.getDeparture().getVehicle()" represents the train DEPARTING from the
            // via?
            // Let's assume standard logic:

            // The first LOOP iteration in original:
            // "getCompositionFromCache(aVia.getVehicle())"
            // "displayComposition(..., lltrains.getChildAt(position), aVia, prevTimeFinal)"

            // So indeed, the first view we added (Train 1) is populated by the first Via's
            // "Vehicle" property?
            // Or maybe Via.getVehicle() IS the first train.

            long prevTime = startTime;

            for (Via via : conn.getVias().via) {
                // Populate the LAST added train view (Train arriving at this Via)
                String vehicleId = via.getVehicle();
                TrainComposition.Composition.Segments.Segment.SegmentComposition output = compositions.get(vehicleId);

                View currentTrainView = holder.lltrains.getChildAt(holder.lltrains.getChildCount() - 1);
                displayComposition(output, vehicleId, currentTrainView, via, prevTime);

                // Add Station View
                View vStation = inflater.inflate(R.layout.row_via_station, holder.lltrains, false);
                vStation.setOnClickListener(view -> startStationInfoActivity(via.getDeparture().getStation(),
                        via.getArrival().getTime(), via.getStationInfo().getId()));
                holder.lltrains.addView(vStation);
                loadStations(vStation, via);

                // Add Next Train View (Departing from this Via)
                View vNextTrain = inflater.inflate(R.layout.row_connection_detail, holder.lltrains, false);
                vNextTrain.setOnClickListener(view -> startTrainInfoActivity(via.getDeparture().getVehicle()));
                holder.lltrains.addView(vNextTrain);

                prevTime = Long.valueOf(via.getDeparture().getTime());
            }
        }

        // Final Train (from last station/start to Arrival)
        // Original logic: "getCompositionFromCache(co.getArrival().getVehicle())"

        String lastVehicle = conn.getArrival().getVehicle();
        TrainComposition.Composition.Segments.Segment.SegmentComposition lastCompo = compositions.get(lastVehicle);

        long endTime = ((conn.getVias() == null || conn.getVias().via.size() == 0) ? conn.getDeparture().getTimeLong()
                : conn.getVias().via.get(conn.getVias().via.size() - 1).getDeparture().getTimeLong());
        long lastDuration = conn.getArrival().getTimeLong() - endTime;

        displayComposition(lastCompo, lastVehicle, holder.lltrains.getChildAt(holder.lltrains.getChildCount() - 1),
                null, lastDuration);
    }

    @Override
    public int getItemCount() {
        return connection.size();
    }

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
                        newSubType = "BDX";
                        break;
                    case "BYU":
                        break;
                    case "BUH":
                    case "BDUH":
                    case "BAU":
                    case "BU":
                        newSubType = "B";
                        break;
                    case "AU":
                        newSubType = "A";
                        break;
                    case "BDU":
                        newSubType = "BD";
                        break;
                    case "BDAU":
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
                newSubType = "";
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
        LinearLayout numberoftrainsll;
        TextView alertText;
        LinearLayout container;
        LinearLayout lltrains;
        View parent;
        CardView card;

        ConnectionViewHolder(@NonNull View v) {
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
    }

    private void loadStations(View stationRow, Via aVia) {
        if (stationRow == null)
            return;

        TextView tvArrival = stationRow.findViewById(R.id.tv_arrival_platform);
        tvArrival.setText(aVia.getArrival().getPlatform());

        if (aVia.getArrival().getPlatforminfo() != null && aVia.getArrival().getPlatforminfo().normal == 0)
            tvArrival.setText("!" + tvArrival.getText() + "!");

        TextView tvDeparture = stationRow.findViewById(R.id.tv_departure_platform);
        tvDeparture.setText(aVia.getDeparture().getPlatform());

        if (aVia.getDeparture().getPlatforminfo() != null && aVia.getDeparture().getPlatforminfo().normal == 0)
            tvDeparture.setText("!" + tvDeparture.getText() + "!");

        ((TextView) stationRow.findViewById(R.id.tv_arrival_time))
                .setText(Utils.formatDate(aVia.getArrival().getTime(), false, false));
        ((TextView) stationRow.findViewById(R.id.tv_departure_time))
                .setText(Utils.formatDate(aVia.getDeparture().getTime(), false, false));
        ((TextView) stationRow.findViewById(R.id.tv_station)).setText(aVia.getName());

        if (aVia.getTimeBetween() != null)
            ((TextView) stationRow.findViewById(R.id.tv_duration)).setText("(" + aVia.getTimeBetween() + ")");
    }

    private void displayComposition(TrainComposition.Composition.Segments.Segment.SegmentComposition composition,
            String name, View v, Via aVia, long prevtime) {

        if (v == null || v.findViewById(R.id.train_name) == null) {
            return;
        }

        ((TextView) v.findViewById(R.id.train_name)).setText(name.replace("BE.NMBS.", ""));

        long duration = 0;
        if (aVia != null) {
            duration = Long.valueOf(aVia.getArrival().getTime()) - prevtime;
        } else {
            duration = prevtime; // In the call for last segment, "prevtime" arg is actually duration
        }

        ((TextView) v.findViewById(R.id.tv_duration)).setText(Utils.formatDate(duration, true, false));

        if (composition == null) {
            v.findViewById(R.id.trainiconloco).setVisibility(View.GONE);
            v.findViewById(R.id.trainicon).setVisibility(View.GONE);
            return;
        }

        if (composition.units.unit.size() > 1) {
            try {
                MaterialType type = composition.units.unit.get(1).materialType;
                type = convert(type.parent_type, type.sub_type.toUpperCase(), type.orientation,
                        composition.units.unit.get(1).seatsFirstClass);
                v.findViewById(R.id.trainicon).setVisibility(View.GONE);
                String path = "trains/SNCB_" + type.parent_type
                        + (type.sub_type.length() > 0 ? ("_" + type.sub_type) : "") + "_R.GIF";
                InputStream ims = c.getAssets().open(path);
                Drawable d = Drawable.createFromStream(ims, null);
                ((ImageView) v.findViewById(R.id.trainicon)).setImageDrawable(d);
                ims.close();
                v.findViewById(R.id.trainicon).setVisibility(View.VISIBLE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            MaterialType type = composition.units.unit.get(0).materialType;
            type = convert(type.parent_type, type.sub_type.toUpperCase(), type.orientation,
                    composition.units.unit.get(0).seatsFirstClass);
            v.findViewById(R.id.trainiconloco).setVisibility(View.GONE);
            String path = "trains/SNCB_" + type.parent_type + (type.sub_type.length() > 0 ? ("_" + type.sub_type) : "")
                    + "_R.GIF";
            InputStream ims = c.getAssets().open(path);
            Drawable d = Drawable.createFromStream(ims, null);
            ((ImageView) v.findViewById(R.id.trainiconloco)).setImageDrawable(d);
            ims.close();
            v.findViewById(R.id.trainiconloco).setVisibility(View.VISIBLE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        i.putExtra("Name", vehicle);
        c.startActivity(i);
    }
}
