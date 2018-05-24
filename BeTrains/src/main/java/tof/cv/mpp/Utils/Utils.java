package tof.cv.mpp.Utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;
import android.widget.Button;

import com.google.gson.Gson;
import com.koushikdutta.ion.Response;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import tof.cv.mpp.InfoTrainActivity;
import tof.cv.mpp.NotifBroadcastReceiver;
import tof.cv.mpp.R;
import tof.cv.mpp.bo.Connections;
import tof.cv.mpp.bo.Vehicle;

public class Utils {

    public static Location getLastLoc(Context context) {

        Location bestResult = null;
        long bestTime = 0;
        float bestAccuracy = Float.MAX_VALUE;

        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        List<String> matchingProviders = locationManager.getAllProviders();
        for (String provider : matchingProviders) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                //Log.i("", (new Date(time)).toString() + location.getProvider().toString());

                if ((time > bestTime && accuracy < bestAccuracy * 1.5)) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                    //Log.i("", "Ameliore: " + location.getProvider().toString());
                } else {
                    //Log.i("", "Ignore: " + location.getProvider().toString());
                }

                if (bestResult == null) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                    Log.i("", "Par dÈfaut: "
                            + location.getProvider().toString());
                }
            }
        }
        if (bestResult != null)
            return bestResult;
        else {
            return locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
    }


    public static double distance(double sLat, double sLon, double eLat,
                                  double eLon) {
        double d2r = (Math.PI / 180);

        try {
            double dlong = (eLon - sLon) * d2r;
            double dlat = (eLat - sLat) * d2r;
            double a = Math.pow(Math.sin(dlat / 2.0), 2) + Math.cos(sLat * d2r)
                    * Math.cos(eLat * d2r) * Math.pow(Math.sin(dlong / 2.0), 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            return 6367 * c; // en km* 1000;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static String getTimeFromDate(String dateFromAPI) {
        Date date;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
        try {

            date = new Date((Long.valueOf(dateFromAPI)) * 1000);
            //Log.i("", "getMinutsFromDate: " + date.toString());
            return dateFormat.format(date);
        } catch (Exception e) {
            return dateFromAPI;
        }

    }

    public static String getTrainId(String train) {

        String[] array = train.split("\\.");

        if (array.length == 0)
            return train;
        else
            return array[array.length - 1];

    }

    public static String formatDate(String dateFromAPI, boolean isDuration,
                                    boolean isDelay) {

        return formatDate(Long.valueOf(dateFromAPI), isDuration, isDelay);

    }


    public static String formatDateWidget(Date dateFromAPI) {

        return formatDate(dateFromAPI, "HH:mm:ss");

    }

    public static String formatDate(long dateFromAPI, boolean isDuration,
                                    boolean isDelay) {
        // TODO: Lot of tweaks, need to be cleaned
        Date date;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));

        if (dateFromAPI == 0)
            return "";
        try {
            if (isDuration) {

                if (isDelay)
                    return "+" + dateFromAPI / 60 + "'";
                else
                    date = new Date((dateFromAPI - 3600) * 1000);
            } else {
                date = new Date(dateFromAPI * 1000);
            }
            return dateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "" + dateFromAPI;
        }

    }

    public static Connections getCachedConnections(String c) {
        try {
            return new Gson().fromJson(c, Connections.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    public static String formatDate(Date d, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(d);
    }

    public static ArrayList<String> getFavFromDb(Activity context,
                                                 DbAdapterConnection mDbHelper) {
        ArrayList<String> mArrayList = new ArrayList<String>();
        mDbHelper.open();
        Cursor mCursor = mDbHelper.fetchAllFavStations();
        mCursor.moveToFirst();

        // TODO: Bug: I have to add first item manually.. Why?
        if (!mCursor.isAfterLast())
            mArrayList.add(mCursor.getString(mCursor
                    // .getColumnIndex(DbAdapterConnection.KEY_FAV_NAME)));
                    .getColumnIndex(DbAdapterConnection.KEY_ROWID)));
        for (mCursor.moveToFirst(); mCursor.moveToNext(); mCursor.isAfterLast()) {
            // The Cursor is now set to the right position
            mArrayList.add(mCursor.getString(mCursor
                    // .getColumnIndex(DbAdapterConnection.KEY_FAV_NAME)));
                    .getColumnIndex(DbAdapterConnection.KEY_ROWID)));
        }
        return mArrayList;
    }

    public static void addAsStarred(String item, String item2, int type,
                                    Context context) {
        // TYPE 1 = Station
        // TYPE 2 = Vehicle
        // TYPE 3 = Trip
        DbAdapterConnection mDbHelper = new DbAdapterConnection(context);
        mDbHelper.open();
        mDbHelper.createFav(item, item2, type);
        mDbHelper.close();

    }

    public static Vehicle getMemoryvehicle(String fileName, Context context) {

        try {
            File f = context.getDir("COMPENSATION", Context.MODE_PRIVATE);
            File file = new File(f, fileName);
            Gson gson = new Gson();
            return gson.fromJson(getFromFile(file), Vehicle.class);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }

    public static BufferedReader getFromFile(File file) {
        try {
            Log.i("", file.getCanonicalPath());
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        StringBuilder text = new StringBuilder();

        try {
            return new BufferedReader(new FileReader(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int createNotif(Response<Vehicle> result, String trainId, Context c) {
        int type = 0;
        try {
            type = result.getResult().getVehicleStops().getVehicleStop().get(result.getResult().getVehicleStops().getVehicleStop().size() - 1).hasLeft();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        Intent openIntent = new Intent(c, InfoTrainActivity.class);
        openIntent.putExtra("Name", trainId);
        openIntent.putExtra("timestamp", System.currentTimeMillis() / 1000);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openPendingIntent = PendingIntent.getActivity(c, 0,
                openIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissIntent = new Intent(c, NotifBroadcastReceiver.class);
        dismissIntent.setAction("ACTION_OPEN");
        dismissIntent.putExtra("id", trainId);
        PendingIntent dismissPendingIntent =
                PendingIntent.getBroadcast(c, 0, dismissIntent, 0);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c, "NOTIF")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(openPendingIntent)
                .addAction(new NotificationCompat.Action(R.mipmap.ic_launcher, c.getString(R.string.notif_open), openPendingIntent))
                .addAction(new NotificationCompat.Action(R.mipmap.ic_launcher, c.getString(R.string.notif_dismiss), dismissPendingIntent));

        int totaldelay = 0;
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        int count = 0;
        if (result.getResult().getVehicleStops() != null)
            for (Vehicle.VehicleStop aStop : result.getResult().getVehicleStops().getVehicleStop()) {
                if (aStop.hasLeft() == type) {
                    if (aStop.getDelayinMin() > totaldelay)
                        totaldelay = aStop.getDelayinMin();
                    style = style.addLine(aStop.getStation() + " - " + Utils.formatDate(aStop.getTime(), false, false) + " " + (aStop.delay == 0 ? "" : " +" + (aStop.getDelayinMin()) + "'"));
                }
            }
        mBuilder.setStyle(style).setContentTitle(trainId).setContentText(c.getString(R.string.totalDelay) + " " + totaldelay + "min")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).setOngoing(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(c);

        notificationManager.notify(0, mBuilder.build());

        return 1;
    }
}