package tof.cv.mpp;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.bo.Vehicle;

/**
 * Created by 0116234 on 13-Mar-2018.
 */

public class NotifJobService extends JobService {

    String trainId;

    @Override
    public boolean onStartJob(final JobParameters job) {
        trainId = (String) job.getExtras().get("id");
        Log.e("CVEJOB", "JOB " + trainId);

        final String url = "http://api.irail.be/vehicle.php/?id=" + trainId
                + "&lang=" + getString(R.string.url_lang) + "&format=JSON&alerts=true";
        Ion.with(this).load(url).userAgent("WazaBe: BeTrains " + BuildConfig.VERSION_NAME + " for Android").as(new TypeToken<Vehicle>() {
        }).withResponse().setCallback(new FutureCallback<Response<Vehicle>>() {
            @Override
            public void onCompleted(Exception e, Response<Vehicle> result) {

                if (result == null)
                    return;

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "NOTIF")
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
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                notificationManager.notify(0, mBuilder.build());

                jobFinished(job, false);
            }
        });

        return true; // Answers the question: "Is there still work going on?"
    }


    @Override
    public boolean onStopJob(JobParameters job) {
        return true; // Answers the question: "Should this job be retried?"
    }
}