package tof.cv.mpp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.location.Geofence;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.util.ArrayList;

import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.bo.StationLocation;
import tof.cv.mpp.bo.Vehicle;

public class NotifFragment extends Fragment {

    public String trainId;

    public NotifFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle myExtrasBundle = new Bundle();
                myExtrasBundle.putString("id", trainId);

                Log.e("CVE", "CLICK " + trainId);

                final String url = "http://api.irail.be/vehicle.php/?id=" + trainId
                        + "&lang=" + getString(R.string.url_lang) + "&format=JSON&alerts=true";
                Ion.with(getContext()).load(url).userAgent("WazaBe: BeTrains " + BuildConfig.VERSION_NAME + " for Android").as(new TypeToken<Vehicle>() {
                }).withResponse().setCallback(new FutureCallback<Response<Vehicle>>() {
                    @Override
                    public void onCompleted(Exception e, Response<Vehicle> result) {

                        if (result == null)
                            return;

                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext(), "NOTIF")
                                .setSmallIcon(R.mipmap.ic_launcher);
                        int totaldelay = 0;
                        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
                        int count = 0;
                        for (Vehicle.VehicleStop aStop : result.getResult().getVehicleStops().getVehicleStop()) {
                            if (aStop.hasLeft() == 0) {
                                if (aStop.getDelayinMin() > totaldelay)
                                    totaldelay = aStop.getDelayinMin();
                                style = style.addLine(aStop.getStation() + " - " + Utils.formatDate(aStop.getTime(), false, false) + " " + (aStop.delay == 0 ? "" : " +" + (aStop.getDelayinMin()) + "'"));
                            }
                        }
                        mBuilder.setStyle(style).setContentTitle(trainId).setContentText(getString(R.string.totalDelay) + " " + totaldelay + "min")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
                        notificationManager.notify(0, mBuilder.build());

                    }
                });

                FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getContext()));

                dispatcher.cancelAll();

                Job myJob = dispatcher.newJobBuilder()
                        .setService(NotifJobService.class) // the JobService that will be called
                        .setTag("NOTIF")        // uniquely identifies the job
                        .setRecurring(true)
                        .setExtras(myExtrasBundle)
                        .setReplaceCurrent(true)
                        .setLifetime(Lifetime.FOREVER)
                        .addConstraint(Constraint.ON_ANY_NETWORK)
                        .setTrigger(Trigger.executionWindow(20, 20))
                        .build();

                dispatcher.mustSchedule(myJob);

                //dispatcher.cancel("NotifJobService");
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notif_list, container, false);
        return view;

    }

}
