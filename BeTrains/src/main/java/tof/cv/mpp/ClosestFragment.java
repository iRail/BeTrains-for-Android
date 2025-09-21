package tof.cv.mpp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.ListFragment;

import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

import tof.cv.mpp.Utils.DbAdapterLocation;
import tof.cv.mpp.Utils.GPS;
import tof.cv.mpp.adapter.StationLocationAdapter;
import tof.cv.mpp.bo.StationLocation;
import tof.cv.mpp.bo.StationLocationApi;

public class ClosestFragment extends ListFragment {
    private static final String TAG = "ClosestFragment";
    private static final int REQ_LOCATION_PERMISSION = 1001;

    private LocationManager locationManager;
    private MyGPSLocationListener locationGpsListener;
    private MyNetworkLocationListener locationNetworkListener;

    private Button btnUpdate;
    private TextView tvEmpty;

    private Location bestLocationFound;
    private StationLocationAdapter myLocationAdapter;
    private ArrayList<StationLocation> stationList = new ArrayList<>();

    private ProgressDialog progressDialog;
    private DbAdapterLocation mDbHelper;

    private static final long INT_MINTIME = 3000;
    private static final long INT_MINDISTANCE = 50;

    private long startTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_closest, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        try {
            ((AppCompatActivity) requireActivity()).getSupportActionBar()
                    .setTitle(R.string.nav_drawer_closest);
        } catch (Exception ignored) {}

        mDbHelper = new DbAdapterLocation(getActivity());
        tvEmpty = requireView().findViewById(R.id.empty_tv);

        Button btEmpty = requireView().findViewById(R.id.empty_bt);
        btEmpty.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });

        btnUpdate = requireView().findViewById(R.id.btn_update);
        btnUpdate.setOnClickListener(v -> {
            if (bestLocationFound != null) {
                updateListToBestLocation(bestLocationFound);
                btnUpdate.setVisibility(View.GONE);
            }
        });

        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        locationGpsListener = new MyGPSLocationListener();
        locationNetworkListener = new MyNetworkLocationListener();

        myLocationAdapter = new StationLocationAdapter(requireActivity(),
                R.layout.row_closest, new ArrayList<>());
        setListAdapter(myLocationAdapter);

        bestLocationFound = GPS.getLastLoc(requireActivity());
        if (bestLocationFound != null) {
            updateListToBestLocation(bestLocationFound);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        StationLocation clicked = (StationLocation) l.getItemAtPosition(position);
        CharSequence[] items = {
                getString(R.string.info),
                getString(R.string.closest_navigate),
                getString(R.string.closest_map)
        };

        new AlertDialog.Builder(requireActivity())
                .setTitle(clicked.getStation())
                .setItems(items, (dialog, item) -> {
                    switch (item) {
                        case 0:
                            Intent info = new Intent(getActivity(), InfoStationActivity.class);
                            info.putExtra("Name", clicked.getStation());
                            info.putExtra("ID", clicked.getId());
                            startActivity(info);
                            break;
                        case 1:
                            try {
                                Uri navUri = Uri.parse("google.navigation:q=" + clicked.getLat() + "," + clicked.getLon());
                                startActivity(new Intent(Intent.ACTION_VIEW, navUri));
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getActivity(), R.string.closest_navigate_err, Toast.LENGTH_LONG).show();
                            }
                            break;
                        case 2:
                            try {
                                Uri mapUri = Uri.parse("geo:0,0?q=" + clicked.getLat() + "," + clicked.getLon() + "(" + clicked.getStation() + ")");
                                startActivity(new Intent(Intent.ACTION_VIEW, mapUri));
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(getActivity(), R.string.closest_map_err, Toast.LENGTH_LONG).show();
                            }
                            break;
                    }
                }).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, 0, Menu.NONE, R.string.refresh)
                .setIcon(R.drawable.ic_menu_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            progressDialog = ProgressDialog.show(getActivity(),
                    getString(R.string.patient),
                    getString(R.string.closest_looking),
                    true, false);
            downloadStationListFromApi();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyGPSLocationListener implements LocationListener {
        private final DecimalFormat df = new DecimalFormat();

        @Override
        public void onLocationChanged(Location loc) {
            Log.v(TAG, "GPS");
            if (locationManager != null) {
                locationManager.removeUpdates(locationNetworkListener);
            }
            if (loc != null) {
                bestLocationFound = loc;
                df.setMaximumFractionDigits(2);
                btnUpdate.setVisibility(View.VISIBLE);
                btnUpdate.setText(getString(R.string.closest_update_gps, df.format(loc.getAccuracy())));
                ((MaterialTextView) requireView().findViewById(R.id.tv_title))
                        .setText(R.string.closest_ok_gps);
                updateListToBestLocation(loc);
            }
        }
    }

    private class MyNetworkLocationListener implements LocationListener {
        private final DecimalFormat df = new DecimalFormat();

        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null && btnUpdate != null) {
                bestLocationFound = loc;
                df.setMaximumFractionDigits(2);
                btnUpdate.setVisibility(View.VISIBLE);
                btnUpdate.setText(getString(R.string.closest_update_gps, df.format(loc.getAccuracy())));
                ((MaterialTextView) requireView().findViewById(R.id.tv_title))
                        .setText(R.string.closest_ok_gps);
                updateListToBestLocation(loc);
            }
        }
    }

    private void updateListToBestLocation(Location loc) {
        startTime = System.currentTimeMillis();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String cache = prefs.getString("stations", "");

        if (!cache.isEmpty()) {
            StationLocationApi api = new Gson().fromJson(cache, StationLocationApi.class);
            stationList = api.station;
            fillAdapterWithDistances(loc);
            Log.e(TAG, "Time with cache: " + (System.currentTimeMillis() - startTime));
        } else {
            Ion.with(getActivity())
                    .load("https://api.irail.be/stations.php?format=json")
                    .as(new TypeToken<StationLocationApi>() {})
                    .setCallback((e, apiList) -> {
                        if (apiList != null && apiList.station != null) {
                            prefs.edit().putString("stations", new Gson().toJson(apiList)).apply();
                            stationList = apiList.station;
                            fillAdapterWithDistances(loc);
                            Log.e(TAG, "Time with API: " + (System.currentTimeMillis() - startTime));
                        }
                        if (progressDialog != null) progressDialog.dismiss();
                    });
        }
    }

    private void fillAdapterWithDistances(Location loc) {
        myLocationAdapter.clear();
        for (StationLocation s : stationList) {
            float[] results = new float[1];
            Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), s.getLat(), s.getLon(), results);
            s.setAway(results[0]);
        }
        Collections.sort(stationList);
        myLocationAdapter.addAll(stationList);
        myLocationAdapter.notifyDataSetChanged();
    }

    private void downloadStationListFromApi() {
        Ion.with(getActivity())
                .load("https://api.irail.be/stations.php?format=json")
                .as(new TypeToken<StationLocationApi>() {})
                .setCallback((e, apiList) -> {
                    if (apiList == null || apiList.station == null) {
                        if (progressDialog != null) progressDialog.dismiss();
                        tvEmpty.setText(R.string.check_connection);
                        return;
                    }
                    mDbHelper.open();
                    if (!apiList.station.isEmpty()) {
                        mDbHelper.deleteAllLocations();
                    }
                    for (StationLocation s : apiList.station) {
                        mDbHelper.createStationLocation(s.getStation(), s.getId(),
                                (int) (s.getLat() * 1E6), (int) (s.getLon() * 1E6), 0.0);
                    }
                    mDbHelper.close();
                    if (progressDialog != null) progressDialog.dismiss();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!checkLocationPermission()) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOCATION_PERMISSION);
            return;
        }
        startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private boolean checkLocationPermission() {
        Context ctx = getContext();
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        StringBuilder txt = new StringBuilder();
        for (String provider : locationManager.getAllProviders()) {
            txt.append("<br>").append(provider).append(": <b>")
                    .append(locationManager.isProviderEnabled(provider) ? "ON" : "OFF").append("</b>");
        }
        txt.append("<br><br>").append(getString(R.string.closest_tuto_gps));
        tvEmpty.setText(Html.fromHtml(txt.toString()));

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INT_MINTIME, INT_MINDISTANCE, locationGpsListener);
        } catch (Exception ignored) {}
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INT_MINTIME, INT_MINDISTANCE, locationNetworkListener);
        } catch (Exception e) {
            Log.e(TAG, "No network provider", e);
        }
    }

    private void stopLocationUpdates() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationGpsListener);
            locationManager.removeUpdates(locationNetworkListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQ_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                tvEmpty.setText(R.string.check_connection);
            }
        }
    }
}
