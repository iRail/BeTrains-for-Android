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

        final String url = "http://api.irail.be/vehicle.php/?id=" + trainId
                + "&lang=" + getString(R.string.url_lang) + "&format=JSON&alerts=true";
        Ion.with(this).load(url).userAgent("WazaBe: BeTrains " + BuildConfig.VERSION_NAME + " for Android").as(new TypeToken<Vehicle>() {
        }).withResponse().setCallback(new FutureCallback<Response<Vehicle>>() {
            @Override
            public void onCompleted(Exception e, Response<Vehicle> result) {

                if (result == null)
                    return;

                Utils.createNotif(result, trainId, getApplicationContext());

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