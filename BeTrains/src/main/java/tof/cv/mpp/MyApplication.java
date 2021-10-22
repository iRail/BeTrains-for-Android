package tof.cv.mpp;

import android.app.Application;

import androidx.multidex.MultiDexApplication;

import com.google.android.material.color.DynamicColors;

public class MyApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
