package tof.cv.mpp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.WorkManager;

//import com.firebase.jobdispatcher.FirebaseJobDispatcher;
//import com.firebase.jobdispatcher.GooglePlayDriver;

/**
 * Created by 0116234 on 13-Mar-2018.
 */

public class NotifBroadcastReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
      //  FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
       // dispatcher.cancelAll();
        WorkManager.getInstance(context).cancelAllWork();
       NotificationManagerCompat.from(context).cancel(0);
    }
}
