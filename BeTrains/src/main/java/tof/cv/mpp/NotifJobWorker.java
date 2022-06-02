package tof.cv.mpp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.bo.Vehicle;

/**
 * Created by 0116234 on 13-Mar-2018.
 */

public class NotifJobWorker extends Worker {

    String trainId;

    public NotifJobWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {

        trainId =getInputData().getString("id");

        final String url = "https://api.irail.be/vehicle.php/?id=" + trainId
                + "&lang=" + getApplicationContext().getString(R.string.url_lang) + "&format=JSON&alerts=true";
        Ion.with(getApplicationContext()).load(url).userAgent("WazaBe: BeTrains " + BuildConfig.VERSION_NAME + " for Android").as(new TypeToken<Vehicle>() {
        }).withResponse().setCallback(new FutureCallback<Response<Vehicle>>() {
            @Override
            public void onCompleted(Exception e, Response<Vehicle> result) {

                if (result == null)
                    return;

                Utils.createNotif(result, trainId, getApplicationContext());

            }
        });
        return Result.success();

    }
}
