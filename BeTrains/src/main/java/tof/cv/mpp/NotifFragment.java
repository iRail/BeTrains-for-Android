package tof.cv.mpp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.location.Geofence;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.util.ArrayList;

import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.bo.StationLocation;
import tof.cv.mpp.bo.Vehicle;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

public class NotifFragment extends Fragment {

    public String trainId;
    int notif = 0;
    private FirebaseAnalytics mFirebaseAnalytics;

    public NotifFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notif = ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).getActiveNotifications().length;
            if (notif > 0)
                ((Button) getView().findViewById(R.id.button)).setText(getString(R.string.cancel));
        }

        getView().findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle myExtrasBundle = new Bundle();
                myExtrasBundle.putString("id", trainId);
                Bundle params = new Bundle();
                mFirebaseAnalytics.logEvent("ButtonNotif", params);
                if (notif > 0) {
                    FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getContext()));
                    dispatcher.cancelAll();
                    NotificationManagerCompat.from(getContext()).cancel(0);
                    notif = 0;
                    ((Button) getView().findViewById(R.id.button)).setText(getString(R.string.ok));

                } else {
                    final String url = "http://api.irail.be/vehicle.php/?id=" + trainId
                            + "&lang=" + getString(R.string.url_lang) + "&format=JSON&alerts=true";
                    Ion.with(getContext()).load(url).userAgent("WazaBe: BeTrains " + BuildConfig.VERSION_NAME + " for Android").as(new TypeToken<Vehicle>() {
                    }).withResponse().setCallback(new FutureCallback<Response<Vehicle>>() {
                        @Override
                        public void onCompleted(Exception e, Response<Vehicle> result) {

                            if (result == null)
                                return;

                            notif = Utils.createNotif(result, trainId, getContext());

                            ((Button) getView().findViewById(R.id.button)).setText(getString(R.string.cancel));

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
                            .setTrigger(Trigger.executionWindow(30, 120))
                            .build();

                    dispatcher.mustSchedule(myJob);

                }

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
