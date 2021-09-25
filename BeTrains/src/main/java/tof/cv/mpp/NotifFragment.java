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
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.location.Geofence;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
    public void onViewCreated(View v,@Nullable Bundle savedInstanceState) {
        super.onViewCreated(v,savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notif = ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).getActiveNotifications().length;
            if (notif > 0)
                ((Button) getView().findViewById(R.id.button)).setText(getString(R.string.cancel));
        }

        getView().findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Bundle myExtrasBundle = new Bundle();
               // myExtrasBundle.putString("id", trainId);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    notif = ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).getActiveNotifications().length;
                }
                Bundle params = new Bundle();
                mFirebaseAnalytics.logEvent("ButtonNotif", params);
                Log.e("CVE","NOTIF "+notif);
                if (notif > 0) {
                    NotificationManagerCompat.from(getContext()).cancel(0);
                    notif = 0;
                    WorkManager.getInstance(getContext()).cancelAllWork();
                    ((Button) getView().findViewById(R.id.button)).setText(getString(R.string.ok));

                } else {
                    Data.Builder data = new Data.Builder();
                    data.putString("id", trainId);
                    PeriodicWorkRequest notifRequest =
                            new PeriodicWorkRequest.Builder(NotifJobWorker.class, 2, TimeUnit.MINUTES)
                                    .addTag("NOTIF")
                                    .setInputData(data.build())
                                    .build();
                    WorkManager.getInstance(getContext()).enqueue(notifRequest);

                    if (getView() != null)
                        ((Button) getView().findViewById(R.id.button)).setText(getString(R.string.cancel));
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
