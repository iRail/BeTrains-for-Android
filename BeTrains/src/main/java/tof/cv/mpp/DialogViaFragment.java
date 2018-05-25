package tof.cv.mpp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.adapter.ViaAdapter;
import tof.cv.mpp.bo.Alert;
import tof.cv.mpp.bo.Connection;
import tof.cv.mpp.bo.Station;
import tof.cv.mpp.bo.Via;

@SuppressLint("ValidFragment")
public class DialogViaFragment extends DialogFragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.setRetainInstance(true);
        // this.getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
        // R.layout.dialog_title);
        return null;

    }


    /*
     * private void setOnListListener() { listview.setOnItemClickListener(new
     * OnItemClickListener() {
     *
     * public void onItemClick(AdapterView<?> arg0, View aView, int position,
     * long aLong) {
     *
     * final Via currentVia = currentConnection.getVias().via .get(position /
     * 2);
     *
     * if (position % 2 == 1) {
     * startTrainInfoActivity(Utils.getTrainId(currentVia .getVehicle())); }
     *
     * else startStationInfoActivity(currentVia.getName(), currentVia
     * .getDeparture().getTime());
     *
     * } }); }
     */



    /*
     * private String getDepartureVehicle() { return
     * currentConnection.getDeparture().getVehicle(); }
     *
     * private String getArrivalVehicle() { return
     * currentConnection.getArrival().getVehicle(); }
     */


}