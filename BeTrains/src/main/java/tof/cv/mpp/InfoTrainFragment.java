package tof.cv.mpp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.fragment.app.Fragment;
import androidx.core.app.ShareCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import tof.cv.mpp.Utils.DbAdapterConnection;
import tof.cv.mpp.Utils.Utils;
import tof.cv.mpp.adapter.TrainInfoAdapter;
import tof.cv.mpp.bo.Vehicle;
import tof.cv.mpp.widget.TrainAppWidgetProvider;
import tof.cv.mpp.widget.TrainWidgetProvider;

public class InfoTrainFragment extends Fragment implements OnMapReadyCallback {
    protected static final String TAG = "ChatFragment";
    private Vehicle currentVehicle;
    private SwipeRefreshLayout swipeContainer;

    private String fromTo;
    String id = null;
    private long timestamp;
    GoogleMap myMap;
    RecyclerView recyclerView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info_train, container, false);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());

        if ((!(status == ConnectionResult.SUCCESS)) && getView().findViewById(R.id.mapContainer) != null) {
            getView().findViewById(R.id.mapContainer).setVisibility(View.GONE);
        }

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        recyclerView = (RecyclerView) getView().findViewById(android.R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        registerForContextMenu(recyclerView);



        swipeContainer = (SwipeRefreshLayout) getView().findViewById(R.id.swipeContainer);
        if (swipeContainer != null) {
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // Your code to refresh the list here.
                    // Make sure you call swipeContainer.setRefreshing(false)
                    // once the network request has completed successfully.
                    displayInfo(id, fromTo, timestamp);
                }
            });
            swipeContainer.setColorSchemeResources(
                    R.color.primarycolor);
        }

        // Configure the refreshing colors


    }

    public void displayInfoFromMemory(final String fileName, final String vehicle) {
        getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
        this.timestamp = System.currentTimeMillis();

        currentVehicle = Utils.getMemoryvehicle(fileName, InfoTrainFragment.this.getActivity());
        getView().findViewById(R.id.progress).setVisibility(View.GONE);
        if (currentVehicle != null
                && currentVehicle.getVehicleStops() != null) {
            TrainInfoAdapter trainInfoAdapter = new TrainInfoAdapter(currentVehicle
                    .getVehicleStops().getVehicleStop(), getActivity(), currentVehicle.getAlerts(), currentVehicle.getVehicleInfo().name);


            recyclerView.setAdapter(trainInfoAdapter);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(Utils.formatDate(new Date(timestamp), "HH:mm"));
        } else {
            Toast.makeText(getActivity(), R.string.check_connection,
                    Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    public void setInfo(String vehicle, String fromTo, long timestamp) {
        this.id = vehicle;
        this.fromTo = fromTo;
        if (timestamp != 0)
            this.timestamp = timestamp;
        else
            this.timestamp = System.currentTimeMillis();
    }

    public void displayInfo(String vehicle, String fromTo, long timestamp) {
        getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);

        this.id = vehicle;
        this.fromTo = fromTo;
        if (timestamp != 0)
            this.timestamp = timestamp;
        else
            this.timestamp = System.currentTimeMillis();

        myTrainSearch(vehicle);
    }


    private void myTrainSearch(final String vehicle) {

        getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
        String dateTime = "";
        if (timestamp != 0) {
            String formattedDate = tof.cv.mpp.Utils.Utils.formatDate(new Date(timestamp),
                    "ddMMyy");
            String formattedTime = tof.cv.mpp.Utils.Utils
                    .formatDate(new Date(timestamp), "HHmm");
            dateTime = "&date=" + formattedDate + "&time=" + formattedTime;
        }
        String lan = getString(R.string.url_lang);
        if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("prefnl", false))
            lan = "NL";

        final String url = "http://api.irail.be/vehicle.php/?id=" + vehicle
                + "&lang=" + lan + dateTime + "&format=JSON&alerts=true";

        Ion.with(this).load(url).userAgent("WazaBe: BeTrains " + BuildConfig.VERSION_NAME + " for Android").as(new TypeToken<Vehicle>() {
        }).withResponse().setCallback(new FutureCallback<Response<Vehicle>>() {
                                          @Override
                                          public void onCompleted(Exception e, Response<Vehicle> result) {
                                              if (swipeContainer != null)
                                                  swipeContainer.setRefreshing(false);

                                              if (result != null) {
                                                  currentVehicle = result.getResult();
                                                  getView().findViewById(R.id.progress).setVisibility(View.GONE);
                                                  getView().findViewById(android.R.id.empty).setVisibility(View.GONE);
                                              }

                                              if (currentVehicle != null
                                                      && currentVehicle.getVehicleStops() != null) {

                                                  TrainInfoAdapter trainInfoAdapter = new TrainInfoAdapter(currentVehicle
                                                          .getVehicleStops().getVehicleStop(), getActivity(), currentVehicle.getAlerts(), currentVehicle.getVehicleInfo().name);
                                                  recyclerView.setAdapter(trainInfoAdapter);

                                                  ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(currentVehicle.getVehicleInfo().name.replace("BE.NMBS.", ""));
                                                  PolylineOptions rectOptions = new PolylineOptions();
                                                  PolylineOptions rectOptionsTranspa = new PolylineOptions();

                                                  double minLat = 90;
                                                  double maxLat = 0;
                                                  double minLon = 180;
                                                  double maxLon = 0;
                                                  double delta = 0.05;

                                                  boolean trainRunning=true;

                                                  myMap.clear();
                                                  try {
                                                      for (Vehicle.VehicleStop aStop : currentVehicle.getVehicleStops().getVehicleStop()) {

                                                              myMap.addMarker(new MarkerOptions()
                                                                      .position(new LatLng(aStop.getStationInfo().getLocationY(), aStop.getStationInfo().getLocationX()))
                                                                      .anchor(0.5f, 0.5f)
                                                                      .icon(BitmapDescriptorFactory.fromResource(aStop.hasLeft() ?
                                                                              R.drawable.stop :
                                                                              R.drawable.stopt)));

                                                          if (aStop.hasLeft()) {
                                                              rectOptions.add(new LatLng(aStop.getStationInfo().getLocationY(), aStop.getStationInfo().getLocationX()));
                                                              rectOptionsTranspa.getPoints().clear();
                                                              rectOptionsTranspa.add(new LatLng(aStop.getStationInfo().getLocationY(), aStop.getStationInfo().getLocationX()));
                                                          } else{
                                                              rectOptionsTranspa.add(new LatLng(aStop.getStationInfo().getLocationY(), aStop.getStationInfo().getLocationX()));
                                                                trainRunning=false;
                                                          }


                                                          if (maxLat < aStop.getStationInfo().getLocationY())
                                                              maxLat = aStop.getStationInfo().getLocationY();

                                                          if (minLat > aStop.getStationInfo().getLocationY())
                                                              minLat = aStop.getStationInfo().getLocationY();

                                                          if (maxLon < aStop.getStationInfo().getLocationX())
                                                              maxLon = aStop.getStationInfo().getLocationX();

                                                          if (minLon > aStop.getStationInfo().getLocationX())
                                                              minLon = aStop.getStationInfo().getLocationX();
                                                      }

                                                      myMap.addPolyline(rectOptions.color(Color.rgb( 0, 0, 255)));

                                                      myMap.addPolyline(rectOptionsTranspa.color(Color.rgb( 150, 150, 150)));

                                                      LatLngBounds bounds = new LatLngBounds(
                                                              new LatLng(minLat - delta, minLon - delta), new LatLng(maxLat + delta, maxLon + delta));

                                                      myMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));

                                                      //myMap.addMarker(new MarkerOptions()
                                                      //        .position(new LatLng(currentVehicle.getVehicleInfo().locationY,currentVehicle.getVehicleInfo().locationX))
                                                      //        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                                  } catch (Exception e1) {
                                                      //map crashing, probably custom ROM
                                                      e1.printStackTrace();
                                                  }


                                              } else {
                                                  if (e != null) {
                                                      Toast.makeText(getActivity(), e.getLocalizedMessage(),
                                                              Toast.LENGTH_LONG).show();
                                                      getActivity().finish();
                                                  } else {
                                                      if (result.getHeaders().code() == 502) {
                                                          AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                          builder.setTitle(R.string.irail_issue);
                                                          builder.setMessage(R.string.irail_issue_detail);
                                                          builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                              public void onClick(DialogInterface dialog, int id) {
                                                                  getActivity().finish();
                                                              }
                                                          });
                                                          builder.setNegativeButton(R.string.report, new DialogInterface.OnClickListener() {
                                                              public void onClick(DialogInterface dialog, int id) {

                                                                  ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(getActivity());
                                                                  builder.setType("message/rfc822");
                                                                  builder.addEmailTo("iRail@list.iRail.be");
                                                                  builder.setSubject("Issue with iRail API");
                                                                  builder.setText("Hello, I am currently using the Android application BeTrains, and I get an error while using the iRail API.\n\n" +
                                                                          "I get this message: 'Could not get data. Please report this problem to iRail@list.iRail.be' while trying to query :\n" + url + "\n\n" +
                                                                          "I hope you can fix that soon.\nHave a nice day.");
                                                                  builder.setChooserTitle("Send Email");
                                                                  builder.startChooser();

                                                                  //getActivity().finish();
                                                              }
                                                          });
                                                          builder.create().show();
                                                      }

                                                  }


                                              }
                                          }
                                      }

        );

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        // if (requestCode == 0) {
        //     getActivity().finish();
        //}
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, 0, Menu.NONE, R.string.train_add_to_widget)
                .setIcon(R.drawable.ic_menu_save)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        menu.add(Menu.NONE, 1, Menu.NONE, R.string.action_add_to_favorites)
                .setIcon(R.drawable.ic_menu_star)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        // menu.add(Menu.NONE, 2, Menu.NONE, "Map")
        //         .setIcon(R.drawable.ic_menu_mapmode)
        //         .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        menu.add(Menu.NONE, 4, Menu.NONE, R.string.activity_label_compensation)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        if (!(getActivity() instanceof InfoTrainActivity))
            menu.add(Menu.NONE, 3, Menu.NONE, R.string.nav_drawer_chat)
                    .setIcon(R.drawable.ic_menu_start_conversation)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                widget();
                return true;
            case 1:
                if (currentVehicle != null) {
                    Utils.addAsStarred(currentVehicle.getId(), "", 2, getActivity());
                    startActivity(new Intent(getActivity(), StarredActivity.class));
                }
                return true;
            case 2:
              /*  if (currentVehicle != null) {
                    Intent i = new Intent(getActivity(), MapVehicleActivity.class);
                    i.putExtra("Name", currentVehicle.getId());
                    startActivity(i);
                }*/
                return true;
            case 3:
                if (currentVehicle != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString(DbAdapterConnection.KEY_NAME,
                            currentVehicle.getId());
                    Intent mIntent = new Intent(getActivity(), ChatActivity.class);
                    mIntent.putExtras(bundle);
                    startActivityForResult(mIntent, 0);
                    return true;
                }

            case 4:
                if (currentVehicle != null) {
                    new Thread(new Runnable() {
                        public void run() {
                            saveToSd();
                        }
                    }).start();
                }

                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveToSd() {

        int maxDelay = 0;
        if (currentVehicle.getVehicleStops() != null)
            for (Vehicle.VehicleStop aStop : currentVehicle.getVehicleStops().getVehicleStop()) {
                if (Integer.valueOf(aStop.getDelay()) > maxDelay)
                    maxDelay = Integer.valueOf(aStop.getDelay());
            }

        final FileOutputStream f;
        Context context = getActivity();
        final File myFile = new File(getActivity().getDir("COMPENSATION", Context.MODE_PRIVATE), "" + System.currentTimeMillis() + ";" + (maxDelay / 60) + ";;" + id);
        Log.i("", "" + myFile.exists());
        if (myFile.exists())
            myFile.delete();

        String langue = context.getString(R.string.url_lang);
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                "prefnl", false))
            langue = "nl";
        String dateTime = "";
        if (timestamp != 0) {
            String formattedDate = Utils.formatDate(new Date(timestamp),
                    "ddMMyy");
            String formattedTime = Utils
                    .formatDate(new Date(timestamp), "HHmm");
            dateTime = "&date=" + formattedDate + "&time=" + formattedTime;
        }

        final String url = "http://api.irail.be/vehicle.php/?id=" + id
                + "&lang=" + langue + dateTime + "&format=JSON&fast=true";

        Ion.with(this).load(url).asString().setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                try {
                    FileOutputStream f = new FileOutputStream(myFile);
                    f.write(new Gson().toJson(currentVehicle).getBytes());
                    f.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                Intent i = new Intent(getActivity(), CompensationActivity.class);
                startActivity(i);
            }
        });

    }

    public void widget() {

        AlertDialog.Builder ad;
        ad = new AlertDialog.Builder(getActivity());
        ad.setTitle(R.string.widget_confirm);
        ad.setPositiveButton(android.R.string.ok,
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        if (currentVehicle.getId() == null)
                            return;
                        final DbAdapterConnection mDbHelper = new DbAdapterConnection(
                                getActivity());
                        mDbHelper.open();
                        mDbHelper.deleteAllWidgetStops();
                        if (fromTo == null)
                            fromTo = currentVehicle.getVehicleStops()
                                    .getVehicleStop().get(0).getStation()
                                    + " - "
                                    + currentVehicle
                                    .getVehicleStops()
                                    .getVehicleStop()
                                    .get(currentVehicle
                                            .getVehicleStops()
                                            .getVehicleStop().size() - 1).getStation();

                        mDbHelper.createWidgetStop(currentVehicle.getId()
                                .replace("BE.NMBS.", ""), "1", Utils
                                .formatDateWidget(new Date()), fromTo);

                        for (Vehicle.VehicleStop oneStop : currentVehicle
                                .getVehicleStops().getVehicleStop())
                            mDbHelper.createWidgetStop(oneStop.getStation(), ""
                                            + oneStop.getTime(), oneStop.getDelay(),
                                    oneStop.getStatus());

                        mDbHelper.close();

                        Intent intent = new Intent(
                                TrainAppWidgetProvider.TRAIN_WIDGET_UPDATE);
                        getActivity().sendBroadcast(intent);

                        intent = new Intent(TrainWidgetProvider.UPDATE_ACTION);
                        getActivity().sendBroadcast(intent);

                        Toast.makeText(getActivity(),
                                getString(R.string.widget_added, ""),
                                Toast.LENGTH_SHORT).show();

                    }
                });

        ad.setNegativeButton(android.R.string.no,
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {

                    }
                });
        if (currentVehicle != null)
            ad.show();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.getUiSettings().setScrollGesturesEnabled(false);

        if (id != null)
            displayInfo(id, fromTo, timestamp);
    }


}
