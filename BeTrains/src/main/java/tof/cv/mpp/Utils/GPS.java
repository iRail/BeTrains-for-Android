package tof.cv.mpp.Utils;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.text.format.DateUtils;
import android.util.Log;

/**
 * Created by 201601 on 14/06/13.
 */
public class GPS {

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > (DateUtils.MINUTE_IN_MILLIS*2);
        boolean isSignificantlyOlder = timeDelta < -(DateUtils.MINUTE_IN_MILLIS*2);
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    public static Location getLastLoc(Context context) {

        Location loc = null;

        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String best = locationManager.getBestProvider(criteria, true);

        if (best != null) {
            loc = locationManager.getLastKnownLocation(best);
        }

        //Log.i("***", "**" + best);
        if (loc != null && (System.currentTimeMillis() - loc.getTime()) < DateUtils.HOUR_IN_MILLIS)
            return loc;


        // Sometimes getLastKnownLocation return null (new device), so I use
        // network as default when possible.

        try {
            if (locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null){
                loc = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Log.i("***", "**NETWORK_PROVIDER");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return loc;
    }

}
