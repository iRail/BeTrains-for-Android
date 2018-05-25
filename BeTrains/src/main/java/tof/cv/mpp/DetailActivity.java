package tof.cv.mpp;

import androidx.appcompat.app.AppCompatActivity;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.adapter.ViaAdapter;
import tof.cv.mpp.bo.Alert;
import tof.cv.mpp.bo.Connection;
import tof.cv.mpp.bo.Station;
import tof.cv.mpp.bo.Via;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "BETRAINS";
    private ViaAdapter viaAdapter = null;
    private ListView listview;
    private View departureRow;
    private View arrivalRow;
    private Connection currentConnection;
    private View lastTrain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_connection_detail);

        currentConnection = new Gson().fromJson(getIntent().getStringExtra("connection"), Connection.class);
        Log.e("CVE", "" + currentConnection);

        LinearLayout ll = findViewById(R.id.ll);
        //ll.setOrientation(LinearLayout.VERTICAL);


        if (currentConnection != null) {
            // Setup the dialog

            departureRow = getLayoutInflater().inflate(R.layout.row_via_first_station,
                    null, false);
            ColorStateList c = fillDetailRow(departureRow,
                    currentConnection.getDeparture(), true);
            setStationListener(departureRow, currentConnection.getDeparture());

            this.setTitle(
                    currentConnection.getDeparture().getStation() + " - "
                            + currentConnection.getArrival().getStation());

            ll.addView(departureRow);
            getLayoutInflater().inflate(R.layout.row_separator, ll, true);

            long prevtime = Long.valueOf(currentConnection.getDeparture()
                    .getTime());

            if (currentConnection.getVias() != null
                    && currentConnection.getVias().via != null)
                for (Via aVia : currentConnection.getVias().via) {

                    View trainRow = getLayoutInflater().inflate(R.layout.row_via_train,
                            null, false);

                    ((TextView) trainRow.findViewById(R.id.tv_train))
                            .setText(Utils.getTrainId(aVia.getVehicle()));
                    ((TextView) trainRow.findViewById(R.id.tv_duration))
                            .setText(Utils.formatDate(
                                    (Long.valueOf(aVia.getArrival().getTime()) - prevtime),
                                    true, false));
                    setTrainListener(trainRow, aVia.getVehicle());
                    ll.addView(trainRow);
                    getLayoutInflater().inflate(R.layout.row_separator, ll, true);

                    View stationRow = getLayoutInflater().inflate(
                            R.layout.row_via_station, null, false);
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

                    ((TextView) stationRow.findViewById(R.id.tv_duration))
                            .setTextColor(c);
                    ((TextView) stationRow.findViewById(R.id.tv_arrival_time))
                            .setTextColor(c);
                    ((TextView) stationRow.findViewById(R.id.tv_departure_time))
                            .setTextColor(c);
                    ((TextView) stationRow
                            .findViewById(R.id.tv_arrival_platform))
                            .setTextColor(c);
                    ((TextView) stationRow
                            .findViewById(R.id.tv_departure_platform))
                            .setTextColor(c);
                    setStationListener(stationRow, aVia);
                    ll.addView(stationRow);
                    getLayoutInflater().inflate(R.layout.row_separator, ll, true);

                    prevtime = Long.valueOf(aVia.getDeparture().getTime());

                }

            lastTrain = getLayoutInflater().inflate(R.layout.row_via_train, null, false);

            ((TextView) lastTrain.findViewById(R.id.tv_train)).setText(Utils
                    .getTrainId(currentConnection.getArrival().getVehicle()));

            ((TextView) lastTrain.findViewById(R.id.tv_duration)).setText(Utils
                    .formatDate((Long.valueOf(currentConnection.getArrival()
                            .getTime()) - prevtime), true, false));


            setTrainListener(lastTrain, currentConnection.getArrival().getVehicle().toString());
            ll.addView(lastTrain);

            getLayoutInflater().inflate(R.layout.row_separator, ll, true);

            arrivalRow = getLayoutInflater().inflate(R.layout.row_via_first_station, null,
                    false);
            fillDetailRow(arrivalRow, currentConnection.getArrival(), false);
            setStationListener(arrivalRow, currentConnection.getArrival());
            ll.addView(arrivalRow);

            if (currentConnection.getAlerts() != null && currentConnection.getAlerts().getNumber() > 0 && currentConnection.getAlerts().getAlertlist() != null) {

                TextView tv = new TextView(this);
                tv.setTextColor(getResources().getColor(R.color.red));
                int padding_in_dp = 8;  // 6 dps
                final float scale = getResources().getDisplayMetrics().density;
                int padding_in_px = (int) (padding_in_dp * scale + 0.5f);

                tv.setPadding(padding_in_px, 0, padding_in_px, padding_in_px);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tv.setLayoutParams(layoutParams);
                tv.setGravity(Gravity.CENTER);

                String text = "";

                for (Alert anAlert : currentConnection.getAlerts().getAlertlist())
                    text += anAlert.getDescription() + "\n";

                if (text.endsWith(" / "))
                    text = text.substring(0, text.length() - 2);

                tv.setText(Html.fromHtml(text));
                //  ll.addView(tv);
            }

            // scroll.addView(ll);


        }


    }

    public ColorStateList fillDetailRow(View row, Station station,
                                        Boolean isDeparture) {
        TextView tvStation = (TextView) row.findViewById(R.id.tv_station);
        tvStation.setText(station.getStation());
        /*
         * TextView tvTrain = (TextView) row.findViewById(R.id.tv_train); if
         * (isDeparture) tvTrain.setText("");
         *
         * else tvTrain.setText(Utils.getTrainId(station.getVehicle()));
         */
        TextView tvPlatform = (TextView) row.findViewById(R.id.tv_platform);
        tvPlatform.setText(station.getPlatform());

        if (station.getPlatforminfo() != null && station.getPlatforminfo().normal == 0)
            tvPlatform
                    .setText("!" + tvPlatform.getText() + "!");

        TextView tvTime = (TextView) row.findViewById(R.id.tv_time);
        tvTime.setText(Utils.formatDate(station.getTime(), false, false));

        TextView tvDelay = (TextView) row.findViewById(R.id.tv_delay);
        if (!station.getDelay().contentEquals("0")) {
            Log.i(TAG, "delay: " + Integer.valueOf(station.getDelay()));
            tvDelay.setText("+"
                    + (int) (Integer.valueOf(station.getDelay()) / 60) + "'");
        } else
            tvDelay.setText("");

        return tvPlatform.getTextColors();
    }

    private void setStationListener(View v, final Station s) {
        v.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                startStationInfoActivity(s.getStation(),
                        s.getTime(), s.getStationInfo().getId());

            }
        });
    }

    private void setStationListener(View v, final Via via) {
        v.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                startStationInfoActivity(via.getName(),
                        via.getDeparture().getTime(), via.getStationInfo().getId());

            }
        });
    }

    private void setTrainListener(View v, final String s) {

        v.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Log.i("", "***" + s);
                startTrainInfoActivity(Utils.getTrainId(s));
            }
        });

    }


    private String getDeparture() {
        return currentConnection.getDeparture().getStation();
    }

    private String getArrival() {
        return currentConnection.getArrival().getStation();
    }

    private void startStationInfoActivity(String station, String time, String id) {
        Intent i = new Intent(this, InfoStationActivity.class);
        i.putExtra("Name", station);
        i.putExtra("ID", id);
        i.putExtra("timestamp", Long.valueOf(time));
        startActivity(i);
    }

    private void startTrainInfoActivity(String vehicle) {
        Intent i = new Intent(this, InfoTrainActivity.class);
        i.putExtra("fromto", getDeparture() + " - " + getArrival());
        i.putExtra("Name", vehicle);
        startActivity(i);
    }

}
