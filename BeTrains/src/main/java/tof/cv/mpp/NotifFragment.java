package tof.cv.mpp;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.concurrent.TimeUnit;

public class NotifFragment extends Fragment {

    public String trainId;
    int notif = 0;
    private FirebaseAnalytics mFirebaseAnalytics;

    public NotifFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notif = ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).getActiveNotifications().length;
            if (notif > 0)
                ((Button) getView().findViewById(R.id.button)).setText(getString(R.string.cancel));
            else
                ((Button) getView().findViewById(R.id.button)).setText(getString(R.string.ok));
        }

        if (ContextCompat.checkSelfPermission(
                getContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED){
            getView().findViewById(R.id.buttonnotif).setVisibility(View.GONE);
            getView().findViewById(R.id.textnotif).setVisibility(View.GONE);
        }
        else
        {
            Toast.makeText(getActivity(), R.string.notif_refused, Toast.LENGTH_LONG).show();
            //ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
            getView().findViewById(R.id.buttonnotif).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.textnotif).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notif = ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).getActiveNotifications().length;
            if (notif > 0)
                ((Button) getView().findViewById(R.id.button)).setText(getString(R.string.cancel));
        }



        getView().findViewById(R.id.buttonnotif).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", getContext().getPackageName(), null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        getView().findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    notif = ((NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE)).getActiveNotifications().length;
                }
                Bundle params = new Bundle();
                mFirebaseAnalytics.logEvent("ButtonNotif", params);
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
