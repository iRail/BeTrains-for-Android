package tof.cv.mpp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.fragment.app.ListFragment;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.Collections;

import tof.cv.mpp.Utils.DbAdapterLocation;
import tof.cv.mpp.Utils.GPS;
import tof.cv.mpp.adapter.StationLocationAdapter;
import tof.cv.mpp.bo.StationLocation;
import tof.cv.mpp.bo.StationLocationApi;

public class ClosestFragment extends ListFragment {
    protected static final String TAG = "ClosestFragment";
    private MyGPSLocationListener locationGpsListener;
    private MyNetworkLocationListener locationNetworkListener;
    private LocationManager locationManager;
    private Button btnUpdate;
    private Location bestLocationFound;
    private boolean threadLock = false;
    private boolean isFirst = false;
    private MyProgressDialog m_ProgressDialog;
    private Thread thread = null;
    private StationLocationAdapter myLocationAdapter;
    private String strCharacters;
    private DbAdapterLocation mDbHelper;
    ArrayList<StationLocation> stationList = new ArrayList<StationLocation>();
    Cursor locationCursor;
    private TextView tvEmpty;
    private Button btEmpty;
    StationLocation clicked;

    private static final long INT_MINTIME = 3000;
    private static final long INT_MINDISTANCE = 50;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_closest, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

        try {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.nav_drawer_closest);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        m_ProgressDialog = new MyProgressDialog(getActivity());
        mDbHelper = new DbAdapterLocation(getActivity());

        tvEmpty = (TextView) getView().findViewById(R.id.empty_tv);

        btEmpty = (Button) getView().findViewById(R.id.empty_bt);
        btEmpty.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Intent myIntent = new Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        });

        btnUpdate = (Button) getView().findViewById(R.id.btn_update);
        btnUpdate.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                if (!threadLock) {
                    notifyList(true);
                    btnUpdate.setVisibility(View.GONE);
                }

            }
        });

        locationManager = (LocationManager) getActivity().getSystemService(
                Context.LOCATION_SERVICE);
        locationGpsListener = new MyGPSLocationListener();
        locationNetworkListener = new MyNetworkLocationListener();

        myLocationAdapter = new StationLocationAdapter(getActivity(),
                R.layout.row_closest, new ArrayList<StationLocation>());
        setListAdapter(myLocationAdapter);

        bestLocationFound = GPS.getLastLoc(this.getActivity());
        if (bestLocationFound != null)
            updateListToBestLocationNew(bestLocationFound);

    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        final CharSequence[] items = {getString(R.string.info), getString(R.string.closest_navigate),
                getString(R.string.closest_map)};
        final StationLocation clicked = (StationLocation) l
                .getItemAtPosition(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this.getActivity());
        builder.setTitle(clicked.getStation());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Toast.makeText(getActivity().getApplicationContext(),
                // items[item], Toast.LENGTH_SHORT).show();
                switch (item) {
                    case 0:
                        Intent it = new Intent(getActivity(),
                                InfoStationActivity.class);
                        it.putExtra("Name", clicked.getStation());
                        it.putExtra("ID", clicked.getId());
                        //Log.e("CVE", "CLICK " + clicked.getId());
                        startActivity(it);

                        break;
                    case 1:
                        try {
                            Uri uri = Uri.parse("google.navigation:q="
                                    + ((double) clicked.getLat() ) + ","
                                    + ((double) clicked.getLon() ));
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            (Toast.makeText(getActivity(),
                                    R.string.closest_navigate_err,
                                    Toast.LENGTH_LONG)).show();
                        }
                        break;
                    case 2:
                        try {

                            Intent i = new Intent(
                                    android.content.Intent.ACTION_VIEW, Uri
                                    .parse("geo:0,0?q="
                                            + (clicked.getLat() )
                                            + ","
                                            + (clicked.getLon() )
                                            + " (" + clicked.getStation()
                                            + ")"));

                            startActivity(i);
                        } catch (ActivityNotFoundException e) {
                            (Toast.makeText(getActivity(), R.string.closest_map_err,
                                    Toast.LENGTH_LONG)).show();
                        }
                        break;
                }

            }
        });
        builder.create().show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, 0, Menu.NONE, R.string.refresh)
                .setIcon(R.drawable.ic_menu_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                m_ProgressDialog = new MyProgressDialog(this.getActivity());
                m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                m_ProgressDialog.setCancelable(false);
                m_ProgressDialog.setTitle(getString(R.string.patient));
                m_ProgressDialog.setMessage(getString(R.string.closest_looking));
                m_ProgressDialog.show();
                downloadStationListFromApi();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class MyGPSLocationListener implements LocationListener

    {

        public void onLocationChanged(final Location loc) {
            Log.v(TAG, "GPS");
            locationManager.removeUpdates(locationNetworkListener);
            if (loc != null) {
                // GPS Location is considered as the best
                // We can of course improve that.
                bestLocationFound = loc;
                btnUpdate.setVisibility(View.VISIBLE);
                btnUpdate.setText(getActivity().getString(
                        R.string.closest_update_gps, loc.getAccuracy()));

                if (!threadLock)
                    notifyList(false);

            }

        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status,

                                    Bundle extras) {
        }

    }

    private class MyNetworkLocationListener implements LocationListener

    {

        public void onLocationChanged(final Location loc) {

            if (loc != null && btnUpdate != null && getActivity() != null) {
                bestLocationFound = loc;
                btnUpdate.setVisibility(View.VISIBLE);
                btnUpdate.setText(getActivity().getString(
                        R.string.closest_update_gps, loc.getAccuracy()));
                if (!threadLock)
                    notifyList(false);
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    private void updateListToBestLocationNew(final Location loc) {
        startTime = System.currentTimeMillis();
        final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (mPrefs.getString("stations", "").length() > 1) {
            StationLocationApi cache = new Gson().fromJson(mPrefs.getString("stations", ""), StationLocationApi.class);
            stationList = cache.station;

            myLocationAdapter.clear();
            int i = 0;
            for (StationLocation object : stationList) {
                float[] results = new float[1];

                Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                        object.getLat(), object.getLon(),
                        results);
                object.setAway(results[0]);
                stationList.set(i, object);
                i++;
            }

            Collections.sort(stationList);

            for (StationLocation object : stationList) {
                myLocationAdapter.add(object);
            }

            myLocationAdapter.notifyDataSetChanged();
            Log.e("CVE", "Time With JSON from cache: " + (System.currentTimeMillis() - startTime));
        } else
            Ion.with(getActivity())
                    .load("https://api.irail.be/stations.php?format=json")
                    .as(new TypeToken<StationLocationApi>() {
                    })
                    .setCallback(new FutureCallback<StationLocationApi>() {
                        @Override
                        public void onCompleted(Exception e, StationLocationApi apiList) {
                            if (apiList != null && apiList.station != null) {
                                SharedPreferences.Editor ed = mPrefs.edit();
                                Gson gson = new Gson();
                                ed.putString("stations", gson.toJson(apiList));
                                ed.apply();

                                stationList = apiList.station;

                                myLocationAdapter.clear();
                                int i = 0;
                                for (StationLocation object : stationList) {
                                    float[] results = new float[1];

                                    Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                                            object.getLat(), object.getLon(),
                                            results);
                                    object.setAway(results[0]);
                                    stationList.set(i, object);
                                    i++;
                                }

                                Collections.sort(stationList);

                                for (StationLocation object : stationList) {
                                    myLocationAdapter.add(object);
                                }

                                myLocationAdapter.notifyDataSetChanged();
                                Log.e("CVE", "Time With JSON from URL: " + (System.currentTimeMillis() - startTime));
                            }

                        }
                    });


        getActivity().runOnUiThread(hideProgressdialog);
        Log.v(TAG, "Finish to parse");
    }

    long startTime;


    private Runnable lockOff = new Runnable() {

        public void run() {
            getActivity().getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    };

    private Runnable lockOn = new Runnable() {

        public void run() {
            getActivity().getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    };

    class MyProgressDialog extends ProgressDialog {
        public MyProgressDialog(Context context) {
            super(context);
        }

        public void onBackPressed() {
            super.onBackPressed();
            try {//Lazy catching - bug report from Play Store
                if (thread != null)
                    thread.interrupt();
                getActivity().runOnUiThread(hideProgressdialog);
                thread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
    }

    private Runnable hideProgressdialog = new Runnable() {
        public void run() {
            m_ProgressDialog.dismiss();
        }
    };

    protected void downloadStationListFromApi() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.patient))
                .setMessage(getString(R.string.closest_first_download))
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                               /* Runnable fillDataRunnable = new Runnable() {

                                    public void run() {
                                        downloadStationListThread();
                                    }
                                };

                                thread = new Thread(null, fillDataRunnable,
                                        "MagentoBackground");
                                thread.start();*/
                                mDbHelper.open();
                                getActivity().runOnUiThread(lockOff);
                                DownloadAndParseStationList();
                                mDbHelper.open();
                                final Cursor locationCursor = mDbHelper.fetchAllLocations();
                                if (locationCursor.getCount() > 0) {
                                    // TODO Refresh
                                    mDbHelper.close();
                                } else {
                                    Activity a = getActivity();
                                    if (a != null) {
                                        a.runOnUiThread(hideProgressdialog);
                                        a.runOnUiThread(noConnexion);
                                    }
                                }
                                getActivity().runOnUiThread(lockOn);
                                mDbHelper.close();
                                try {
                                    m_ProgressDialog.hide();
                                    m_ProgressDialog = new MyProgressDialog(
                                            getActivity());
                                    // Looper.prepare();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                m_ProgressDialog.setCancelable(false);
                                m_ProgressDialog
                                        .setTitle(getString(R.string.patient));
                                m_ProgressDialog
                                        .setMessage(getString(R.string.closest_downloading));
                                m_ProgressDialog.setMax(660);
                                m_ProgressDialog.show();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    /**
     * Fill the list at the activity creation
     */
    public void downloadStationListThread() {
        mDbHelper.open();
        getActivity().runOnUiThread(lockOff);
        DownloadAndParseStationList();
        mDbHelper.open();
        final Cursor locationCursor = mDbHelper.fetchAllLocations();
        if (locationCursor.getCount() > 0) {
            // TODO Refresh
            mDbHelper.close();
        } else {
            Activity a = getActivity();
            if (a != null) {
                a.runOnUiThread(hideProgressdialog);
                a.runOnUiThread(noConnexion);
            }
        }
        getActivity().runOnUiThread(lockOn);
        mDbHelper.close();

    }

    /**
     * Each time I come back in the activity, I listen to GPS
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "RESUME");
        String txt = "";
        locationManager = (LocationManager) getActivity().getSystemService(
                Context.LOCATION_SERVICE);
        for (String aProvider : locationManager.getAllProviders())
            txt += ("<br>"
                    + aProvider
                    + ": <b>"
                    + (locationManager.isProviderEnabled(aProvider) ? "ON"
                    : "OFF") + "</b>");
        txt += "<br><br>" + getString(R.string.closest_tuto_gps);
        tvEmpty.setText(Html.fromHtml(txt));

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, INT_MINTIME, INT_MINDISTANCE,
                    locationGpsListener);
        } catch (Exception e) {
            Log.i("", "No GPS on this device");
        }

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, INT_MINTIME, INT_MINDISTANCE,
                    locationNetworkListener);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Each time I leave the activity, I stop listening to GPS (battery)
     */
    @Override
    public void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationGpsListener);
            locationManager.removeUpdates(locationNetworkListener);
        }

        locationManager = null;
    }
/*
    public void compareStationsListToMyLocation(Cursor locationCursor, int i,
                                                double lat, double lon) {
        locationCursor.moveToPosition(i);
        String strName = locationCursor.getString(locationCursor
                .getColumnIndex(DbAdapterLocation.KEY_STATION_NAME));
        m_ProgressDialog.incrementProgressBy(1);

        double iLat = locationCursor.getInt(locationCursor
                .getColumnIndex(DbAdapterLocation.KEY_STATION_LAT));

        double iLon = locationCursor.getInt(locationCursor
                .getColumnIndex(DbAdapterLocation.KEY_STATION_LON));

        String id = locationCursor.getString(locationCursor
                .getColumnIndex(DbAdapterLocation.KEY_STATION_ID));

        double dDis = StationLocationAdapter.distance(lat, lon, iLat / 1E6,
                iLon / 1E6);

        stationList.add(new StationLocation(strName, iLat, iLon, dDis + "", id));
    }*/

    public void DownloadAndParseStationList() {

        try {
            Ion.with(getActivity())
                    .load("https://api.irail.be/stations.php?format=json")
                    .as(new TypeToken<StationLocationApi>() {
                    })
                    .setCallback(new FutureCallback<StationLocationApi>() {
                        @Override
                        public void onCompleted(Exception e, StationLocationApi apiList) {

                            if(apiList == null || apiList.station == null){
                                m_ProgressDialog.hide();
                                return;
                            }
                            mDbHelper.open();
                            Log.e("CVE", "SIZE= " + apiList.station.size() + "");

                            if (apiList.station.size() > 0)
                                mDbHelper.deleteAllLocations();

                            for (final StationLocation anItem : apiList.station) {
                                mDbHelper.createStationLocation(anItem.getStation(), anItem.getId(), (int) (anItem.getLat() * 1E6),
                                        (int) (anItem.getLon() * 1E6), 0.0);
                                /*getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        m_ProgressDialog.setMessage(anItem.getStation());
                                        m_ProgressDialog.incrementProgressBy(1);
                                    }
                                });*/
                            }
                            m_ProgressDialog.hide();
                            mDbHelper.close();
                        }
                    });


            getActivity().runOnUiThread(hideProgressdialog);
            Log.v(TAG, "Finish to parse");

        } catch (Exception e) {
            e.printStackTrace();
            Log.v(TAG, "Connexion error");
            try {
                getActivity().runOnUiThread(noConnexion);
                getActivity().runOnUiThread(hideProgressdialog);
            } catch (Exception f) {
                // Si il a quitté l'activité.
            }

        }
    }

    private Runnable noConnexion = new Runnable() {

        public void run() {
            getActivity().runOnUiThread(hideProgressdialog);
            tvEmpty.setText(R.string.check_connection);
            if (locationManager != null) {
                locationManager.removeUpdates(locationGpsListener);
                locationManager.removeUpdates(locationNetworkListener);
            }

            locationManager = null;
        }
    };

    public void notifyList(boolean manual) {
      /*  threadLock = true;
        // m_ProgressDialog.hide();
        m_ProgressDialog = new MyProgressDialog(this.getActivity());
        m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        m_ProgressDialog.setCancelable(false);
        m_ProgressDialog.setTitle(getString(R.string.txt_patient));
        m_ProgressDialog.setMessage(getString(R.string.txt_fill_closest));
        m_ProgressDialog.show();
        // I only update automatically first time
        if (!isFirst || manual) {
            isFirst = true;
            // Upate the list and notify Adapter
            Runnable notifyListRunnable = new Runnable() {
                public void run() {
                    updateListToBestLocation();
                    myLocationAdapter.clear();
                    //Log.e("CVE", "SIZE= " + stationList.size());
                    for (StationLocation object : stationList) {
                        myLocationAdapter.add(object);
                    }
                    myLocationAdapter.notifyDataSetChanged();
                    threadLock = false;
                    m_ProgressDialog.hide();
                    Log.e("CVE", "Time With Database: " + (System.currentTimeMillis() - startTime));
                }
            };
            getActivity().runOnUiThread(notifyListRunnable);
        }
        threadLock = false;
        m_ProgressDialog.hide();*/
    }

}
